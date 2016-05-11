package com.chencye.wikiScan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.chencye.wikiScan.bean.Wiki;
import com.chencye.wikiScan.config.Config;
import com.chencye.wikiScan.utils.EncodeUtils;
import com.chencye.wikiScan.utils.FileUtils;
import com.chencye.wikiScan.utils.IOUtils;
import com.chencye.wikiScan.utils.ShellUtils;

public class WikiScan {
    public static void main(String[] args) {
        new WikiScan().scan();
    }
    
    public void scan() {
        clear();
        List<File> files = FileUtils.loadFile(Config.PATH_WIKI, Config.EXCLUDE_DIRS, Config.EXCLUDE_FILES);
        FileUtils.sortFile(files);
        List<Wiki> wikis = WikiUtils.read(files);
        generate(wikis);
        commit();
    }
    
    private void clear() {
        ShellUtils.execute(Config.COMMAN_CLEAR);
    }
    
    private void commit() {
        ShellUtils.execute(Config.COMMAN_COMMIT);
    }
    
    private void generate(List<Wiki> wikis) {
        generateLatestModified(wikis);
        
        Map<String, Integer> tagNumMap = new TreeMap<String, Integer>();
        Map<String, List<Wiki>> tagWikiMap = new HashMap<String, List<Wiki>>();
        for (Wiki wiki : wikis) {
            String[] tags = wiki.getTags();
            if (ArrayUtils.isNotEmpty(tags)) {
                for (String tag : tags) {
                    Integer num = tagNumMap.get(tag);
                    num = num == null ? 1 : num++;
                    tagNumMap.put(tag, num);
                    
                    List<Wiki> tagWikis = tagWikiMap.get(tag);
                    tagWikis = tagWikis == null ? new ArrayList<Wiki>() : tagWikis;
                    tagWikis.add(wiki);
                    tagWikiMap.put(tag, tagWikis);
                }
            }
        }
        
        generateTagsList(tagNumMap);
        generateTagWikiList(tagWikiMap);
    }
    
    private void generateLatestModified(List<Wiki> wikis) {
        BufferedWriter bw = null;
        try {
            File targetFile = new File(FileUtils.mergePath(Config.PATH_WIKI, Config.LATEST_WIKI_LIST_FILE_PATH));
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile, true), Config.ENCODING_WIKI_FILE));
            bw.write(Config.FIXED_CONTENT_LATEST_FILE);
            bw.newLine();
            bw.newLine();
            bw.flush();
            
            int latestModifiedFileNum = Math.min(Config.MAX_LATEST_MODIFIED_FILE_NUM, wikis.size());
            int lineCount = 0;
            for (int i = 0; i < latestModifiedFileNum; i++) {
                Wiki wiki = wikis.get(i);
                bw.write("* [" + wiki.getTitle() + "](" + wiki.getUrl() + ")");
                bw.newLine();
                
                if (++lineCount == Config.COUNT_SPLIT_FOR_LIST) {
                    bw.newLine();
                    bw.write(Config.SPLIT_LINE_FOR_LIST);
                    bw.newLine();
                    bw.newLine();
                    lineCount = 0;
                }
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(bw);
        }
    }
    
    private void generateTagsList(Map<String, Integer> tagMap) {
        BufferedWriter bw = null;
        try {
            File targetFile = new File(FileUtils.mergePath(Config.PATH_WIKI, Config.TAGS_LIST_FILE_PATH));
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile, true), Config.ENCODING_WIKI_FILE));
            bw.write(Config.FIXED_CONTENT_TAGS_LIST_FILE);
            bw.newLine();
            bw.newLine();
            bw.flush();
            
            for (Entry<String, Integer> entry : tagMap.entrySet()) {
                String tag = entry.getKey();
                String tagsListUrl = FileUtils.mergePath(File.separator, Config.PATH_TAG_WIKI_LIST, EncodeUtils.toUnicode(tag));
                int num = entry.getValue();
                bw.write("* [" + tag + "](" + tagsListUrl + ") (" + num + ")");
                bw.newLine();
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(bw);
        }
    }
    
    private void generateTagWikiList(Map<String, List<Wiki>> tagWikiMap) {
        for (Entry<String, List<Wiki>> entry : tagWikiMap.entrySet()) {
            BufferedWriter bw = null;
            try {
                String tag = entry.getKey();
                String path = FileUtils.mergePath(Config.PATH_WIKI, Config.PATH_TAG_WIKI_LIST, EncodeUtils.toUnicode(tag));
                File targetFile = new File(path);
                if (!targetFile.isFile()) {
                    targetFile.createNewFile();
                }
                
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile, true), Config.ENCODING_WIKI_FILE));
                List<Wiki> tagWikis = entry.getValue();
                for (Wiki wiki : tagWikis) {
                    bw.write("* [" + wiki.getTitle() + "](" + wiki.getUrl() + ")");
                    bw.newLine();
                    bw.flush();
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(bw);
            }
        }
    }
    
}
