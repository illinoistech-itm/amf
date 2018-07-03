# Autonomous Movement Framework

This is the source code for the 915 mhz radio based command and control software for the autonomous movement framework for UAVs (drones).  

The code is contained in 4 folders

## AMF Android App

This folder contains the Android Studio 3.1 project source code for building and deploying an Android Phone app.

## AMF Gear

This folder contains the Android Studio 3.1 Android Wear project for a Samsung Gear Live V1 Android watch.

## Antenna

A Python project that was used to re-write radio ID firmware for cerating a 1 to many realtionship on a standard 915 mhz radio

## Server

 Python 3.x based HTTP server code to receive drone requests send from the AMF Android app and Wear App.   This Server application calls code built around the [Python Dronekit API](http://python.dronekit.io/ "Python Dronekit API")

### Prerequisites

1. A drone that communicates using MAVLink protocol using a [PixHawk controller](http://radiolink.com.cn/doce/product-detail-116.html "Radiolink PixHawk") and other members of the [DroneCode Foundation](https://www.dronecode.org/about/project-members))

1. [Android Studio 3.1.x](https://developer.android.com/studio/index.html) installed.

1. Python3 and Python2 and Pip installed 

1. Clone this repository on your computer. You can do this by opening the command prompt on your machine and typing:

```
git clone https://github.com/illinoistech-itm/amf.git
```

### License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

#### Acknowledgments

* Illinois Institute of Technology/School of Applied Technology
* Capes (Coordenação de Aperfeiçoamento de Pessoal de Nível Superior)
