package pumpController;

import ij.IJ;
import java.io.*;
import javax.swing.JOptionPane;

import org.micromanager.utils.ReportingUtils;
import mmcorej.MMCoreJJNI;
import mmcorej.CharVector;
import mmcorej.CMMCore;

public class Pumps{ //implements MMPlugin{

    public String port; //string of port name
    public String portLabel = null; //shorthand label for port

    public CharVector sendSpeed = new CharVector(); //CharVector with speed setting control
    public CharVector sendRun = new CharVector(); //CharVector with motor running control
    public CharVector g = new CharVector();

    public String response = null; //response from serial port

    private CMMCore core = new CMMCore();

    //constructor
    public Pumps(String portName){

        this.port = portName;
    }

    // LOGS NOW SAVED IN CORELOGS.TXT IN C:\Program Files\Micro-Manager-1.4\CoreLogs

    public void connect(){

        ReportingUtils.logMessage("Connecting to port: " + port);

        portLabel = port;
        try {
            core.loadDevice(portLabel, "SerialManager", port);
        } catch (Exception e) {
            ReportingUtils.logError(e);
            e.printStackTrace();
        }

        try {
            core.setProperty(portLabel, "StopBits", "2");
        } catch (Exception e) {
            ReportingUtils.logError(e);
            e.printStackTrace();
        }

        try {
            core.setProperty(portLabel, "Parity", "None");
        } catch (Exception e) {
            ReportingUtils.logError(e);
            e.printStackTrace();
        }

        try {
            core.setProperty(portLabel, "BaudRate", "57600");
        } catch (Exception e) {
            ReportingUtils.logError(e);
            e.printStackTrace();
        }

        try {
            core.initializeDevice(portLabel);
        } catch (Exception e) {
            ReportingUtils.logError(e);
            e.printStackTrace();
        }

        try {
            core.setSerialPortCommand(port, "", "\r");
        } catch (Exception e) {
            ReportingUtils.logError(e);
            e.printStackTrace();
        }

        try {
            response = core.getSerialPortAnswer(port, "\n");
            ReportingUtils.logMessage(response);
        } catch (Exception e) {
            ReportingUtils.logError(e);
            e.printStackTrace();
        }

        return;
    }

    public void makeSpeed(){

        ReportingUtils.logMessage("Initializing sendSpeed vector");

        //Create message for setting motor speed
        sendSpeed.add('s'); //set sendSpeed.set(0, c)
        sendSpeed.add('x'); //shield sendSpeed.set(1, c)
        sendSpeed.add('y'); //motor sendSpeed.set(2, c)
        sendSpeed.add('n'); //speed sendSpeed.set(3, c)
        sendSpeed.add('n'); //speed sendSpeed.set(4, c)
        sendSpeed.add('n'); //speed sendSpeed.set(5, c)
        sendSpeed.add('\r'); //return/end message DO NOT EDIT

        return;
    }

    public void makeRun(){

        ReportingUtils.logMessage("Initializing sendRun vector");

        //Create message for running motor
        sendRun.add('r'); //run sendRun.set(0, c)
        sendRun.add('x'); //shield sendRun.set(1, c)
        sendRun.add('y'); //motor sendRun.set(2, c)
        sendRun.add('d'); //direction (1 FW, 2 BW) sendRun.set(3, c)
        sendRun.add('t'); //time sendRun.set(4, c)
        sendRun.add('t'); //time sendRun.set(5, c)
        sendRun.add('t'); //time sendRun.set(6, c)
        sendRun.add('t'); //time sendRun.set(7, c)
        sendRun.add('t'); //time sendRun.set(8, c)
        sendRun.add('\r'); //return/end message DO NOT EDIT

        return;
    }

    public void setSpeed(char x, char y, char n1, char n2, char n3){ //set speed of shield x motor y to nnn

        sendSpeed.set(1, x);
        sendSpeed.set(2, y);
        sendSpeed.set(3, n1);
        sendSpeed.set(4, n2);
        sendSpeed.set(5, n3);

        try {
            core.writeToSerialPort(port, sendSpeed);
        } catch (Exception e) {
            System.out.println("Trouble writing to serial port");
            e.printStackTrace();
        }

        ReportingUtils.logMessage("setting speed for shield " + x + ", motor " + y + " to " + n1 + n2 + n3);

        return;
    }

    public void setAllFull(){

        ReportingUtils.logMessage("Setting all motors to full speed");

        //set all motors to full speed
        setSpeed('1', '1', '2', '5', '5');
        setSpeed('1', '2', '2', '5', '5');
        setSpeed('1', '3', '2', '5', '5');
        setSpeed('1', '4', '2', '5', '5');
        setSpeed('2', '3', '2', '5', '5');
        setSpeed('2', '4', '2', '5', '5');

        return;
    }


    public void runMotor(char x, char y, char d, char t1, char t2, char t3, char t4, char t5){

        sendRun.set(1, x);
        sendRun.set(2, y);
        sendRun.set(3, d);
        sendRun.set(4, t1);
        sendRun.set(5, t2);
        sendRun.set(6, t3);
        sendRun.set(7, t4);
        sendRun.set(8, t5);

        ReportingUtils.logMessage("Shield: " + x + ", Motor: " + y + ", direction: " + d + ", time: " + t1 + t2 + t3 + t4 + t5 + "s");

        try {
            core.writeToSerialPort(port, sendRun);
        } catch (Exception e) {
            ReportingUtils.logError(e);
            e.printStackTrace();
        }

        int T1 = Character.getNumericValue(t1);
        int T2 = Character.getNumericValue(t2);
        int T3 = Character.getNumericValue(t3);
        int T4 = Character.getNumericValue(t4);
        int T5 = Character.getNumericValue(t5);

        try {
            Thread.sleep(T1*10000000+T2*1000000+T3*100000+T4*10000+T5*1000+500); //wait until the pump has finished to call the next command
        } catch(InterruptedException e) {
            System.out.println("Sleep was interrupted");
            e.printStackTrace();
        }

        return;
    }

    public void clearChamber(){

        ReportingUtils.logMessage("Clearing Chamber");
        runMotor('2', '3', '2', '0', '0', '0', '1', '2'); //run perastaltic pump for 12s, about the amount it takes to clear a full chamber

        return;
    }

    public void wash(int n, int t, int v){

        //n is number of washes, t is time (in seconds) the buffer should sit in the chamber before getting cleared, v is the number of mL the wash should be
        int i = 0;
        int j = 0;

        for(i = 0; i < n; i++){

            ReportingUtils.logMessage("Wash " + (i+1) + " of " + n);
            //clear chamber with perastaltic pump
            clearChamber();

            //infuse buffer PBS
            ReportingUtils.logMessage("Infusing " + v + "mL of buffer");

            for(j = 0; j < v; j++){ //run once for each number of mL in your wash step
                runMotor('2', '4', '1', '0', '0', '0', '1', '2'); //infuse 1 mL of buffer (~24s on our motor)
            }

            ReportingUtils.logMessage("Waiting " + t + " seconds before clearing chamber");

            try {
                Thread.sleep(t*1000); //wait until the pump has finished to call the next command
            } catch(InterruptedException e) {
                e.printStackTrace();
                ReportingUtils.logMessage("Sleep was interrupted");
            }

            //clear chamber with perastaltic pump
            clearChamber();

        };

        return;
    }

    //Initialize procedures
    public void startup(){

        ReportingUtils.logMessage("Initializing... ");

        connect();
        makeSpeed(); //make speed vector
        makeRun(); //make run vector
        setAllFull(); //set all motors to full speed
        runMotor('1', '1', '1', '0', '0', '0', '0', '1');

        return;
    }

}