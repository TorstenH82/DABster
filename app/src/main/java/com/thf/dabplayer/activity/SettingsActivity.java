package com.thf.dabplayer.activity;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import com.thf.dabplayer.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* renamed from: com.ex.dabplayer.pad.activity.SettingsActivity */
/* loaded from: classes.dex */
public class SettingsActivity extends TabActivity {
    public static final String pref_default_scan_type = "default_scan_type";
    public static final float pref_defvalue_audioLevel = 1.0f;
    public static final float pref_defvalue_audioLevelWhenDucked = 0.5f;
    public static final boolean pref_defvalue_audioloss_support = true;
    public static final boolean pref_defvalue_auto_maximize = false;
    public static final Long pref_defvalue_auto_maximize_timeout_msec = 10000L;
    public static final boolean pref_defvalue_background_boxes = false;
    public static final boolean pref_defvalue_declip = true;
    public static final boolean pref_defvalue_declip_notification = true;
    public static final int pref_defvalue_default_scan_type = 2;
    public static final int pref_defvalue_dim_percent = 50;
    public static final int pref_defvalue_dlsSizeIncrement = 0;
    public static final boolean pref_defvalue_logo_as_mot = true;
    public static final float pref_defvalue_maxScaleFactor = 2.0f;
    public static final boolean pref_defvalue_motSlideshowEnabled = true;
    public static final boolean pref_defvalue_onstartbyusb_gotobackground = false;
    public static final boolean pref_defvalue_record_button = false;
    public static final boolean pref_defvalue_sendBroadcastIntent = false;
    public static final boolean pref_defvalue_service_link_switch = true;
    public static final boolean pref_defvalue_showAdditionalInfos = true;
    public static final boolean pref_defvalue_showStationLogoInList = true;
    public static final int pref_defvalue_signalBarColorOption = 1;
    public static final boolean pref_defvalue_startOnUsbAttached = true;
    public static final int pref_defvalue_stationNameColor = -1;
    public static final int pref_defvalue_stationNameSizeIncrement = 0;
    public static final boolean pref_defvalue_swapPrevNext = false;
    public static final String pref_key_audioLevel = "audioLevel";
    public static final String pref_key_audioLevelWhenDucked = "audioLevelWhenDucked";
    public static final String pref_key_audioloss_support = "audioloss_support";
    public static final String pref_key_auto_maximize = "auto_maximize";
    public static final String pref_key_auto_maximize_timeout = "auto_maximize_timeout_sec";
    public static final String pref_key_background_boxes = "background_boxes";
    public static final String pref_key_declip = "declip";
    public static final String pref_key_declip_notification = "declip_notification";
    public static final String pref_key_dim_percent = "dim_percent";
    public static final String pref_key_dlsSizeIncrement = "dlsSizeIncrement";
    public static final String pref_key_logo_as_mot = "logo_as_mot";
    public static final String pref_key_maxScaleFactor = "maxScaleFactor";
    public static final String pref_key_motSlideshowEnabled = "motSlideshowEnabled";
    public static final String pref_key_onstartbyusb_gotobackground = "onstartbyusb_gotobackground";
    public static final String pref_key_record_button = "record_button";
    public static final String pref_key_sendBroadcastIntent = "sendBroadcastIntent";
    public static final String pref_key_service_link_switch = "service_link_switch";
    public static final String pref_key_showAdditionalInfos = "showAdditionalInfos";
    public static final String pref_key_showStationLogoInList = "showStationLogoInList";
    public static final String pref_key_signalBarColorOption = "signalBarColorOption";
    public static final String pref_key_startOnUsbAttached = "startOnUsbAttached";
    public static final String pref_key_stationNameColor = "stationNameColor";
    public static final String pref_key_stationNameSizeIncrement = "stationNameSizeIncrement";
    public static final String pref_key_swapPrevNext = "swapPrevNext";
    public static final int pref_val_signalBarColorOption_colored = 1;
    public static final int pref_val_signalBarColorOption_sameColorAsStationName = 0;
    public static final String prefname_playing = "playing";
    public static final String prefname_settings = "settings";

    @Override // android.app.ActivityGroup, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        TabHost tabHost = (TabHost) findViewById(16908306);
        if (tabHost != null) {
            TabHost.TabSpec tabSpec = tabHost.newTabSpec("_GENERAL");
            tabSpec.setIndicator(getResources().getString(R.string.settings_header_general));
            Intent intent = new Intent(this, SettingsGeneralActivity.class);
            tabSpec.setContent(intent);
            tabHost.addTab(tabSpec);
            TabHost.TabSpec tabSpec2 = tabHost.newTabSpec("_LAYOUT");
            tabSpec2.setIndicator(getResources().getString(R.string.settings_header_layout));
            Intent intent2 = new Intent(this, SettingsLayoutActivity.class);
            tabSpec2.setContent(intent2);
            tabHost.addTab(tabSpec2);
            TabHost.TabSpec tabSpec3 = tabHost.newTabSpec("_AUDIO");
            tabSpec3.setIndicator(getResources().getString(R.string.settings_header_audio));
            Intent intent3 = new Intent(this, SettingsAudioActivity.class);
            tabSpec3.setContent(intent3);
            tabHost.addTab(tabSpec3);
            TabHost.TabSpec tabSpec4 = tabHost.newTabSpec("_STATIONLOGO");
            tabSpec4.setIndicator(getResources().getString(R.string.settings_header_stationlogos));
            Intent intent4 = new Intent(this, SettingsStationLogoActivity.class);
            tabSpec4.setContent(intent4);
            tabHost.addTab(tabSpec4);
            //TabHost.TabSpec tabSpec5 = tabHost.newTabSpec("_ABOUT");
            //tabSpec5.setIndicator(getResources().getString(R.string.settings_header_about));
            //Intent intent5 = new Intent(this, SettingsAboutActivity.class);
            //tabSpec5.setContent(intent5);
            //tabHost.addTab(tabSpec5);
        }
    }

    public static void createSimpleTextSpinnerAdapter(@NonNull Spinner spinner, @NonNull String key, @NonNull Context context, @ArrayRes int stringArrayResId) {
        String[] from = {key};
        int[] to = {16908308};
        List<Map<String, String>> data = new ArrayList<>();
        String[] array = context.getResources().getStringArray(stringArrayResId);
        for (String str : array) {
            HashMap<String, String> map = new HashMap<>();
            map.put(key, str);
            data.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(context, data, 17367048, from, to);
        simpleAdapter.setDropDownViewResource(17367049);
        SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() { // from class: com.ex.dabplayer.pad.activity.SettingsActivity.1
            @Override // android.widget.SimpleAdapter.ViewBinder
            public boolean setViewValue(View view, Object data2, String textRepresentation) {
                TextView textView = (TextView) view;
                textView.setText(textRepresentation);
                return true;
            }
        };
        simpleAdapter.setViewBinder(viewBinder);
        spinner.setAdapter((SpinnerAdapter) simpleAdapter);
    }
}
