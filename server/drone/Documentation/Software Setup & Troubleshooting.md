# Software Setup & Troubleshooting

This guide will help you get set up to run the AMF python scripts and troubleshoot issues you may encounter.

## Software to Install

**Python**: specifically, Python 2.

[Dronekit & Dronekit-sitl](http://python.dronekit.io/guide/quick_start.html#installation)

[MAVLink](https://mavlink.io/en/getting_started/installation.html)

Depending on what you're doing, you may not need MAVLink, but if you plan to send the drone commands in real-time you will need this.

## Ground Control software

Again, depending on what you're doing you don't necessarily need ground control software; however, it makes calibrating your drone hardware very easy and provides a nice GUI. Mission Planner also has a feature that allows you to upload python scripts to the drone through the GUI.

**For Linux**: [APM Planner 2.0](http://ardupilot.org/planner2/docs/installing-apm-planner-2.html)

**For Windows/MacOSX**: [Mission Planner](http://ardupilot.org/planner/docs/common-install-mission-planner.html)

APM Planner 2.0 also works on Windows/Mac, but Mission Planner is more robust.

## How to Launch the Drone

1. Clone the AMF repository from Github to your PC if you haven't already.
2. Open a terminal and navigate to amf/server/drone. You should see a file called launch.py.
3. You'll need to run this as root/administrator, and the script takes four arguments: USB port/baud rate, latitude, longitude, and console (1 to run in new console window, 0 to run in current console window). Linux example:

`sudo python2 launch.py /dev/ttyUSB0,57600 41.834873 -87.627006 1`

There are several things at this point that could go wrong:

1. If you get a message about a "heartbeat timeout", there is a file in the python library itself you need to edit. The file is __init__.py and it should be located in /home/.local/lib/python2.7/site-packages/dronekit. Now, go to line 2181 and find the statement `timeout = kwargs.get('timeout', 30)`. Change the number of seconds from 30 to 120, which will extend the time allotted before launch.py fails.

2. After connecting initially, if you get a message that says something about a heartbeat timeout after 5 seconds, followed shortly by a "link restored" message, you are most likely experiencing radio communication problems. Check the six-to-six pin connector between the drone radio and the Pixhawk. If that appears to be in working order, try using a different set of paired radios.

3. After you press enter to initialize and arm the drone, it could get stuck on "wait to initialize" or "waiting for arming". If the former, you most likely do not have GPS lock. If the latter, check the LED status indicator on the Pixhawk. If you see a flashing yellow light accompanied by one short beep, it means the battery failsafe has been activated. Make sure you are using a fully charged battery. Otherwise, you can disable the battery failsafe, either through your ground control station or through your python script. 
