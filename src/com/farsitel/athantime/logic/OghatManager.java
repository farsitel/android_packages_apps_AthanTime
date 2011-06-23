/*
 * Copyright (C) 2011 Iranian Supreme Council of ICT, The FarsiTel Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASICS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.farsitel.athantime.logic;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import android.location.Location;

import com.farsitel.athantime.data.AthanTime;

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
