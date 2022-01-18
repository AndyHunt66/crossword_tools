package uk.me.andrewhunt.crossword;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;


public class sortOfDiff
{
    public sortOfDiff(String file1, String file2, String outFile) throws IOException
    {
        ArrayList<String> list1 = new ArrayList<>();
        for (String word : Files.readAllLines(Paths.get(file1)))
        {
            list1.add(word);
        }
        for (String word : Files.readAllLines(Paths.get(file2)))
        {
            if (! list1.contains(word))
            {
                System.out.println(word);
                list1.add(word);
            }
        }
        Collections.sort(list1);
        if (!outFile.equals(null))
        {
            PrintStream fileStream = new PrintStream(new File(outFile));
            for (String word : list1)
            {
                fileStream.println(word);
            }
            fileStream.close();
        }
    }
    public sortOfDiff(String file1, String file2) throws IOException
    {
        ArrayList<String> list1 = new ArrayList<>();
        for (String word : Files.readAllLines(Paths.get(file1)))
        {
            list1.add(word);
        }
        for (String word : Files.readAllLines(Paths.get(file2)))
        {
            if (! list1.contains(word))
            {
                System.out.println(word);
                list1.add(word);
            }
        }
    }


}
