from drone import drone


class Fleet():
	# [droneObj, port, logfile]
	drone_list = [[None, "com6", None],[None, "com7", None]]


	def request(self, lat, lon): #returns id or -1
		""" 
		Requests for a drone.
		Returns the id of the drone if there is one available.
		Returns -1 if all drones are occupied.
		"""
		for i in range(len(drone_list)):
			if drone_list[i][0] == None:
				fo = open("log/{:%Y-%m-%d %H:%M:%S}::{}".format(datetime.datetime.now(),i))
				drone_list[i][2] = fo
				drone_list[i][0] = Drone(drone_list[i][1], lat, lon, altitude=10, output=fo)
				return i

		return -1

	def connect(self, id):
		"""Connects to drone with id."""
		drone_list[id][0].connect()

	def run(self, id):
		"""Upload and execute mission on drone with id."""
		drone_list[id][0].run()

	def get_location(self, id):
		"""Gets tuple (lat, lon) of drone with id."""
		return drone_list[id][0].get_location()

	def mission_ended(self, id):
		"""
		Returns True if drone with id has completed its mission, False otherwise.
		disconnect() needs to be called on the drone to free it for a new mission.
		"""
		return drone_list[id][0].mission_ended()

	def disconnect(self, id):
		"""Disconnects from drone with id and frees it for use."""
		drone_list[id][0].close()
		drone_list[id][0] = None
		drone_list[id][2].close()
		drone_list[id][2] = None

	def get_status(self,id):
		"""
		Gets status of drone with id.
		Returns dictionary with fields: current, total, command, distance and altitude.
		"""
		return drone_list[id][0].get_status()
