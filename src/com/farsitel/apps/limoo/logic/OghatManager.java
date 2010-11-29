package com.farsitel.apps.limoo.logic;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import android.location.Location;

import com.farsitel.apps.limoo.data.AthanTime;

public class OghatManager {
//    private List<PrayerTimeChangeListener> prayerTimeChangeListeners = new ArrayList<PrayerTimeChangeListener>();
    private static final double MIN_LATITUDE_FOR_PRAYER_CHANGE = 1;
    private static final double MIN_LONGITUDE_FOR_PRAYER_CHANGE = 1;
    private Location location;
//    public void addPrayerTimeChangeListener(PrayerTimeChangeListener p){
//        this.prayerTimeChangeListeners.add(p);
//    }
//    public void removePrayerTimeChangeListener(PrayerTimeChangeListener p){
//        this.prayerTimeChangeListeners.remove(p);
//    }
//    private void raisePrayerTimeChangeEvent(AthanTime athanTime){
//        Iterator<PrayerTimeChangeListener> i = prayerTimeChangeListeners.iterator();
//        while(i.hasNext()){
//            PrayerTimeChangeListener p = i.next();
//            p.onPrayerTimeChangeListener(athanTime);
//        }
//    }
    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        if(( location != null) && (this.location ==null || (this.location.getLatitude()-location.getLatitude()>MIN_LATITUDE_FOR_PRAYER_CHANGE ) || (this.location.getLongitude() - location.getLongitude())>MIN_LONGITUDE_FOR_PRAYER_CHANGE  )){
//            raisePrayerTimeChangeEvent(AthanTimeCalculator.getAthanTime(new Date(), location.getLatitude(), location.getLongitude(), TimeZone.getTimeZone("Iran")));
        }
        this.location = location;
    }

}
