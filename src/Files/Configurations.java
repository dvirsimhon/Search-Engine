package Files;

import java.io.*;
import java.util.Properties;

public class Configurations {

    private static Properties prop = new Properties();

    private Configurations(){}

    public static void setCorpusPath(String corpusPath) {
        prop.setProperty("CorpusPath", corpusPath);
        try {
            File userHome = new File(System.getProperty("user.dir"));
            File propertiesFile = new File(userHome, "config.properties");
            prop.store(new FileOutputStream(propertiesFile), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCorpusPath(){
        String path="";
        try {
            InputStream input = new FileInputStream(System.getProperty("user.dir") + "\\config.properties");
            prop.load(input);
            if (!prop.isEmpty()) {
                path = prop.getProperty("CorpusPath");
            }
            input.close();
            return path;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static void setPostingsFilesPath(String postingsFilesPath) {
        prop.setProperty("PostingsPath", postingsFilesPath);
        try {
            File userHome = new File(System.getProperty("user.dir"));
            File propertiesFile = new File(userHome, "config.properties");
            prop.store(new FileOutputStream(propertiesFile), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPostingsFilePath(){
        String path="";
        try {
            InputStream input = new FileInputStream(System.getProperty("user.dir") + "\\config.properties");
            prop.load(input);
            if (!prop.isEmpty()) {
                path = prop.getProperty("PostingsPath");
            }
            input.close();
            return path;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static void setStemming(String stemming){
        prop.setProperty("Stemming",stemming);
        try {
            File userHome = new File(System.getProperty("user.dir"));
            File propertiesFile = new File(userHome, "config.properties");
            prop.store(new FileOutputStream(propertiesFile), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean getStemmingProp(){
        boolean stemming = false;
        try {
            InputStream input = new FileInputStream(System.getProperty("user.dir") + "\\config.properties");
            prop.load(input);
            if (!prop.isEmpty()) {
                if (prop.getProperty("Stemming").equals("false")){
                    stemming = false;
                } else
                    stemming = true;
            }
            input.close();
            return stemming;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
