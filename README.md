# PumpControls
Interfacing pumpy with micromanager and scope

### Installation instructions:

* Navigate to your MicroManger-1.4 install location (something like C:/Program Files/MicroManager-1.4)

* Install pumpControllerV1.jar under MicroManager-1.4/jre/lib/ext

* [Set up freeSerialPort](https://micro-manager.org/wiki/FreeSerialPort) in hardware configuration

* Open micromanager script panel under "Tools>Script Panel"

* Use "import pumpController.Pumps;" at the top of your script

* Find serial port label under windows devices menu (Printers & Devices should recognize any serial ports that are in use)

* Create the pump object using "Pumps objectName = new Pumps(String portLabel);"

* Call any commands using "objectName.command();"

### Command list:

```
startup();
```
Connects to serialPort, sets all motors to 100% speed

___

```
setSpeed(char Shield, char Motor, char n1, char n2, char n3); 
```
Sets speed of Motor on Shield to n1n2n3

___

```
runMotor(char Shield, char Motor, char Direction, char t1, char t2, char t3, chat t4, char t5); 
```

Runs Motor on Shield in Direction for t1t2t3t4t5 seconds

___

```
wash(int numberOfWashes, int bufferSitTime, int washVolume); 
```

Washes the chamber a specified number of times with the sepcified ammount of mL sitting in the chamber for the specified ammount of time.

___

```
clearChamber();
```

Clears any fluid remaining in the chamber.

___

```
setAllFull();
```

Sets all motors to full speed. Is called by startup();

___

```
connect();
```

Connects to serial port. Is called by startup().

___

```
makeRun();
```

Initializes vector used to run motors. Is called by startup(). Should always be called before first call to runMotor().

___

```
makeSpeed
```

Initializes vector used to set motor speeds. Is called by startup(). Should always be called before first call to setSpeed().
