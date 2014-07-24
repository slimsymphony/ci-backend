/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

/**
 *
 * @author hhellgre
 */
public class MathUtil {

    public static float getPercentage(float f1, float f2) {
        if (f2 <= 0f) {
            return 0f;
        }
        return (f1 / f2) * 100;
    }

    public static float getPercentage(int i1, int i2) {
        return getPercentage((float) i1, (float) i2);
    }

    public static int getPercentageInt(float f1, float f2) {
        float p = getPercentage(f1, f2);
        return Math.round(p);
    }

    public static int getPercentageInt(int i1, int i2) {
        float p = getPercentage(i1, i2);
        return Math.round(p);
    }
}
