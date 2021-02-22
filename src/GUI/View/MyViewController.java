package GUI.View;

import GUI.ViewModel.MyViewModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MyViewController implements Observer, IView {

    private static MyViewModel viewModel;
    @FXML
    private TextArea corpusPath;
    @FXML
    private TextArea postingsPath;
    @FXML
    private RadioButton stemmingButton;
    @FXML
    private TableColumn Terms;
    @FXML
    private TableColumn Number;
    @FXML
    private TableView table;
    @FXML
    private Label lbl_docNum;
    @FXML
    private Label lbl_exTerms;
    @FXML
    private Label lbl_totalTime;

    //Stages
    private static Stage mainStage;
    private static Stage subStage;
    private static Scene mainScene;

    public void initialize(MyViewModel viewModel, Stage mainStage, Scene mainScene) {
        this.viewModel = viewModel;
        this.mainScene = mainScene;
        this.mainStage = mainStage;
        setCorpusPathListener();
        setPostingsPathListener();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            if (arg instanceof double[]){
                showResult((double[])arg);
            }
            else if (arg instanceof Object[]) {
                Object[] file = (Object[]) arg;
                if ((int) file[1] == 1)
                    corpusPath.setText(((File) file[0]).getAbsolutePath());
                else if ((int) file[1] == 2)
                    postingsPath.setText(((File) file[0]).getAbsolutePath());
            }
            else if (arg instanceof LinkedHashMap){
                LinkedHashMap<String,int[]> dictionaryTerm = (LinkedHashMap<String, int[]>)arg;
                showDictionaryInTable(dictionaryTerm);
            }
            else if (arg instanceof String){
                showError((String)arg,"ERROR: NO Dictionary Found");
            }
        }
    }

    public void startSystem() {
        if (corpusPath.getText().equals("") || postingsPath.getText().equals("")) {
            showError("Please insert Corpus and Posting path.", "Path is missing");
        } else if (!(new File(corpusPath.getText()).exists()) || !(new File(postingsPath.getText()).exists())) {
            showError("Corpus or Postings File doesn't exist. Please choose other directory.", "Path doesn't exist");
        } else {
            enableStemming();
            waitingScreen();
            viewModel.startSystem();
        }
    }

    public void waitingScreen(){
        try {
            Stage stage = new Stage();
            stage.setTitle("Loading...");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("/WaitingScreen.fxml").openStream());
            Scene scene = new Scene(root);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.resizableProperty().setValue(Boolean.FALSE);
            MyViewController view = fxmlLoader.getController();
            stage.setScene(scene);
            subStage = stage;
            subStage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * shows the results of the process
     * @param indexInfo - First - document frequency, Second- term total frequency, Third - total time of process
     */
    public void showResult(double[] indexInfo){
        try {
            if (subStage != null) {
                subStage.close();
            }
            Stage stage = new Stage();
            stage.setTitle("Results");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("/ResultScreen.fxml").openStream());
            Scene scene = new Scene(root);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.resizableProperty().setValue(Boolean.FALSE);
            MyViewController view = fxmlLoader.getController();
            view.lbl_docNum.textProperty().setValue(Double.toString(indexInfo[0]));
            view.lbl_exTerms.textProperty().setValue(Double.toString(indexInfo[1]));
            view.lbl_totalTime.textProperty().setValue(Double.toString(indexInfo[2]));
            stage.setScene(scene);
            subStage = stage;
            subStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * closes the window
     */
    public void closeWindow() {
        if (subStage != null)
            subStage.close();
    }

    /**
     * reset the system - remove the memory and posting files
     */
    public void resetSystem() {
        table.setItems(null);
        viewModel.resetSystem();
    }

    /**
     * loads the dictionary from the posting files
     */
    public void loadDictionary() {
        waitingScreen();
        viewModel.loadDictionaryFromPosting();
        subStage.close();
    }

    /**
     * get the dictionary from the model
     */
    public void getDictionary() {
        viewModel.showDictionary();
    }

    /**
     * shows dictionary inside the table
     * @param termsDictionary
     */
    private void showDictionaryInTable(LinkedHashMap<String,int[]> termsDictionary){
        //reset table
        table.setItems(null);

        Terms.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>) p -> {
            return new SimpleStringProperty(p.getValue().getKey());
        });


        Number.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Map.Entry<String, int[]>, Integer>, ObservableValue<Integer>>) p -> {
            return new SimpleIntegerProperty(p.getValue().getValue()[2]).asObject();
        });

        ObservableList<Map.Entry<String, int[]>> items = FXCollections.observableArrayList(termsDictionary.entrySet());
        table.setItems(items);
    }

    /**
     * chooses the path of the corpus with the browse button
     */
    public void chooseCorpusPathBrowse() {
        viewModel.chooseCorpusDirBrowse(mainStage);
    }

    /**
     * chooses the path of the postings with the browse button
     */
    public void choosePostingsFilesPathBrowse() {
        viewModel.choosePostingFilesDirBrowse(mainStage);
    }

    /**
     * chooses the path of the corpus with the text area
     */
    public void setCorpusPathListener() {
        corpusPath.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                viewModel.chooseCorpusDir(mainStage, newValue);
                corpusPath.setText(newValue);
            }
        });
    }

    /**
     * chooses the path of the postings with the text area
     */
    public void setPostingsPathListener() {
        postingsPath.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                viewModel.choosePostingFilesDir(mainStage, newValue);
                postingsPath.setText(newValue);
            }
        });
    }

    /**
     * sets stemming option - off/on
     */
    public void enableStemming() {
        viewModel.enableStemming(stemmingButton.isSelected());
    }

    /**
     * shows error according to the given cause
     * @param alertMessage
     * @param errorType
     */
    private void showError(String alertMessage, String errorType) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(alertMessage);
        alert.setHeaderText(errorType);
        alert.show();
    }

}
