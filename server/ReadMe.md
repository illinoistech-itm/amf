[THINGS TO DO - follow README.md]
sudo apt install python-pip

pip install dronekit
pip install dronekit-sitl
pip install serial
pip install pyserial
pip install simplejson
pip install requests

install dronekit documentation

git clone https://github.com/illinoistech-itm/amf.git

## Sample structure of JSON block

```json
{
"instanceID":"9cd08b91-0960-4208-a1a7-9fa582e50e91",
"requestID":"REQUEST_19-Jul-2016_10:31:56:3156",
"latitude":41.83173105748931,
"longitude":-87.6270828768611,
"address":"3300 South Federal Street"
}  
```

[OPEN THE SERVER]
~/amf/server python3 server.py  // command under python3


[MAKE SURE]
#under the same WIFI network (app & server)
#install visual studio code
#make python2.7 as a default version
#change timeout value 30 to 120 in file 
#change address 10.0.0.7:8080 in server.py and string.xml 


 [ERROR]
#if there's problem in connecting with radio, check dmesg log
#if server can't open port for ttyUSB0, type 'sudo chmod 777 /dev/ttyUSB0'
 (on linux)
