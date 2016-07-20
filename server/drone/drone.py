from __future__ import print_function
import dronekit
from pymavlink import mavutil
import time
import frame_conversion
import sys


commands_dict = { mavutil.mavlink.MAV_CMD_NAV_TAKEOFF: "taking off",
                  mavutil.mavlink.MAV_CMD_NAV_WAYPOINT: "flying to waypoint",
                  mavutil.mavlink.MAV_CMD_NAV_LAND: "landing",
                  mavutil.mavlink.MAV_CMD_NAV_RETURN_TO_LAUNCH: "RTL",
                  mavutil.mavlink.MAV_CMD_DO_SET_RELAY: "toggling relay"
}

class Drone():
    """docstring for ClassName"""
    cruise_altitude = None
    address = None
    vehicle = None
    cmds = None
    lat = None
    lon = None

    output = None
    mission_ended_aux = None

    def __init__(self, address, latitude, longitude, altitude=10, output=sys.stdout):
        self.lat = latitude
        self.lon = longitude
        self.cruise_altitude = altitude
        self.address = address

        self.output = output
        self.mission_ended_aux = False
    
    def connect(self):
        print('Connecting to vehicle on: {}'.format(self.address), file=self.output)
        self.vehicle = dronekit.connect(self.address, wait_ready=True, baud=57600)#, heartbeat_timeout=10)
        print('Connection established', file=self.output)

        self.cmds = self.vehicle.commands
        self.vehicle.add_attribute_listener('location', self.location_callback)
        self.vehicle.add_attribute_listener('mode', self.mode_callback)

    def clear_mission(self):
        self.cmds.clear()
        self.cmds.upload()

    def download_mission(self):
        self.cmds.download()
        self.cmds.wait_ready()

    def begin_mission(self):
        self.vehicle.mode = dronekit.VehicleMode("AUTO")
        self.download_mission()

    def prepare_mission(self):
        # Takeoff (For some reason first command isnt added, so we add it twice)
        self.cmds.add(command_takeoff(self.cruise_altitude))
        self.cmds.add(command_takeoff(self.cruise_altitude))
        # Go to destination
        self.cmds.add(command_waypoint(self.lat, self.lon, self.cruise_altitude))
        # Land
        self.cmds.add(command_land())
        # Release package
        self.cmds.add(command_unlock())
        # Takeoff
        self.cmds.add(command_takeoff(self.cruise_altitude))
        # Undo release
        self.cmds.add(command_lock())
        # MAV_CMD_NAV_RETURN_TO_LAUNCH
        self.cmds.add(command_rtl(self.cruise_altitude))
        # Dummy
        # self.cmds.add(command_dummy())

    def upload_mission(self):
        self.cmds.upload()

    def distance_to_current_waypoint(self):
        """
        Gets distance in metres to the current waypoint.
        It returns None for the first waypoint (Home location).
        """
        nextwaypoint=self.cmds.next
        if nextwaypoint == 0:
            return None
        missionitem=self.cmds[nextwaypoint-1] #commands are zero indexed
        if missionitem.command == mavutil.mavlink.MAV_CMD_NAV_WAYPOINT:
            lat = missionitem.x
            lon = missionitem.y
            alt = missionitem.z
            targetWaypointLocation = dronekit.LocationGlobalRelative(lat,lon,alt)
            distancetopoint = frame_conversion.get_distance_metres(self.vehicle.location.global_frame, targetWaypointLocation)
            return distancetopoint
        if missionitem.command == mavutil.mavlink.MAV_CMD_NAV_RETURN_TO_LAUNCH:
            targetWaypointLocation = self.vehicle.home_location
            distancetopoint = frame_conversion.get_distance_metres(self.vehicle.location.global_frame, targetWaypointLocation)
            return distancetopoint

    def arm(self):
        print("Basic pre-arm checks", file=self.output)
        # Don't try to arm until autopilot is ready
        while not self.vehicle.is_armable:
            print(" Waiting for vehicle to initialise...", file=self.output)
            time.sleep(1)

            
        print("Arming motors", file=self.output)
        # Copter should arm in GUIDED mode
        self.vehicle.mode = dronekit.VehicleMode("GUIDED")
        self.vehicle.armed = True    

        # Confirm vehicle armed before attempting to take off
        while not self.vehicle.armed:      
            print(" Waiting for arming...", file=self.output)
            time.sleep(1)

    def start_throtle(self):
        print("Starting throtle", file=self.output)
        self.vehicle.simple_takeoff(1)#self.cruise_altitude) # Take off to target altitude

        # Wait until the vehicle reaches a safe height before processing the goto (otherwise the command 
        #  after Vehicle.simple_takeoff will execute immediately).
        # while True:
        #     print(" Altitude: {:.1f}".format(self.vehicle.location.global_relative_frame.alt ), file=self.output)
        #     #Break and return from function just below target altitude.        
        #     if self.vehicle.location.global_relative_frame.alt>=0.95:#self.cruise_altitude*0.95: 
        #         print("Reached target altitude", file=self.output)
        #         break
        #     time.sleep(1)

    def simple_goto(self, waypoint):
        self.vehicle.simple_goto(waypoint)

    def run(self):
        self.show_battery()
        print("Clearing mission", file=self.output)
        self.clear_mission()
        print("Preparing mission", file=self.output)
        self.prepare_mission()
        print("Uploading mission", file=self.output)
        self.upload_mission()
        time.sleep(2)
        if self.output == sys.stdout:
            raw_input("Press enter to begin arming and taking off")
        self.arm()
        self.start_throtle()
        self.begin_mission()

    def close(self):
        print("Closing vehicle", file=self.output)
        self.vehicle.close()

    def wait(self):
        # while self.cmds.next != self.cmds.count:
        while not self.mission_ended():
            current_command = self.cmds[self.cmds.next-1].command

            if current_command not in commands_dict:
                print("!!! Command not in command dictionary! Command number: {}".format(current_command), file=self.output)

            print("{current} / {total}: {command} ".format(current=self.cmds.next,
                    total=self.cmds.count, command=commands_dict[current_command]), end="", file=self.output)

            if current_command in [mavutil.mavlink.MAV_CMD_NAV_WAYPOINT, mavutil.mavlink.MAV_CMD_NAV_RETURN_TO_LAUNCH]:
                print("@ distance {:.1f} ".format(self.distance_to_current_waypoint()), end="", file=self.output)

            print("@ altitude {:.1f}".format(self.altitude), file=self.output)

            time.sleep(1)

        print("## Mission ended", file=self.output)

    def get_status(self):
        current_command = self.cmds[self.cmds.next-1].command

        return {"current" : self.cmds.next, 
                "total" : self.cmds.count,
                "command" : commands_dict[current_command],
                "distance" : self.distance_to_current_waypoint(),
                "altitude" : self.altitude
                }

    def mission_ended(self):
        if self.cmds.next > 1:
            self.mission_ended_aux = True
            return False
        elif self.cmds.next == 1 and self.mission_ended_aux:
            self.mission_ended_aux = False
            return True
        elif self.cmds.next == 1 and not self.mission_ended_aux:
            return False

    def show_battery(self):

        print("########################\n"
              "# Battery information: #\n"
              "# voltage : {:>7.2f}    #\n"
              "# current : {:>7.3f}    #\n"
              "# level   : {:>7d}    #\n"
              "########################"
              .format(self.vehicle.battery.voltage, 
                       self.vehicle.battery.current, 
                       self.vehicle.battery.level), file=self.output)

    def get_location(self):
        return (self.current_location.lat, self.current_location.lon)

    def location_callback(self, vehicle, name, location):
        if location.global_relative_frame.alt is not None:
            self.altitude = location.global_relative_frame.alt

        self.current_location = location.global_relative_frame

    def mode_callback(self, vehicle, name, mode):
        print("## mode changed: {}".format(mode.name), file=self.output)


def command_takeoff(alt):
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_NAV_TAKEOFF, 0, 0, 0, 0, 0, 0, 0, 0, alt)

def command_waypoint(lat, lon, alt):
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_NAV_WAYPOINT, 0, 0, 0, 0, 0, 0, lat, lon, alt)

def command_land():
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_NAV_LAND, 0, 0, 0, 0, 0, 0, 0, 0, 0)

def command_rtl(alt):
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_NAV_RETURN_TO_LAUNCH, 0, 0, 0, 0, 0, 0, 0, 0, alt)

def command_lock():
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_DO_SET_RELAY, 0, 0, 0, 0, 0, 0, 0, 0, 0)

def command_unlock():
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_DO_SET_RELAY, 0, 0, 0, 1, 0, 0, 0, 0, 0)

def command_dummy():
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_NAV_LAND, 0, 0, 0, 0, 0, 0, 0, 0, 0)