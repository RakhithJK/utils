package com.chencye.wikiScan.log;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

public class LogImpl implements Log {
    public void newLine() {
        info("");
    }
    
    public void info(String msg) {
        System.out.println("  --->  " + msg);
    }
    
    public void info(String msg, Object... params) {
        System.out.println("  --->  " + mergeMsg(msg, params));
    }
    
    public void info(String msg, Throwable e) {
        System.out.println("  --->  " + msg);
        e.printStackTrace();
    }
    
    public String mergeMsg(String msg, Object... params) {
        msg = String.valueOf(msg);
        if (ArrayUtils.isNotEmpty(params)) {
            for (Object param : params) {
                if (param != null) {
                    String value = null;
                    if (param instanceof Object[]) {
                        value = Arrays.asList((Object[]) param).toString();
                    } else {
                        value = String.valueOf(param);
                    }
                    msg = msg.replaceFirst("\\{\\}", value);
                }
            }
        }
        return msg;
    }
}
