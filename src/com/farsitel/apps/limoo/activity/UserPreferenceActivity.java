package com.farsitel.apps.limoo.activity;

import com.farsitel.apps.limoo.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class UserPreferenceActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings);
    }


}
