#include <BounceButton.h>
#include <clickTimer.h>

#define PIN_BTN0 3
#define PIN_BTN1 4

#define SETTINGS 0
#define CLICKING 1
#define SHOWING 2

clickTimer clicker;
BounceButton btn0 = BounceButton(PIN_BTN0);
BounceButton btn1 = BounceButton(PIN_BTN1);

unsigned long actualMillis;
byte Activity = SETTINGS;
String str = "";
String INSTR[16];

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(10);
}

void loop() {

  actualMillis = millis();
  btn0.actButtonState();
  btn1.actButtonState();

  if(Activity == SETTINGS){
    if(btn0.click()) {
      Activity = CLICKING;
      Serial.println("clicking");
    }  
    if(btn1.click()) {
      Activity = SETTINGS;
      Serial.println("settings");      
    }  

    readBT();      
  }


  if(Activity == CLICKING){
    if(btn0.click()) {
       clicker.click();  
       Serial.println("click");  
    }

   if(btn1.pressedTime() > 1500) {
      Activity = SETTINGS;
      Serial.println("settings");      
    } 
    else if(btn1.pressedTime() > 50) {
      Activity = SHOWING;
      Serial.println("showing");      
    }    
    
  }

  if(Activity == SHOWING){
    String str = clicker.getClicksString();
    Serial.println(str);
    clicker.reset();
    Activity = CLICKING;
  }  
}


void splitArray(){

 for(int i = 0 ; i < 16 ; i++){
    INSTR[i] = "";
  }
  
  int index = 0 ;
  for(int i = 0 ; i < str.length() ; i++){
    char in = str.charAt(i);
    if(in == ',') index++;
    else INSTR[index] += in;    
  } 
}

void readBT(){
  
  if(Serial.available()){
    str = Serial.readString(); 
    splitArray();

    Serial.println(str);
    
    if(INSTR[0] == "a") Serial.println("llego a jjaja"); 
    if(INSTR[1] == "b") Serial.println("llego b jjaja");   
  }
}
