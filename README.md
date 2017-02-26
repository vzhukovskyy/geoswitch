# GeoSwitch
GeoSwitch is an Android geofencing app. Its main functionality is to execute HTTP POST request when geolocation trigger fires. 
I use it to switch on my home IoT device when I approach to my house, hence the name - GeoSwitch.

Since GPS sensor consumes battery power a lot, in addition to manual activation there is a on-charger mode designed for in-car usage when the app monitors 
GPS data only if device is connected to charger.

As supplementary functionality the app records the location history which can be exported to Google Earth or another application which handles KML format.
The app does not sumbit location data to anywhere, it keeps the history on the device for reasonably short amount of time in log file. The app does not 
utilize any geofencing API, it handles location data coming from GPS sensor by itself.

The app verified on Android 5.0 only.

Download from [here](https://github.com/vzhukovskyy/geoswitch/releases/)
