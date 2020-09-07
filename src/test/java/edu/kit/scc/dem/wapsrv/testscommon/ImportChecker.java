package edu.kit.scc.dem.wapsrv.testscommon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints a list of all imports to help find unwanted dependencies
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
public class ImportChecker {

    private static final Logger logger = LoggerFactory.getLogger(ImportChecker.class);

    private final Map<String, Integer> import2count = new Hashtable<String, Integer>();

    /**
     * @param args ignored
     */
    public static void main(String[] args) {
        new ImportChecker().scan();
    }

    private void scan() {
        List<File> allFiles = new Vector<File>();
        listFiles(new File("src/main/java"), allFiles);
        for (File srcFile : allFiles) {
            processSourceFile(srcFile);
        }
        List<String> keys = new Vector<String>();
        for (String key : import2count.keySet()) {
            keys.add(key);
        }
        java.util.Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        for (String key : keys) {
            int count = import2count.get(key);
            logger.trace(count + " : " + key);
        }
        logger.trace("\n################################################\n");
        java.util.Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return import2count.get(o1).compareTo(import2count.get(o2));
            }
        });
        for (String key : keys) {
            int count = import2count.get(key);
            logger.trace(count + " : " + key);
        }
    }

    private void processSourceFile(File srcFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(srcFile));
            String line = reader.readLine();
            while (line != null) { // org.apache.commons.lang3.NotImplementedException
                line = line.trim();
                if (line.startsWith("import")) {
                    line = line.substring("import".length());
                    line = line.trim();
                    if (line.startsWith("static")) {
                        line = line.substring("static".length());
                        line = line.trim();
                    }
                    // remove .*;
                    line = line.replaceAll(Pattern.quote(".*;"), "");
                    line = line.replaceAll(Pattern.quote(";"), "");
                    line = line.trim();
                    Integer count = import2count.get(line);
                    if (count == null) {
                        count = 0;
                    }
                    count++;
                    import2count.put(line, count);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listFiles(File folder, List<File> allFiles) {
        if (folder == null || !folder.canRead()) {
            return;
        }
        File[] files = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".java");
            }
        });
        for (File file : files) {
            if (file.isDirectory()) {
                listFiles(file, allFiles);
            } else {
                allFiles.add(file);
            }
        }
    }
}
