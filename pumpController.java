import java.io.*;
import mmcoreJ.CharVector;

public class pumpController{

  CharVector sendSpeed = new CharVector(); //CharVector with speed setting control
  //Create message for setting motor speed
  sendSpeed.add('s'); //set sendSpeed.set(0, c)
  sendSpeed.add('x'); //shield sendSpeed.set(1, c)
  sendSpeed.add('y'); //motor sendSpeed.set(2, c)
  sendSpeed.add('n'); //speed sendSpeed.set(3, c)
  sendSpeed.add('n'); //speed sendSpeed.set(4, c)
  sendSpeed.add('n'); //speed sendSpeed.set(5, c)
  sendSpeed.add('\r'); //return/end message DO NOT EDIT

  CharVector sendRun = new CharVector(); //CharVector with motor running control
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

  void setSpeed(char x, char y, char n1, char n2, char n3){ //set speed of shield x motor y to nnn

    sendSpeed.set(1, x);
    sendSpeed.set(2, y);
    sendSpeed.set(3, n1);
    sendSpeed.set(4, n2);
    sendSpeed.set(5, n3);
    mmc.writeToSerialPort(port, sendSpeed);
    print("setting speed for motor " + y + " to " + n1 + n2 + n3);

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
    print("Shield: " + x + ", Motor: " + y + ", direction: " + d + ", time: " + t1 + t2 + t3 + t4 + t5 + "s");
    mmc.writeToSerialPort(port, sendRun);

    int T1 = Character.getNumericValue(t1);
    int T2 = Character.getNumericValue(t2);
    int T3 = Character.getNumericValue(t3);
    int T4 = Character.getNumericValue(t4);
    int T5 = Character.getNumericValue(t5);

    Thread.sleep(T1*10000000+T2*1000000+T3*100000+T4*10000+T5*1000+500); //wait until the pump has finished to call the next command

    return;

  }

  void clearChamber(){
    print("Clearing Chamber");
    run('2', '3', '2', '0', '0', '0', '1', '2'); //run perastaltic pump for 12s, about the ammount it takes to clear a full chamber
  }

  void wash(int n, int t, int v){
    //n is number of washes, t is time (in seconds) the buffer should sit in the chamber before getting cleared, v is the number of mL the wash should be
    int i = 0;
    int j = 0;
    for(i = 0; i < n; i++){
      print("Wash " + (i+1) + " of " + n);
      //clear chamber with perastaltic pump
      clearChamber();
      //infuse buffer PBS
      print("Infusing " + v + "mL of buffer");
      for(j = 0; j < v; j++){ //run once for each number of mL in your wash step
        run(vec, '1', '4', '1', '0', '0', '0', '2', '4'); //infuse 1 mL of buffer (~24s on our motor)
      }
      print("Waiting " + t + " seconds before clearing chamber");
      Thread.sleep(t*1000);
      //clear chamber with perastaltic pump
      clearChamber(sendRun);

    };

    return;

  }

  void clearTube(CharVector vec, char color){
    //The clearTube function insures that your desired solution is not diluted
    //by first filling the tube, then clearing the chamber
    if(color == 'b'){
      run(vec, '1', '1', '1', '0', '0', '0', '2', '3'); //fill the tube from clear point to chamber with desired solution
      clearChamber(vec);
    }
    if(color == 'c'){
      run(vec, '1', '2', '1', '0', '0', '0', '2', '5'); //fill the tube from clear point to chamber with desired solution
      clearChamber(vec); //clear solution from chamber
    }
    if(color == 'y'){
      run(vec, '1', '3', '1', '0', '0', '0', '2', '7'); //fill the tube from yellow point to chamber with desired solution
      clearChamber(vec);
    }
    if(color == 'g'){
      run(vec, '1', '4', '1', '0', '0', '0', '1', '1'); //fill the tube from yellow point to chamber with desired solution
      clearChamber(vec);
    }

    return;

  }

  void pushBuffer(CharVector vec, char color){
    if(color == 'b'){
      run(vec, '1', '4', '1', '0', '0', '0', '0', '5'); //fill the tube from clear point to chamber with desired solution
    }
    if(color == 'c'){
      run(vec, '1', '4', '1', '0', '0', '0', '0', '9'); //fill the tube from clear point to chamber with desired solution
    }
    if(color == 'y'){
      run(vec, '1', '4', '1', '0', '0', '0', '1', '1'); //fill the tube from yellow point to chamber with desired solution
    }
    if(color == 'g'){
      run(vec, '1', '4', '1', '0', '0', '0', '1', '1'); //fill the tube from yellow point to chamber with desired solution
    }
  }

}
