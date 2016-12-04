from drone import drone
import datetime
import os


class Fleet():
    # [droneObj, port, logfile]
    drone_list = []

    def __init__(self, com_list):
        for string in com_list:
            self.drone_list.append([None, string, None])

    def request(self, lat, lon): #returns id or -1
        """
        Requests for a drone.
        Returns the id of the drone if there is one available.
        Returns -1 if all drones are occupied.
        """
        for i in range(len(self.drone_list)):
            if self.drone_list[i][0] == None:
                fo = open(os.getcwd() + "/logs/{:%Y-%m-%d..%H.%M.%S}..{}.txt".format(datetime.datetime.now(),i), "w+")
                #fo = open("D:/amf/{:%Y-%m-%d..%H.%M.%S}..{}.txt".format(datetime.datetime.now(),i), "w")
            
                self.drone_list[i][2] = fo
                self.drone_list[i][0] = drone.Drone(self.drone_list[i][1], lat, lon, altitude=10 + i*5, output=fo)
                return i

        return -1

    def requestSITL(self, lat, lon):
        for i in range(len(self.drone_list)):
            if self.drone_list[i][0] == None:
                fo = open(os.getcwd() + "/logs/{:%Y-%m-%d..%H.%M.%S}..{}.txt".format(datetime.datetime.now(),i), "w+")
                #fo = open("D:/amf/{:%Y-%m-%d..%H.%M.%S}..{}.txt".format(datetime.datetime.now(),i), "w")
            
                self.drone_list[i][2] = fo
                start_lat = 41.833474
                start_lon = -87.626819
                import dronekit_sitl
                sitl = dronekit_sitl.start_default(start_lat, start_lon)
                address = sitl.connection_string()
                self.drone_list[i][0] = drone.Drone(address, lat, lon, altitude=10, output=fo)
                return i

        return -1


    def connect(self, id):
        """Connects to drone with id."""
        self.drone_list[id][0].connect()
        # print "!!!!!!!!!! connecting to drone :: {}".format(id)
        # import time
        # while True:
        #   time.sleep(1)

    def run(self, id):
        """Upload and execute mission on drone with id."""
        self.drone_list[id][0].run()

    def get_location(self, id):
        """Gets tuple (lat, lon) of drone with id."""
        return self.drone_list[id][0].get_location()

    def mission_ended(self, id):
        """
        Returns True if drone with id has completed its mission, False otherwise.
        disconnect() needs to be called on the drone to free it for a new mission.
        """
        return self.drone_list[id][0].mission_ended()

    def disconnect(self, id):
        """Disconnects from drone with id and frees it for use."""
        self.drone_list[id][0].close()
        self.drone_list[id][0] = None
        self.drone_list[id][2].close()
        self.drone_list[id][2] = None

    def get_status(self,id):
        """
        Gets status of drone with id.
        Returns dictionary with fields: current, total, command, distance and altitude.
        """
        return self.drone_list[id][0].get_status()

    def log_status(self,id):
        self.drone_list[id][0].log_status()
