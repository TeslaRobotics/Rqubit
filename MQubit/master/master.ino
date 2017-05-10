#include <SoftwareSerial.h>
#include <BounceButton.h>
#include <clickTimer.h>

#define PIN_BTN0 2
#define PIN_BTN1 4
#define PIN_BTN2 3
#define PIN_BUZZ 6


#define SETTINGS 0
#define CLICKING 1
#define PREGAME  2
#define SHOWING  3
#define FREEFALL 4

const char GO_SHOW = 'q';
const char GO_PRE = 'w';
const char ROTOR_IND = '<';
const char BALL_IND  = '>';
const char plusfall = '+';
const char minusfall = '-';
const String GO_SET = "?";
const String DATA_MODE = "!";
const String PRED_MODE = "#";
const String HANDS_ON = "$";
const String HANDS_OFF = "~";
const String PENETRATE = "@";

clickTimer clicker;
BounceButton btn0 = BounceButton(PIN_BTN0, 0);
BounceButton btn1 = BounceButton(PIN_BTN1, 0);
BounceButton btn2 = BounceButton(PIN_BTN2, 0);

unsigned long actualMillis;
unsigned long lastClick;

byte Activity = SETTINGS;
int cutTiming = 900;
boolean syncState = false;
boolean handsfree = false;
boolean firstclick = true;

String str = "";
String INSTR[16];
String gameMode = DATA_MODE;

SoftwareSerial Serial1(12, 13); // RX, TX

void setup() {
  Serial1.begin(9600);
  Serial1.setTimeout(10);

  pinMode(PIN_BUZZ, OUTPUT);

}

void loop() {

  actualMillis = millis();
  actButtonsState();
  readBT();

  if (Activity == SETTINGS) {
    if (btn0.click()) {
      Activity = PREGAME;
      buzz(300);
      Serial1.println("syncing...");
    }
    if (btn1.click()) {
      Activity = SETTINGS;
      Serial1.println("settings");
    }

  }

  if (Activity == PREGAME) {
    Activity = CLICKING;
    firstclick = true;
    clicker.reset();
    //clicker.setOffset();
    Serial1.println("clicking");
    lastClick = millis();
  }


  if (Activity == CLICKING) {
    if (btn0.click()) {
      clicker.click();

      if (clicker.getCounter() == 2) Serial1.println("rotor captured");

      else if (clicker.getCounter() > 2) {
        if (gameMode == PRED_MODE && !firstclick && millis() - lastClick > cutTiming ) {
          Activity = SHOWING;
          Serial1.println("showing");
        }
        lastClick = millis();
        firstclick = false;

      }
    }

    if (btn1.pressedTime() > 1500) {
      Activity = SETTINGS;
      Serial1.println("settings");
    }
    else if (gameMode == DATA_MODE && btn1.pressedTime() > 50) {
      Activity = SHOWING;
      Serial1.println("showing");
    }
  }

  if (Activity == SHOWING) {

    long* clickTimes = clicker.getClickTimes();

    for (int i = 0; i < clicker.getCounter(); ++i) {
      if (i == 0) Serial1.print(ROTOR_IND);
      if (i == 2) {
        Serial1.println();
        Serial1.print(BALL_IND);
      }
      Serial1.print(clickTimes[i]);
      if (i != clicker.getCounter() - 1) Serial1.print(',');
    }
    Serial1.println();
    clicker.reset();

    if (handsfree && gameMode == DATA_MODE) {
      Activity = FREEFALL;
      Serial1.println("fall number selection");
      actButtonsState();
    }
    else {
      Serial1.println("settings");
      Activity = SETTINGS;
    }


  }

  if (Activity == FREEFALL) {

    if (btn1.pressedTime() > 1500) {
      Activity = SETTINGS;
      Serial1.println("settings");
      Serial1.println(GO_SET);
    }
    else if (btn1.pressedTime() > 50) {
      Serial1.println(plusfall);
    }

    if (btn0.pressedTime() > 50) {
      Serial1.println(minusfall);
    }

    if (btn2.pressedTime() > 50) {
      Serial1.println(PENETRATE);
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

  if (Serial1.available()) {

    str = Serial1.readStringUntil('\n');
    splitArray();

    if (Activity == SETTINGS) {

      //Serial.println(str);

      if (INSTR[0] == DATA_MODE) {
        gameMode = DATA_MODE;
        Serial1.println("devices in data mode");
      }
      if (INSTR[0] == PRED_MODE) {
        gameMode = PRED_MODE;
        cutTiming =   INSTR[1].toInt();

        Serial1.println("devices in predicition mode with threshold of " + INSTR[1]);
        //Serial.println(cutTiming);
      }

      if (INSTR[2] == HANDS_ON) {
        handsfree = true;
        Serial1.println("handsfree now on ");
      }

      if (INSTR[2] == HANDS_OFF) {
        handsfree = false;
        Serial1.println("handsfree now off ");
      }


    }
    else {
      if (INSTR[0] == GO_SET) {
        Serial1.println("settings");
        Activity = SETTINGS;
      }
      else {
        Serial1.println("no se puede");
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

void buzz(int mill) {
  analogWrite(PIN_BUZZ, 255);
  delay(mill);
  analogWrite(PIN_BUZZ, 0);
  delay(100);
}

void actButtonsState() {
  btn0.actButtonState();
  btn1.actButtonState();
  btn2.actButtonState();
}


