package com.farsitel.athantime.logic;


import java.util.Date;
import java.util.TimeZone;

import com.farsitel.athantime.data.AthanTime;
import com.farsitel.athantime.data.DayTime;
import com.farsitel.athantime.util.JulianGregorianConverter;
import com.farsitel.athantime.util.MathUtil;
import com.farsitel.athantime.util.TimeZoneUtil;

public class CopyOfAthanTimeCalculator {

    private static final String EQT = "EQT";
    private static final String D = "D";
    private static final String L = "l";

    // calculation methods
    public static final int JAFARI = 0; // Ithna Ashari
    public static final int KARACHI = 1; // University of Islamic Sciences,
                                         // Karachi
    public static final int ISNA = 2; // Islamic Society of North America (ISNA)
    public static final int MWL = 3; // Muslim World League (MWL)
    public static final int MAKKAH = 4; // Umm al-Qura, Makkah
    public static final int EGYPT = 5; // Egyptian General Authority of Survey
    public static final int CUSTOM = 6; // Custom Setting
    public static final int TEHRAN_UNIVERSITY = 7; // Institute of Geophysics,
                                                   // University of Tehran

    // Juristic Methods
    public static final int ShAFII = 0; // Shafii (standard)
    public static final int HANAFI = 1; // Hanafi

    // Adjusting Methods for Higher Latitudes
    public static final int NONE = 0; // No adjustment
    public static final int MIDNIGHT = 1; // middle of night
    public static final int ONE_SEVENTH = 2; // 1/7th of night
    public static final int ANGLE_BASED = 3; // angle/60th of night

    // ---------------------- Global Variables --------------------

    private int calcMethod = 0; // calculation method
    private int asrJuristic = 0; // Juristic method for Asr
    private int dhuhrMinutes = 0; // minutes after mid-day for Dhuhr
    private int adjustHighLats = 1; // adjusting method for higher latitudes
    private int timeFormat = 0;

    private double latitude; // latitude
    private double longitude; // longitude
    private double timeZone; // time-zone
    private double JDate; // Julian date

    // Time Formats
    public static final int TIME_24 = 0; // 24-hour format
    public static final int TIME_12 = 1; // 12-hour format
    public static final int TIME_12_NS = 2; // 12-hour format with no suffix
    public static final int FLOAT = 3; // floating point number

    // --------------------- Technical Settings --------------------
    private int numIterations = 1; // number of iterations needed to compute
                                   // times

    // ------------------- Calc Method Parameters --------------------
    private static double[][] methodParams = new double[8][5];

    /*
     * this.methodParams[methodNum] = new Array(fa, ms, mv, is, iv);
     * 
     * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
     * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
     * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter value
     * (in angle or minutes)
     */
    static {
        methodParams[JAFARI] = new double[] { 16d, 0d, 4d, 0d, 14d };
        methodParams[KARACHI] = new double[] { 18, 1, 0, 0, 18 };
        methodParams[ISNA] = new double[] { 15, 1, 0, 0, 15 };
        methodParams[MWL] = new double[] { 18, 1, 0, 0, 17 };
        methodParams[MAKKAH] = new double[] { 18.5, 1, 0, 1, 90 };
        methodParams[EGYPT] = new double[] { 19.5, 1, 0, 0, 17.5 };
        methodParams[TEHRAN_UNIVERSITY] = new double[] { 17.7, 0, 4.5, 0, 15 };
        methodParams[CUSTOM] = new double[] { 18, 1, 0, 0, 17 };
    }

    // -------------------- Interface Functions --------------------

    public AthanTime getDatePrayerTimes(int year, int month, int day,
            double latitude, double logitude, TimeZone timezone) {
        this.latitude = latitude;
        this.longitude = logitude;
        this.JDate = JulianGregorianConverter.toJulian(new int[] { year, month,
                day })
                - longitude / (15d * 20d);
        this.timeZone = TimeZoneUtil.effectiveTimeZone(year, month, day,
                timezone);
        return computeDayTimes();
    }

    public AthanTime getAthanTime(Date date, double latitude, double longitude,
            TimeZone timeZone) {
        return getDatePrayerTimes(date.getYear(), date.getMonth() + 1,
                date.getDay(), latitude, longitude, timeZone);
    }

    // set the calculation method
    public void setCalcMethod(int methodID) {
        this.calcMethod = methodID;
    }

    // set the juristic method for Asr
    public void setAsrMethod(int methodID) {
        if (methodID < 0 || methodID > 1)
            return;
        this.asrJuristic = methodID;
    }

    // set the angle for calculating Fajr
    public void setFajrAngle(double angle) {
        this.setCustomParams(new Double[] { angle, null, null, null, null });
    }

    // set the angle for calculating Maghrib
    public void setMaghribAngle(double angle) {
        this.setCustomParams(new Double[] { null, 0d, angle, null, null });
    }

    // set the angle for calculating Isha
    public void setIshaAngle(double angle) {
        this.setCustomParams(new Double[] { null, null, null, 0d, angle });
    }

    // set the minutes after mid-day for calculating Dhuhr
    public void setDhuhrMinutes(int minutes) {
        this.dhuhrMinutes = minutes;
    }

    // set the minutes after Sunset for calculating Maghrib
    public void setMaghribMinutes(double minutes) {
        this.setCustomParams(new Double[] { null, 1d, minutes, null, null });
    }

    // set the minutes after Maghrib for calculating Isha
    public void setIshaMinutes(double minutes) {
        this.setCustomParams(new Double[] { null, null, null, 1d, minutes });
    }

    // set custom values for calculation parameters
    public void setCustomParams(Double[] params) {
        for (int i = 0; i < 5; i++) {
            if (params[i] == null)
                CopyOfAthanTimeCalculator.methodParams[CopyOfAthanTimeCalculator.CUSTOM][i] = CopyOfAthanTimeCalculator.methodParams[calcMethod][i];
            else
                CopyOfAthanTimeCalculator.methodParams[CopyOfAthanTimeCalculator.CUSTOM][i] = params[i];
        }
        this.calcMethod = CopyOfAthanTimeCalculator.CUSTOM;
    }

    // set adjusting method for higher latitudes
    public void setHighLatsMethod(int methodID) {
        this.adjustHighLats = methodID;
    }

    // set the time format
    public void setTimeFormat(int timeFormat) {
        this.timeFormat = timeFormat;
    }

    // convert float hours to 24h format
    public DayTime floatToTime24(double time) {
        time = MathUtil.fixHour(time + 0.5d / 60d); // add 0.5 minutes to round
        double hours = Math.floor(time);
        double minutes = Math.floor((time - hours) * 60);
        return new DayTime(new Double(hours).intValue(),
                new Double(minutes).intValue(), 0);
    }

    // convert float hours to 12h format
    public String floatToTime12(double time, boolean noSuffix) {

        time = MathUtil.fixHour(time + 0.5 / 60); // add 0.5 minutes to round
        double hours = Math.floor(time);
        double minutes = Math.floor((time - hours) * 60);
        String suffix = hours >= 12 ? "pm" : "am";
        hours = (hours + 12 - 1) % 12 + 1;
        return hours + ":" + MathUtil.twoDigitsFormat(minutes)
                + (noSuffix ? "" : suffix);
    }

    // convert float hours to 12h format with no suffix
    public String floatToTime12NS(double time) {
        return floatToTime12(time, true);
    }

    // ---------------------- Calculation Functions -----------------------

    // References:
    // http://www.ummah.net/astronomy/saltime
    // http://aa.usno.navy.mil/faq/docs/SunApprox.html

    // compute declination angle of sun and equation of time
    private double[] sunPosition(double jd) {
        double D = jd - 2451545.0d;
        double g = MathUtil.fixAngle(357.529 + 0.98560028 * D);
        double q = MathUtil.fixAngle(280.459 + 0.98564736 * D);
        double L = MathUtil.fixAngle(q + 1.915 * MathUtil.dSin(g) + 0.020
                * MathUtil.dSin(2 * g));

        double R = 1.00014 - 0.01671 * MathUtil.dCos(g) - 0.00014
                * MathUtil.dCos(2 * g);
        double e = 23.439 - 0.00000036 * D;

        double d = MathUtil.dArcSin(MathUtil.dSin(e) * MathUtil.dSin(L));
        double RA = MathUtil.dArcTan2(MathUtil.dCos(e) * MathUtil.dSin(L),
                MathUtil.dCos(L)) / 15;
        RA = MathUtil.fixHour(RA);
        double EqT = q / 15 - RA;

        return new double[] { d, EqT };
    }

    // compute equation of time
    private double equationOfTime(double jd) {
        return this.sunPosition(jd)[1];
    }

    // compute declination angle of sun
    private double sunDeclination(double jd) {
        return this.sunPosition(jd)[0];
    }

    // compute mid-day (Dhuhr, Zawal) time
    public double computeMidDay(double t) {
        double T = this.equationOfTime(this.JDate + t);
        double Z = MathUtil.fixHour(12 - T);
        return Z;
    }

    // compute time for a given angle G
    public double computeTime(double G, double t) {
        double D = this.sunDeclination(this.JDate + t);
        double Z = this.computeMidDay(t);
        double V = ((double)1 / (double)15) * MathUtil.dArcCos((-MathUtil.dSin(G) - MathUtil
                .dSin(D) * MathUtil.dSin(latitude))
                / (MathUtil.dCos(D) * MathUtil.dCos(latitude)));
        return Z + (G > 90 ? -V : V);
    }
//    public double computeTime(double G, double t) {
//        double D = this.sunDeclination(this.JDate + t);
//        double Z = this.computeMidDay(t);
//        double V = ((double)1 / (double)15) * MathUtil.dArcCos((-MathUtil.dSin(t) - MathUtil
//                .dSin(this.latitude) * MathUtil.dSin(D))
//                / (MathUtil.dCos(D) * MathUtil.dCos(latitude)));
//        return Z + (G > 90 ? -V : V);
//    }

    // compute the time of Asr
    public double computeAsr(double step, double t) // Shafii: step=1, Hanafi:
                                                    // step=2
    {
        double D = this.sunDeclination(this.JDate + t);
        double G = -MathUtil.dArcCot(step
                + MathUtil.dTan(Math.abs(latitude - D)));
        return this.computeTime(G, t);
    }

    // ---------------------- Compute Prayer Times -----------------------

    // compute prayer times at given julian date
    public double[] computeTimes(double[] times) {
        double[] t = this.dayPortion(times);

        double Fajr = computeTime(180 - methodParams[this.calcMethod][0], t[0]);
        double Sunrise = computeTime(180 - 0.833, t[1]);
        double Dhuhr = computeMidDay(t[2]);
        double Asr = computeAsr(1 + this.asrJuristic, t[3]);
        double Sunset = computeTime(0.833, t[4]);
        double Maghrib = computeTime(methodParams[this.calcMethod][2], t[5]);
        double Isha = computeTime(methodParams[this.calcMethod][4], t[6]);

        return new double[] { Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha };
    }

    // compute prayer times at given julian date
    public AthanTime computeDayTimes() {
        double[] times = new double[] { 5, 6, 12, 13, 18, 18, 18 }; // default
                                                                    // times

        for (int i = 1; i <= this.numIterations; i++)
            times = this.computeTimes(times);

        times = adjustTimes(times);
        AthanTime athanTime = new AthanTime();
        athanTime.setFajr(floatToTime24(times[0]));
        athanTime.setSunrise(floatToTime24(times[1]));
        athanTime.setDhuhr(floatToTime24(times[2]));
        athanTime.setAsr(floatToTime24(times[3]));
        athanTime.setSunset(floatToTime24(times[4]));
        athanTime.setMaghrib(floatToTime24(times[5]));
        athanTime.setIsha(floatToTime24(times[6]));
        return athanTime;
    }

    // adjust times in a prayer time array
    private double[] adjustTimes(double[] times) {
        for (int i = 0; i < 7; i++)
            times[i] += this.timeZone - this.longitude / 15;
        times[2] += this.dhuhrMinutes / 60; // Dhuhr
        if (methodParams[this.calcMethod][1] == 1) // Maghrib
            times[5] = times[4] + methodParams[this.calcMethod][2] / 60;
        if (methodParams[this.calcMethod][3] == 1) // Isha
            times[6] = times[5] + methodParams[this.calcMethod][4] / 60;

        if (this.adjustHighLats != NONE)
            times = this.adjustHighLatTimes(times);
        return times;
    }

    // convert times array to given time format
    public double[] adjustTimesFormat(double[] times) {
        if (this.timeFormat == FLOAT)
            return times;
        // for (int i=0; i<7; i++)
        // if (this.timeFormat == TIME_12)
        // // times[i] = this.floatToTime12(times[i]);
        // else if (this.timeFormat == TIME_12_NS)
        // // times[i] = this.floatToTime12(times[i], true);
        // else
        // // times[i] = this.floatToTime24(times[i]);
        return times;
    }

    // adjust Fajr, Isha and Maghrib for locations in higher latitudes
    private double[] adjustHighLatTimes(double[] times) {
        double nightTime = MathUtil.timeDiff(times[4], times[1]); // sunset to
                                                                  // sunrise

        // Adjust Fajr
        double FajrDiff = this.nightPortion(methodParams[this.calcMethod][0])
                * nightTime;
        if (MathUtil.timeDiff(times[0], times[1]) > FajrDiff)
            times[0] = times[1] - FajrDiff;

        // Adjust Isha
        double IshaAngle = (methodParams[this.calcMethod][3] == 0) ? methodParams[this.calcMethod][4]
                : 18;
        double IshaDiff = this.nightPortion(IshaAngle) * nightTime;
        if (MathUtil.timeDiff(times[4], times[6]) > IshaDiff)
            times[6] = times[4] + IshaDiff;

        // Adjust Maghrib
        double MaghribAngle = (methodParams[this.calcMethod][1] == 0) ? methodParams[this.calcMethod][2]
                : 4;
        double MaghribDiff = this.nightPortion(MaghribAngle) * nightTime;
        if (MathUtil.timeDiff(times[4], times[5]) > MaghribDiff)
            times[5] = times[4] + MaghribDiff;

        return times;
    }

    // the night portion used for adjusting times in higher latitudes
    private double nightPortion(double angle) {
        if (this.adjustHighLats == ANGLE_BASED)
            return (1 / 60 * angle);
        else if (this.adjustHighLats == MIDNIGHT)
            return 1d / 2d;
        else if (this.adjustHighLats == ONE_SEVENTH)
            return 1d / 7d;
        else
            return 1d / 2d;
    }

    // convert hours to day portions
    private double[] dayPortion(double[] times) {
        for (int i = 0; i < 7; i++)
            times[i] /= 24;
        return times;
    }
}
