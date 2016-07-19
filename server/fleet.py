from drone import drone


class Fleet():
	# [droneObj, port]
	drone_list = [[None, "com6"],[None, "com7"]]


	def request(self, lat, lon): #returns id or -1
		""" 
		Requests for a drone.
		Returns the id of the drone if there is one available.
		Returns -1 if all drones are occupied.
		"""
		for i in range(len(drone_list)):
			if drone_list[i][0] == None:
				drone_list[i][0] = Drone(drone_list[i][1], lat, lon, altitude=10)
				return i

		return -1

	def connect(self, id):
		"""Connects to drone with id."""
		drone_list[id].connect()

	def run(self, id):
		"""Upload and execute mission on drone with id."""
		drone_list[id].run()

	def get_location(self, id):
		"""Gets tuple (lat, lon) of drone with id."""
		return drone_list[id].get_location()

	def mission_ended(self, id):
		"""
		Returns True if drone with id has completed its mission, False otherwise.
		disconnect() needs to be called on the drone to free it for a new mission.
		"""
		return drone_list[id].mission_ended()

	def disconnect(self, id):
		"""Disconnects from drone with id and frees it for use."""
		drone_list[id].close()
		drone_list[id] = None

