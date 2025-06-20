With Bluetooth App
==================
With Bluetooth is a sample app exploring android BLE api by allowing it to connect to Bluetooth GATT Server on Raspberry Pi transmitting live cpu temperature.   
Code to run the Raspberry Pi python GATT server can be found at https://github.com/shyamkp-11/cputemp. Server advertises 2 characteristics under one service:  
1. A _read/notify_ characteristic representing the Pi's _CPU temperature_ as a string.
2. A _read/write_ characteristic indicating the units to use to display the temperature; _'F'_ or _'C'_.

When connected user can:
1. Request current value of pi cpu temperature can be requested in Fahrenheit or Celsius by altering temperature unit characteristic. 
2. Enable _notify_ which set pi to continuously notify any changes in its temperature without need for an explicit request while is app is open. 
3. Activate foreground service option which will provide live updates of pi cpu temperature even if the app is closed. 

App uses Android bluetooth library along with Jetpack Compose, Foreground Services, device pairing using ConnectionDeviceManager, Koin and Jetpack Navigation.

## Screenshots
| App Launch Flow                                                                                     | Transmit data Flow                                                                                          | Temperature Service Flow                                                                                    |
|-----------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| <img src="docs/WithBluetooth%20launch%20flow.gif" width="230" height="512" alt="App Launch image"/> | <img src="docs/WithBluetooth%20notify%20flow.gif" width="230" height="512" alt="transmit data flow image"/> | <img src="docs/WithBluetooth%20service%20flow.gif" width="230" height="512" alt="Temperatur service flow"/> |


# Build and run the App
1. Clone the [Android App](https://github.com/shyamkp-11/GithubPlayroom) into Android Studio.
2. Clone repository [PiCpuTemp](https://github.com/shyamkp-11/cputemp) into a raspberry pi. And run script`python3 cputemp.py` on a raspberry pi 3 with bluetooth.
3. Gradle sync, clean and ▶️ Run the app `withbluetooth`.