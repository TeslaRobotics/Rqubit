package com.example.santiago.btqubit;

import android.util.Log;



public class DataPreProcessing {
    private final int[] NUMBERS = new int[]{0, 26, 3, 35, 12, 28, 7, 29, 18, 22, 9, 31, 14, 20, 1, 33, 16, 24, 5, 10, 23, 8, 30, 11, 36, 13, 27, 6, 34, 17, 25, 2, 21, 4, 19, 15, 32};

    public DataPreProcessing() {

    }


    public String intArray2String(int[] A) {
        String Astring = "[";
        for (int i = 0; i < A.length; i++) {
            Astring = Astring + Integer.toString(A[i]);
            if (i == A.length - 1) {
                Astring = Astring + "]";
            } else {
                Astring = Astring + ",";
            }
        }
        return Astring;
    }

    public int number2position(int number) {
        int i = 0;
        while (number != this.NUMBERS[i]) {
            i++;
        }
        return i;
    }

    public void printArray(int[] A) {
        for (int num : A) {
            Log.d("ContentValues", Integer.toString(num));
        }
    }

    public int[] string2array(String TimeString) {
        String[] TimeArray = TimeString.replaceAll("\\s+", BuildConfig.FLAVOR).replaceAll("\\[", BuildConfig.FLAVOR).replaceAll("\\]", BuildConfig.FLAVOR).split(",");
        int[] TimeInt = new int[TimeArray.length];
        for (int i = 0; i < TimeArray.length; i++) {
            TimeInt[i] = Integer.parseInt(TimeArray[i]);
        }
        return TimeInt;
    }


}

