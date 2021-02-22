package GUI.Model;

import Files.Configurations;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public interface IModel {

    void startSystem();

    void closeSystem();

    void loadDictionary();

    void showDictionary();

    void resetSystem();

    void enableStemming(boolean value);

    void chooseCorpusPathBrowse(Stage systemStage);

    void chooseCorpusPath(Stage systemStage,String path);

    void choosePostingPathBrowse(Stage systemStage);

    void choosePostingPath(Stage systemStage,String path);

}
