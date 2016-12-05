from dronekit import LocationGlobalRelative, LocationGlobal
import argparse
import frame_conversion
from drone import Drone, command_unlock, command_lock, command_land
import time

parser = argparse.ArgumentParser(description='Commands vehicle using vehicle.simple_goto.')
parser.add_argument('--connect', 
                   help="Vehicle connection target string. If not specified, SITL automatically started and used.")
args = parser.parse_args()

connection_string = args.connect
sitl = None

print "Connection string: %s" % connection_string

if not connection_string:
    start_lat = 41.833474
    start_lon = -87.626819
    start_location = LocationGlobal(start_lat, start_lon, 584)
    import dronekit_sitl
    sitl = dronekit_sitl.start_default(start_lat, start_lon)
    connection_string = sitl.connection_string()

# # Get waypoint latitude and longitude
# latitude = raw_input("Enter latitude:")
# longitude = raw_input("Enter longitude:")
# altitude = raw_input("Enter altitude:")

# latitude = float(latitude)
# longitude = float(longitude)
# altitude = float(altitude)
# print "Set waypoint as %f, %f, %f?" % (latitude, longitude, altitude)
# while raw_input("y/n?")is not "y":
#     latitude = raw_input("Enter latitude:")
#     longitude = raw_input("Enter longitude:")
#     altitude = raw_input("Enter altitude:")

#     latitude = float(latitude)
#     longitude = float(longitude)
#     altitude = float(altitude)
#     print "Set waypoint as %f, %f, %f?" % (latitude, longitude, altitude)

# waypoint = LocationGlobalRelative(latitude, longitude, altitude)
# print "WAYPOINT SET as %f, %f, %f?" % (latitude, longitude, altitude)

if sitl:
    waypoint_location = frame_conversion.get_location_metres(start_location, 122, 0)
    # waypoint_location = LocationGlobalRelative(41.834575, -87.626842, 10) #simm
else:
    waypoint_location = LocationGlobalRelative(41.837283, -87.624709, 10) #real field


test = Drone(connection_string, waypoint_location.lat, waypoint_location.lon)
# print "Clearing mission"
# test.clear_mission()
# test.download_mission()
# print "Preparing mission"
# test.prepare_mission()
# print "Uploading mission"
# test.upload_mission()
# print "oops"
# raw_input("Press enter to begin arming and taking off")
# test.arm()
# test.takeoff()


# test.begin_mission()


# test.simple_goto(waypoint_location)
# time.sleep(60)

test.connect()
test.run()
test.wait()
test.close()




# while True:
#     print "Current Waypoint: %s" % test.vehicle.commands.next
#     print "command: %s" % test.vehicle.commands[test.vehicle.commands.next].command
#     print "Number of Waypoints: %s" % test.vehicle.commands.count
#     print test.distance_to_current_waypoint()
#     time.sleep(1)

# i=0
# while True:
#     print "useless loop %s" % i
#     i+=1