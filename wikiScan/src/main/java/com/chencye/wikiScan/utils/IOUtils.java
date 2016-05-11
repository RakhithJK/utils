package com.chencye.wikiScan.utils;

import java.io.Closeable;
import java.io.IOException;

public final class IOUtils {
    
    public static void close(Closeable source) {
        if (source != null) {
            try {
                source.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                source = null;
            }
        }
    }
    
}
