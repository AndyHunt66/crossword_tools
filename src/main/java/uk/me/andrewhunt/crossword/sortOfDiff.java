package uk.me.andrewhunt.crossword;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;


public class sortOfDiff
{
    private ArrayList<String> list1 = new ArrayList<>(Collections.singletonList(""));
    public sortOfDiff(String file1, String file2, String outFile) throws IOException
    {
        new sortOfDiff(file1,file2);
        Collections.sort(list1);
        PrintStream fileStream = new PrintStream(outFile);
        for (String word : list1)
        {
            fileStream.println(word);
        }
        fileStream.close();
    }
    public sortOfDiff(String file1, String file2) throws IOException
    {
        list1 = new ArrayList<>(Files.readAllLines(Paths.get(file1)));
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
