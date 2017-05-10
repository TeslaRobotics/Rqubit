#include <SPI.h>
#include "RF24.h"

bool syncState = false;
bool radioNumber = 0;
RF24 radio(9,10);

byte addresses[][6] = {"1Node","2Node"};


void setup() {

  Serial.begin(9600);
  
  radio.begin();
  radio.setPALevel(RF24_PA_LOW);
  
  if(radioNumber){
    radio.openWritingPipe(addresses[1]);
    radio.openReadingPipe(1,addresses[0]);
  }else{
    radio.openWritingPipe(addresses[0]);
    radio.openReadingPipe(1,addresses[1]);
  }
  
  radio.startListening();
}


void loop() {
  if(!syncState) sendSync();
  else {
    Serial.println();
    delay(500);
  }

}


void sendSync(){  
  const char SYNC = 's';
  radio.stopListening(); 
  while (!radio.write( &SYNC, sizeof(char) ));
  radio.startListening();

  syncState = true ;
  
}

