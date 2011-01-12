/**
 *  Ubuntu Countdown Widget
 *  Copyright (C) 2011 Roberto Leinardi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package com.leinardi.ubuntucountdownwidget.ui;

import com.leinardi.ubuntucountdownwidget.customviews.DatePreference;
import com.leinardi.ubuntucountdownwidget.misc.Log;
import com.leinardi.ubuntucountdownwidget.R;
import com.leinardi.ubuntucountdownwidget.misc.Constants;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.KeyEvent;

import java.text.DateFormat;
import java.util.Locale;

public class ConfigActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private final static String TAG = "ConfigActivity";
    private static String CONFIGURE_ACTION="android.appwidget.action.APPWIDGET_CONFIGURE";
    private final String REPORT_A_BUG_URL = "http://code.google.com/p/ubuntu-countdown-widget/issues/list";

    SharedPreferences mPrefs;
    Preference customDatePicker;
    CheckBoxPreference customDateCheckbox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String releaseDate = DateFormat.getDateInstance(DateFormat.LONG,
                Locale.getDefault()).format(Constants.ubuntuReleaseCal.getTime());
        findPreference(getString(R.string.pref_default_release_date_key)).setSummary(releaseDate);

        customDateCheckbox = (CheckBoxPreference) findPreference(getString(R.string.pref_custom_date_checkbox_key));
        customDatePicker = findPreference(getString(R.string.pref_custom_date_key));

        String version;
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
            version = getString(R.string.pref_info_version_error);
        }

        findPreference(getString(R.string.pref_info_version_key)).setSummary(version);
        Preference reportABug = findPreference(getString(R.string.pref_report_a_bug_key));
        reportABug.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(REPORT_A_BUG_URL));
                startActivity(browserIntent);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!customDateCheckbox.isChecked()){
            mPrefs.edit().putLong(getString(R.string.pref_custom_date_key),
                    Constants.ubuntuReleaseCal.getTimeInMillis()).commit();
        }
        // Setup the initial values
        long dateInMillis = mPrefs.getLong(getString(R.string.pref_custom_date_key),
                DatePreference.DEFAULT_VALUE);
        customDatePicker.setSummary(DateFormat.getDateInstance(DateFormat.LONG,
                Locale.getDefault()).format(dateInMillis));

        // Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Let's do something a preference value changes
        if (key.equals(getString(R.string.pref_custom_date_key))) {
            long dateInMillis = sharedPreferences.getLong(getString(R.string.pref_custom_date_key),
                    DatePreference.DEFAULT_VALUE);
            customDatePicker.setSummary(DateFormat.getDateInstance(DateFormat.LONG,
                    Locale.getDefault()).format(dateInMillis));
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions of
            // the platform where it doesn't exist.
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // This will be called either automatically for you on 2.0
        // or later, or by the code above on earlier versions of the
        // platform.
        
        resultIntent();
        finish();
        return;
    }

    private void resultIntent() {
        sendBroadcast(new Intent(Constants.FORCE_WIDGET_UPDATE));
        if (CONFIGURE_ACTION.equals(getIntent().getAction())) {
            Intent intent=getIntent();
            Bundle extras=intent.getExtras();
            if (extras!=null) {
                int appWidgetId=extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                Intent result=new Intent();

                result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, result);
            }
        }
    }

}
