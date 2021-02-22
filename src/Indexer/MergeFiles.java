package Indexer;

import Files.Configurations;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MergeFiles {

    public static void mergePostings() {
        removePreviousFiles();
        long start = System.nanoTime();
        sortFiles();
        long end = System.nanoTime();
        double total = (end - start) / 1000000000.0;
        System.out.println("Sort: " + total);

        start = System.nanoTime();
        mergeEntities();
        end = System.nanoTime();
        total = (end - start) / 1000000000.0;
        System.out.println("Merge entities: " + total);

        start = System.nanoTime();
        mergeTerms();
        end = System.nanoTime();
        total = (end - start) / 1000000000.0;
        System.out.println("Merge terms: " + total);

        start = System.nanoTime();
        mergeTermsWithEntities();
        end = System.nanoTime();
        total = (end - start) / 1000000000.0;
        System.out.println("Merge Total: " + total);

    }

    private static void sortFiles() {
        File postings = new File(Configurations.getPostingsFilePath() + "\\Postings\\" + Indexer.isStem);
        BufferedWriter fwPostings;
        BufferedReader brPostings;
        ArrayList<String> fileToSort;
        String line, lineToWrite = "";
        String[] firstLine, secondLine;
        try {
            if (postings.exists()) {
                for (File file : postings.listFiles()) {
                    fileToSort = new ArrayList();
                    brPostings = new BufferedReader(new FileReader(file));
                    while ((line = brPostings.readLine()) != null) {
                        fileToSort.add(line);
                    }
                    brPostings.close();

                    List<String> sortedFile =  fileToSort.parallelStream().sorted(new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            return o1.split("#")[0].toLowerCase().compareTo(o2.split("#")[0].toLowerCase());
                        }
                    }).collect(Collectors.toList());

                    fwPostings = new BufferedWriter(new FileWriter(file));
                    for (int i = 0; i < sortedFile.size() - 1; i++) {
                        lineToWrite = sortedFile.get(i);
                        firstLine = lineToWrite.split("#");
                        secondLine = sortedFile.get(i + 1).split("#");
                        while (firstLine[0].toLowerCase().equals(secondLine[0].toLowerCase())) {
                            lineToWrite = mergeDocumentListOfTheSameTerm(lineToWrite.split("#"), secondLine);
                            if (i + 2 < sortedFile.size()) {
                                ++i;
                                secondLine = sortedFile.get(i + 1).split("#");
                            } else
                                break;
                        }
                        fwPostings.write(lineToWrite);
                        fwPostings.write(System.getProperty("line.separator"));
                        if (i + 2 == sortedFile.size() && !firstLine[0].equals(secondLine[0])) { // last line doesnt equals to the previous line
                            fwPostings.write(sortedFile.get(i + 1));
                            fwPostings.write(System.getProperty("line.separator"));
                        }
                    }
                    fwPostings.flush();
                    fwPostings.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void mergeEntities() {
        mergePostingFiles("Entity");
    }

    private static boolean isLegalTerm(String[] entityLineFromPosting) {
        if (entityLineFromPosting[0].contains(" ")) {
            return entityLineFromPosting[1].split("\\?").length > 2; // One Document = DocID and docFrequency (2 values), therefore need more than 2 boxes
        }
        return true;
    }

    private static void mergeTerms() {
        mergePostingFiles("Terms");
    }

    private static void mergeTermsWithEntities() {
        mergePostingFiles("");
    }

    private static void mergePostingFiles(String typeOfMerge) {
        File postingsDir = new File(Configurations.getPostingsFilePath() + "\\Postings\\" + Indexer.isStem);
        File[] postingsFiles = postingsDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (!name.equals("")) {
                    if (!typeOfMerge.equals("")) return name.startsWith(typeOfMerge);
                    else return name.contains("Terms") || name.contains("Entity");
                }
                return false;
            }
        }); // all the posting files
        if (postingsFiles.length > 1) {
            chooseFilesToReadAndWriteForPostingFiles(postingsFiles, typeOfMerge);
        }
    }

    private static void chooseFilesToReadAndWriteForPostingFiles(File[] postingsFiles, String typeOfMerge) {
        BufferedReader brFirstPosting, brSecondPosting; //reads the posting file
        File firstPostingFileMerged, secondPostingFileMerged; // the full Posting file - two in order to merge them with the rest. in the end only one file exists
        firstPostingFileMerged = secondPostingFileMerged = null;
        BufferedWriter tempFileWriter;
        int i;
        try {
            for (i = 1; i < postingsFiles.length; i++) {
                brFirstPosting = new BufferedReader(new FileReader(postingsFiles[i]));
                //merge all with the full posting file
                if (i > 1 && i % 2 == 0) {
                    brSecondPosting = new BufferedReader(new FileReader(firstPostingFileMerged));
                    if (secondPostingFileMerged == null)
                        secondPostingFileMerged = new File(Configurations.getPostingsFilePath() + "\\Postings\\" + Indexer.isStem + "\\fullPosting2" + typeOfMerge + ".txt");
                    tempFileWriter = new BufferedWriter(new FileWriter(secondPostingFileMerged));
                } else if (i > 1 && i % 2 == 1) {
                    brSecondPosting = new BufferedReader(new FileReader(secondPostingFileMerged));
                    tempFileWriter = new BufferedWriter(new FileWriter(firstPostingFileMerged));
                }
                //the first and second files still doesnt have the future full file
                else {
                    brSecondPosting = new BufferedReader(new FileReader(postingsFiles[i - 1]));
                    firstPostingFileMerged = new File(Configurations.getPostingsFilePath() + "\\Postings\\" + Indexer.isStem + "\\fullPosting1" + typeOfMerge + ".txt");
                    tempFileWriter = new BufferedWriter(new FileWriter(firstPostingFileMerged));
                }
                readWriteLinesFromPostingFiles(brFirstPosting, brSecondPosting, tempFileWriter, typeOfMerge);
                //remove unnecessary files
                brFirstPosting.close();
                brSecondPosting.close();
                tempFileWriter.flush();
                tempFileWriter.close();
                Files.delete(postingsFiles[i].toPath());
                if (i == 1)
                    Files.delete(postingsFiles[0].toPath());
            }
            if (firstPostingFileMerged != null && secondPostingFileMerged != null && firstPostingFileMerged.exists() && secondPostingFileMerged.exists()) {
                if (firstPostingFileMerged.lastModified() > secondPostingFileMerged.lastModified())
                    secondPostingFileMerged.delete();
                else
                    firstPostingFileMerged.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readWriteLinesFromPostingFiles(BufferedReader brFirstPosting, BufferedReader brSecondPosting, BufferedWriter tempFileWriter, String typeOfMerge) {
        String lineFirstPosting, lineSecondPosting; //the lines in the posting file - got it from the buffer reader
        boolean termEntityMerge = false;

        if (typeOfMerge.equals(""))
            termEntityMerge = true; // merge terms and entities - the reason is to check how many docs the entity has
        try {
            lineFirstPosting = brFirstPosting.readLine();
            lineSecondPosting = brSecondPosting.readLine();

            //runs over all the lines in the posting files
            while (lineFirstPosting != null && lineSecondPosting != null) {
                String[] firstPostingTerm = lineFirstPosting.split("#");
                String[] secondPostingTerm = lineSecondPosting.split("#");


                //write first term
                if (firstPostingTerm[0].toLowerCase().compareTo(secondPostingTerm[0].toLowerCase()) < 0) {
                    if (termEntityMerge && !isLegalTerm(firstPostingTerm)) { // checks if the term is legal while merge with entities
                        lineFirstPosting = brFirstPosting.readLine();
                        continue;
                    }
                    tempFileWriter.write(lineFirstPosting);
                    tempFileWriter.write(System.getProperty("line.separator"));
                    lineFirstPosting = brFirstPosting.readLine();
                }

                //write second term
                else if (firstPostingTerm[0].toLowerCase().compareTo(secondPostingTerm[0].toLowerCase()) > 0) {
                    if (termEntityMerge && !isLegalTerm(secondPostingTerm)) { // checks if the term is legal while merge with entities
                        lineSecondPosting = brSecondPosting.readLine();
                        continue;
                    }
                    tempFileWriter.write(lineSecondPosting);
                    tempFileWriter.write(System.getProperty("line.separator"));
                    lineSecondPosting = brSecondPosting.readLine();

                }

                //equals merge docs list amd write
                else {
                    String mergeLine = mergeDocumentListOfTheSameTerm(firstPostingTerm, secondPostingTerm);
                    tempFileWriter.write(mergeLine);//writes one line for two duplicates
                    tempFileWriter.write(System.getProperty("line.separator"));
                    lineFirstPosting = brFirstPosting.readLine();
                    lineSecondPosting = brSecondPosting.readLine();
                }
            }
            //still lines in the first file
            while (lineFirstPosting != null) {
                if (termEntityMerge && !isLegalTerm(lineFirstPosting.split("#"))) {
                    lineFirstPosting = brFirstPosting.readLine();
                    continue;
                }
                tempFileWriter.write(lineFirstPosting);
                tempFileWriter.write(System.getProperty("line.separator"));
                lineFirstPosting = brFirstPosting.readLine();
            }
            //still lines in the second file
            while (lineSecondPosting != null) {
                if (termEntityMerge && !isLegalTerm(lineSecondPosting.split("#"))) {
                    lineSecondPosting = brSecondPosting.readLine();
                    continue;
                }
                tempFileWriter.write(lineSecondPosting);
                tempFileWriter.write(System.getProperty("line.separator"));
                lineSecondPosting = brSecondPosting.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String mergeDocumentListOfTheSameTerm(String[] firstPostingTerm, String[] secondPostingTerm) {
        String termLine;
        if (firstPostingTerm[0].equals(firstPostingTerm[0].toLowerCase())) { // if lower case save it like this
            termLine = firstPostingTerm[0] + "#";
        } else { // doesnt matter what is the format of the term
            termLine = secondPostingTerm[0] + "#";
        }
        termLine += firstPostingTerm[1] + "?" + secondPostingTerm[1];
        return termLine;
    }

    private static void removePreviousFiles() {
        File removeFirstPreviousFiles = new File("Postings\\fullPosting1.txt");
        File removeSecondPreviousFiles = new File("Postings\\fullPosting2.txt");
        if (removeFirstPreviousFiles.exists())
            removeFirstPreviousFiles.delete();
        if (removeSecondPreviousFiles.exists())
            removeSecondPreviousFiles.delete();
    }
}
