# Autonomous Movement Framework

This repository contains the Android Studio project, communication scripts and server files for drone request and location-based delivery.

## Setting Up

+ Android Studio
 1. Install link : <https://developer.android.com/studio/install.html>
 (Last coded Android Studio version 3.x)

 2. Import project : Open Android Studio and go to File > New > Import Project. Select the repository folder that you downloaded with the `git clone` command and Android Studio should be able to recognize the build files itself.

 3. Download SDK : Built device's android version should be between minSdkVersion and targetSdkVersion. It is written in `amf/build.gradle` (updated Jan,25,2018)
   - compileSdkVersion : 23
   - buildToolsVersion : 26.0.2
   - minSdkVersion : 16
   - targetSdkVersion : 27

 4. Change Google Maps Android API key: [Get information and API key. ](https://developers.google.com/maps/documentation/android-api/)
   - The location where the code be changed :  `amf/app/src/debug/res/values/google_maps_api.xml`

 5. Download smartphone USB Driver and install

### Build project with smartphone
 Connect your Android smartphone via USB on your computer and hit "Run" on  the upper toolbar on Android Studio.

###### Run button on right up side.
![Run button](./image/runButton.jpg)
###### Run button in Menu. (Run>Run 'app')
![Run Menu](./image/runMenu.jpg)

This will install the app on your phone and open it when it's ready. It will ask for permissions to receive your GPS location. Click "Allow".

This step is required every time you make changes on your app, since your mobile device needs to be updated to the latest build.

