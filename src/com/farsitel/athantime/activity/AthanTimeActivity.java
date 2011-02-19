package com.farsitel.athantime.activity;

/*
 * This is the main Activity for Athan application.
 * 
 *  Written by: Majid Kalkatehchi
 *  Email: majid@farsitel.com
 */

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.farsitel.athantime.R;
import com.farsitel.athantime.data.AthanTime;
import com.farsitel.athantime.data.DayTime;
import com.farsitel.athantime.logic.AthanTimeCalculator;
import com.farsitel.athantime.util.ConstantUtilInterface;
import com.farsitel.athantime.util.LocationEnum;
import com.farsitel.athantime.views.PanelSwitcher;

public class AthanTimeActivity extends Activity implements LocationListener,
        OnSharedPreferenceChangeListener, OnClickListener,
        ConstantUtilInterface {
    // Component that handles right and left
    PanelSwitcher panelSwitcher;

    public static final int DATE_CHANGED = 1;
    public boolean isRegistered = false;

    AthanTimeCalculator athanTime = null;
    private Location previousLocation = null;
    private int previousDate = -1;

    public Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DATE_CHANGED:
                if (previousLocation != null) {
                    onLocationChanged(previousLocation);
                    previousDate = msg.arg1;
                }
            }
        }

    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        panelSwitcher = (PanelSwitcher) findViewById(R.id.panelswitch);

        setContentView(R.layout.main);
        uiSetup();
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);
        boolean isGPS = isGPSOn();
        if (isGPS) {
            registerListener();
            setTextOnGPSOn();

        } else
            useDefaultLocation(prefs,
                    getString(R.string.state_location_pref_key));
        Button nextBtn = (Button) findViewById(R.id.nextBtn);
        Button previousBtn = (Button) findViewById(R.id.previous);
        nextBtn.setOnClickListener(this);
        previousBtn.setOnClickListener(this);
        TimerTask dateChecker = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Calendar c = Calendar.getInstance();
                int date = c.get(Calendar.DATE);

                if (date != previousDate || previousDate < 0) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = DATE_CHANGED;
                    msg.arg1 = date;
                    mHandler.sendMessage(msg);
                }
            }

        };
        Timer timer = new Timer();
        timer.schedule(dateChecker, 0, 10 * 1000);
    }

    private void setTextOnGPSOn() {
        ((TextView) findViewById(R.id.fajrID)).setText("");
        ((TextView) findViewById(R.id.sunriseID)).setText("");
        ((TextView) findViewById(R.id.noonID)).setText("");
        ((TextView) findViewById(R.id.nightID)).setText("");
        ((TextView) findViewById(R.id.nightAthanID)).setText("");
        ((TextView) findViewById(R.id.fajrID2)).setText("");
        ((TextView) findViewById(R.id.sunriseID2)).setText("");
        ((TextView) findViewById(R.id.noonID2)).setText("");
        ((TextView) findViewById(R.id.nightID2)).setText("");
        ((TextView) findViewById(R.id.nightAthanID2)).setText("");
        ((TextView) findViewById(R.id.fajrStr)).setText("");
        ((TextView) findViewById(R.id.sunriseStr)).setText("");
        ((TextView) findViewById(R.id.noonStr)).setText("");
        ((TextView) findViewById(R.id.nightStr)).setText("");
        ((TextView) findViewById(R.id.nightAthanStr)).setText("");
        ((TextView) findViewById(R.id.fajrStr2)).setText("");
        ((TextView) findViewById(R.id.sunriseStr2)).setText("");
        ((TextView) findViewById(R.id.noonStr2)).setText("");
        ((TextView) findViewById(R.id.nightStr2)).setText("");
        ((TextView) findViewById(R.id.nightAthanStr2)).setText("");
        ((Button) findViewById(R.id.previous)).setVisibility(View.INVISIBLE);
        ((Button) findViewById(R.id.nextBtn)).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.LocationTextID)).setText("");
        ((TextView) findViewById(R.id.LocationTextID2)).setText("");
        TextView todayID = (TextView) findViewById(R.id.todayID);
        TextView todayTextID = (TextView) findViewById(R.id.todayTextID);
        TextView tommorrowID = (TextView) findViewById(R.id.tommorrowId);
        TextView tommorrowTextID = (TextView) findViewById(R.id.tommorrowTextID);
        // String line1 = getString(R.string.no_location_line1);
        String line1 = "";
        String line2 = getString(R.string.no_location_yet);

        todayID.setText(line1);
        todayTextID.setText(line2);
        tommorrowID.setText(line1);
        tommorrowTextID.setText(line2);
    }

    public boolean isGPSOn() {
        String gpsPerfKey = getString(R.string.gps_pref_key);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean isGPS = false;
        try {
            isGPS = prefs.getBoolean(gpsPerfKey, false);
        } catch (ClassCastException e) {
            isGPS = Boolean.parseBoolean(prefs.getString(gpsPerfKey, "false"));
        }
        return isGPS;
    }

    private void registerListener() {
        if (isGPSOn()) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setSpeedRequired(false);
            criteria.setCostAllowed(true);
            LocationManager locationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
            String provider = locationManager.getBestProvider(criteria, true);

            locationManager.requestLocationUpdates(provider, MIN_LOCATION_TIME,
                    MIN_LOCATION_DISTANCE, this);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, MIN_LOCATION_TIME,
                    MIN_LOCATION_DISTANCE, this);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, MIN_LOCATION_TIME,
                    MIN_LOCATION_DISTANCE, this);
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (location != null) {
                this.onLocationChanged(location);
            }

            isRegistered = true;
        }
    }

    private void unregisterListener() {

        ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                .removeUpdates(this);

    }

    private boolean isDateChanged(Calendar c) {
        if (previousDate != c.get(Calendar.DATE))
            return false;
        else
            return true;
    }

    private void uiSetup() {
        TextView todayFajr = (TextView) findViewById(R.id.fajrID);
        TextView todaySunrise = (TextView) findViewById(R.id.sunriseID);
        TextView todayDhuhr = (TextView) findViewById(R.id.noonID);
        TextView todaySunset = (TextView) findViewById(R.id.nightID);
        TextView todayMaghrib = (TextView) findViewById(R.id.nightAthanID);
        TextView tomorrowFajr = (TextView) findViewById(R.id.fajrID2);
        TextView tomorrowSunrise = (TextView) findViewById(R.id.sunriseID2);
        TextView tomorrowDhuhr = (TextView) findViewById(R.id.noonID2);
        TextView tomorrowSunset = (TextView) findViewById(R.id.nightID2);
        TextView tomorrowMaghrib = (TextView) findViewById(R.id.nightAthanID2);
        TextView todayText = (TextView) findViewById(R.id.todayID);
        TextView todayDateText = (TextView) findViewById(R.id.todayTextID);
        TextView tomorrowText = (TextView) findViewById(R.id.tommorrowId);
        TextView tomorrowDateText = (TextView) findViewById(R.id.tommorrowTextID);
        TextView todayFajrStr = (TextView) findViewById(R.id.fajrStr);
        TextView todaySunriseStr = (TextView) findViewById(R.id.sunriseStr);
        TextView todayDhuhrStr = (TextView) findViewById(R.id.noonStr);
        TextView todaySunsetStr = (TextView) findViewById(R.id.nightStr);
        TextView todayMaghribStr = (TextView) findViewById(R.id.nightAthanStr);
        TextView tomorrowFajrStr = (TextView) findViewById(R.id.fajrStr2);
        TextView tomorrowSunriseStr = (TextView) findViewById(R.id.sunriseStr2);
        TextView tomorrowDhuhrStr = (TextView) findViewById(R.id.noonStr2);
        TextView tomorrowSunsetStr = (TextView) findViewById(R.id.nightStr2);
        TextView tomorrowMaghribStr = (TextView) findViewById(R.id.nightAthanStr2);
        ((Button) findViewById(R.id.previous)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.nextBtn)).setVisibility(View.VISIBLE);

        //
        todayFajr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todaySunrise.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todayDhuhr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todaySunset.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todayMaghrib.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todayText.setTextColor(getResources().getColor(R.color.black));
        tomorrowFajr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowSunrise.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowDhuhr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowSunset.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowMaghrib.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todayText.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowText.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todayDateText.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowDateText.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        //
        todayFajrStr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todaySunriseStr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todayDhuhrStr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todaySunsetStr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        todayMaghribStr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowFajrStr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowSunriseStr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowDhuhrStr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowSunsetStr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowMaghribStr.setTextSize(getResources().getDimension(
                R.dimen.about_text_size));
        tomorrowText.setTextColor(getResources().getColor(R.color.black));

    }

    @Override
    public void onLocationChanged(Location location) {
        panelSwitcher = (PanelSwitcher) findViewById(R.id.panelswitch);
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getDefault());
        c.setTimeInMillis(System.currentTimeMillis());
        if ((previousLocation == null)
                || (isFarEnough(location, previousLocation))
                || isDateChanged(c)) {
            if (athanTime == null) {
                athanTime = new AthanTimeCalculator();
                athanTime = prepareAthanCalculator(athanTime);
                previousLocation = location;
            }

            athanTime.setCalcMethod(AthanTimeCalculator.Jafari);
            athanTime.setAsrJuristic(AthanTimeCalculator.Shafii);
            athanTime.setAdjustHighLats(AthanTimeCalculator.AngleBased);
            int[] offsets = { 0, 0, 0, 0, 0, 0, 0 }; // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
            athanTime.tune(offsets);
            AthanTime prayerTimes = athanTime.getPrayerTimes(c,
                    location.getLatitude(), location.getLongitude(),
                    athanTime.getBaseTimeZone());
            c.add(Calendar.DATE, 1);
            // todayAthanTime = prayerTimes;
            AthanTime tommorowPrayerTime = athanTime.getPrayerTimes(c,
                    location.getLatitude(), location.getLongitude(),
                    athanTime.getBaseTimeZone());
            // tomorrowAthanTime = tommorowPrayerTime;
            setAthanTimeText(prayerTimes, tommorowPrayerTime);
        }

    }

    private void setAthanTimeText(AthanTime todayPrayerTimes,
            AthanTime tommorowPrayerTimes) {
        uiSetup();

        ((TextView) findViewById(R.id.fajrStr))
                .setText(getString(R.string.fajr));
        ((TextView) findViewById(R.id.sunriseStr))
                .setText(getString(R.string.sunrise));
        ((TextView) findViewById(R.id.noonStr))
                .setText(getString(R.string.noon));
        ((TextView) findViewById(R.id.nightStr))
                .setText(getString(R.string.night));
        ((TextView) findViewById(R.id.nightAthanStr))
                .setText(getString(R.string.nightAthan));
        ((TextView) findViewById(R.id.fajrStr2))
                .setText(getString(R.string.fajr));
        ((TextView) findViewById(R.id.sunriseStr2))
                .setText(getString(R.string.sunrise));
        ((TextView) findViewById(R.id.noonStr2))
                .setText(getString(R.string.noon));
        ((TextView) findViewById(R.id.nightStr2))
                .setText(getString(R.string.night));
        ((TextView) findViewById(R.id.nightAthanStr2))
                .setText(getString(R.string.nightAthan));

        TextView fajr;
        TextView sunrise;
        TextView duhr;
        TextView sunset;
        TextView maghrib;
        TextView datePrefixText;
        TextView dateText;
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getDefault());
        c.setTimeInMillis(System.currentTimeMillis());

        fajr = (TextView) findViewById(R.id.fajrID);
        sunrise = (TextView) findViewById(R.id.sunriseID);
        duhr = (TextView) findViewById(R.id.noonID);
        sunset = (TextView) findViewById(R.id.nightID);
        maghrib = (TextView) findViewById(R.id.nightAthanID);
        datePrefixText = (TextView) findViewById(R.id.todayID);
        dateText = (TextView) findViewById(R.id.todayTextID);

        dateText.setText(getFormattedDate(c));
        fajr.setText(getTimeForPrint(todayPrayerTimes.getFajr()));
        sunrise.setText(getTimeForPrint(todayPrayerTimes.getSunrise()));
        duhr.setText(getTimeForPrint(todayPrayerTimes.getDhuhr()));
        sunset.setText(getTimeForPrint(todayPrayerTimes.getSunset()));
        maghrib.setText(getTimeForPrint(todayPrayerTimes.getMaghrib()));
        //
        c.add(Calendar.DATE, 1);
        fajr = (TextView) findViewById(R.id.fajrID2);
        sunrise = (TextView) findViewById(R.id.sunriseID2);
        duhr = (TextView) findViewById(R.id.noonID2);
        sunset = (TextView) findViewById(R.id.nightID2);
        maghrib = (TextView) findViewById(R.id.nightAthanID2);
        datePrefixText = (TextView) findViewById(R.id.tommorrowId);
        dateText = (TextView) findViewById(R.id.tommorrowTextID);

        dateText.setText(getFormattedDate(c));
        datePrefixText.setText(getString(R.string.tomorrow));
        fajr.setText(getTimeForPrint(tommorowPrayerTimes.getFajr()));
        sunrise.setText(getTimeForPrint(tommorowPrayerTimes.getSunrise()));
        duhr.setText(getTimeForPrint(tommorowPrayerTimes.getDhuhr()));
        sunset.setText(getTimeForPrint(tommorowPrayerTimes.getSunset()));
        maghrib.setText(getTimeForPrint(tommorowPrayerTimes.getMaghrib()));
    }

    // @Override
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        super.onTouchEvent(event);
        panelSwitcher = (PanelSwitcher) findViewById(R.id.panelswitch);
        boolean isTurned = panelSwitcher.onTouchEvent(event);
        // rebuildForPanelChange(panelSwitcher);
        return isTurned;

    }

    private String getFormattedDate(Calendar c) {
        return ""
                + android.text.format.DateFormat.format("d MMM yyy",
                        c.getTimeInMillis()); // FIXME , this);
    }

    private String getTimeForPrint(DayTime dayTime) {
        int hour = dayTime.getHour();
        int minute = dayTime.getMinute();
        return persianDigitsIfPersian(String.format("%02d:%02d", hour, minute));
    }

    private String persianDigitsIfPersian(String str) {
        if (!"fa".equals(Locale.getDefault().getLanguage()))
            return str;

        String result = "";
        char ch;
        int i;
        for (i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if ((ch >= '0') && (ch <= '9'))
                result += Character.toString((char)(0x06f0 + (ch - '0')));
            else
                result += ch;
        }
        return result;
    }

    // private String getLocationForPrint(double value, boolean isLatitude) {
    // int degree = (new Double(Math.floor(value))).intValue();
    // String end = getString(((isLatitude) ? R.string.latitude_south
    // : R.string.longitude_west));
    // if (degree > 0) {
    // end = getString(((isLatitude) ? R.string.latitude_north
    // : R.string.longitude_east));
    // }
    // double second = (value - degree) * 100;
    // double minDouble = (second * 3d / 5d);
    // int minute = new Double(Math.floor(minDouble)).intValue();
    // return Jalali.persianDigits(degree + "") + "" + '\u00B0' + " "
    // + Jalali.persianDigits("" + minute) + "" + '\u00B4' + "" + end;
    //
    // }

    @Override
    public void onResume() {
        super.onResume();
        registerListener();
    }

    /*
     * checking is the new location worth the computation of athan Time
     */
    private boolean isFarEnough(Location newLocation, Location previousLocation) {
        return true;
    }

    /*
     * This method sets the default configuration values for the athan time
     * calculation. (Currently we set the calculation to Shia Ithna ashari
     * method)
     */
    private AthanTimeCalculator prepareAthanCalculator(
            AthanTimeCalculator athanTime) {
        athanTime.setCalcMethod(AthanTimeCalculator.ISNA);
        return athanTime;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        int aboutGroupID = 0;
        int prefsGroupID = 2;
        int aboutItemOrder = 3;
        // int helpItemOrder = 2;
        int prefsItemOrder = Menu.FIRST;
        MenuItem prefsMenuItem = menu.add(prefsGroupID, MENU_PREFS,
                prefsItemOrder, R.string.prefs_menu_title);
        prefsMenuItem.setIcon(R.drawable.settings);

        MenuItem aboutMenuItem = menu.add(aboutGroupID, MENU_ABOUT,
                aboutItemOrder, R.string.about_menu_title);
        aboutMenuItem.setIcon(R.drawable.about);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        // Check for each known menu item
        case (MENU_PREFS):
            Intent i = new Intent(this, UserPreferenceActivity.class);
            startActivityForResult(i, 1);
            return true;
        case (MENU_ABOUT):
            Intent i2 = new Intent(this, AboutActivity.class);
            startActivityForResult(i2, 2);
            return true;
        case (MENU_HELP):
            Intent i3 = new Intent(this, HelpActivity.class);
            startActivityForResult(i3, 3);
            return true;
        default:
            return false;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        String gpsPerfKey = getString(R.string.gps_pref_key);
        String defaultLocationPerfKey = getString(R.string.state_location_pref_key);
        if (gpsPerfKey.equals(key)) {
            boolean isGPS = false;
            try {
                isGPS = sharedPreferences.getBoolean(key, false);
            } catch (ClassCastException e) {
                isGPS = Boolean.parseBoolean(sharedPreferences.getString(key,
                        "false"));
            }
            if (isGPS) {
                registerListener();
                setTextOnGPSOn();
            } else {
                unregisterListener();
                useDefaultLocation(sharedPreferences, defaultLocationPerfKey);
            }
        } else if (defaultLocationPerfKey.equals(key)) {
            sharedPreferences.edit().putBoolean(gpsPerfKey, false);
            sharedPreferences.edit().commit();
            useDefaultLocation(sharedPreferences, key);
        } else {
            Log.w(NAMAZ_LOG_TAG, "preference with key:" + key
                    + " is changed and it is not handled properly");
        }

    }

    private void useDefaultLocation(SharedPreferences perfs, String key) {
        int defLocationID = Integer.parseInt(perfs.getString(key, ""
                + LocationEnum.MENU_TEHRAN.getId()));
        LocationEnum locationEnum = LocationEnum.values()[defLocationID - 1];
        Location location = locationEnum.getLocation();
        ((TextView) findViewById(R.id.LocationTextID)).setText(String.format(
                getString(R.string.be_ofogh), locationEnum.getName(this)));
        ((TextView) findViewById(R.id.LocationTextID2)).setText(String.format(
                getString(R.string.be_ofogh), locationEnum.getName(this)));
        onLocationChanged(location);
    }

    @Override
    protected void onPause() {
        unregisterListener();
        super.onPause();
    }

    /*
     * We are Overriding this method to catch touch on the screen and trigger
     * the panelSwitch mechanism.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        PanelSwitcher panel = (PanelSwitcher) findViewById(R.id.panelswitch);
        switch (id) {
        case R.id.previous:
            if (panel.isRTL())
                panel.moveLeft();
            else
                panel.moveRight();
            break;
        case R.id.nextBtn:
            if (panel.isRTL())
                panel.moveRight();
            else
                panel.moveLeft();
        }
    }

}
