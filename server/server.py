from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import SocketServer
import simplejson
import random
from drone import drone
import sys
import subprocess

class S(BaseHTTPRequestHandler):
    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    # def do_GET(self):
    #     self._set_headers()
    #     f = open("index.html", "r")
    #     self.wfile.write(f.read())

    def do_HEAD(self):
        self._set_headers()

    def do_POST(self):
        self._set_headers()
        print "in post method"
        self.data_string = self.rfile.read(int(self.headers['Content-Length']))

        self.send_response(200)
        self.end_headers()
        # simplejson.loads(s.replace('\r\n', ''))
        data = simplejson.loads(self.data_string)
        # with open("test123456.json", "w") as outfile:
            # simplejson.dump(data, outfile)
        print "{}".format(data)
        lat = data['latitude']
        print lat
        lon = data['longitude']
        print lon
        # address = data['address']
        # address = data['address']
        subprocess.Popen("python drone/launch.py sitl %s %s 0" % (lat, lon), shell=True)

        # start_lat = 41.833474
        # start_lon = -87.626819
        # import dronekit_sitl
        # sitl = dronekit_sitl.start_default(start_lat, start_lon)
        # address = sitl.connection_string()
        #
        # d = drone.Drone(address, lat, lon)
        # d.run()
        # d.wait()

        # f = open("for_presen.py")
        # self.wfile.write(f.read())
        return


def run(server_class=HTTPServer, handler_class=S, port=8080):
    # raw_input("Press Enter to continue...")
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    sa = httpd.socket.getsockname()
    print "Serving HTTP on", sa[0], "port", sa[1], "..."
    httpd.serve_forever()

if __name__ == "__main__":
    from sys import argv

if len(argv) == 2:
    run(port=int(argv[1]))
else:
    run()
