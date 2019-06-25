import java.io.*;
import mmcorej.CharVector;
import mmcorej.CMMCore;

public class pumpController{

    public String port; //string of port name
    public String consoleLog; //name of log
    private Writer log = null; //prealloaction of log
    public CharVector sendSpeed = new CharVector(); //CharVector with speed setting control
    public CharVector sendRun = new CharVector(); //CharVector with motor running control
    private CMMCore core = new CMMCore();

    //create console log
    void createLog(){

        try{
            log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(consoleLog), "utf-8"));
            log.write("Created log");
            System.out.println("Log created with name: " + consoleLog);
        }
        catch(IOException ex){
            ex.printStackTrace();
            System.out.println("Error creating or writing to log");
        }

        return;
    }

    void makeSpeed(){

        try {
            log.write("Initializing sendSpeed vector");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't write to log");
        }

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

    void makeRun(){

        try {
            log.write("Initializing sendRun vector");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't write to log");
        }

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

    void setSpeed(char x, char y, char n1, char n2, char n3){ //set speed of shield x motor y to nnn

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

        try {
            log.write("setting speed for shield " + x + ", motor " + y + " to " + n1 + n2 + n3);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't write to log");
        }

        return;
    }

    void setAllFull(){

        try {
            log.write("Setting all motors to full speed");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't write to log");
        }

        //set all motors to full speed
        setSpeed('1', '1', '2', '5', '5');
        setSpeed('1', '2', '2', '5', '5');
        setSpeed('1', '3', '2', '5', '5');
        setSpeed('1', '4', '2', '5', '5');
        setSpeed('2', '3', '2', '5', '5');
        setSpeed('2', '4', '2', '5', '5');

        return;
    }


    void runMotor(char x, char y, char d, char t1, char t2, char t3, char t4, char t5){

        sendRun.set(1, x);
        sendRun.set(2, y);
        sendRun.set(3, d);
        sendRun.set(4, t1);
        sendRun.set(5, t2);
        sendRun.set(6, t3);
        sendRun.set(7, t4);
        sendRun.set(8, t5);

        try {
            log.write("Shield: " + x + ", Motor: " + y + ", direction: " + d + ", time: " + t1 + t2 + t3 + t4 + t5 + "s");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't write to log");
        }

        try {
            core.writeToSerialPort(port, sendRun);
        } catch (Exception e) {
            System.out.println("Trouble writing to serial port");
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

    void clearChamber(){
        try {
            log.write("Clearing Chamber");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't write to log");
        }
        runMotor('2', '3', '2', '0', '0', '0', '1', '2'); //run perastaltic pump for 12s, about the amount it takes to clear a full chamber

        return;
    }

    void wash(int n, int t, int v){
        //n is number of washes, t is time (in seconds) the buffer should sit in the chamber before getting cleared, v is the number of mL the wash should be
        int i = 0;
        int j = 0;

        for(i = 0; i < n; i++){

            try {
                log.write("Wash " + (i+1) + " of " + n);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Couldn't write to log");
            }
            //clear chamber with perastaltic pump
            clearChamber();

            //infuse buffer PBS
            try {
                log.write("Infusing " + v + "mL of buffer");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Couldn't write to log");
            }

            for(j = 0; j < v; j++){ //run once for each number of mL in your wash step
                runMotor('1', '4', '1', '0', '0', '0', '2', '4'); //infuse 1 mL of buffer (~24s on our motor)
            }

            try {
                log.write("Waiting " + t + " seconds before clearing chamber");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Couldn't write to log");
            }

            try {
                Thread.sleep(t*1000); //wait until the pump has finished to call the next command
            } catch(InterruptedException e) {
                e.printStackTrace();
                System.out.println("Sleep was interrupted");
            }

            //clear chamber with perastaltic pump
            clearChamber();

        };

        return;
    }

    //Initialize procedures
    void startup(){

        createLog(); //create the log
        makeSpeed(); //make speed vector
        makeRun(); //make run vector
        setAllFull(); //set all motors to full speed

    }

}