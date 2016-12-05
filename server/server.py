from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler
from SocketServer import ThreadingMixIn
import threading
import sys
from fleet import Fleet
import simplejson
import threading
import urlparse
import ports

"""
This threading server was implemented based on:
https://pymotw.com/2/BaseHTTPServer/index.html#module-BaseHTTPServer
"""


port_list = ports.serial_ports()
print (port_list)
fleet = Fleet(port_list)
fleet_lock = threading.Lock()

#app_dict entry format: {"instanceID" : [droneID, running_bool]}
app_dict = {}
dict_lock = threading.Lock()

def connect_and_run(instanceID):
    """
    Function to connect and run the drone.
    Sets the drone as running at the end.
    Created to be ran on a new thread so POST response isn't blocked.
    """
    droneid = app_dict[instanceID][0]
    fleet.connect(droneid)
    fleet.run(droneid)
    app_dict[instanceID][1] = True

class Handler(BaseHTTPRequestHandler):

    def _set_headers(self):
        self.send_response(200)
        self.end_headers()

    def log_message(self, format, *args):
        """
        Prevents console from being flooded with the accepted GETs.
        """
        return

    def do_GET(self):
        """
        GET request handler.
        """
        self._set_headers()

        # get instanceID from url
        parsed_path = urlparse.urlparse(self.path)
        instanceID = urlparse.parse_qs(parsed_path.query)['instanceID'][0]

        #check instanceID is associated with a drone
        if instanceID not in app_dict:
            response = {
                "METHOD": "GET",
                "RESPONSE": -1,
                "DESCRIPTION": "instanceID not in dictionary",
                "LATITUDE": 0,
                "LONGITUDE": 0
            }
        #check if the drone is running. If it is still connecting, you can't get its location
        elif not app_dict[instanceID][1]:
            response = {
                "METHOD": "GET",
                "RESPONSE": -3,
                "DESCRIPTION": "connecting to drone",
                "LATITUDE": 0,
                "LONGITUDE": 0
            }
        else:
            droneid = app_dict[instanceID][0]

            #check for mission end
            if not fleet.mission_ended(droneid):
                lat, lon = fleet.get_location(droneid)

                fleet.log_status(droneid)
                response = {
                    "METHOD": "GET",
                    "RESPONSE": 200,
                    "DESCRIPTION": "mision underway, returning location",
                    "LATITUDE": lat,
                    "LONGITUDE": lon
                }
            else:
                response = {
                    "METHOD": "GET",
                    "RESPONSE": -2,
                    "DESCRIPTION": "mission finished",
                    "LATITUDE": 0,
                    "LONGITUDE": 0
                }
                fleet_lock.acquire()
                fleet.disconnect(droneid)
                fleet_lock.release()

                dict_lock.acquire()
                app_dict.pop(instanceID, None)
                dict_lock.release()
                print("#######disconnecting drone#######")

        self.wfile.write(response)
        self.wfile.write('\n')
        return

    def do_POST(self):
        """
        POST request handler.
        """
        self._set_headers()
        print "@@@@@ start POST"
        self.data_string = self.rfile.read(int(self.headers['Content-Length']))

        data = simplejson.loads(self.data_string)

        print "{}".format(data)
        lat = data['latitude']
        lon = data['longitude']

        fleet_lock.acquire()
        droneid = fleet.request(lat, lon)
        fleet_lock.release()

        #check if there is a drone available
        if droneid is not -1:
            appID = data['instanceID']

            dict_lock.acquire()
            app_dict[appID] = [droneid, False]
            dict_lock.release()

            response = {
                "METHOD": "POST",
                "RESPONSE": 200,
                "DESCRIPTION": "drone available",
                "ADDRESS": data['address']
            }

            t = threading.Thread(target=connect_and_run, args=(appID,))
            t.start()
        else:
            response = {
                "METHOD": "POST",
                "RESPONSE": -1,
                "DESCRIPTION": "all drones busy",
                "ADDRESS": data['address']
            }


        self.wfile.write(response)
        self.wfile.write('\n')
        print "@@@@@ end POST"
        return

class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""

if __name__ == '__main__':
    if sys.argv[1:]:
        port = int(sys.argv[1])
    else:
        port = 8080
    #server created on current IP
    server = ThreadedHTTPServer(('', port), Handler)
    print 'Starting server, use <Ctrl-C> to stop'
    server.serve_forever()
