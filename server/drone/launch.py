import sys
import drone
import subprocess

if len(sys.argv) != 5:
    print "Incorrect number of arguments, invoke this script with: \n \
    python launch.py adressname target-latitude target-longitude flag. \n \
    Addressname of sitl will run it in a simulation \n \
    If flag is 1, a new terminal will be created, if it is 0, it will run on the open terminal"

    sys.exit()

if sys.argv[4] == "1":
    subprocess.Popen("python launch.py %s %s %s 0" % (sys.argv[1], sys.argv[2], sys.argv[3]), creationflags = subprocess.CREATE_NEW_CONSOLE)
    sys.exit()

address = sys.argv[1]
lat = float(sys.argv[2])
lon = float(sys.argv[3])

if address == "sitl":
    start_lat = 41.833474
    start_lon = -87.626819
    import dronekit_sitl
    sitl = dronekit_sitl.start_default(start_lat, start_lon)
    address = sitl.connection_string()


d = drone.Drone(address, lat, lon)
d.connect()
d.run()
d.wait()
d.close()