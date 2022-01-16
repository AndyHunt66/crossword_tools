package uk.me.andrewhunt.crossword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args) throws IOException
    {

        // index <output index dir> <word list file>
        if (args[0].equals("index") && args.length == 3)
        {

            Indexer indexer = new Indexer(args[1]);
            indexer.indexFileOfWords(args[2]);
            indexer.finished();

            System.exit(0);
        }
        // search <index directory> <search type> <search text>
        else if (args[0].equals("search"))
        {
            ArrayList<String> results = null;
            Searcher searcher = new Searcher(args[1]);
            results = searcher.search(args[2]);
            for (String result : results)
            {
                System.out.println(result);
            }
        }
        else if (args[0].equals("console"))
        {
            Searcher searcher = new Searcher(args[1]);
            Scanner scanner = new Scanner(System.in);
            while (true)
            {
                System.out.println("Please input a line");
                String line = scanner.nextLine();
                ArrayList<String> words = searcher.search(line);
                for (String word : words)
                {
                    System.out.println(word);
                }
            }
        }
        else
        {
            System.out.println("AAAAAAAAA");
        }


    }

}
