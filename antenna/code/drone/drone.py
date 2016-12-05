from __future__ import print_function
import dronekit
from pymavlink import mavutil
import time
import frame_conversion
import sys

"""
Important links for resources:

http://ardupilot.org/copter/docs/common-mavlink-mission-command-messages-mav_cmd.html
http://ardupilot.org/planner/docs/common-mavlink-mission-command-messages-mav_cmd.html

http://python.dronekit.io/automodule.html
http://python.dronekit.io/about/index.html
"""

commands_dict = { mavutil.mavlink.MAV_CMD_NAV_TAKEOFF: "taking off",
                  mavutil.mavlink.MAV_CMD_NAV_WAYPOINT: "flying to waypoint",
                  mavutil.mavlink.MAV_CMD_NAV_LAND: "landing",
                  mavutil.mavlink.MAV_CMD_NAV_RETURN_TO_LAUNCH: "RTL",
                  mavutil.mavlink.MAV_CMD_DO_SET_RELAY: "toggling relay"
}

class Drone():
    """
    Class that encapsulates a single drone.
    Designed with our specific application in mind.
    """
    cruise_altitude = None
    address = None
    vehicle = None
    cmds = None
    lat = None
    lon = None

    output = None

    #auxiliary variables for detecting mission end
    mission_ended_aux = None
    mission_ended_bool = None

    def __init__(self, address, latitude, longitude, altitude=10, output=sys.stdout):
        """
        Object constructor.
        address - string to be used to connect to the drone (eg. "com4", "tcp:127.0.0.1:5760")
        latitude - target latitude
        longitude - target longitude
        altitude - travel altidude
        output - log location
        """
        self.lat = latitude
        self.lon = longitude
        self.cruise_altitude = altitude
        self.address = address

        self.output = output

        #auxiliary variables for detecting mission end
        self.mission_ended_aux = False
        self.mission_ended_bool = False

        print('Drone target: {}, {}'.format(self.lat, self.lon), file=self.output)

    def connect(self):
        """
        Connects to the drone using the provided address and register callbacks.
        Blocks until connection is established.
        """
        print('Connecting to vehicle on: {}'.format(self.address), file=self.output)
        self.vehicle = dronekit.connect(self.address, wait_ready=True, baud=57600)
        print('Connection established', file=self.output)

        self.cmds = self.vehicle.commands
        self.vehicle.add_attribute_listener('location', self.location_callback)
        self.vehicle.add_attribute_listener('mode', self.mode_callback)

    def clear_mission(self):
        """
        Clears the mission and uploads the blank mission to the drone
        """
        self.cmds.clear()
        self.cmds.upload()

    def download_mission(self):
        """
        Downloads the current mission. Blocks until download finishes.
        """
        self.cmds.download()
        self.cmds.wait_ready()

    def begin_mission(self):
        """
        Changes mode to AUTO so uploaded mission begins to be executed.
        Downloads the mission to we have access to the home location, which
        is only now set.
        """
        #auxiliary variables for detecting mission end
        self.mission_ended_aux = False
        self.mission_ended_bool = False

        self.vehicle.mode = dronekit.VehicleMode("AUTO")
        self.download_mission()

    def prepare_mission(self):
        """
        Prepares the default mission which then needs to be uploaded.
        """
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
        # Return to launch
        self.cmds.add(command_rtl(self.cruise_altitude))


    def upload_mission(self):
        """
        Uploads the current mission.
        """
        self.cmds.upload()

    def distance_to_current_waypoint(self):
        """
        Gets distance in metres to the current waypoint.
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
        """
        Attempts to arm the motors.
        Should be called before trying to take off.
        Blocks until they arm.
        """
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
        """
        The copter can't start a mission from the ground without the throttle 
        being activated. This is provided as a way to do that.
        """
        print("Starting throtle", file=self.output)
        self.vehicle.simple_takeoff(1)

    def simple_goto(self, waypoint):
        """
        simple_goto wrapper for test purposes.
        """
        self.vehicle.simple_goto(waypoint)

    def run(self):
        """
        Default operation order.
        """
        #self.show_battery()
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
        """
        Closes the vehicle object and connection to the drone, freeing the port.
        """
        print("Closing vehicle", file=self.output)
        self.vehicle.close()

    def wait(self):
        """
        Blocks until mission ended.
        Logs the state periodicaly.
        """
        while not self.mission_ended():
            self.log_status()

            time.sleep(1)

        print("## Mission ended", file=self.output)

    def get_status(self):
        """
        Returns a dictionary with the current status.
        current - current command index + 1 (they are zero indexed)
        total - total number of mission items
        command - current command name, depends on dictionary at begining of file.
        distance - distnce to next waypoint.
        altitude - current altitude
        """
        current_command = self.cmds[self.cmds.next-1].command

        return {"current" : self.cmds.next,
                "total" : self.cmds.count,
                "command" : commands_dict[current_command],
                "distance" : self.distance_to_current_waypoint(),
                "altitude" : self.altitude
                }

    def check_mission_ended(self):
        """
        Dronekit doesn't provide a way to check for end of mission.
        Fairly ugly way to do it. For some reason when mission ends the next index
        is set back to 1.
        This checks if we have already passed item 1 once.
        Needs to be called at least once in the middle of the mission after the 
        first command is done. This is done on the location callback function,
        ensuring this works.
        Use mission_ended() to actually get the bool value.
        """
        if self.cmds.next > 1:
            self.mission_ended_aux = True
        elif self.cmds.next == 1 and self.mission_ended_aux:
            self.mission_ended_bool = True


    def mission_ended(self):
        """
        Returns True if the mission has ended.
        """
        return self.mission_ended_bool

    def show_battery(self):
        """
        Logs the current battery information.
        This depends on proper configuration of power unit.
        """

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
        """
        Returns tuple (latitude, longitude) of current location.
        """
        return (self.current_location.lat, self.current_location.lon)

    def location_callback(self, vehicle, name, location):
        """
        Saves the current location every time a new location message is received
        via telemetry.
        Also calls check_mission_ended() to ensure it works properly.
        """
        if location.global_relative_frame.alt is not None:
            self.altitude = location.global_relative_frame.alt

        self.check_mission_ended()
        self.current_location = location.global_relative_frame

    def mode_callback(self, vehicle, name, mode):
        """
        Logs when the mode is changed.
        """
        print("## mode changed: {}".format(mode.name), file=self.output)

    def log_status(self):
        """
        Logs the current status.
        """
        current_command = self.cmds[self.cmds.next-1].command

        if current_command not in commands_dict:
                print("!!! Command not in command dictionary! Command number: {}".format(current_command), file=self.output)
        else:
            print("{current} / {total}: {command} ".format(current=self.cmds.next,
                        total=self.cmds.count, command=commands_dict[current_command]), end="", file=self.output)

            if current_command in [mavutil.mavlink.MAV_CMD_NAV_WAYPOINT, mavutil.mavlink.MAV_CMD_NAV_RETURN_TO_LAUNCH]:
                print("@ distance {:.1f} ".format(self.distance_to_current_waypoint()), end="", file=self.output)

            print("@ altitude {:.1f}".format(self.altitude), file=self.output)


def command_takeoff(alt):
    """
    Wrapper to hide mavlink parameters.
    Returns command to takeoff to specified altitude.
    """
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_NAV_TAKEOFF, 0, 0, 0, 0, 0, 0, 0, 0, alt)

def command_waypoint(lat, lon, alt):
    """
    Wrapper to hide mavlink parameters.
    Returns command to go to specified location.
    """
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_NAV_WAYPOINT, 0, 0, 0, 0, 0, 0, lat, lon, alt)

def command_land():
    """
    Wrapper to hide mavlink parameters.
    Returns command to land.
    """
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_NAV_LAND, 0, 0, 0, 0, 0, 0, 0, 0, 0)

def command_rtl(alt):
    """
    Wrapper to hide mavlink parameters.
    Returns command to return to launch.
    """
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_NAV_RETURN_TO_LAUNCH, 0, 0, 0, 0, 0, 0, 0, 0, alt)

def command_lock():
    """
    Wrapper to hide mavlink parameters.
    Returns command to lock the solenoid.
    Depends on solenoid configuration.
    """
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_DO_SET_RELAY, 0, 0, 0, 0, 0, 0, 0, 0, 0)

def command_unlock():
    """
    Wrapper to hide mavlink parameters.
    Returns command to unlock the solenoid.
    Depends on solenoid configuration.
    """
    return dronekit.Command(0, 0, 0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavutil.mavlink.MAV_CMD_DO_SET_RELAY, 0, 0, 0, 1, 0, 0, 0, 0, 0)
