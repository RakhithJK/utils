package com.chencye.wikiScan.log;

public interface Log {
    public void newLine();
    
    public void info(String msg);
    
    public void info(String msg, Object... params);
    
    public void info(String msg, Throwable e);
}
