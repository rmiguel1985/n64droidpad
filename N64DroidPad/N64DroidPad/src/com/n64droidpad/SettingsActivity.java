package com.n64droidpad;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;
import android.view.WindowManager;

public class SettingsActivity extends PreferenceActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Carrega automaticament les preferencies del xml
        addPreferencesFromResource(R.layout.settings_activity);
    }
}
