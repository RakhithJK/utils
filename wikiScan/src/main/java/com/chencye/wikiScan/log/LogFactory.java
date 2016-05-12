package com.chencye.wikiScan.log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LogFactory {
    
    private static Map<String, Log> map = new HashMap<String, Log>();
    private static Lock lock = new ReentrantLock();
    
    public static Log getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }
    
    public static Log getLog(String name) {
        lock.lock();
        Log log = map.get(name);
        if (log == null) {
            log = new LogImpl();
            map.put(name, log);
        }
        lock.unlock();
        return log;
    }
}
