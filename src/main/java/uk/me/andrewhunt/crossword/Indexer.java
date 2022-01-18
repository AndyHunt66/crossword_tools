package uk.me.andrewhunt.crossword;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

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

    private IndexWriter writer;


    public Indexer(String dirPath, String openMode) throws IOException
    {
        IndexWriterConfig.OpenMode mode = IndexWriterConfig.OpenMode.CREATE;
        if (openMode.toLowerCase().equals("append"))
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
        word = word.toLowerCase();
        ArrayList<Field> fields = new ArrayList<Field>();
        Document doc = new Document();
        // Full word
        fields.add(new StringField("word", word, Field.Store.YES));
        for (int i = 1; i <= word.length(); i++)
        {
            fields.add(new StringField("pos." + i, word.substring(i - 1, i), Field.Store.NO));
        }

        String parts[] = word.split("");
        Arrays.sort(parts);
        String sortedWord = String.join("", parts);
        fields.add(new StringField("alphaSort", sortedWord, Field.Store.NO));

        for (Field field : fields)
        {
            doc.add(field);
        }
        System.out.println("adding " + word);
        writer.addDocument(doc);
    }

    public void indexFileOfWords(String filename) throws IOException
    {
        for (String word : Files.readAllLines(Paths.get(filename)))
        {
            indexWord(word);
        }
    }
}
