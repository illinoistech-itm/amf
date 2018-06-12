from http.server import HTTPServer, BaseHTTPRequestHandler
from io import BytesIO
import SimpleHTTPServer
import SocketServer
import logging
import logging.handlers
import sqlite3

"""
sqlite3
"""



"""
logging
"""

logger=logging.getLogger("AMF")
logger.setLevel(logging.DEBUG)

formatter=logging.Formatter('%(asctime)s - %(filename)s - %(name)s  - %(levelname)s - %(message)s')    

fileHandler=logging.FileHandler('AMF.log')
streamHandler=logging.StreamHandler()

fileHandler.setFormatter(formatter)
streamHandler.setFormatter(formatter)

logger.addHandler(fileHandler)
logger.addHandler(streamHandler)

logger.debug("Server start!")


"""
HTTPS connection
"""

class SimpleHTTPRequestHandler(BaseHTTPRequestHandler):    

	def do_GET(self):
		self.send_response(200)
		self.end_headers()
		self.wfile.write(b'Hello, world!')    
		print('This is GET request. ')

	def do_POST(self):
		content_length = int(self.headers['Content-Length'])
		body = self.rfile.read(content_length)
		self.send_response(200)
		response = BytesIO()
		print('This is POST request. ')
		#response.write(b'This is POST request. ')
		#response.write(b'Received: ')
		#response.write(body)
		print(body) 
		#self.wfile.write(response.getvalue())

httpd = HTTPServer(('10.0.0.10', 8080), SimpleHTTPRequestHandler)
httpd.serve_forever()
