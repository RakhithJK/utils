package com.chencye.wikiScan.utils;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import com.chencye.wikiScan.config.Config;
import com.chencye.wikiScan.log.Log;
import com.chencye.wikiScan.log.LogFactory;

public final class ShellUtils {
    public static Log log = LogFactory.getLog(ShellUtils.class);
    
    public static void execute(String shell) {
        log.newLine();
        LineNumberReader input = null;
        try {
            Process process = Runtime.getRuntime().exec(shell);
            input = new LineNumberReader(new InputStreamReader(process.getInputStream(), Config.ENCODING_SHELL));
            String line;
            process.waitFor();
            while ((line = input.readLine()) != null) {
                log.info(line);
            }
        } catch (Exception e) {
            log.info("执行shell脚本异常。", e);
        } finally {
            IOUtils.close(input);
        }
        log.newLine();
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
