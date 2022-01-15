package uk.me.andrewhunt.crossword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

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
        // search <index directory> <search type> <search text>
        else if (args[0].equals("search") )//&& args.length ==4)
        {
            ArrayList<String> results = null;
            try
            {
                Searcher searcher = new Searcher(args[1]);
                results = searcher.search(args[2]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String result : results)
            {
                System.out.println(result);
            }
        }
        else if (args[0].equals("console"))
        {
            Searcher searcher = new Searcher(args[1]);
            Scanner scanner = new Scanner(System.in);
            try {
                while (true) {
                    System.out.println("Please input a line");
                    String line = scanner.nextLine();
                    ArrayList<String> words = searcher.search(line);
                    for (String word : words)
                    {
                        System.out.println(word);
                    }

                }
            } catch(IllegalStateException  e) {
                // System.in has been closed
                System.out.println("System.in was closed; exiting");
            }
        }
        else
        {
            System.out.println("AAAAAAAAA");
        }



    }

}
