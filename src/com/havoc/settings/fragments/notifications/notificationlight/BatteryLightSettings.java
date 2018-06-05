/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.havoc.settings.fragments.notifications.notificationlight;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.lineageos.internal.notification.LightsCapabilities;
import com.havoc.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

import lineageos.preference.LineageSystemSettingSwitchPreference;
import lineageos.providers.LineageSettings;

public class BatteryLightSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "BatteryLightSettings";

    private static final String GENERAL_SECTION = "general_section";
    private static final String COLORS_SECTION = "colors_list";
    private static final String BRIGHTNESS_SECTION = "brightness_section";

    private static final String LOW_COLOR_PREF = "low_color";
    private static final String MEDIUM_COLOR_PREF = "medium_color";
    private static final String FULL_COLOR_PREF = "full_color";
    private static final String REALLY_FULL_COLOR_PREF = "really_full_color";
    private static final String LIGHT_ENABLED_PREF = "battery_light_enabled";
    private static final String PULSE_ENABLED_PREF = "battery_light_pulse";
    private static final String BRIGHTNESS_PREFERENCE = "battery_light_brightness_level";
    private static final String BRIGHTNESS_ZEN_PREFERENCE = "battery_light_brightness_level_zen";

    private PreferenceGroup mColorPrefs;
    private ApplicationLightPreference mLowColorPref;
    private ApplicationLightPreference mMediumColorPref;
    private ApplicationLightPreference mFullColorPref;
    private ApplicationLightPreference mReallyFullColorPref;
    private LineageSystemSettingSwitchPreference mLightEnabledPref;
    private LineageSystemSettingSwitchPreference mPulseEnabledPref;
    private BatteryBrightnessPreference mBatteryBrightnessPref;
    private BatteryBrightnessZenPreference mBatteryBrightnessZenPref;
    private int mDefaultLowColor;
    private int mDefaultMediumColor;
    private int mDefaultFullColor;
    private int mDefaultReallyFullColor;
    private int mBatteryBrightness;
    // liblights supports brightness control
    private boolean mHALAdjustableBrightness;
    private boolean mMultiColorLed;

    private static final int MENU_RESET = Menu.FIRST;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Context context = getContext();
        final Resources res = getResources();

        // Collect battery led capabilities.
        mMultiColorLed =
                LightsCapabilities.supports(context, LightsCapabilities.LIGHTS_RGB_BATTERY_LED);
        mHALAdjustableBrightness = LightsCapabilities.supports(
                context, LightsCapabilities.LIGHTS_ADJUSTABLE_NOTIFICATION_LED_BRIGHTNESS);
        final boolean pulsatingLed =
                LightsCapabilities.supports(context, LightsCapabilities.LIGHTS_PULSATING_LED);
        final boolean segmentedBatteryLed =
                LightsCapabilities.supports(context, LightsCapabilities.LIGHTS_SEGMENTED_BATTERY_LED);

        addPreferencesFromResource(R.xml.battery_light_settings);
        getActivity().getActionBar().setTitle(R.string.battery_light_title);

        PreferenceScreen prefSet = getPreferenceScreen();

        PreferenceGroup generalPrefs = (PreferenceGroup) prefSet.findPreference(GENERAL_SECTION);

        mLightEnabledPref =
                (LineageSystemSettingSwitchPreference) prefSet.findPreference(LIGHT_ENABLED_PREF);
        mPulseEnabledPref =
                (LineageSystemSettingSwitchPreference) prefSet.findPreference(PULSE_ENABLED_PREF);
        mBatteryBrightnessPref =
                (BatteryBrightnessPreference) prefSet.findPreference(BRIGHTNESS_PREFERENCE);
        mBatteryBrightnessZenPref =
                (BatteryBrightnessZenPreference) prefSet.findPreference(BRIGHTNESS_ZEN_PREFERENCE);

        mDefaultLowColor = res.getInteger(
                com.android.internal.R.integer.config_notificationsBatteryLowARGB);
        mDefaultMediumColor = res.getInteger(
                com.android.internal.R.integer.config_notificationsBatteryMediumARGB);
        mDefaultFullColor = res.getInteger(
                com.android.internal.R.integer.config_notificationsBatteryFullARGB);
        mDefaultReallyFullColor = res.getInteger(
                com.android.internal.R.integer.config_notificationsBatteryReallyFullARGB);

        mBatteryBrightness = mBatteryBrightnessPref.getBrightnessSetting();

        if (!pulsatingLed || segmentedBatteryLed) {
            generalPrefs.removePreference(mPulseEnabledPref);
        }

        if (mMultiColorLed) {
            setHasOptionsMenu(true);

            // Low, Medium and full color preferences
            mLowColorPref = (ApplicationLightPreference) prefSet.findPreference(LOW_COLOR_PREF);
            mLowColorPref.setOnPreferenceChangeListener(this);
            mLowColorPref.setDefaultValues(mDefaultLowColor, 0, 0);
            mLowColorPref.setBrightness(mBatteryBrightness);

            mMediumColorPref = (ApplicationLightPreference) prefSet.findPreference(MEDIUM_COLOR_PREF);
            mMediumColorPref.setOnPreferenceChangeListener(this);
            mMediumColorPref.setDefaultValues(mDefaultMediumColor, 0, 0);
            mMediumColorPref.setBrightness(mBatteryBrightness);

            mFullColorPref = (ApplicationLightPreference) prefSet.findPreference(FULL_COLOR_PREF);
            mFullColorPref.setOnPreferenceChangeListener(this);
            mFullColorPref.setDefaultValues(mDefaultFullColor, 0, 0);
            mFullColorPref.setBrightness(mBatteryBrightness);

            mReallyFullColorPref = (ApplicationLightPreference) prefSet.findPreference(REALLY_FULL_COLOR_PREF);
            mReallyFullColorPref.setOnPreferenceChangeListener(this);
            mReallyFullColorPref.setDefaultValues(mDefaultReallyFullColor, 0, 0);
            mReallyFullColorPref.setBrightness(mBatteryBrightness);

            final BrightnessPreference.OnBrightnessChangedListener brightnessListener =
                    new BrightnessPreference.OnBrightnessChangedListener() {
                @Override
                public void onBrightnessChanged(int brightness) {
                    mLowColorPref.setBrightness(brightness);
                    mMediumColorPref.setBrightness(brightness);
                    mFullColorPref.setBrightness(brightness);
                    mReallyFullColorPref.setBrightness(brightness);
                }
            };
            mBatteryBrightnessPref.setOnBrightnessChangedListener(brightnessListener);
        } else {
            prefSet.removePreference(prefSet.findPreference(COLORS_SECTION));
            resetColors();
        }

        // Remove battery LED brightness controls if we can't support them.
        if (segmentedBatteryLed || (!mMultiColorLed && !mHALAdjustableBrightness)) {
            prefSet.removePreference(prefSet.findPreference(BRIGHTNESS_SECTION));
        }

        //watch(LineageSettings.System.getUriFor(LineageSettings.System.BATTERY_LIGHT_ENABLED));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshColors();
    }

    private void refreshColors() {
        ContentResolver resolver = getActivity().getContentResolver();
        Resources res = getResources();

        if (mLowColorPref != null) {
            int lowColor = LineageSettings.System.getIntForUser(resolver,
                    LineageSettings.System.BATTERY_LIGHT_LOW_COLOR, mDefaultLowColor, UserHandle.USER_CURRENT);
            mLowColorPref.setAllValues(lowColor, 0, 0, false);
        }

        if (mMediumColorPref != null) {
            int mediumColor = LineageSettings.System.getIntForUser(resolver,
                    LineageSettings.System.BATTERY_LIGHT_MEDIUM_COLOR, mDefaultMediumColor, UserHandle.USER_CURRENT);
            mMediumColorPref.setAllValues(mediumColor, 0, 0, false);
        }

        if (mFullColorPref != null) {
            int fullColor = LineageSettings.System.getIntForUser(resolver,
                    LineageSettings.System.BATTERY_LIGHT_FULL_COLOR, mDefaultFullColor, UserHandle.USER_CURRENT);
            mFullColorPref.setAllValues(fullColor, 0, 0, false);
            updateBrightnessPrefColor(fullColor);
        }

        if (mReallyFullColorPref != null) {
            int reallyfullColor = LineageSettings.System.getIntForUser(resolver,
                    LineageSettings.System.BATTERY_LIGHT_REALLY_FULL_COLOR, mDefaultReallyFullColor, UserHandle.USER_CURRENT);
            mReallyFullColorPref.setAllValues(reallyfullColor, 0, 0, false);
            updateBrightnessPrefColor(reallyfullColor);
        }
    }

    private void updateBrightnessPrefColor(int color) {
        // If the user has selected no light (ie black) for
        // full charge, use white for the brightness preference.
        if (color == 0) {
            color = 0xFFFFFF;
        }
        mBatteryBrightnessPref.setLedColor(color);
        mBatteryBrightnessZenPref.setLedColor(color);
    }

    /**
     * Updates the default or application specific notification settings.
     *
     * @param key of the specific setting to update
     * @param color
     */
    protected void updateValues(String key, Integer color) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (key.equals(LOW_COLOR_PREF)) {
            LineageSettings.System.putIntForUser(resolver, LineageSettings.System.BATTERY_LIGHT_LOW_COLOR, color, UserHandle.USER_CURRENT);
        } else if (key.equals(MEDIUM_COLOR_PREF)) {
            LineageSettings.System.putIntForUser(resolver, LineageSettings.System.BATTERY_LIGHT_MEDIUM_COLOR, color, UserHandle.USER_CURRENT);
        } else if (key.equals(FULL_COLOR_PREF)) {
            LineageSettings.System.putIntForUser(resolver, LineageSettings.System.BATTERY_LIGHT_FULL_COLOR, color, UserHandle.USER_CURRENT);
            updateBrightnessPrefColor(color);
        } else if (key.equals(REALLY_FULL_COLOR_PREF)) {
            LineageSettings.System.putIntForUser(resolver, LineageSettings.System.BATTERY_LIGHT_REALLY_FULL_COLOR, color, UserHandle.USER_CURRENT);
            updateBrightnessPrefColor(color);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mMultiColorLed) {
            menu.add(0, MENU_RESET, 0, R.string.reset)
                    .setIcon(R.drawable.ic_settings_backup_restore)
                    .setAlphabeticShortcut('r')
                    .setShowAsActionFlags(
                            MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefaults();
                return true;
        }
        return false;
    }

    protected void resetColors() {
        ContentResolver resolver = getActivity().getContentResolver();

        // Reset to the framework default colors
        LineageSettings.System.putIntForUser(resolver, LineageSettings.System.BATTERY_LIGHT_LOW_COLOR,
                mDefaultLowColor, UserHandle.USER_CURRENT);
        LineageSettings.System.putIntForUser(resolver, LineageSettings.System.BATTERY_LIGHT_MEDIUM_COLOR,
                mDefaultMediumColor, UserHandle.USER_CURRENT);
        LineageSettings.System.putIntForUser(resolver, LineageSettings.System.BATTERY_LIGHT_FULL_COLOR,
                mDefaultFullColor, UserHandle.USER_CURRENT);
        LineageSettings.System.putIntForUser(resolver, LineageSettings.System.BATTERY_LIGHT_REALLY_FULL_COLOR,
                mDefaultReallyFullColor, UserHandle.USER_CURRENT);
        refreshColors();
    }

    protected void resetToDefaults() {
/*
        final Resources res = getResources();
        final boolean batteryLightEnabled = res.getBoolean(R.bool.def_battery_light_enabled);
        final boolean batteryLightPulseEnabled = res.getBoolean(R.bool.def_battery_light_pulse);

        if (mLightEnabledPref != null) mLightEnabledPref.setChecked(batteryLightEnabled);
        if (mPulseEnabledPref != null) mPulseEnabledPref.setChecked(batteryLightPulseEnabled);
*/
        resetColors();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ApplicationLightPreference lightPref = (ApplicationLightPreference) preference;
        updateValues(lightPref.getKey(), lightPref.getColor());
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HAVOC_SETTINGS;
    }
}
