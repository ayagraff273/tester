package com.graff.tester;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_USE_GEN_AI = "use_gen_ai";
    private static final String ALARM_SET = "alarmSet";


    public static void setUseGenAI(Context context, boolean useGenAI) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_USE_GEN_AI, useGenAI).apply();
    }

    public static boolean getUseGenAI(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_USE_GEN_AI, false); // default
    }

    public static void setAlarmOn(Context context, boolean alarmOn) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(ALARM_SET, alarmOn).apply();
    }

    public static boolean getAlarmOn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(ALARM_SET, false); // default
    }
}


