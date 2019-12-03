package com.asc.apps.citruscalc;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseArray;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA. User: ejn Date: 8/29/13 Time: 8:26 PM To change this template use File | Settings | File
 * Templates.
 */
class MainActivitySO {
    private final MainActivity mainActivity;
    private float flowRate;
    private int diameter;
    private String type;

    private final SparseArray<Double[]> gpdLookup = new SparseArray<Double[]>();
    //new HashMap<Integer, Double[]>();

    public MainActivitySO(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        flowRate = getPreferences().getFloat("flowRate", -1.0f);


        initTable();
    }

    private SharedPreferences getPreferences() {
        return mainActivity.getPreferences(Context.MODE_PRIVATE);
    }

    private void initTable() {
        // extracted from Table 1 of the article "Irrigating Citrus Trees" by Glenn C. Wright, Associate Specialist,
        // The University of Arizona College of Agriculture.  This uses an average pan evaporation value instead of
        // the more accurate calculated one.
        gpdLookup.put(2, new Double[]{0.1, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.6, 0.4, 0.3, 0.1, 0.1});
        gpdLookup.put(4, new Double[]{0.3, 0.4, 0.9, 1.3, 1.6, 2.1, 2.4, 2.2, 1.8, 1.0, 0.4, 0.3});
        gpdLookup.put(6, new Double[]{0.7, 1.0, 2.1, 3.0, 3.6, 4.7, 5.4, 5.1, 3.9, 2.3, 1.0, 0.7});
        gpdLookup.put(8, new Double[]{1.2, 1.8, 3.7, 5.3, 6.5, 8.4, 9.6, 9.0, 7.0, 4.1, 1.8, 1.2});
        gpdLookup.put(10, new Double[]{1.9, 2.7, 5.7, 8.2, 10.1, 13.1, 15.1, 14.0, 11.0, 6.4, 2.7, 1.9});
        gpdLookup.put(12, new Double[]{2.7, 3.9, 8.3, 11.8, 14.6, 18.9, 21.7, 20.2, 15.8, 9.2, 3.9, 2.7});
        gpdLookup.put(14, new Double[]{3.7, 5.4, 11.3, 16.1, 19.9, 25.7, 29.5, 27.5, 21.5, 12.5, 5.4, 3.7});
        gpdLookup.put(16, new Double[]{4.8, 7.0, 14.7, 21.0, 25.9, 33.5, 38.6, 35.9, 28.0, 16.4, 7.0, 4.8});
        gpdLookup.put(18, new Double[]{6.1, 8.9, 18.6, 26.6, 32.8, 42.4, 48.8, 45.5, 35.5, 20.7, 8.9, 6.1});
        gpdLookup.put(20, new Double[]{7.5, 11.0, 23.0, 32.9, 40.5, 52.4, 60.2, 56.1, 43.8, 25.6, 11.0, 7.5});
        gpdLookup.put(22, new Double[]{9.1, 13.3, 27.8, 39.8, 49.0, 63.4, 72.9, 67.9, 53.0, 31.0, 13.3, 9.1});
        gpdLookup.put(24, new Double[]{10.8, 15.8, 33.1, 47.3, 58.4, 75.4, 86.7, 80.8, 63.1, 36.9, 15.8, 10.8});
        gpdLookup.put(26, new Double[]{12.7, 18.5, 38.9, 55.5, 68.5, 88.5, 101.8, 94.9, 47.0, 43.3, 18.5, 12.7});
        gpdLookup.put(28, new Double[]{14.8, 21.5, 45.1, 64.4, 79.4, 102.6, 118.1, 110.0, 85.9, 50.2, 21.5, 14.8});
        gpdLookup.put(30, new Double[]{16.9, 24.6, 51.7, 73.9, 91.2, 117.8, 135.5, 126.3, 98.6, 57.6, 24.6, 16.9});
    }

    public void diameterSelected(int diameter) {
        this.diameter = diameter;
    }

    public void typeSelected(String type) {
        this.type = type;
    }

    public void calculate() {
        double typeMultiplier = 1.0;

        if (type.equals("Lemon") || type.equals("Grapefruit")) {
            typeMultiplier = 1.20; // 20% more than oranges
        } else if (type.equals("Mandarin")) {
            typeMultiplier = .90;  // 10% less than oranges
        }

        Calendar calendar = Calendar.getInstance();
        double gpd = typeMultiplier * gpdLookup.get(diameter)[calendar.get(Calendar.MONTH)];

        mainActivity.setGpd(gpd);
        mainActivity.setTimerValue(gpd / flowRate);
    }

    public void setFlowRate(float flowRate) {
        this.flowRate = flowRate;
        // don't forget to persist it.
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putFloat("flowRate", flowRate);
        editor.commit();
    }

    public float getFlowRate() {
        return flowRate;
    }
}
