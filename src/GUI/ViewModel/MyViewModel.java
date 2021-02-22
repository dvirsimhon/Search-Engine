package GUI.ViewModel;

import GUI.Model.IModel;
import javafx.stage.Stage;

import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer {

    private IModel model;

    public MyViewModel(IModel model){
        this.model = model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o==model){
            //Notify my observer (View) that I have changed
            setChanged();
            notifyObservers(arg);
        }
    }

    public void startSystem(){
        model.startSystem();
    }

    public void loadDictionaryFromPosting(){
        model.loadDictionary();
    }

    public void resetSystem(){
        model.resetSystem();
    }

    //view Model Functionality
    public void chooseCorpusDirBrowse(Stage systemStage){
        model.chooseCorpusPathBrowse(systemStage);
    }

    public void choosePostingFilesDirBrowse(Stage systemStage){
        model.choosePostingPathBrowse(systemStage);
    }

    public void chooseCorpusDir(Stage systemStage,String path){
        model.chooseCorpusPath(systemStage,path);
    }

    public void choosePostingFilesDir(Stage systemStage,String path){
        model.choosePostingPath(systemStage,path);
    }

    public void enableStemming(boolean enable){
        model.enableStemming(enable);
    }

    public void showDictionary(){
        model.showDictionary();
    }
}
