package com.chencye.wikiScan.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public final class FileUtils {
    
    public static String mergePath(String... paths) {
        StringBuilder sb = new StringBuilder();
        if (ArrayUtils.isNotEmpty(paths)) {
            for (int i = 0, j = paths.length; i < j; i++) {
                String path = paths[i];
                if (StringUtils.isNotBlank(path)) {
                    if (sb.length() == 0) {
                        sb.append(path);
                        continue;
                    }
                    if (sb.lastIndexOf(File.separator) != sb.length() - 1) {
                        sb.append(File.separator);
                    }
                    path = StringUtils.trim(path);
                    path = path.startsWith(File.separator) ? path.substring(path.indexOf(File.separator) + 1) : path;
                    sb.append(path);
                }
            }
        }
        return sb.toString();
    }
    
    public static String[] generatePaths(String path, String[] subPaths) {
        subPaths = subPaths == null ? new String[0] : subPaths;
        for (int i = 0, j = subPaths.length; i < j; i++) {
            subPaths[i] = FileUtils.mergePath(path, subPaths[i]);
        }
        return subPaths;
    }
    
    public static void sortFile(List<File> files) {
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                long lastModified1 = o1.lastModified();
                long lastModified2 = o2.lastModified();
                return lastModified1 == lastModified2 ? 0 : lastModified1 > lastModified2 ? -1 : 1;
            }
        });
    }
    
    public static List<File> loadFile(String path, String[] excludeDirs, String[] excludeFiles) {
        List<File> files = new ArrayList<File>();
        
        File rootFile = new File(path);
        if (rootFile.exists()) {
            final String[] excludeDirPaths = generatePaths(path, excludeDirs);
            final String[] excludeFilePaths = generatePaths(path, excludeFiles);
            Stack<File> stack = new Stack<File>();
            stack.push(rootFile);
            while (!stack.isEmpty()) {
                
                File file = stack.pop();
                
                if (file.isDirectory()) {
                    
                    File[] subFiles = file.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            boolean isAcceptable = true;
                            if (excludeDirs != null) {
                                for (String excludeDir : excludeDirPaths) {
                                    if (pathname.getAbsolutePath().contains(excludeDir)) {
                                        isAcceptable = false;
                                        break;
                                    }
                                }
                            }
                            return isAcceptable;
                        }
                    });
                    
                    for (File subFile : subFiles) {
                        stack.push(subFile);
                    }
                    
                } else if (file.isFile()) {
                    
                    boolean isAcceptable = true;
                    
                    if (excludeFiles != null) {
                        for (String excludeFile : excludeFilePaths) {
                            if (file.getAbsolutePath().contains(excludeFile)) {
                                isAcceptable = false;
                                break;
                            }
                        }
                    }
                    
                    if (isAcceptable) {
                        files.add(file);
                    }
                    
                } else {
                    throw new RuntimeException("unkown file: " + file.getAbsolutePath());
                }
            }
        }
        return files;
    }
    
}
