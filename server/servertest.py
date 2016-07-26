from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler
from SocketServer import ThreadingMixIn
import threading
import sys
from fleet import Fleet
import simplejson
import threading
import urlparse

fleet = Fleet(['tcp:127.0.0.1:5760'])
fleet_lock = threading.Lock()
app_dict = {}
dict_lock = threading.Lock()

def connect_and_run(instanceID):
    droneid = app_dict[instanceID][0]
    fleet.connect(droneid)
    fleet.run(droneid)
    app_dict[instanceID][1] = True

class Handler(BaseHTTPRequestHandler):

    def _set_headers(self):
        self.send_response(200)
        # self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        # self.send_response(200)
        self._set_headers()
        # self.end_headers()
        parsed_path = urlparse.urlparse(self.path)
        instanceID = urlparse.parse_qs(parsed_path.query)['instanceID'][0]

        if instanceID not in app_dict:
            # response = "-1"
            response = {
                "METHOD": "GET",
                "RESPONSE": -1,
                "LATITUDE": 0,
                "LONGITUDE": 0
            }
        elif not app_dict[instanceID][1]:
            response = {
                "METHOD": "GET",
                "RESPONSE": -3,
                "LATITUDE": 0,
                "LONGITUDE": 0
            }
        else:
            droneid = app_dict[instanceID][0]

            # message = "{}, {}".format(lat, lon)
            if not fleet.mission_ended(droneid):
                lat, lon = fleet.get_location(droneid)

                fleet.log_status(droneid)
                response = {
                    "METHOD": "GET",
                    "RESPONSE": 200,
                    "LATITUDE": lat,
                    "LONGITUDE": lon
                }
            else:
                # response = "-2"
                response = {
                    "METHOD": "GET",
                    "RESPONSE": -2,
                    "LATITUDE": 0,
                    "LONGITUDE": 0
                }
                fleet_lock.acquire()
                fleet.disconnect(droneid)
                fleet_lock.release()

                dict_lock.acquire()
                app_dict.pop(instanceID, None)
                dict_lock.release()


        self.wfile.write(response)
        self.wfile.write('\n')
        return

    def do_POST(self):
        self._set_headers()
        print "@@@@@ start POST"
        self.data_string = self.rfile.read(int(self.headers['Content-Length']))

        # self.send_response(200)
        # self.end_headers()

        data = simplejson.loads(self.data_string)

        print "{}".format(data)
        lat = data['latitude']
        lon = data['longitude']

        fleet_lock.acquire()
        droneid = fleet.request(lat, lon)
        fleet_lock.release()

        # droneid = fleet.requestSITL(lat, lon)
        # droneid = -1
        if droneid is not -1:
            appID = data['instanceID']

            dict_lock.acquire()
            app_dict[appID] = [droneid, False]
            dict_lock.release()

            response = {
                "METHOD": "POST",
                "RESPONSE": 200,
                "ADDRESS": data['address']
            }

            t = threading.Thread(target=connect_and_run, args=(appID,))
            t.start()
        else:
            response = {
                "METHOD": "POST",
                "RESPONSE": -1,
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
    # server = ThreadedHTTPServer(('localhost', port), Handler)
    # server = ThreadedHTTPServer(('104.194.103.165', port), Handler)
    server = ThreadedHTTPServer(('', port), Handler)
    print 'Starting server, use <Ctrl-C> to stop'
    server.serve_forever()
