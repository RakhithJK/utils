package com.chencye.wikiScan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.chencye.wikiScan.bean.Wiki;
import com.chencye.wikiScan.config.Config;
import com.chencye.wikiScan.log.Log;
import com.chencye.wikiScan.log.LogFactory;
import com.chencye.wikiScan.utils.EncodeUtils;
import com.chencye.wikiScan.utils.FileUtils;
import com.chencye.wikiScan.utils.IOUtils;
import com.chencye.wikiScan.utils.ShellUtils;

public class WikiScan {
    public static Log log = LogFactory.getLog(WikiScan.class);
    
    public static void main(String[] args) {
        new WikiScan().scan();
    }
    
    public void scan() {
        log.info("wiki路径：{}", Config.PATH_WIKI);
        log.info("忽略的路径：{}", (Object) Config.EXCLUDE_DIRS);
        log.info("忽略的文件：{}", (Object) Config.EXCLUDE_FILES);
        log.newLine();
        readFixedContent();
        log.newLine();
        clear();
        log.newLine();
        writeFixedContent();
        log.newLine();
        log.info("开始扫描...");
        List<File> files = FileUtils.loadFile(Config.PATH_WIKI, Config.EXCLUDE_DIRS, Config.EXCLUDE_FILES);
        log.info("扫描结束。文件数量：{}", files.size());
        FileUtils.sortFile(files);
        log.info("文件排序完毕。");
        List<Wiki> wikis = WikiUtils.read(files);
        log.info("转换为wiki对象完毕。size={}", wikis.size());
        log.newLine();
        generate(wikis);
        commit();
    }
    
    private Map<String, List<String>> fixedMap = new HashMap<String, List<String>>();
    
    private void readFixedContent() {
        log.info("开始读取固定内容...");
        String[] paths = FileUtils.generatePaths(Config.PATH_WIKI, Config.WIKIS_HAS_FIXED_CONTENT);
        for (String path : paths) {
            Stack<File> stack = new Stack<File>();
            stack.add(new File(path));
            while (!stack.isEmpty()) {
                File file = stack.pop();
                if (file.isFile()) {
                    List<String> lines = fixedMap.get(path);
                    lines = lines == null ? new ArrayList<String>() : lines;
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            if (Config.PATTERN_BORDER_FIXED_CONTENT.matcher(line).matches()) {
                                break;
                            }
                            lines.add(line);
                            log.info("从文件{}中读取到固定内容：{}", path, line);
                        }
                        lines.add(Config.BORDER_FIXED_CONTENT);
                        fixedMap.put(path, lines);
                        log.newLine();
                    } catch (IOException e) {
                        log.info("读取固定内容出现异常。", e);
                    } finally {
                        IOUtils.close(br);
                    }
                } else if (file.isDirectory()) {
                    for (File subFile : file.listFiles()) {
                        stack.add(subFile);
                    }
                } else {
                    log.info("在路径中没有找到文件。path={}", path);
                }
            }
        }
        log.info("读取固定内容完毕！");
    }
    
    private void writeFixedContent() {
        log.info("开始写入固定内容...");
        for (Entry<String, List<String>> entry : fixedMap.entrySet()) {
            BufferedWriter bw = null;
            try {
                String path = entry.getKey();
                List<String> fixedContents = entry.getValue();
                File file = new File(path);
                if (!file.exists()) {
                    file.createNewFile();
                }
                if (file.isFile() && !fixedContents.isEmpty()) {
                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), Config.ENCODING_WIKI_FILE));
                    for (String line : fixedContents) {
                        bw.write(line);
                        bw.newLine();
                        bw.newLine();
                        bw.flush();
                        log.info("向文件{}输入固定内容：{}", path, line);
                    }
                    log.newLine();
                }
            } catch (IOException e) {
                log.info("写入固定内容出现异常。", e);
            } finally {
                IOUtils.close(bw);
            }
        }
        log.info("写入固定内容完毕！");
    }
    
    private void clear() {
        System.out.println("  --->  执行清理操作");
        ShellUtils.execute(Config.COMMAN_CLEAR);
    }
    
    private void commit() {
        System.out.println("  --->  执行提交操作");
        ShellUtils.execute(Config.COMMAN_COMMIT);
    }
    
    private void generate(List<Wiki> wikis) {
        log.info("开始生成...");
        generateLatestModified(wikis);
        
        Map<String, Integer> tagNumMap = new TreeMap<String, Integer>();
        Map<String, List<Wiki>> tagWikiMap = new HashMap<String, List<Wiki>>();
        for (Wiki wiki : wikis) {
            String[] tags = wiki.getTags();
            if (ArrayUtils.isNotEmpty(tags)) {
                for (String tag : tags) {
                    Integer num = tagNumMap.get(tag);
                    num = num == null ? 1 : ++num;
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
        log.info("生成完毕！");
    }
    
    private void generateLatestModified(List<Wiki> wikis) {
        log.newLine();
        log.info("开始生成最新wiki链接...");
        BufferedWriter bw = null;
        try {
            String path = FileUtils.mergePath(Config.PATH_WIKI, Config.LATEST_WIKI_LIST_FILE_PATH);
            log.info("生成最新wiki链接到路径：{}", path);
            File targetFile = new File(path);
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile, true), Config.ENCODING_WIKI_FILE));
            
            int latestModifiedFileNum = Math.min(Config.MAX_LATEST_MODIFIED_FILE_NUM, wikis.size());
            int lineCount = 0;
            for (int i = 0; i < latestModifiedFileNum; i++) {
                Wiki wiki = wikis.get(i);
                String link = "* [" + wiki.getTitle() + "](" + wiki.getUrl() + ") -- " + wiki.getLastModified();
                log.info("最新wiki链接：{}", link);
                bw.write(link);
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
        log.info("生成最新wiki链接完毕！");
        log.newLine();
    }
    
    private void generateTagsList(Map<String, Integer> tagMap) {
        log.newLine();
        log.info("开始生成标签列表...");
        BufferedWriter bw = null;
        try {
            String path = FileUtils.mergePath(Config.PATH_WIKI, Config.TAGS_LIST_FILE_PATH);
            log.info("生成标签列表到路径：{}", path);
            File targetFile = new File(path);
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile, true), Config.ENCODING_WIKI_FILE));
            
            for (Entry<String, Integer> entry : tagMap.entrySet()) {
                String tag = entry.getKey();
                String tagsListUrl = FileUtils.mergePath(File.separator, Config.PATH_TAG_WIKI_LIST, EncodeUtils.toUnicode(tag));
                int num = entry.getValue();
                String link = "* [" + tag + "](" + tagsListUrl + ") (" + num + ")";
                log.info("生成的标签链接：{}", link);
                bw.write(link);
                bw.newLine();
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(bw);
        }
        log.info("生成标签列表完毕！");
    }
    
    private void generateTagWikiList(Map<String, List<Wiki>> tagWikiMap) {
        log.newLine();
        log.info("开始生成每个标签的wiki链接列表...");
        for (Entry<String, List<Wiki>> entry : tagWikiMap.entrySet()) {
            BufferedWriter bw = null;
            try {
                String tag = entry.getKey();
                String path = FileUtils.mergePath(Config.PATH_WIKI, Config.PATH_TAG_WIKI_LIST, EncodeUtils.toUnicode(tag)) + Config.SUFFIX_WIKI_FILE;
                log.info("输出标签为【{}】的wiki链接到文件：{}", tag, path);
                File targetFile = new File(path);
                if (!targetFile.isFile()) {
                    targetFile.createNewFile();
                }
                
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile, true), Config.ENCODING_WIKI_FILE));
                // 先输出标题
                bw.write("<!-- --- title : 标签列表：" + tag + " -->");
                int lineCount = 0;
                List<Wiki> tagWikis = entry.getValue();
                for (Wiki wiki : tagWikis) {
                    String link = "* [" + wiki.getTitle() + "](" + wiki.getUrl() + ") -- " + wiki.getLastModified();
                    log.info("wiki链接：{}", link);
                    bw.write(link);
                    bw.newLine();
                    bw.flush();
                    
                    if (++lineCount == Config.COUNT_SPLIT_FOR_LIST) {
                        bw.newLine();
                        bw.write(Config.SPLIT_LINE_FOR_LIST);
                        bw.newLine();
                        bw.newLine();
                        lineCount = 0;
                    }
                }
                log.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(bw);
            }
        }
        log.info("生成每个标签的wiki链接列表完毕！");
    }
    
}
