# Autonomous Movement Framework

This repository contains the Android Studio project, communication scripts and server files for drone request and location-based delivery.

![Android App Screenshot](http://i.imgur.com/7NMi2oy.jpg)

<!-- ## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. -->

### Prerequisites

1. A drone that communicates using MAVLink protocol (most vehicles made by [3DRobotics](https://3drobotics.com/) and other members of the [DroneCode Foundation](https://www.dronecode.org/about/project-members))

2. [Android Studio](https://developer.android.com/studio/index.html) installed.

3. DroneKit-Python environment installed. The [DroneKit documentation](http://python.dronekit.io/guide/quick_start.html) has a quick start tutorial where you can check how to download pip, Python 2.7 and dronekit on your machine. Run the "Hello Drone" script to see if everything is properly installed.

4. Clone this repository on your computer. You can do this by opening the command prompt on your machine and typing:
```
git clone https://github.com/illinoistech-itm/amf.git
```



### Setting Up

Open Android Studio and go to File > New > Import Project. Select the repository folder that you downloaded with the `git clone` command and Android Studio should be able to recognize the build files itself.

Connect your Android smartphone via USB on your computer and hit "Run" on  the upper toolbar on Android Studio.

![Run button](http://i.imgur.com/oV0oDbK.png)

This will install the app on your phone and open it when it's ready. It will ask for permissions to receive your GPS location. Click "Allow".

This step is required every time you make changes on your app, since your mobile device needs to be updated to the latest build.

## Running the tests

If you did the [Quick Start Tutorial](http://python.dronekit.io/guide/quick_start.html) on DroneKit's documentation you should have already installed and tested the tool for creating [simulated vehicles (SITL)](http://python.dronekit.io/develop/sitl_setup.html) on your computer. This is needed to test your algorithms on a fake simulated vehicle, instead of a real drone.

To make the tests run on a simulated environment, you need to change a line of code. Open drone.py on the server folder and search for the line that calls the function fleet.request(). Comment that line by typing the character "#" at the start of the line. Delete the "#" character on the line below to uncomment the request to the simulator (fleet.requestSITL()). Save the changes. You need to reverse these to the original form if you want to test on a real drone.  

Open your operational system command prompt and type:

```
dronekit-sitl copter --home=41.8348731,-87.62700589999997,584,353
```

This will initialize the simulated vehicle at the specified `--home` location (Illinois Institute of Technology in this case). You can modify the latitude and longitude parameters to change the start drone location.

Open another command prompt on your machine and go to the server subdirectory located on this project folder. You can change directories on your command prompt by typing `cd` followed by the path of the file.
Like this:

```
cd amf/server
```

After that, if you are on a Windows machine, type:
```
py server.py
```

If you are on a Linux/Mac machine, instead type:
```
python server.py
```

This will start the python server on your machine and it will be accessible by making a connection to your IP address.

Open the app and insert your IP address on the upper app menu (you can check this by typing `ipconfig` on your command prompt). After that, send an example request to your simulated drone. If everything is set correctly, your drone should arrive at your position, deliver the package and return to its home location.

## Built With

* Android Studio
* Atom
* Sublime Text

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Illinoin Institute of Technology/School of Applied Technology
* Capes (Coordenação de Aperfeiçoamento de Pessoal de Nível Superior)
