# Project Antenna

`code` - contains the revised Server Code. Explanations are inside the folder.
`demo-video` - contains a demo video of the project.
`diagrams` - contains a poster of the general overview of the system.
`paper` - contains the paper.
`presentation` - contains the PowerPoint presentation.

## Pre-reqs

```sudo apt-get install python-pip python-dev```

pip is then used to install dronekit and dronekit-sitl. Mac and Linux may require you to prefix these commands with sudo:

```pip install dronekit```
```pip install dronekit-sitl```

## Description

Project Antenna is a Python script that uses Dronekit Python libraries to reprogram 915 Mhz radio IDs to allow 1 to many relationship -- enabling the creating of a single central radio based server that can control up to 500 units from a single radio.

Project Antenna is a parallel development of the **Server** folder 