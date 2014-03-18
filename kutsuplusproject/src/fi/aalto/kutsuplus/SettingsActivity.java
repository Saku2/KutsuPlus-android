package fi.aalto.kutsuplus;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Deprecation fix PreferenceFragment needs API level 11. That is why this is used now
        addPreferencesFromResource(R.xml.preferences);
    }
    
}