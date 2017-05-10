package com.example.santiago.btqubit;

import android.util.Log;



public class SampleAnalysis {


    public final int BUFFER = 20;
    private int[] BallTimeInt;
    private DataPreProcessing Dpp = new DataPreProcessing();;
    private int[] RotorTimeInt;
    public final String TAG = "DEV";
    private String _BallTimeString = "[0, 200, 250, 300, 400]";
    private String _RotorTimeString = "[50, 150, 270, 390]";
    private String ballDiffString;
    private final int defaultTimingSlope = 1;
    private String rotorDiffString;
    private int trustLevel;
    public boolean validSample = true;


    public SampleAnalysis() {
        updateParams();
    }

    public SampleAnalysis(String _BallTimeString, String _RotorTimeString) {
        this._BallTimeString = _BallTimeString.replaceAll("\\s+", BuildConfig.FLAVOR);
        this._RotorTimeString = _RotorTimeString.replaceAll("\\s+", BuildConfig.FLAVOR);
        updateParams();
    }

    private void updateParams() {


        if (_BallTimeString.length() < 3 || _RotorTimeString.length() < 3) {
            validSample = false;
        } else {
            int[] BallTimeIntTmp = Dpp.string2array(_BallTimeString);
            int[] RotorTimeIntTmp = Dpp.string2array(_RotorTimeString);
            if (BallTimeIntTmp.length < 2 || RotorTimeIntTmp.length < 2) {
                validSample = false;
                return;
            }
            int levels;
            int Sn;
            int An;
            int[] SnArray;
            int nBall = BallTimeIntTmp.length;
            int nRoto = RotorTimeIntTmp.length;
            int timingSlope = defaultTimingSlope;
            trustLevel = nRoto - 1;
            if (trustLevel > 2) {
                trustLevel = 2;
            }
            int[] rotorArray = (int[]) RotorTimeIntTmp.clone();
            if (RotorTimeIntTmp[nRoto - 1] < BallTimeIntTmp[nBall - 1]) {
                if (trustLevel >= 2) {
                    timingSlope = (RotorTimeIntTmp[nRoto - 1] + RotorTimeIntTmp[nRoto - 3]) - (RotorTimeIntTmp[nRoto - 2] * 2);
                }
                levels = 0;
                Sn = RotorTimeIntTmp[nRoto - 1];
                An = RotorTimeIntTmp[nRoto - 1] - RotorTimeIntTmp[nRoto - 2];
                SnArray = new int[20];
                while (Sn < BallTimeIntTmp[nBall - 1]) {
                    levels++;
                    An += levels * timingSlope;
                    Sn += An;
                    if (levels - 1 == 20) {
                        validSample = false;
                        return;
                    }
                    SnArray[levels - 1] = Sn;
                }
                rotorArray = new int[(nRoto + levels)];
                System.arraycopy(RotorTimeIntTmp, 0, rotorArray, 0, nRoto);
                System.arraycopy(SnArray, 0, rotorArray, nRoto, levels);
            }
            int[] rotorArray2 = (int[]) rotorArray.clone();
            int nRoto2 = rotorArray2.length;
            if (RotorTimeIntTmp[0] > BallTimeIntTmp[0]) {
                if (trustLevel >= 2) {
                    timingSlope = (RotorTimeIntTmp[2] + RotorTimeIntTmp[0]) - (RotorTimeIntTmp[1] * 2);
                }
                levels = 0;
                Sn = RotorTimeIntTmp[0];
                An = RotorTimeIntTmp[0] - RotorTimeIntTmp[1];
                SnArray = new int[20];
                while (Sn > BallTimeIntTmp[0]) {
                    levels++;
                    An += levels * timingSlope;
                    Sn += An;
                    if (20 - levels == -1) {
                        validSample = false;
                        return;
                    }
                    SnArray[20 - levels] = Sn;
                }
                rotorArray2 = new int[(nRoto2 + levels)];
                System.arraycopy(rotorArray, 0, rotorArray2, levels, nRoto2);
                System.arraycopy(SnArray, 20 - levels, rotorArray2, 0, levels);
            }
            BallTimeInt = BallTimeIntTmp;
            RotorTimeInt = rotorArray2;
            _RotorTimeString = Dpp.intArray2String(RotorTimeInt);
            _BallTimeString = Dpp.intArray2String(BallTimeInt);
            Log.d("DEV", _RotorTimeString);
        }
    }

    public int findRotorNumber(int intBallTime) {
        int[] intRotor = this.RotorTimeInt;
        int j = 0;
        while (j < intRotor.length && intBallTime > intRotor[j]) {
            j++;
        }
        int place = j - 1;
        return 36 - (((int) ((37.0d * ((double) ((float) (intBallTime - intRotor[place])))) / ((double) ((float) (intRotor[place + 1] - intRotor[place]))))) % 37);
    }

    public int[] findRotorNumbers(int[] intBall, int[] intRotor) {
        int[] places = new int[intBall.length];
        int i = 0;
        while (i < intBall.length) {
            int j = 0;
            while (j < intRotor.length && intBall[i] > intRotor[j]) {
                j++;
            }
            places[i] = j - 1;
            i++;
        }
        int[] rotorNumbers = new int[intBall.length];
        for (i = 0; i < intBall.length; i++) {
            rotorNumbers[i] = 36 - (((int) ((37.0d * ((double) ((float) (intBall[i] - intRotor[places[i]])))) / ((double) ((float) (intRotor[places[i] + 1] - intRotor[places[i]]))))) % 37);
        }
        return rotorNumbers;
    }

    public String getBallDiff() {
        String diffString = BuildConfig.FLAVOR;
        for (int i = 1; i < this.BallTimeInt.length; i++) {
            diffString = (diffString + Integer.toString(this.BallTimeInt[i] - this.BallTimeInt[i - 1])) + " ";
        }
        return diffString;
    }

    public String getNumbers() {
        return this.Dpp.intArray2String(findRotorNumbers(this.BallTimeInt, this.RotorTimeInt));
    }

    public int[] getNumbersArrayInt() {
            return findRotorNumbers(this.BallTimeInt, this.RotorTimeInt);
    }

    public String getRotorDiff() {
        String diffString = BuildConfig.FLAVOR;
        for (int i = 1; i < this.RotorTimeInt.length; i++) {
            diffString = (diffString + Integer.toString(this.RotorTimeInt[i] - this.RotorTimeInt[i - 1])) + " ";
        }
        return diffString;
    }

    public int[] getRotorIntArray() {
            return this.RotorTimeInt;
    }

    public String getRotorTimeString() {
            return this._RotorTimeString;
    }
    public String getBallTimeString() {
        return this._BallTimeString;
    }

    public boolean isValid() {
            return this.validSample;
    }


}
