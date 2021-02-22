package IRSystem;

import Files.*;
import Indexer.*;
import Parse.Parse;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IRSystem {

    public static Indexer start() {
        startReset();
        long start = System.nanoTime();
        ReadFile r = new ReadFile(Configurations.getCorpusPath() + "\\corpus");
        List<CorpusDocument> l = r.getL_corpusDocuments();
        long end = System.nanoTime();
        double total = (end - start) / 1000000000.0;
        System.out.println("Dictionary " + total);
        int numOfProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(numOfProcessors);
        SystemThread thread;
        int i = 0;
        //load stop words
        Parse.createStopWordList(Configurations.getCorpusPath() + "\\stop_words.txt");
        Parse.setStemming();
        while (i + 47500 < l.size()) {
            thread = new SystemThread(l.subList(i, i + 47500));
            exec.execute(thread);
            i = i + 47500;
        }
        thread = new SystemThread(l.subList(i, l.size()));
        exec.execute(thread);
        exec.shutdown();
        try {
            exec.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        l.clear();
        r.clear();
        MergeFiles m = new MergeFiles();
        m.mergePostings();
        Indexer indexer = new Indexer();
        indexer.buildDocsFromPosting();
        indexer.buildDictionaryFromPosting();
        end = System.nanoTime();
        total = (end - start) / 1000000000.0;
        System.out.println(total);
        return indexer;
    }

    public static boolean close() {
        resetSystem();
        return true;
    }

    public static boolean resetSystem() {
        try {
            if (Files.exists(Paths.get(Configurations.getPostingsFilePath() + "\\Postings")))
                FileUtils.deleteDirectory(new File(Configurations.getPostingsFilePath() + "\\Postings"));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void startReset() {
        if (Files.exists(Paths.get(Configurations.getPostingsFilePath() + "\\Postings"))) {
            if (Configurations.getStemmingProp()) {
                resetStemFiles();
            } else {
                resetWithoutStemFiles();
            }
            resetDocs();
        }
    }

    private static void resetDocs() {
        try {
            if (Files.exists(Paths.get(Configurations.getPostingsFilePath() + "\\Postings\\Docs")))
                FileUtils.cleanDirectory(new File(Configurations.getPostingsFilePath() + "\\Postings\\Docs"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void resetStemFiles() {
        try {
            if (Files.exists(Paths.get(Configurations.getPostingsFilePath() + "\\Postings\\Stem")))
                FileUtils.cleanDirectory(new File(Configurations.getPostingsFilePath() + "\\Postings\\Stem"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void resetWithoutStemFiles() {
        try {
            if (Files.exists(Paths.get(Configurations.getPostingsFilePath() + "\\Postings\\WithoutStem")))
                FileUtils.cleanDirectory(new File(Configurations.getPostingsFilePath() + "\\Postings\\WithoutStem"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
