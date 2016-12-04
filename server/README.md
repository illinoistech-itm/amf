# Autonomous Movement Framework - Server

This repository contains the updated code for the AMF Server framework. This update should enable the support for dynamically switching the NetID of the 3DR Antenna before a drone is sent.

The NetID is updated by sending a custom command to the antenna via the serial port. This is highly based on Sikset (a command-line tool developed to program the 3DR antennas), found at: https://github.com/mr337/sikset

The 'database' folder contains the sqlite database which is populated with the current drones, their names and their respective NetID. The database should be viewed or modified with an sqlite editor tool (such as DB Browser in Ubuntu App Store). 

The Server runs on port 8080 and accepts commands directly from the Android app. Once a request is accepted from the application, a drone is chosen randomly from the databse, the NetID of the sending antenna is switched to match that of the drone and then the flight plan is sent to the drone. Upon completion, the drone is marked as not available in the database.


To-do:

1. Figure out a better timing for the serial commands to be sent as sometimes it misses an update.
2. Make the NetID change request asynchronously so that the application does not hang.
