package com.chencye.wikiScan.utils;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public final class EncodeUtils {
    public static final Pattern PATTERN_NAME_EN = Pattern.compile("^[-A-Za-z_0-9]++$");
    
    public static String toUnicode(String str) {
        if (StringUtils.isNotBlank(str) && !PATTERN_NAME_EN.matcher(str).matches()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, j = str.length(); i < j; i++) {
                sb.append(Integer.toHexString(str.charAt(i)));
            }
            str = sb.toString();
        }
        return str;
    }
}
