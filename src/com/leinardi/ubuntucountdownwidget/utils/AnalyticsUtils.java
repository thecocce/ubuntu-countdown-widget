/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leinardi.ubuntucountdownwidget.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Helper singleton class for the Google Analytics tracking library.
 */
public class AnalyticsUtils {
    private static final String TAG = "AnalyticsUtils";

    GoogleAnalyticsTracker mTracker;
    private Context mApplicationContext;

    /**
     * The analytics tracking code for the app.
     */
    // TODO: insert your Analytics UA code here.
    private static final String UACODE = "INSERT_YOUR_ANALYTICS_UA_CODE_HERE";

    /**
     * Custom variables scope levels.
     */
    public static final int SCOPE_VISITOR_LEVEL = 1;
    public static final int SCOPE_SESSION_LEVEL = 2;
    public static final int SCOPE_PAGE_LEVEL = 3;

    /**
     * Custom variables slot (1-5).
     */
    private static final int CUSTOM_VAR_INDEX_DEVICE = 1;
    private static final int CUSTOM_VAR_INDEX_APP_VERSION = 2;
    private static final int CUSTOM_VAR_INDEX_ANDROID_VERSION = 3;
    private static final int CUSTOM_VAR_INDEX_SDK_VERSION = 4;
    private static final int CUSTOM_VAR_INDEX_OPERATOR = 5;

    private static final String FIRST_RUN_KEY = "firstRun";
    private static final boolean ANALYTICS_ENABLED = true;

    private static AnalyticsUtils sInstance;

    /**
     * Returns the global {@link AnalyticsUtils} singleton object, creating one
     * if necessary.
     */
    public static AnalyticsUtils getInstance(Context context) {
        if (!ANALYTICS_ENABLED) {
            return sEmptyAnalyticsUtils;
        }

        if (sInstance == null) {
            if (context == null) {
                return sEmptyAnalyticsUtils;
            }
            sInstance = new AnalyticsUtils(context);
        }

        return sInstance;
    }

    private AnalyticsUtils(Context context) {
        if (context == null) {
            // This should only occur for the empty Analytics utils object.
            return;
        }

        mApplicationContext = context.getApplicationContext();
        mTracker = GoogleAnalyticsTracker.getInstance();

        // Unfortunately this needs to be synchronous.
        mTracker.start(UACODE, 300, mApplicationContext);

        Log.d(TAG, "Initializing Analytics");

        // Since visitor CV's should only be declared the first time an app
        // runs, check if
        // it's run before. Add as necessary.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                mApplicationContext);
        final boolean firstRun = prefs.getBoolean(FIRST_RUN_KEY, true);
        if (firstRun) {
            Log.d(TAG, "Analytics firstRun");

            initTrackerWithUserData(mApplicationContext);

            // Close out so we never run this block again, unless app is removed
            // & =
            // reinstalled.
            prefs.edit().putBoolean(FIRST_RUN_KEY, false).commit();
        }
    }

    private void initTrackerWithUserData(Context context) {
        // only 5 Custom Variables allowed!

        // 1 - Application version
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            String appVersion = packageInfo.versionName;
            Log.v(TAG, "app_version: '" + appVersion + "'.");
            mTracker.setCustomVar(CUSTOM_VAR_INDEX_APP_VERSION, "version", appVersion,
                    SCOPE_VISITOR_LEVEL);
            // getTracker(context).setProductVersion("MonTransit", appVersion);
        } catch (Exception e) {
        }

        // 2 - Android version
        String androidVersion = Build.VERSION.RELEASE;
        Log.v(TAG, "os_rel: '" + androidVersion + "'.");
        mTracker.setCustomVar(CUSTOM_VAR_INDEX_ANDROID_VERSION, "android", androidVersion,
                SCOPE_VISITOR_LEVEL);

        // 3 - Android SDK
        String sdk = Build.VERSION.SDK;
        Log.v(TAG, "sdk_version: '" + sdk + "'.");
        mTracker.setCustomVar(CUSTOM_VAR_INDEX_SDK_VERSION, "sdk", sdk, SCOPE_VISITOR_LEVEL);

        // 4 - Device
        String device = Build.MODEL;
        Log.v(TAG, "device: '" + device + "'.");
        mTracker.setCustomVar(CUSTOM_VAR_INDEX_DEVICE, "device", device, SCOPE_VISITOR_LEVEL);

        // 5 - Network Operator
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String operator = telephonyManager.getNetworkOperatorName();
            Log.v(TAG, "operator: '" + operator + "'.");
            mTracker.setCustomVar(CUSTOM_VAR_INDEX_OPERATOR, "operator", operator,
                    SCOPE_VISITOR_LEVEL);
        } catch (Exception e) {
        }
        // already provided by Analytics:
        // user language + country
        // service provider
    }

    public void trackEvent(final String category, final String action, final String label,
            final int value) {
        // We wrap the call in an AsyncTask since the Google Analytics library
        // writes to disk
        // on its calling thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mTracker.trackEvent(category, action, label, value);
                    Log.d(TAG, "iosched Analytics trackEvent: "
                            + category + " / " + action + " / " + label + " / " + value);
                } catch (Exception e) {
                    // We don't want to crash if there's an Analytics library
                    // exception.
                    Log.w(TAG, "iosched Analytics trackEvent error: "
                            + category + " / " + action + " / " + label + " / " + value, e);
                }
                return null;
            }
        }.execute();
    }

    public void trackPageView(final String path) {
        // We wrap the call in an AsyncTask since the Google Analytics library
        // writes to disk
        // on its calling thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mTracker.trackPageView(path);
                    Log.d(TAG, "iosched Analytics trackPageView: " + path);
                } catch (Exception e) {
                    // We don't want to crash if there's an Analytics library
                    // exception.
                    Log.w(TAG, "iosched Analytics trackPageView error: " + path, e);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Empty instance for use when Analytics is disabled or there was no Context
     * available.
     */
    private static AnalyticsUtils sEmptyAnalyticsUtils = new AnalyticsUtils(null) {
        @Override
        public void trackEvent(String category, String action, String label, int value) {
        }

        @Override
        public void trackPageView(String path) {
        }
    };
}
