package com.chencye.wikiScan.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.chencye.wikiScan.utils.FileUtils;

public class Config {
    private static final String CONFIG_FILE_PATH = "conf/config.properties";
    private static final String CONFIG_FILE_ENCODING = "UTF-8";
    private static final String REGEX_SPLIT_PROP = "=";
    private static final String PREFIX_COMMONT = "#";
    
    public static final String COMMAN_CLEAR;
    public static final String COMMAN_COMMIT;
    
    public static final String ENCODING_WIKI_FILE;
    
    public static final String PATH_WIKI;
    public static final String SUFFIX_WIKI_FILE;
    
    public static final String REGEX_SPLIT_ARRAY;
    public static final String[] EXCLUDE_DIRS;
    public static final String[] EXCLUDE_FILES;
    
    public static final Pattern PATTERN_TITLE;
    public static final Pattern PATTERN_TAGS;
    
    public static final String REGEX_SPLIT_TAGS;
    
    public static final String LATEST_WIKI_LIST_FILE_PATH;
    public static final int MAX_LATEST_MODIFIED_FILE_NUM;
    public static final int COUNT_SPLIT_FOR_LIST;
    public static final String SPLIT_LINE_FOR_LIST;
    public static final String FIXED_CONTENT_LATEST_FILE;
    
    public static final String TAGS_LIST_FILE_PATH;
    public static final String FIXED_CONTENT_TAGS_LIST_FILE;
    public static final String PATH_TAG_WIKI_LIST;
    
    static {
        init();
        
        COMMAN_CLEAR = get("comman_clear", "/work/wiki/scan/comman.sh clear");
        COMMAN_COMMIT = get("comman_commit", "/work/wiki/scan/comman.sh commit");
        
        ENCODING_WIKI_FILE = get("encoding_wiki_file", "UTF-8");
        
        PATH_WIKI = get("path_wiki", "/work/wiki");
        SUFFIX_WIKI_FILE = get("suffix_wiki_file", ".md");
        
        REGEX_SPLIT_ARRAY = get("regex_split_array", ",");
        EXCLUDE_DIRS = get("exclude_dirs", new String[] { ".git", "uploads", "pic", "scanProccess" });
        EXCLUDE_FILES = get("exclude_files", new String[] { "custom.css", "Home.md", "_Sidebar.md", "_Footer.md", "restart.sh", "stop.sh" });
        
        PATTERN_TITLE = Pattern.compile(get("pattern_title", "^\\s*+<!--\\s++---\\s*+title\\s*+:\\s*+(.*?)\\s*-->\\s*+$"));
        PATTERN_TAGS = Pattern.compile(get("pattern_tags", "^\\s*+<!--\\s++---\\s*+tags\\s*+:\\s*+(.*?)\\s*-->\\s*+$"));
        
        REGEX_SPLIT_TAGS = get("regex_split_tags", "\\s+");
        
        LATEST_WIKI_LIST_FILE_PATH = get("latest_wiki_list_file_path", "Home.md");
        MAX_LATEST_MODIFIED_FILE_NUM = get("max_latest_modified_file_num", 50);
        COUNT_SPLIT_FOR_LIST = get("count_split_for_list", 10);
        SPLIT_LINE_FOR_LIST = get("split_line_for_list", "***");
        FIXED_CONTENT_LATEST_FILE = get("fixed_content_latest_file", "");
        
        TAGS_LIST_FILE_PATH = get("tags_list_file_path", "_Sidebar.md");
        FIXED_CONTENT_TAGS_LIST_FILE = get("fixed_content_tags_list_file", "");
        PATH_TAG_WIKI_LIST = get("path_tag_wiki_list", "/tags/");
    }
    
    public static String get(String key, String defaultValue) {
        String value = confMap.get(key);
        return value == null ? defaultValue : value;
    }
    
    public static int get(String key, int defaultValue) {
        String value = confMap.get(key);
        return value == null || !NumberUtils.isDigits(value) ? defaultValue : Integer.valueOf(value);
    }
    
    public static String[] get(String key, String[] defaultValue) {
        String value = confMap.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        String[] array = value.split(REGEX_SPLIT_ARRAY);
        for (int i = 0, j = array.length; i < j; i++) {
            array[i] = StringUtils.trim(array[i]);
        }
        return array;
    }
    
    private static final Map<String, String> confMap = new HashMap<String, String>();
    
    private static void init() {
        File file = new File(FileUtils.mergePath(System.getProperty("user.dir"), CONFIG_FILE_PATH));
        if (file.isFile()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), CONFIG_FILE_ENCODING));
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (StringUtils.isBlank(line) || line.startsWith(PREFIX_COMMONT)) {
                        continue;
                    }
                    String[] props = line.split(REGEX_SPLIT_PROP);
                    if (props.length > 1) {
                        String key = StringUtils.trim(props[0]);
                        String value = StringUtils.trimToNull(props[1]);
                        confMap.put(key, value);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
