import json
import requests
from math import radians, cos, sin, asin, sqrt

IS_SIM=True
if IS_SIM:
    print "Start simulator (SITL)"
    import dronekit_sitl
    sitl = dronekit_sitl.start_default()
    connection_string = sitl.connection_string()
else:
    connection_string = "/dev/ttyACM0"

# Import DroneKit-Python
from dronekit import connect, VehicleMode, LocationGlobal
import time
import pdb

BASE_URL = 'http://localhost:3000'
NSECS = 5

def arm_and_takeoff(aTargetAltitude):
    """
    Arms vehicle and fly to aTargetAltitude.
    """

    print "Basic pre-arm checks"
    # Don't try to arm until autopilot is ready
   
    while not vehicle.is_armable:
        print " Waiting for vehicle to initialise..."
        time.sleep(1)
    
    print "Arming motors"
    # Copter should arm in GUIDED mode
    vehicle.mode    = VehicleMode("GUIDED")
    vehicle.armed   = True

    # Confirm vehicle armed before attempting to take off
    while not vehicle.armed:
        print " Waiting for arming..."
        time.sleep(1)

    print "Taking off!"
    vehicle.simple_takeoff(aTargetAltitude) # Take off to target altitude

    # Wait until the vehicle reaches a safe height before processing the goto (otherwise the command
    #  after Vehicle.simple_takeoff will execute immediately).
    while True:
        print " Altitude: ", vehicle.location.global_relative_frame.alt
        payload = {
            "h":vehicle.location.global_relative_frame.alt
        }
        requests.put(BASE_URL + '/dron/' + uuid, data=payload)
        #Break and return from function just below target altitude.
        if vehicle.location.global_relative_frame.alt>=aTargetAltitude*0.98:
            print "Reached target altitude"
            break
        time.sleep(1)

def goto(lat, lng, h):
    p1 = LocationGlobal(lat, lng, h)
    vehicle.simple_goto(p1)

def isDroneAt(lat, lng):
    threshold = 0.020 # km
    pos = vehicle.location.global_frame
    pdb.set_trace()
    return haversine(lng, lat, pos.lon, pos.lat) < threshold

def haversine(lon1, lat1, lon2, lat2):
    """
    Calculate the great circle distance between two points 
    on the earth (specified in decimal degrees)
    """
    # convert decimal degrees to radians 
    lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])
    # haversine formula 
    dlon = lon2 - lon1 
    dlat = lat2 - lat1 
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * asin(sqrt(a)) 
    # Radius of earth in kilometers is 6371
    km = 6371* c
    return km

# Connect to the Vehicle.
print("Connecting to vehicle on: %s" % (connection_string,))
vehicle = connect(connection_string,  baud=57600, wait_ready=True)

# Get some vehicle attributes (state)
print "Get some vehicle attribute values:"
print " GPS: %s" % vehicle.gps_0
print " Battery: %s" % vehicle.battery
print " Last Heartbeat: %s" % vehicle.last_heartbeat
print " Is Armable?: %s" % vehicle.is_armable
print " System status: %s" % vehicle.system_status.state
print " Mode: %s" % vehicle.mode.name    # settable

# Close vehicle object before exiting script
#vehicle.close()

# Shut down simulator
#sitl.stop()
print("Completed")

uuid = raw_input('DRON UUID: ')

# Create DRON
payload = {
	"uuid":uuid,
	"h":vehicle.location.global_relative_frame.alt,
	"lat": 41.839660, 
	"lng": -87.624243
}

r = requests.post(BASE_URL + '/dron', data=payload)

path = []
t = 0

while True:
    # Get Path
    res = requests.get(BASE_URL + '/dron/' + uuid + '/path')
    if not len(res.text) == 0 and not len(json.loads(res.text)) == 0:
        path = json.loads(res.text)
        break
    print("Path not ready, sleeping "+NSECS+" seconds..."))
    time.sleep(NSECS)

print("Arming and taking off to height="+str(path[0]['h']))
arm_and_takeoff(path[0]['h'])

while True:
    # Check if at t
    pdb.set_trace()
    if isDroneAt(path[t]['lat'], path[t]['lng']):
        print("Drone at => "+str(path[t]))
        # Increase T
        t += 1
        if (t > len(path) - 1):
            break

        pos = vehicle.location.global_frame
        pdb.set_trace()
        # Notify Server
        payload = {
            "h":path[t]['h'],
            "lat": pos.lat, 
            "lng": pos.lon
        }

        requests.put(BASE_URL + '/dron/' + uuid, data=payload)

        # Go to next location
        goto(path[t]['lat'], path[t]['lng'], path[t]['h'])
    
    time.sleep(NSECS)

uuid = raw_input('Enter to finish simulation: ')

vehicle.close()
if IS_SIM:
    sitl.stop()