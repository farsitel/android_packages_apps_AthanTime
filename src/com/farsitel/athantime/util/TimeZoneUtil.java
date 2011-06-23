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

package com.farsitel.athantime.util;


import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeZoneUtil {
  //---------------------- Time-Zone Functions -----------------------


 // compute local time-zone for a specific date
 public static double getTimeZone(Date date) 
 {
     Date localDate = new Date(date.getYear(), date.getMonth(), date.getDay());
     String GMTString = localDate.toGMTString();
     Date GMTDate = new Date(GMTString.substring(0, GMTString.lastIndexOf(' ')- 1));
     double hoursDiff = (localDate.getTime()- GMTDate.getTime()) / (1000* 60* 60);
     return hoursDiff;
 }


 // compute base time-zone of the system
 public static TimeZone getBaseTimeZone () 
 {
     return TimeZone.getDefault();
 }


 // detect daylight saving in a given date
 public static boolean detectDaylightSaving(Date date,TimeZone timeZone) 
 {
     return timeZone.inDaylightTime(date);
 }


 // return effective timezone for a given date
 public static double effectiveTimeZone(int year,int month,int day,TimeZone timeZone)
 {
     if (timeZone == null)
         timeZone = TimeZone.getDefault();
     long time = (new Date(year,month,day)).getTime();
     if(!timeZone.inDaylightTime(new Date(year,month,day))){
         return (timeZone.getOffset(time))/(60d*60d*1000d);
     }else{
         return (timeZone.getOffset(time))/(60d*60d*1000d)-1;
     }
     
 }

}
