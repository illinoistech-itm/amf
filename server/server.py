from http.server import HTTPServer, BaseHTTPRequestHandler
from io import BytesIO
import SimpleHTTPServer
import SocketServer
import logging
import logging.handlers
import sqlite3
import string
import urlparse
import json
import requests
import re
import operator
import sys
import subprocess

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
HTTP connection
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

		"""
		"instanceID":"fb1e7622-8dce-4835-8c8d-fdbad655910c",
		"requestID":"REQUEST_12-Jun-2018_11:31:31:3131",
		"latitude":41.823755168694916,
		"longitude":-87.62914348393679,
		"address":"108 W Pershing Rd, Chicago, IL 60609, USA"
		"drone type":1
		"""

		self.send_response(200)
		response = BytesIO()

		print('This is POST request. ')
		
		print(body)  #print all post information but not parsed, in dictionary (key,value)
	
		
		#dict = {'instanceID' : 'fb1e7622-8dce-4835-8c8d-fdbad655910c', 'latitude' : '41.823755168694916' , 'longtitude' : '-87.62914348393679', 'address' : '108 W Pershing Rd, Chicago, IL 60609, USA'}
		#put temporary information


		#for key, value in dict.items():
				#print(key, value)

		# print dict
		
		#with open("example.json",'wb') as f:
		#	json.dump(dict,f)
		# make json file 

		lon=body.get("longitude","none")
		lat =body.get("latitude","none")
		
		

		subprocess.Popen(['python', '../server/drone/launch.py', '/dev/ttyUSB0', lat, lon, '0'])
	
httpd = HTTPServer(('10.0.0.10', 8080), SimpleHTTPRequestHandler)
httpd.serve_forever() 