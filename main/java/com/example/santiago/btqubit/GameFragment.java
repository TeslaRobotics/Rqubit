package com.example.santiago.btqubit;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static android.R.color.darker_gray;
import static android.R.color.holo_green_dark;
import static com.example.santiago.btqubit.GeneralStatsFragment.cutTiming;
import static com.example.santiago.btqubit.SecondActivity.tableName;
import static com.example.santiago.btqubit.SettingsFragment.handsfree;


public class GameFragment extends Fragment {
    public static final String ARG_SECTION_NUMBER = "section_number";

    TextToSpeech tts;
    BluetoothChatService BTserv;

    final String MASTER_IND = "<";
    final String SLAVE_IND  = ">";
    static final String DATA_MODE = "!";
    static final String PRED_MODE = "#";
    static final String GO_SET = "?";
    static final String HANDS_ON = "$";
    static final String HANDS_OFF = "~";
    final String PENETRATE = "@";

    final String PLUS_FALL = "+";
    final String MINUS_FALL = "-";

    static int handsFreeFallPos = 0;
    static boolean inFallsetting = false;


    final String SETFALL_COMMAND  = "setFall";
    final String SENDBT_COMMAND  = "btSend";
    final String SHOWSAMPLE_COMMAND  = "showSample";
    final String SAVESAMPLE_COMMAND  = "saveSample";

    static final byte datamode = 0;
    static final byte prdcmode = 1;

    final byte COMMAND = 0;
    final byte BTSEND = 1;
    final byte BTRECE = 2;
    final byte COMMENT = 3;



    TextView BTterminalText;
    ScrollView BTterminalScroll;

    Button sendButton, TclearButton, TautoButton, TmodeButton, CfallButton, ThandsButton;
    EditText sendEditText;

    Boolean autScrollOn = true;
    Boolean fallCommandState = false;
    String commandString = "";
    static byte gameMode = datamode;
    static byte penetrationLevel = 0;

    final String clearTerString = "Holi boli , este es un terminal muy sexy" + "\n\n\n";
    SpannableStringBuilder terminalSb = new SpannableStringBuilder();

    String ballData = "";
    String fallPos = "";
    String rotorData = "";
    Handler h;

    DataAnalysis Dana;
    SampleAnalysis ana;

    private final int[] NUMBERS = new int[]{0, 26, 3, 35, 12, 28, 7, 29, 18, 22, 9, 31, 14, 20, 1, 33, 16, 24, 5, 10, 23, 8, 30, 11, 36, 13, 27, 6, 34, 17, 25, 2, 21, 4, 19, 15, 32};


    int[] luckys;

    DataBaseHandler db;
    private FragmentActivity activity ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_game, container, false);



        activity = (FragmentActivity) getActivity();


        tts = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.US);
                tts.speak("Game", TextToSpeech.QUEUE_ADD, null);
            }
        });

        db = DataBaseHandler.getInstance(activity);



        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        String inStr = (String) msg.obj;

                        sendToMonitor("rec : ",BTRECE);
                        terminalSb.append(( inStr + "\n"));

                        if(inStr.contains("clicking")|| inStr.contains("showing") || inStr.contains("settings") || inStr.contains("fall number") || inStr.contains("rotor captured") ){
                            tts.speak(inStr, TextToSpeech.QUEUE_ADD, null);
                            if(inStr.contains("fall number")) tts.speak("0", TextToSpeech.QUEUE_ADD, null);
                        }

                        if(autScrollOn) scrollToBot();

                        if(inStr.contains(MASTER_IND)){
                            rotorData = inStr.substring(1,inStr.length());
                        }
                        if(inStr.contains(SLAVE_IND)){
                            ballData = inStr.substring(1,inStr.length());
                            ana = new SampleAnalysis(ballData, rotorData);

                            if(gameMode == prdcmode){


                                Dana = new DataAnalysis(activity,tableName,cutTiming);
                                luckys = Dana.getLuckyNumbers();

                                if(ana.isValid()){
                                    int[] BallNumbers = ana.getNumbersArrayInt();
                                    int number = BallNumbers[BallNumbers.length - 1];
                                    /*int num1 = NUMBERS[(luckys[0] + number) % 37];
                                    int num2 = NUMBERS[(luckys[1] + number) % 37];*/



                                    int num1 = (luckys[0] + number) % 37;
                                    int num2 = (luckys[1] + number) % 37;


                                    String pred1 = Integer.toString(num1);
                                    String pred2 = Integer.toString(num2);
                                    terminalSb.append("predicted positions : " + pred1 + " " + pred2 + "\n");


                                    num1 = NUMBERS[num1];
                                    num2 = NUMBERS[num2];

                                    pred1 = Integer.toString(num1);
                                    pred2 = Integer.toString(num2);
                                    terminalSb.append("predicted numbers " + pred1 + " " + pred2 + "\n");
                                    tts.speak(pred1 + " " + pred2, TextToSpeech.QUEUE_FLUSH, null);

                                    //tts.speak("uaaa", TextToSpeech.QUEUE_ADD, null);
                                }
                            }
                            if(gameMode == datamode && handsfree){

                                if(ana.isValid()){
                                    sendEditText.performClick();
                                    inFallsetting = true;
                                    sendEditText.setText("setFall " + Integer.toString(handsFreeFallPos));
                                }
                                else {
                                    tts.speak("invalid sample", TextToSpeech.QUEUE_ADD, null);
                                    inFallsetting = false;
                                    BTserv.write(GO_SET+"\n");
                                }

                            }
                        }

                        if(gameMode == datamode && handsfree && inFallsetting){
                            if(inStr.contains(PLUS_FALL)){
                                penetrationLevel = 0;
                                handsFreeFallPos++;
                                if(handsFreeFallPos < 0 ) handsFreeFallPos = 36;
                                if(handsFreeFallPos > 36) handsFreeFallPos = 0;
                                tts.speak(Integer.toString(handsFreeFallPos), TextToSpeech.QUEUE_FLUSH, null);
                                sendEditText.setText("setFall " + Integer.toString(handsFreeFallPos));
                            }
                            if(inStr.contains(MINUS_FALL)){
                                penetrationLevel = 0;
                                handsFreeFallPos--;
                                if(handsFreeFallPos < 0 ) handsFreeFallPos = 36;
                                if(handsFreeFallPos > 36) handsFreeFallPos = 0;
                                tts.speak(Integer.toString(handsFreeFallPos), TextToSpeech.QUEUE_FLUSH, null);
                                sendEditText.setText("setFall " + Integer.toString(handsFreeFallPos));
                            }
                            if(inStr.contains(PENETRATE) && penetrationLevel == 1){
                                sendEditText.setText("saveSample");
                                penetrationLevel = 0;
                                sendButton.performClick();
                            }
                            if(inStr.contains(PENETRATE) && penetrationLevel == 0){
                                penetrationLevel = 1;
                                sendButton.performClick();
                            }
                        }

                        if(inStr.contains(GO_SET)){
                            if(handsfree){
                                penetrationLevel = 0;
                                handsFreeFallPos = 0;
                                //sendButton.performClick();
                                sendEditText.setText("");
                                fallPos = "";
                                sendToMonitor("setFallVoid",COMMAND);
                                terminalSb.append("\n");
                                tts.speak("fall selection canceled", TextToSpeech.QUEUE_ADD, null);
                            }
                        }





                        BTterminalText.setText(terminalSb);

                        break;
                }
            }
        };


        FragmentActivity activity = getActivity();
        BTserv = ((SecondActivity)getActivity()).BTserv;
        BTserv.setHandler(h);
        BTserv.checkBTState();


        return rootView;
    }

    @Override

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        BTterminalText = (TextView) view.findViewById(R.id.BTterminalText);
        BTterminalScroll = (ScrollView) view.findViewById(R.id.Tscroll);
        sendEditText = (EditText) view.findViewById(R.id.sendEditText);
        sendButton = (Button) view.findViewById(R.id.sendButton);
        TclearButton = (Button) view.findViewById(R.id.TclearButton);
        TautoButton = (Button) view.findViewById(R.id.TautoButton);
        TmodeButton = (Button) view.findViewById(R.id.TmodeButton);
        ThandsButton = (Button) view.findViewById(R.id.ThandsButton);
        CfallButton = (Button) view.findViewById(R.id.CfallButton);



        restoreState();


        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    String message = sendEditText.getText().toString();
                    message = message.replaceAll("\\s+", BuildConfig.FLAVOR);
                    sendToMonitor(message,COMMAND);
                    terminalSb.append("\n");

                    if(message.contains(SENDBT_COMMAND)){
                        message = message.replace(SENDBT_COMMAND,"");
                        BTserv.write(message);
                    }

                    if(message.contains(SETFALL_COMMAND)){
                        message = message.replace(SETFALL_COMMAND,"");

                        handsFreeFallPos = Integer.parseInt(message);

                        tts.speak(Integer.toString(handsFreeFallPos) + " selected", TextToSpeech.QUEUE_ADD, null);


                        fallPos = Integer.toString(new DataPreProcessing().number2position(handsFreeFallPos));

                        if(penetrationLevel != 1){
                            BTserv.write(GO_SET);
                            handsFreeFallPos = 0;
                            inFallsetting = false;
                        }



                    }

                    if(message.contains(SHOWSAMPLE_COMMAND)){
                        message = message.replace(SHOWSAMPLE_COMMAND,"");

                        SampleAnalysis ana = new SampleAnalysis(ballData, rotorData);

                        sendToMonitor("|||||||||| actual sample |||||||||" + "\n",COMMENT);
                        terminalSb.append("ball data : " + (ballData + "\n"));
                        terminalSb.append("rot data : " + (rotorData + "\n"));
                        terminalSb.append("fall data : " + (fallPos + "\n"));

                        if(ana.isValid()) {
                            sendToMonitor("|||||||||| processed sample |||||||||" + "\n",COMMENT);
                            terminalSb.append("ball string : " + (ana.getBallTimeString() + "\n"));
                            terminalSb.append("rot string : " + (ana.getRotorTimeString() + "\n"));
                            terminalSb.append("rotor numbers : " + (ana.getNumbers() + "\n"));
                            sendToMonitor("|||||||||| diff strings |||||||||" + "\n",COMMENT);
                            terminalSb.append("ball diff : " + (ana.getBallDiff() + "\n"));
                            terminalSb.append("rot diff : " + (ana.getRotorDiff() + "\n"));
                        }
                        else terminalSb.append("(INVALID SAMPLE) " + "\n");

                    }

                    if(message.contains(SAVESAMPLE_COMMAND)){
                        message = message.replace(SHOWSAMPLE_COMMAND,"");

                        SampleAnalysis ana = new SampleAnalysis(ballData, rotorData);

                        if(ana.isValid()){
                            sendToMonitor("|||||||||| saved sample |||||||||" + "\n",COMMENT);
                            terminalSb.append("ball data : " + (ballData + "\n"));
                            terminalSb.append("rot data : " + (rotorData + "\n"));
                            terminalSb.append("fall data : " + (fallPos + "\n"));
                            sendToMonitor("|||||||||| processed sample |||||||||" + "\n",COMMENT);
                            terminalSb.append("ball string : " + (ana.getBallTimeString() + "\n"));
                            terminalSb.append("rot string : " + (ana.getRotorTimeString() + "\n"));
                            terminalSb.append("rotor numbers : " + (ana.getNumbers() + "\n"));
                            sendToMonitor("|||||||||| diff strings |||||||||" + "\n",COMMENT);
                            terminalSb.append("ball diff : " + (ana.getBallDiff() + "\n"));
                            terminalSb.append("rot diff : " + (ana.getRotorDiff() + "\n"));

                            penetrationLevel = 0;
                            handsFreeFallPos = 0;
                            tts.speak("sample saved", TextToSpeech.QUEUE_ADD, null);
                            Sample smp = new Sample(ballData, rotorData,fallPos);
                            db.addSample(tableName,smp);
                            BTserv.write(GO_SET);
                        }
                        else {
                            Toast.makeText(activity, "invalid sample", Toast.LENGTH_SHORT).show();
                            tts.speak("invalid sample", TextToSpeech.QUEUE_ADD, null);
                        }
                    }


                    BTterminalText.setText(terminalSb);
                    sendEditText.setText("");
                    if(autScrollOn) scrollToBot();
                }
            }
        });

        TclearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    terminalSb = new SpannableStringBuilder();
                    BTterminalText.setText(terminalSb);
                }
            }
        });

        TautoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {

                    if (autScrollOn) {
                        TautoButton.setTextColor(getResources().getColor(darker_gray));

                    } else {
                        TautoButton.setTextColor(getResources().getColor(holo_green_dark));
                    }

                    autScrollOn = !autScrollOn;

                }
            }
        });

        TmodeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    if (gameMode == datamode) {
                        TmodeButton.setText("pred mode");
                        
                        String send = PRED_MODE + "," + Integer.toString(cutTiming);
                        //if(handsfree) send += HANDS_TOGGLE;
                        sendToMonitor("send : ",BTSEND);
                        terminalSb.append(send + "\n");


                        BTserv.write(send);
                        gameMode = prdcmode;
                    }
                    else if(gameMode == prdcmode) {
                        TmodeButton.setText("data mode");

                        sendToMonitor("send : ",BTSEND);
                        terminalSb.append(DATA_MODE + "\n");

                        BTserv.write(DATA_MODE);
                        gameMode = datamode;
                    }

                }
            }
        });



        ThandsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {

                    if (handsfree) {
                        ThandsButton.setTextColor(getResources().getColor(darker_gray));
                        BTserv.write("neutral,neutral,"+HANDS_OFF);

                    } else {
                        ThandsButton.setTextColor(getResources().getColor(holo_green_dark));
                        BTserv.write("neutral,neutral,"+HANDS_ON);
                    }

                    handsfree = !handsfree;

                }
            }
        });



        CfallButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {

                    if (fallCommandState) {
                        CfallButton.setTextColor(getResources().getColor(darker_gray));
                        commandString = commandString.replace(SETFALL_COMMAND + " ", "");
                    } else {
                        CfallButton.setTextColor(getResources().getColor(holo_green_dark));
                        commandString += (SETFALL_COMMAND + " ");
                        sendEditText.requestFocus();
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }

                    fallCommandState = !fallCommandState;
                    sendEditText.setText(commandString);
                    sendEditText.setSelection(sendEditText.getText().length());

                }
            }
        });



    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BTserv != null) {
            BTserv.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void scrollToBot(){
        BTterminalScroll.post(new Runnable() {
            @Override
            public void run() {
                BTterminalScroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    void restoreState(){

        BTterminalText.setText(terminalSb);

        if (autScrollOn) {
            TautoButton.setTextColor(getResources().getColor(holo_green_dark));

        } else {
            TautoButton.setTextColor(getResources().getColor(darker_gray));
        }

        if (gameMode == datamode) {
            TmodeButton.setText("data mode");
        }
        else if(gameMode == prdcmode) {
            TmodeButton.setText("pred mode");
        }

        if (fallCommandState) {
            CfallButton.setTextColor(getResources().getColor(holo_green_dark));

        } else {
            CfallButton.setTextColor(getResources().getColor(darker_gray));
        }

        if (handsfree) {
            ThandsButton.setTextColor(getResources().getColor(holo_green_dark));

        } else {
            ThandsButton.setTextColor(getResources().getColor(darker_gray));
        }

    }

    void sendToMonitor(String msg, byte type){

        SpannableStringBuilder command = new SpannableStringBuilder(msg);


        if(type == COMMAND){
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(158, 0, 0));
            StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
            command.setSpan(fcs, 0, msg.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            command.setSpan(bss, 0, msg.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        if(type == BTSEND){
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(0, 158, 0));
            StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
            command.setSpan(fcs, 0, msg.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            command.setSpan(bss, 0, msg.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        if(type == BTRECE){
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(0, 0, 158));
            StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
            command.setSpan(fcs, 0, msg.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            command.setSpan(bss, 0, msg.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        if(type == COMMENT){
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(160, 160, 160));
            StyleSpan bss = new StyleSpan(Typeface.ITALIC);
            command.setSpan(bss, 0, msg.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            command.setSpan(fcs, 0, msg.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        terminalSb.append(command);
    }


}