package com.farsitel.apps.athantime.logic;


import java.util.HashMap;
import java.util.Map;

public class HijriCalendarConverter {
    public static Map<String, Integer> hijriToGregorian(int hYear, int hMonth,
            int hDay) {
        return null;
    }

    public static Map<String, Integer> gregorianToHijri(int gYear, int gMonth,
            int gDay) {
        int jd,l,n,j,m,d,y;
        if ((gYear > 1582) || ((gYear == 582) && (gMonth > 10))
                || ((gYear == 1582) && (gMonth == 10) && (gDay > 14))) {
            jd = intPart((1461 * (gYear + 4800 +
                    intPart((gMonth - 14)/12)))/4) +
                    intPart((367 * (gMonth - 2 - 12 *
                    (intPart((gMonth - 14)/12))))/12) -
                    intPart((3 * (intPart((gYear + 4900 +
                    intPart((gMonth - 14)/12))/100)))/4) + gDay - 32075;

        }else{
            jd = 367 * gYear - intPart((7 * (gYear + 5001 +
                    intPart((gMonth - 9)/7)))/4) + intPart((275 * gMonth)/9)
                    + gDay + 1729777;

                     

        }
        Map<String,Integer> result = new HashMap<String, Integer>();
        l = jd -1948440 + 10632;
        n = intPart((l - 1)/10631);
        l = l - 10631 * n + 354;

        j = (intPart((10985 - l)/5316)) *
        (intPart((50 * l)/17719)) + (intPart(l/5670)) *
        (intPart((43 * l)/15238));

        l = l - (intPart((30 - j)/15)) *
        (intPart((17719 * j)/50)) - (intPart(j/16)) *
        (intPart((15238 * j)/43)) + 29;

        m = intPart((24 * l)/709);
        d = l - intPart((709 * m)/24);
        y = 30 * n + j - 30;

        result.put(DAY,d);
        result.put(MONTH,m);
        result.put(YEAR, y);
        result.put(JULIAN_DATE,jd);
        return result;
    }
    public static final String JULIAN_DATE = "Julian";
    public static final String MONTH ="Month";
    public static final String YEAR="Year";
    public static final String DAY = "day";
    public static final String DAY_OF_WEEK = "DayOfWeek";
    private static int intPart(double num){
        if (num < -0.0000001) return new Double(Math.ceil(num- 0.0000001)).intValue();
        else return new Double(Math.floor(num+0.0000001)).intValue();
    }

}
