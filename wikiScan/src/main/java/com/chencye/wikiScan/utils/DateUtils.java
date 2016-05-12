package com.chencye.wikiScan.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtils {
    public static String format(long time, String format) {
        return new SimpleDateFormat(format).format(new Date(time));
    }
}
