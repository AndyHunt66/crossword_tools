package uk.me.andrewhunt.crossword;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer
{

    private static final int SHORT_WORD_MEME_LIMIT = 3;
    private static final int LONG_WORD_MEME_LIMIT = 8;
    private static final int SHORT_LONG_WORD_BOUNDARY = 10;

    private static final int UPPER_MEME_LIMIT = 15;
    private final IndexWriter writer;
    private int docCount = 0;

    public Indexer(String dirPath, String openMode) throws IOException
    {
        IndexWriterConfig.OpenMode mode = IndexWriterConfig.OpenMode.CREATE;
        if (openMode.equalsIgnoreCase("append"))
        {
            mode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
        }
        Directory dir = FSDirectory.open(Paths.get(dirPath));
        Analyzer analyzer = new KeywordAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(mode);
        this.writer = new IndexWriter(dir, iwc);
    }
    public Indexer(String dirPath) throws IOException
    {
        Directory dir = FSDirectory.open(Paths.get(dirPath));
        Analyzer analyzer = new KeywordAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        this.writer = new IndexWriter(dir, iwc);
    }

    public void finished() throws IOException
    {
        writer.forceMerge(1);
        writer.close();
    }

    public void indexWord(String word) throws IOException
    {

//        System.out.println("Doc indexed: " + word + "  " + docCount);
        word = word.toLowerCase();
        ArrayList<Field> fields = new ArrayList<>();
        Document doc = new Document();
        // Full word
        fields.add(new StringField("word", word, Field.Store.YES));
        for (int i = 1; i <= word.length(); i++)
        {
            fields.add(new StringField("pos." + i, word.substring(i - 1, i), Field.Store.NO));
        }

        String[] parts = word.split("");
        if (word.length() < UPPER_MEME_LIMIT)
        {
            ArrayList<String> memes = generateMemes(parts);
            for (String meme : memes )
            {
                fields.add(new StringField("meme", meme, Field.Store.NO));
            }
        }
        Arrays.sort(parts);
        String sortedWord = String.join("", parts);
        fields.add(new StringField("alphaSort", sortedWord, Field.Store.NO));

        for (Field field : fields)
        {
            doc.add(field);
        }
//        System.out.println("adding " + word);
        writer.addDocument(doc);
        docCount+=1;
        int k = docCount % 1000;
        if (k ==  0 )
        {
            System.out.println("Doc indexed: " + word + "  " + docCount);
        }
    }

    private ArrayList<String> generateMemes(String[] parts)
    {
        ArrayList<String> memes = new ArrayList<>();
        int lowerMemeLength;
        if (parts.length <= SHORT_LONG_WORD_BOUNDARY)
        {
            lowerMemeLength = SHORT_WORD_MEME_LIMIT;
        }
        else
        {
            lowerMemeLength = LONG_WORD_MEME_LIMIT;
        }
        for (int memeLength = lowerMemeLength ; memeLength <= (parts.length -1) ;  memeLength++)
        {
            memes.addAll(generateMemes(parts, memeLength));
        }

        return memes;
    }

    private ArrayList<String> generateMemes(String[] parts, int memeLength)
    {
        HashMap<String, Integer> subMemes = new HashMap<>();
        Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(parts.length, memeLength);
        while (iterator.hasNext())
        {
            final int[] myint = iterator.next();
            StringBuilder meme = new StringBuilder();
            for (int i : myint) {
                meme.append(parts[i]);
            }
            String[] toSort = meme.toString().split("");
            Arrays.sort(toSort);
            meme = new StringBuilder(String.join("", toSort));

            if (!subMemes.containsKey(meme.toString()))
            {
                subMemes.put(meme.toString(), 1);
            }
        }

        return new ArrayList<>(subMemes.keySet());
    }
    public void indexFileOfWords(String filename) throws IOException
    {
        if (filename.equals("testwords"))
        {
            for (String word : testWords() )
            {
                indexWord(word);
            }
            return;
        }
        for (String word : Files.readAllLines(Paths.get(filename)))
        {
            indexWord(word);
        }
    }
    public ArrayList<String> testWords()
    {
        ArrayList<String> testwords = new ArrayList<>();
        testwords.add("aardvark");
        testwords.add("head");
        testwords.add("brains");
        return testwords;
    }
}
