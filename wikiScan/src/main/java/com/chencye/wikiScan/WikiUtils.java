package com.chencye.wikiScan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.chencye.wikiScan.bean.Wiki;
import com.chencye.wikiScan.config.Config;
import com.chencye.wikiScan.log.Log;
import com.chencye.wikiScan.log.LogFactory;
import com.chencye.wikiScan.utils.DateUtils;
import com.chencye.wikiScan.utils.FileUtils;
import com.chencye.wikiScan.utils.IOUtils;

public class WikiUtils {
    public static Log log = LogFactory.getLog(WikiUtils.class);
    
    public static List<Wiki> read(List<File> files) {
        List<Wiki> wikis = new ArrayList<Wiki>();
        for (File file : files) {
            String url = generateWikiUrl(file);
            Wiki wiki = new Wiki(url, DateUtils.format(file.lastModified(), Config.DATE_FORMAT_WIKI));
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Config.ENCODING_WIKI_FILE));
                int step = 0;
                String line = null;
                while ((line = br.readLine()) != null) {
                    Matcher titleMatcher = Config.PATTERN_TITLE.matcher(line);
                    if (titleMatcher.find()) {
                        wiki.setTitle(titleMatcher.group(1));
                        step++;
                    }
                    Matcher tagsMatcher = Config.PATTERN_TAGS.matcher(line);
                    if (tagsMatcher.find()) {
                        wiki.setTags(tagsMatcher.group(1).split(Config.REGEX_SPLIT_TAGS));
                        step++;
                    }
                    if (step == 2) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(br);
            }
            
            wikis.add(wiki);
        }
        return wikis;
    }
    
    private static String generateWikiUrl(File file) {
        String absolutePath = file.getAbsolutePath();
        log.info("absolutePath={}, wikiPath={}, suffix={}", absolutePath, Config.PATH_WIKI, Config.SUFFIX_WIKI_FILE);
        String url = absolutePath.substring(Config.PATH_WIKI.length());
        if (url.endsWith(Config.SUFFIX_WIKI_FILE)) {
            url = url.substring(0, url.indexOf(Config.SUFFIX_WIKI_FILE));
        }
        url = FileUtils.mergePath(File.separator, url);
        return url;
    }
    
}
