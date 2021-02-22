package Files;

import java.util.*;

public class CorpusDocument implements Comparable{

    private String text;
    private String docNO;
    private String title;
    private String date;

    public CorpusDocument(String docNO, String text, String date, String title) {
        this.docNO = docNO;
        this.text = text;
        this.date = date;
        this.title = title;
    }

    public String getDocNO() {
        return docNO;
    }

    public String getText() {
        return this.text;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDate() {
        return this.date;
    }


    @Override
    public String toString() {
        return "DocID: " + this.docNO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CorpusDocument document = (CorpusDocument) o;
        return Objects.equals(docNO, document.docNO);
    }

    @Override
    public int compareTo(Object o) {
       if (o != null && o instanceof CorpusDocument){
           return this.getDocNO().compareTo(((CorpusDocument) o).getDocNO());
       }
       return -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, docNO, title, date);
    }
}
