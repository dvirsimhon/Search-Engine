package GUI.Model;


import Files.Configurations;
import Indexer.Indexer;
import IRSystem.IRSystem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Observable;

public class MyModel extends Observable implements IModel {

    private Indexer indexer;

    public MyModel() {
        Configurations.setPostingsFilesPath(System.getProperty("user.dir"));
        Configurations.setCorpusPath(System.getProperty("user.dir"));
        Configurations.setStemming("false");
    }


    public void startSystem() {
        long start = System.nanoTime();
        if (indexer != null)
            indexer.resetIndexer();
        indexer = IRSystem.start();
        long end = System.nanoTime();
        double total = (end - start) / 1000000000.0;
        double[] indexInfo = new double[3];
        indexInfo[0] = indexer.getDocsDictionary().size(); //number of docs with text in corpus
        indexInfo[1] = indexer.getTermsDictionary().size(); // number of exclusive terms
        indexInfo[2] = total; // total time of the process
        setChanged();
        notifyObservers(indexInfo); //Wave the flag so the observers will notice
    }

    public void closeSystem() {
        IRSystem.close();
        indexer = null;
    }

    public void loadDictionary() {
        if (Files.exists(Paths.get(Configurations.getPostingsFilePath() + "\\Postings"))) {
            String stem = Configurations.getStemmingProp() == false ? "WithoutStem" : "Stem";
            if (new File(Configurations.getPostingsFilePath() + "\\Postings\\" + stem).listFiles().length > 0) {
                indexer = new Indexer();
                indexer.buildDocsFromPosting();
                indexer.buildDictionaryFromPosting();
            } else {
                setChanged();
                notifyObservers("No dictionary in memory. Please load a legal dictionary or start the indexer."); //Wave the flag so the observers will notice
            }
        }
    }

    public void showDictionary() {
        if (indexer != null) {
            setChanged();
            notifyObservers(indexer.getTermsDictionary()); //Wave the flag so the observers will notice
        } else {
            setChanged();
            notifyObservers("No dictionary in memory. Please load a legal dictionary or start the indexer."); //Wave the flag so the observers will notice
        }
    }

    public void resetSystem() {
        IRSystem.resetSystem();
        indexer = null;
    }

    public void chooseCorpusPath(Stage systemStage, String path) {
        if (!path.equals("")) {
            Configurations.setCorpusPath(path);
        }
    }

    public void choosePostingPath(Stage systemStage, String path) {
        if (!path.equals("")) {
            Configurations.setPostingsFilesPath(path);
        }
    }

    public void chooseCorpusPathBrowse(Stage systemStage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Please choose Corpus & Stop words path");
        File defaultDirectory = new File(System.getProperty("user.dir"));
        chooser.setInitialDirectory(defaultDirectory);
        File corpusDir = chooser.showDialog(systemStage);
        if (corpusDir != null) {
            Configurations.setCorpusPath(corpusDir.getAbsolutePath());
            setChanged(); //Raise a flag that I have changed
            Object[] arg = new Object[]{corpusDir, 1};
            notifyObservers(arg); //Wave the flag so the observers will notice
        }

    }

    public void choosePostingPathBrowse(Stage systemStage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Please choose Postings Files path");
        File defaultDirectory = new File(System.getProperty("user.dir"));
        chooser.setInitialDirectory(defaultDirectory);
        File postingsDir = chooser.showDialog(systemStage);
        if (postingsDir != null) {
            Configurations.setPostingsFilesPath(postingsDir.getAbsolutePath());
            setChanged(); //Raise a flag that I have changed
            Object[] arg = new Object[]{postingsDir, 2};
            notifyObservers(arg); //Wave the flag so the observers will notice
        }

    }

    public void enableStemming(boolean value) {
        if (value == false)
            Configurations.setStemming("false");
        else
            Configurations.setStemming("true");
    }


}
