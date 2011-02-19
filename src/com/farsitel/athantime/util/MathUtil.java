package com.farsitel.athantime.util;


public class MathUtil {
    // ---------------------- Trigonometric Functions -----------------------

    // degree sin
    public static double dSin(double degree) {
        return Math.sin(Math.toRadians(degree));
    }

    // degree cos
    public  static double dCos(double degree) {
        return Math.cos(Math.toRadians(degree));
    }

    // degree tan
    public  static double dTan(double degree) {
        return Math.tan(Math.toRadians(degree));
    }

    // degree arcsin
    public  static double dArcSin(double x) {
        return Math.toDegrees(Math.asin(x));
    }

    // degree arccos
    public  static double dArcCos(double x) {
        return Math.toDegrees(Math.acos(x));
    }

    // degree arctan
    public  static double dArcTan(double x) {
        return Math.toDegrees(Math.atan(x));
    }

    // degree arctan2
    public  static double dArcTan2(double y, double x) {
        return Math.toDegrees(Math.atan2(y, x));
    }

    // degree arccot
    public static double dArcCot(double x) {
        return Math.toDegrees(Math.atan(1 / x));
    }

    // range reduce angle in degrees.
    public static double fixAngle(double angle) {
        angle = angle - 360.0d * (Math.floor(angle / 360.0d));
        angle = (angle < 0) ? angle + 360.0d : angle;
        return angle;
    }

    // range reduce hours to 0..23
    public static double fixHour(double hour) {
        hour = hour - 24.0d * (Math.floor(hour / 24.0d));
        hour = (hour < 0) ? hour + 24.0d : hour;
        return hour;
    }

    // ---------------------- Misc Functions -----------------------

    // compute the difference between two times
    public static double timeDiff(double time1, double time2) {
        return fixHour(time2 - time1);
    }

    // add a leading 0 if necessary
    public static String twoDigitsFormat(double num) {
        return (num < 10) ? "0" + num : "" + num;
    }
}
