package IRSystem;

import Files.CorpusDocument;
import Indexer.Indexer;
import Parse.Parse;
import java.util.List;

public class SystemThread implements Runnable {
    List<CorpusDocument> t;
    Parse p;
    Indexer x = new Indexer();
    boolean isIndexer = false;

    public SystemThread(List<CorpusDocument> f) {
        this.t = f;
        p = new Parse();
    }

    public void run() {
        int i;
        for (i = 0; i < t.size(); i++) {
            isIndexer = false;
            p.parsing(t.get(i));
            if (i % 475 == 0 && i != 0) {
                x.startIndexer(p.getCorpusTerms(), p.getEntitiesMap(), p.getDocsMap());
                p.resetParse();
                System.out.println("Done" + i);
                isIndexer = true;
            }
        }
        if (!isIndexer) {
            x.startIndexer(p.getCorpusTerms(), p.getEntitiesMap(), p.getDocsMap());
            System.out.println("Done" + i);
        }
        System.out.println("start:" + Parse.getTime());
        System.out.println("avg:" + Parse.avg());
    }
}
