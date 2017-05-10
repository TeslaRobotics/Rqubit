#include <BounceButton.h>
#include <clickTimer.h>

bool syncState = false;

#define PIN_BTN0 3
#define PIN_BTN1 4
#define PIN_BTN2 6


#define SETTINGS 0
#define CLICKING 1
#define PREGAME 2
#define SHOWING 3
#define FREEFALL 4

clickTimer clicker;
BounceButton btn0 = BounceButton(PIN_BTN0);
BounceButton btn1 = BounceButton(PIN_BTN1);
BounceButton btn2 = BounceButton(PIN_BTN2);

unsigned long actualMillis;
unsigned long lastClick;

byte Activity = SETTINGS;
String str = "";
String INSTR[16];
//String lastRotorData = "";

#define GO_SHOW 'q'
#define GO_PRE 'w'
const String GO_SET = "?";
#define DATA_START    '<'
#define DATA_END      '>'
const char MASTER_IND = '<';
const char SLAVE_IND  = '>';
const String DATA_MODE = "!";
const String PRED_MODE = "#";

const String HANDS_ON = "$";
const String HANDS_OFF = "~";

const String PENETRATE = "@";

const char plusfall = '+';
const char minusfall = '-';

int cutTiming = 900;
String gameMode = DATA_MODE;
boolean handsfree = false;

boolean firstclick = true;


void setup() {
  Serial.begin(38400);
  Serial.setTimeout(10);
}

void loop() {

  actualMillis = millis();
  actButtonsState();
  readBT();

  if (Activity == SETTINGS) {
    if (btn0.click()) {
      Activity = PREGAME;
      Serial.println("syncing...");
    }
    if (btn1.click()) {
      Activity = SETTINGS;
      Serial.println("settings");
    }

  }

  if (Activity == PREGAME) {
    Activity = CLICKING;
    firstclick = true;
    clicker.reset();
    clicker.setOffset();
    Serial.println("clicking");
    lastClick = millis();
  }


  if (Activity == CLICKING) {
    if (btn0.click()) {
      clicker.click();
      if (gameMode == PRED_MODE && !firstclick && millis() - lastClick > cutTiming ) {
        Activity = SHOWING;
        Serial.println("showing");
      }
      lastClick = millis();
      firstclick = false;
    }

    if (btn1.pressedTime() > 1500) {
      Activity = SETTINGS;
      Serial.println("settings");
    }
    else if (gameMode == DATA_MODE && btn1.pressedTime() > 50) {
      Activity = SHOWING;
      Serial.println("showing");
    }
  }

  if (Activity == SHOWING) {
    String str = clicker.getClicksString();
    Serial.println(MASTER_IND + str);
    //Serial.println(SLAVE_IND + lastRotorData);
    clicker.reset();

    if (handsfree && gameMode == DATA_MODE) {
      Activity = FREEFALL;
      Serial.println("fall number selection");
      actButtonsState();
    }
    else {
      Serial.println("settings");
      Activity = SETTINGS;
    }


  }

  if (Activity == FREEFALL) {

    if (btn1.pressedTime() > 1500) {
      Activity = SETTINGS;
      Serial.println("settings");
      Serial.println(GO_SET);
    }
    else if (btn1.pressedTime() > 50) {
      Serial.println(plusfall);
    }

    if (btn0.pressedTime() > 50) {
      Serial.println(minusfall);
    }

    if (btn2.pressedTime() > 50) {
      Serial.println(PENETRATE);
    }


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
    else  INSTR[index] += in;
    //if (in != '\n')
  }
}

void readBT() {

  if (Serial.available()) {

    str = Serial.readStringUntil('\n');
    splitArray();

    if (Activity == SETTINGS) {
      
      //Serial.println(str);

      if (INSTR[0] == DATA_MODE) {        
        gameMode = DATA_MODE;
        Serial.println("devices in data mode");
      }
      if (INSTR[0] == PRED_MODE) {
        gameMode = PRED_MODE;
        cutTiming =   INSTR[1].toInt();

        Serial.println("devices in predicition mode with threshold of " + INSTR[1]);
        //Serial.println(cutTiming);
      }

      if (INSTR[2] == HANDS_ON) {
        handsfree = true;
        Serial.println("handsfree now on ");
      }

      if (INSTR[2] == HANDS_OFF) {
        handsfree = false;
        Serial.println("handsfree now off ");
      }
      

    }
    else {
      if (INSTR[0] == GO_SET) {
        Serial.println("settings");
        Activity = SETTINGS;
      }
      else {
        Serial.println("no se puede");
        //Serial.print(str);
        //Serial.println("no");
      }
    }


  }
}

/*
void readRotorData() {
  char command = 'n';

  while (!radio.available()) ;
  radio.read( &command, sizeof(char) );

  if (command == DATA_START) {
    lastRotorData = "";
    long n;
    while (!radio.available()) ;
    radio.read( &n, sizeof(long) );


    long t0 = millis();
    long t1 = millis();

    long dataPoint;
    long counter = 0;

    while (t1 - t0 < 200 && counter < n) {
      if (radio.available()) {
        radio.read( &dataPoint, sizeof(long) );
        if (counter > 0)lastRotorData += ",";
        lastRotorData += String(dataPoint);

        counter++;
        t0 = millis();
      }

      t1 = millis();
    }

  }

}

void sendCommand(char c) {
  radio.stopListening();
  while (!radio.write( &c, sizeof(char) ));
  radio.startListening();
}*/

void actButtonsState() {
  btn0.actButtonState();
  btn1.actButtonState();
  btn2.actButtonState();
}


