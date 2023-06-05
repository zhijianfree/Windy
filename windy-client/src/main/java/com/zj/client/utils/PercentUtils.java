package com.zj.client.utils;

import java.text.DecimalFormat;

public class PercentUtils {
    private static DecimalFormat decimalFormat=new DecimalFormat(".00");

    public static int calculate(float number){
        String num = decimalFormat.format(number);
        return (int) (Float.parseFloat(num) * 100);
    }
}
