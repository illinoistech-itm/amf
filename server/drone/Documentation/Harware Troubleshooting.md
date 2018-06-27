# AMF Hardware Troubleshooting Guide

This guide is meant to help future students familiarize themselves with the hardware used in the AMF UAVs and solve common hardware configuration problems.

## Components

CPU: [Pixhawk 1](https://docs.px4.io/en/flight_controller/pixhawk.html)

[Pixhawk LED color guide](http://ardupilot.org/copter/docs/common-leds-pixhawk.html)

Important LED colors:

Flashing green: Pixhawk has GPS lock. This is necessary for arming the drone. If you can't get a GPS lock in your current location, go outside into an open area. The GPS module needs to connect with at least six satellites in order to pass pre-arm checks, but you will need more than six satellites to fly with precision.

Flashing yellow + short beep: Battery failsafe activated. We are still researching what triggers this failsafe. We have used batteries that we know are 100% charged but the failsafe still activates. For now, we have disabled this failsafe in the drone.py file.

GPS: [Radiolink SE100](http://www.radiolink.com.cn/doce/product-detail-115.html)

IMPORTANT: the GPS module needs to be facing in the same directions as the Pixhawk, otherwise you will get an "inconsistent compass" error during compass calibration. One side of the GPS unit has two arrows with a green line in the middle, that's the front side. Point that in the same direction as the arrow on the Pixhawk.

Telemetry Radios: [3DR SiK Telemetry Radio](http://ardupilot.org/copter/docs/common-sik-telemetry-radio.html)

These radios can operate at 433 or 915 MHz. It doesn't matter which frequency is used as long as both radios you are pairing are on the same frequency. You will see a solid green light and a flashing red light when the radios are connected and communicating. A flashing green light indicates that the radio is looking for another radio to pair with. These radios have a range of about 300m, but this can be extended by reducing the baud rate and/or employing patch antennas.

One radio should be connected to the Pixhawk "Telem 1" port using a six-to-six pin connector wire. The other radio should be connected to your PC via USB. They can be easily configured and paired using a ground control software like APM Planner 2.0 (Ubuntu) or Mission Planner (Windows/MacOS). Once paired, radios will stay paired until they are reconfigured.

Safety Switch

The safety switch is necessary for the drone to arm. After you connect the battery to the Pixhawk you will hear a loud, obnoxious warning tone indicating that the motors are ready to be enabled. Press the switch to enable the motors and stop the warning tone. This will take a few seconds, so don't panic if you press the switch and nothing happens immediately.
