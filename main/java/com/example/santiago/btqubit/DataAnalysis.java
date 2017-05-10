package com.example.santiago.btqubit;

import android.content.Context;

import java.util.ArrayList;




public class DataAnalysis {

    private static final String COLUMN_BALLTIMINGS = "_balltimings";
    private static final String COLUMN_FALLPOSITION = "_fallposition";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_ROTORTIMINGS = "_rotortimings";
    public static final long serialVersionUID = 0;
    private DataPreProcessing Dpp = new DataPreProcessing();
    private SampleAnalysis ana;
    private ArrayList<String> arrBallSamplesList;
    private ArrayList<String> arrFallSamplesList;
    private ArrayList<String> arrRotorSamplesList;
    private ArrayList<int[]> arrSamplesList;
    public int[] hist = new int[37];
    private String inputString;
    private int threshold;

    public DataAnalysis(Context context, String tablename, int threshold) {

        DataBaseHandler Db = DataBaseHandler.getInstance(context);
        arrRotorSamplesList = new ArrayList(Db.databaseToArrayList(tablename, COLUMN_ROTORTIMINGS));
        arrBallSamplesList = new ArrayList(Db.databaseToArrayList(tablename, COLUMN_BALLTIMINGS));
        arrFallSamplesList = new ArrayList(Db.databaseToArrayList(tablename, COLUMN_FALLPOSITION));
        this.threshold = threshold;
        upSamples();
        upHist37(threshold);
    }

    public ArrayList<String> getArrBallSamplesList(){return arrBallSamplesList;}
    public ArrayList<String> getArrRotorSamplesList(){return arrRotorSamplesList;}
    public ArrayList<String> getArrFallSamplesList(){return arrFallSamplesList;}

    private int getSamplebyTime(int targetTime, int index) {
        String sampleBall = ((String) arrBallSamplesList.get(index)).replaceAll("\\s+", BuildConfig.FLAVOR);
        String sampleRotor = ((String) arrRotorSamplesList.get(index)).replaceAll("\\s+", BuildConfig.FLAVOR);
        int[] rotorTimesArrayInt = Dpp.string2array(sampleBall);
        SampleAnalysis ana = new SampleAnalysis(sampleBall, sampleRotor);
        if (targetTime >= rotorTimesArrayInt[rotorTimesArrayInt.length - 1]) {
            return -1;
        }
        int sample = Integer.parseInt(((String) arrFallSamplesList.get(index)).replaceAll("\\s+", BuildConfig.FLAVOR)) - ana.findRotorNumber(targetTime);
        if (sample >= 0) {
            return sample;
        }
        return sample + 37;
    }

    private void upSamples() {
        arrSamplesList = new ArrayList();
        for (int i = 0; i < arrBallSamplesList.size(); i++) {
            String sampleBall = ((String) arrBallSamplesList.get(i)).replaceAll("\\s+", BuildConfig.FLAVOR);
            String sampleRotor = ((String) arrRotorSamplesList.get(i)).replaceAll("\\s+", BuildConfig.FLAVOR);
            int sampleFallpos = Integer.parseInt(((String) arrFallSamplesList.get(i)).replaceAll("\\s+", BuildConfig.FLAVOR));
            ana = new SampleAnalysis(sampleBall, sampleRotor);
            int[] numbersInt = Dpp.string2array(ana.getNumbers().replaceAll("\\s+", BuildConfig.FLAVOR));
            int[] samplesInt = new int[numbersInt.length];
            for (int j = 0; j < numbersInt.length; j++) {
                int sample = sampleFallpos - numbersInt[j];
                if (sample >= 0) {
                    samplesInt[j] = sample;
                } else {
                    samplesInt[j] = sample + 37;
                }
            }
            arrSamplesList.add(samplesInt);
        }
    }

    public int[] getHist37() {
            return hist;
    }

    public float getHist37Mean() {
        int sum = 0;
        for (int i = 0; i < 37; i++) {
            sum += hist[i];
        }
        return ((float)sum) / 37;
    }

    public String getInputString() {
            return inputString;
    }

    public int[] getLuckyNumbers() {
        int i;
        int[] lucky = new int[14];
        for (i = 0; i < 14; i++) {
            lucky[i] = -1;
        }
        int[] histMod = new int[37];
        int mean = (int) getHist37Mean();
        for (i = 0; i < 37; i++) {
            histMod[i] = hist[i] - mean;
            if (histMod[i] < 0) {
                histMod[i] = 0;
            }
        }
        ArrayList<int[]> anaList = new ArrayList();
        int[] areaAna = new int[4];
        int area = 0;
        int Acentroid = 0;
        boolean flagArea = false;
        i = 0;
        while (i < 37) {
            if (histMod[i] > 0 && !flagArea) {
                areaAna[0] = i;
                flagArea = true;
            }
            if (histMod[i] > 0 && flagArea) {
                area += histMod[i];
                Acentroid += histMod[i] * i;
            }
            if (flagArea && (i + 1 == 37 || histMod[i + 1] == 0)) {
                areaAna[1] = i;
                areaAna[2] = area;
                areaAna[3] = Acentroid / area;
                anaList.add(areaAna);
                area = 0;
                Acentroid = 0;
                areaAna = new int[4];
                flagArea = false;
            }
            i++;
        }
        if (histMod[36] > 0 && histMod[0] > 0) {
            int[] A1 = (int[]) anaList.get(0);
            int[] A2 = (int[]) anaList.get(anaList.size() - 1);
            int centroidT = ((((A1[3] + 37) * A1[2]) + (A2[3] * A2[2])) / (A1[2] + A2[2])) % 37;
            int areaT = A1[2] + A2[2];
            int iT0 = A2[0];
            int iT1 = A1[1];
            anaList.set(0, new int[]{iT0, iT1, areaT, centroidT});
            anaList.remove(anaList.size() - 1);
        }
        int max1 = -1;
        int imax1 = 0;
        for (i = 0; i < anaList.size(); i++) {
            if (max1 < ((int[]) anaList.get(i))[2]) {
                max1 = ((int[]) anaList.get(i))[2];
                imax1 = i;
            }
        }
        int max2 = -1;
        int imax2 = 0;
        i = 0;
        while (i < anaList.size()) {
            if (max2 < ((int[]) anaList.get(i))[2] && i != imax1) {
                max2 = ((int[]) anaList.get(i))[2];
                imax2 = i;
            }
            i++;
        }
        int centroid1 = -1;
        int centroid2 = -1;

        if(!anaList.isEmpty()){
            centroid1 = ((int[]) anaList.get(imax1))[3];
            centroid2 = ((int[]) anaList.get(imax2))[3];
        }

        return new int[]{centroid1, centroid2};
    }

    public int getNumberDataPoints() {
            return arrBallSamplesList.size();
    }

    public void setThreshold(int threshold) {
        upHist37(threshold);
    }

    public void upHist37(int threshold) {

        hist = new int[37];
        inputString = BuildConfig.FLAVOR;
        for (int i = 0; i < arrFallSamplesList.size(); i++) {
            int[] ballTimesArrayInt = Dpp.string2array((String) arrBallSamplesList.get(i));
            int n = ballTimesArrayInt.length;
            if (ballTimesArrayInt[n - 1] - ballTimesArrayInt[n - 2] > threshold && ballTimesArrayInt[1] - ballTimesArrayInt[0] < threshold) {
                int j = 2;
                while (ballTimesArrayInt[j] - ballTimesArrayInt[j - 1] < threshold) {
                    j++;
                }
                int targetSample = ((int[]) arrSamplesList.get(i))[j];
                if (targetSample > -1 && targetSample < 37) {
                    int[] iArr = hist;
                    iArr[targetSample] = iArr[targetSample] + 1;
                }
                inputString += Integer.toString(targetSample) + ",";
            }
        }
    }


}
