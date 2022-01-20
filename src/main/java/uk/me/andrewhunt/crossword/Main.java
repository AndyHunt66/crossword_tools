package uk.me.andrewhunt.crossword;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import java.util.Scanner;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 2 )
        {
            printUsage();
            System.exit(1);
        }
        // index <output index dir> (<word list file> | testwords )
        // or
        // index <output index dir> <word list file> <append>
        if (args[0].equals("index") && ( args.length == 3 || args.length == 4))
        {
            LocalDateTime startTime = LocalDateTime.now();
            Indexer indexer = null;
            if (args.length == 3)
            {
                indexer = new Indexer(args[1]);
            }
            else
            {
                indexer = new Indexer(args[1], args[3]);
            }
            indexer.indexFileOfWords(args[2]);
            indexer.finished();
            LocalDateTime endTime = LocalDateTime.now();
            long diff = ChronoUnit.SECONDS.between(startTime, endTime);
            System.out.println("Took: " + diff + " seconds");
            System.exit(0);
        }
        // search <index directory> <search text>
        else if (args[0].equals("search")  && args.length == 3)
        {
            ArrayList<String> results = null;
            Searcher searcher = new Searcher(args[1]);
            try
            {
                results = searcher.search(args[2]);
            }
            catch (InvalidSearchTermException e)
            {
                e.printStackTrace();
            }
            for (String result : results)
            {
                System.out.println(result);
            }


        }
        else if (args[0].equals("console")  && args.length == 2)
        {
            Searcher searcher = new Searcher(args[1]);
            Scanner scanner = new Scanner(System.in);
            while (true)
            {
                System.out.println("Please input a search term:");
                String line = scanner.nextLine();
                ArrayList<String> words = null;
                try
                {
                    words = searcher.search(line);
                }
                catch (InvalidSearchTermException e)
                {
                    e.printStackTrace();
                }
                for (String word : words)
                {
                    System.out.println(word);
                }
            }
        }
        else if (args[0].equals("diff") )
        {
            if ( args.length == 3)
            {
                sortOfDiff diff = new sortOfDiff(args[1], args[2]);
            }
            if ( args.length == 4)
            {
                sortOfDiff diff = new sortOfDiff(args[1], args[2], args[3]);
            }

        }
        else
        {
            printUsage();
        }
    }

    private static void printUsage()
    {
        System.out.println("Usage: ");
        System.out.println("         index   <index directory> <word file> [<append>]");
        System.out.println("         search  <index directory> <search term>");
        System.out.println("         console <index directory>");
        System.out.println("         diff    <word file 1> <word file 2> [<output file>]");
    }
}
