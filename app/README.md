# amf - application
Android application which performs calling the drone with location.

## Getting Started

Modifying - Usually use '[Android Studio](https://developer.android.com/studio/index.html)'

Testing - As this project 'app' is android application, this needs android device or virtual emulator.

### Prerequisites

1. [Android Studio](https://developer.android.com/studio/index.html) installed.

2. Clone this repository on your computer. You can do this by opening the command prompt on your machine and typing:
```
git clone https://github.com/illinoistech-itm/amf.git
```

### Android Studio Setting Up
 1. Install link : <https://developer.android.com/studio/install.html>
 (Last coded Android Studio version 3.0.1)

 2. Import project : Open Android Studio and go to File > New > Import Project. Select the repository folder that you downloaded with the `git clone` command and Android Studio should be able to recognize the build files itself.

 3. Download SDK : Built device's android version should be between minSdkVersion and targetSdkVersion. It is written in `amf/build.gradle` (updated Jan,25,2018)
   - compileSdkVersion : 23
   - buildToolsVersion : 26.0.2
   - minSdkVersion : 16
   - targetSdkVersion : 27

 4. Change Google Maps Android API key: [Get information and API key. ](https://developers.google.com/maps/documentation/android-api/)
   - The location where the code be changed :  `amf/app/src/debug/res/values/google_maps_api.xml`

## Running the tests

Explain how to run the automated tests for this system

### Using mobile device
Check the mobile device's company's website. Download and install the USB Driver. Connect the device and computer with cable. In Android Studio, run the project. Mobile device will show notification to get permissions about 2~3 times. Then program/project will run in mobile devices.

### Using virtual device
Tools - Android - AVD Manager (Android Virtual Device Manager). Create new virtual device and select the option you need. Choose the type of mobile device and go next. Choose the SDK version the mobile device has and go next. Then press finish. After run the emulator, you can change the orientation, portrait and landscape. At last, run the project with emulator.

## Built With (amf/build.gradle)

1. `compile fileTree(dir: 'libs', include: ['*.jar'])`
 -   A JAR(Java ARchive) is a package file format typically used to aggregate many Java class files and associated metadata and resources(text, images, etc.) into one file for distribution.

2. `compile 'com.android.support:appcompat-v7:23.4.0'`
 -   V7 appcompat library
 -   This library adds support for the ‘Action Bar’ user interface design pattern. This library includes support for material design user interface implementations.
 -   Key classes included in the v7 appcompat library:
ActionBar, AppCompatActivity, AppCompatDialog, ShareActionProvider

3. `compile 'com.google.android.gms:play-services:9.2.1'`
 -   To access the REST API on Android

4. `compile 'com.android.support.constraint:constraint-layout:1.0.0-alpha3'`
 -   This is a ‘ViewGroup’ which allows you to position and size widgets in a flexible way.

5. `testCompile 'junit:junit:4.12'`
 -   This is the most popular and widely-used unit testing framework for Java.

6. `androidTestCompile 'com.android.support.test.espresso:espresso-core:2.2.2'`
 -   A UI testing framework suitable for running functional UI testing inside your app
 -   Entry point to the Espresso framework. Test authors can initiate testing by using one of the on*methods(e.g. onView) or perform top-level user actions(e.g. pressBack)

7. `androidTestCompile 'com.android.support.test:runner:0.5'`
 -   These include the ‘InstrumentationRegistry’ Classes. InstrumentationRegistry is an esposed registry instance that holds a reference to the instrumentation running in the process and it’s arguments.

8. `androidTestCompile 'com.android.support:support-annotations:23.4.0'`
 -   The Annotation package provides APIs to support adding annotation metadata to your apps

## Version (amf/build.gradle)
- compileSdkVersion : 23
- buildToolsVersion : 26.0.2
- minSdkVersion : 16
- targetSdkVersion : 27

## Composition
Major program files in application project. (Except drawable, mipmap, values etc)
### Activity (app/src/main/java/com.example.kaeuc.dronemaster)
- MapsActivity.java (main activity)
- CheckActivity.java
- SplashScreen.java

### Layout (app/src/main/res/layout)
* Activity related
  - activity_maps.xml (main activity)
  - activity_check.xml
  - splash_screen.xml
* View (in CheckActivity)
  - grid_item.xml
  - my_gridview.xml
  - list_item.xml
  - my_listview.xml
* Dialog
  - search_dialog.xml
  - input_dialog.xml
* Alert
  - no_connection.xml

### Class (app/src/main/java/com.example.kaeuc.dronemaster)
* Constants.java - Set of the constants in application.
* DroneLocation.java - Function to sending drone location to server
* DroneLocationResponse.java - Interface of the JSONObject sent to server by DroneLocation.java
* FetchAddressIntentService.java - Get coordinate and send address to textView in MapsActivity
* GridViewAdapter.java - Adapter for grid view in CheckActivity
* ListViewAdapter.java - Adapter for list view in CheckActivity
* LocationDialog.java - On MapsActivity, shown dialog when submit button pressed
* Product.java - Class of the product which is shown on item view on CheckActivity
* ServerAccess.java - Class for accessing server
* ServerTaskResponse.java - Interface of the JSONObject to access server

## What is needed
* MapsActivity.java - sendRequestItemInfo() method should be changed to get information from item view (CheckActivity).
* In server, there should be added variable for get data from sendRequestItemInfo()
* CheckActivity.java - Current grid and list view shows same data, productList. The first idea was that grid view shows product and if user click the item, those items is added to list view. So, at first user found list view, it should be clear. And after send the information of items user chose, list view should be clear again.

## Authors

* **Eun Ji Jun**
 - [GitHib:allisonej](https://github.com/allisonej) (eunji3620@gmail.com)
* **Hye Rim Kim**
 - [GitHub:hkim125](https://github.com/hkim125) (zpooz1234@naver.com)
