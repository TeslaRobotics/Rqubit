#include <SPI.h>
#include "RF24.h"

bool syncState = false;
bool radioNumber = 1;
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
  if(!syncState) waitSync();
  else {
    Serial.println();
    delay(500);
  }

}




void waitSync() {  
  const char SYNC = 's';
  char syncSignal = 'n';

  while ( syncSignal !=  SYNC) {
    if (radio.available()) {                                   // While there is data ready
      radio.read( &syncSignal, sizeof(char) );             // Get the payload
    }
  }
    syncState = true ;
}

