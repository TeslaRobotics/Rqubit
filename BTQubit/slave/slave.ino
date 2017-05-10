#include <BounceButton.h>
#include <clickTimer.h>
#include <SoftwareSerial.h>


#define PIN_BTN0 3
#define PIN_BTN1 4

#define NONE 0
#define CLICKING 1
#define PREGAME 2
#define SHOWING 3

clickTimer clicker;
BounceButton btn0 = BounceButton(PIN_BTN0);
BounceButton btn1 = BounceButton(PIN_BTN1);

unsigned long actualMillis;
byte Activity = NONE;
String str = "";
String INSTR[16];


/* commands */
#define GO_SHOW       'q'
#define GO_PRE        'w'
#define GO_SET        '?'

const char SLAVE_IND  = '>';

char masterCommand = 'n';
SoftwareSerial Serial1(5, 2);

void setup() {
  Serial1.begin(38400);
  Serial1.setTimeout(10);

  //Serial.println("syncing...");
}

void loop() {

  actualMillis = millis();
  btn0.actButtonState();
  btn1.actButtonState();
  readBT();


  if (Activity == PREGAME) {
    Activity = CLICKING;
    clicker.reset();
    clicker.setOffset();
    //Serial.println("clicking");
  }


  if (Activity == CLICKING) {
    if (btn0.click()) {
      clicker.click();
      //Serial.println("click");
    }

  }

  if (Activity == SHOWING) {
    String str = clicker.getClicksString();
    Serial1.print(SLAVE_IND);
    Serial1.println(str);
    //sendData();
    clicker.reset();
    Activity = NONE;
    //Serial.println("waiting for syncing...");
  }
}


void splitArray() {

  for (int i = 0 ; i < 16 ; i++) {
    INSTR[i] = "";
  }

  int index = 0 ;
  for (int i = 0 ; i < str.length() ; i++) {
    char in = str.charAt(i);
    if (in == ',') index++;
    else INSTR[index] += in;
  }
}



void readBT() {
  if (Serial1.available()) {  
    masterCommand = Serial1.read();

    if (masterCommand == GO_SHOW) {
      Activity = SHOWING;
      //Serial.println("showing");
    }

    if (masterCommand == GO_PRE) {
      Activity = PREGAME;
      //Serial.println("syncing...");
    }

    if (masterCommand == GO_SET) {
      Activity = NONE;
      clicker.reset();
      //Serial.println("waiting for syncing...");
    }

    masterCommand = 'n';
  }  
}





