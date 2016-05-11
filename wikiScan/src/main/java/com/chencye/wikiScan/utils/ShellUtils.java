package com.chencye.wikiScan.utils;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public final class ShellUtils {
    public static void execute(String shell) {
        try {
            Runtime rt = Runtime.getRuntime();
            rt.exec(shell);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static List<String> runShell(String shell) throws Exception {
        List<String> strList = new ArrayList<String>();
        
        Process process;
        process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", shell }, null, null);
        InputStreamReader ir = new InputStreamReader(process.getInputStream());
        LineNumberReader input = new LineNumberReader(ir);
        String line;
        process.waitFor();
        while ((line = input.readLine()) != null) {
            strList.add(line);
        }
        
        return strList;
    }
    
}
