package com.hunterdavis.autorobointercom.util;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by hunter on 3/3/14.
 */
public class AutoRoboApplication extends Application{

        // our SP reference label
        protected static final String SHARED_PREFS_REFERENCE_LABEL = "AUTOROBOMAIN";

        // our default name
        protected static final String DEFAULT_CLIENT_NAME = "Default_Client_Name";

        // our current battery level
        public static int currentBatteryLevel = 0;

        private static AutoRoboApplication instance;

        public static AutoRoboApplication getInstance() {
            return instance;
        }

        public static Context getContext(){
            return instance;
            // or return instance.getApplicationContext();
        }

        @Override
        public void onCreate() {
            instance = this;
            super.onCreate();
        }

        // store our name to shared prefs
        public static void storeName(String name) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(SHARED_PREFS_REFERENCE_LABEL, name).commit();
        }

        // get our name from shared prefs
        public static String getName() {
            return PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SHARED_PREFS_REFERENCE_LABEL, DEFAULT_CLIENT_NAME);
        }
}
