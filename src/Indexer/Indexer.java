package Indexer;

import Files.CorpusDocument;
import Files.Configurations;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Indexer {

    public Map<String, int[]> termsDictionary;
    public Map<String, Integer> docsDictionary;
    private static final Object lock = new Object();
    private static Integer postingIndex = 0;
    private static Integer docLineNumber = 1;
    private int index;
    protected static String isStem;
    private static String postingFilePath = Configurations.getPostingsFilePath();


    /**
     * in order you want to create the dictionary from the posting file
     */
    public Indexer() {
        docsDictionary = new LinkedHashMap<>();
        termsDictionary = new LinkedHashMap<>();
        isStem = Configurations.getStemmingProp() == false ? "WithoutStem" : "Stem";
        createPostingsDir();

        synchronized (postingIndex) {
            ++postingIndex;
            index = postingIndex;
        }
    }

    public void startIndexer(Map<String, LinkedHashMap<CorpusDocument, Integer>> termList, Map<String, LinkedHashMap<CorpusDocument, Integer>> entitiesList, LinkedHashMap<CorpusDocument, LinkedHashMap<String, Integer>> docsMap) {

        if (Files.exists(Paths.get(postingFilePath + "\\Postings"))) {
            createDocPostingFile(docsMap);
            createPostingFileTerms(termList);
            createPostingFileEntities(entitiesList);
        } else
            System.err.println("No posting files");

    }

    private void createPostingFileTerms(Map<String, LinkedHashMap<CorpusDocument, Integer>> termHashMap) {
        createPostingFile(termHashMap, "Terms");
    }

    private void createPostingFileEntities(Map<String, LinkedHashMap<CorpusDocument, Integer>> entitiesMap) {
        createPostingFile(entitiesMap, "Entity");
    }

    private void createPostingFile(Map<String, LinkedHashMap<CorpusDocument, Integer>> termHashMap, String fileType) {
        File postingFile = new File(postingFilePath + "\\Postings\\" + isStem + "\\" + fileType + index + ".txt");
        String termEntry = "";
        BufferedWriter fileWriter;
        try {
            if (postingFile.exists())
                fileWriter = new BufferedWriter(new FileWriter(postingFile.getAbsoluteFile(), true));
            else
                fileWriter = new BufferedWriter(new FileWriter(postingFile));
            for (Map.Entry<String, LinkedHashMap<CorpusDocument, Integer>> term : termHashMap.entrySet()) {
                for (Map.Entry<CorpusDocument, Integer> document : term.getValue().entrySet()) {
                    if (termEntry.length() > 0)
                        termEntry += "?";
                    termEntry += docsDictionary.get(document.getKey().getDocNO()) + "?" + document.getValue(); // documents line number + document frequency;
                }
                termEntry = term.getKey() + "#" + termEntry;
                fileWriter.write(termEntry);
                fileWriter.write(System.getProperty("line.separator"));
                termEntry = "";
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDocPostingFile(HashMap<CorpusDocument, LinkedHashMap<String, Integer>> temp) {
        String docInfo = "";
        File docsFile = new File(postingFilePath + "\\Postings\\Docs\\docs.txt");
        try {
            synchronized (lock) {
                BufferedWriter docFileWriter = new BufferedWriter(new FileWriter(docsFile, true));
                for (Map.Entry<CorpusDocument, LinkedHashMap<String, Integer>> doc : temp.entrySet()) {
                    docInfo = doc.getKey().getDocNO() + "#" + doc.getValue().entrySet().stream().max(Map.Entry.comparingByValue()).get().getValue() + "?" + doc.getValue().size() + "?" + doc.getKey().getTitle();
                    docsDictionary.put(doc.getKey().getDocNO(), docLineNumber);
                    ++docLineNumber;
                    docFileWriter.write(docInfo);
                    docFileWriter.write(System.getProperty("line.separator"));
                    docInfo = "";
                }
                docFileWriter.flush();
                docFileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void buildDictionaryFromPosting() {
        try {
            File postingFileDirectory = new File(postingFilePath + "\\Postings\\" + isStem); // only one file the full posting file
            if (postingFileDirectory.exists() && postingFileDirectory.listFiles().length > 0) {
                postingFileDirectory = postingFileDirectory.listFiles()[0];
                BufferedReader brPostingFile = new BufferedReader(new FileReader(postingFileDirectory));
                String line;
                String[] termContent;
                String[] docContent;
                int[] termInfo; //first - doc Frequency, Second - line number in posting file, Third - total term frequency in corpus
                int lineNumber = 0;
                while ((line = brPostingFile.readLine()) != null) {
                    termInfo = new int[3];
                    termContent = line.split("#");
                    termInfo[0] = (termContent[1].split("\\?").length) / 2;
                    termInfo[1] = lineNumber;
                    docContent = termContent[1].split("\\?");
                    for (int i = 1; i < docContent.length; i = i + 2) {
                        termInfo[2] += Integer.parseInt(docContent[i]);
                    }
                    termsDictionary.put(termContent[0], termInfo);
                    ++lineNumber;
                }
                brPostingFile.close();
            } else {
                throw new Exception("PostingFile doesn't exists! Please be sure you have created the inverted file, and try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void buildDocsFromPosting() {
        try {
            File postingFileDirectory = new File(postingFilePath + "\\Postings\\Docs"); // only one file the full posting file
            if (postingFileDirectory.exists() && postingFileDirectory.listFiles().length > 0) {
                postingFileDirectory = postingFileDirectory.listFiles()[0];
                BufferedReader brPostingFile = new BufferedReader(new FileReader(postingFileDirectory));
                String line;
                int lineNumber = 1;
                while ((line = brPostingFile.readLine()) != null) {
                    docsDictionary.put(line.split("#")[0], lineNumber);
                    ++lineNumber;
                }
                brPostingFile.close();
            } else {
                throw new Exception("PostingFile doesn't exists! Please be sure you have created the inverted file, and try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPostingsDir() {
        if (!Files.exists(Paths.get(postingFilePath + "\\Postings"))) {
            File postingFileDir = new File(postingFilePath + "\\Postings");
            boolean created = postingFileDir.mkdir();
            if (created) {
                System.out.println("Postings Directory created successfully");
            }
            File stemDir = new File(postingFilePath + "\\Postings\\Stem");
            created = stemDir.mkdir();
            if (created) {
                System.out.println("Stem Directory created successfully");
            }
            File withoutStemDir = new File(postingFilePath + "\\Postings\\WithoutStem");
            created = withoutStemDir.mkdir();
            if (created) {
                System.out.println("WithoutStem Directory created successfully");
            }
            File docsDir = new File(postingFilePath + "\\Postings\\Docs");
            created = docsDir.mkdir();
            if (created) {
                System.out.println("Docs Directory created successfully");
            }
        }
    }

    public Map<String, int[]> getTermsDictionary() {
        return termsDictionary;
    }

    public Map<String, Integer> getDocsDictionary() {
        return docsDictionary;
    }

    public void resetIndexer(){
        termsDictionary.clear();
        docsDictionary.clear();
        postingIndex = 0;
        docLineNumber = 1;
        postingFilePath = Configurations.getPostingsFilePath();
    }

}