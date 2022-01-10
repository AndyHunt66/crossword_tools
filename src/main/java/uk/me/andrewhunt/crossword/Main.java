package uk.me.andrewhunt.crossword;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        // index <output index dir> <word list file>
        if (args[0].equals("index") && args.length == 3)
        {
            try {
                Indexer indexer = new Indexer(args[1]);
                indexer.indexFileOfWords(args[2]);
                indexer.finished();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
        // serach <index directory> <search type> <search text>
        else if (args[0].equals("search") && args.length ==4)
        {
            ArrayList<String> results = null;
            try {
                Searcher searcher = new Searcher(args[1]);
                results = searcher.search(args[2],args[3]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String result : results)
            {
                System.out.println(result);
            }
        }
        else
        {
            System.out.println("AAAAAAAAA");
        }



    }

    private static ArrayList<String> testWords() {
        ArrayList<String> words = new ArrayList<String>();
        words.add("aardvark");
        words.add("bob");
        words.add("phenotype");
        return words;
    }
}
