#include <BounceButton.h>
#include <clickTimer.h>
#include <SPI.h>
#include "RF24.h"

bool radioNumber = 1;
RF24 radio(9, 10);

byte addresses[][6] = {"1Node", "2Node"};


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
#define DATA_START    '<'
#define DATA_END      '>'
#define GO_SET '?'

char masterCommand = 'n';

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(10);

  radio.begin();
  radio.setPALevel(RF24_PA_LOW);

  if (radioNumber) {
    radio.openWritingPipe(addresses[1]);
    radio.openReadingPipe(1, addresses[0]);
  } else {
    radio.openWritingPipe(addresses[0]);
    radio.openReadingPipe(1, addresses[1]);
  }

  radio.startListening();

  Serial.println("syncing...");
}

void loop() {

  actualMillis = millis();
  btn0.actButtonState();
  btn1.actButtonState();
  readRadio();


  if (Activity == PREGAME) {
    Activity = CLICKING;
    clicker.reset();
    clicker.setOffset();
    Serial.println("clicking");
  }


  if (Activity == CLICKING) {
    if (btn0.click()) {
      clicker.click();
      Serial.println("click");
    }

  }

  if (Activity == SHOWING) {
    String str = clicker.getClicksString();
    Serial.println(str);
    sendData();
    clicker.reset();
    Activity = NONE;
    Serial.println("waiting for syncing...");
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

void sendData() {

  radio.stopListening();
  long n = (long) clicker.getCounter();
  long* data =  clicker.getClickTimes();
  char c = DATA_START;

  while (!radio.write( &c, sizeof(char) ));
  while (!radio.write( &n, sizeof(long) ));

  for (int i = 0; i < n; i++) {
    long d = data[i];
    while (!radio.write( &d, sizeof(long) ));
  }
  radio.startListening();
  //sendCommand(DATA_END);
}


void readRadio() {

  if (radio.available()) {                                   // While there is data ready
    radio.read( &masterCommand, sizeof(char) );             // Get the payload

    if (masterCommand == GO_SHOW) {
      Activity = SHOWING;
      Serial.println("showing");
    }

    if (masterCommand == GO_PRE) {
      Activity = PREGAME;
      Serial.println("syncing...");
    }

    if (masterCommand == GO_SET) {
      Activity = NONE;
      clicker.reset();
      Serial.println("waiting for syncing...");
    }

    masterCommand = 'n';
  }

}

void sendCommand(char c) {
  radio.stopListening();
  while (!radio.write( &c, sizeof(char) ));
  radio.startListening();
}




