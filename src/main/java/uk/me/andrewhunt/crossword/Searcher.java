package uk.me.andrewhunt.crossword;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class Searcher
{
    private IndexReader reader;
    private IndexSearcher indexSearcher;

    public Searcher(String dirPath) throws IOException
    {
        this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(dirPath)));
        this.indexSearcher = new IndexSearcher(reader);
    }


//  Query q1 = new TermsQuery(new Term("field", "foo"), new Term("field", "bar"));
    public ArrayList<String> search(String searchType, String searchText)
    {
        ArrayList<String> words = new ArrayList<String>();
        if (searchType.equals("anagram"))
        {
            char[] charArray = searchText.toCharArray();
            Arrays.sort(charArray);
            String sortedWord = new String(charArray);

            try {
                BooleanQuery.Builder bq = new BooleanQuery.Builder();

                bq.add(new TermQuery(new Term("alphaSort", sortedWord)), BooleanClause.Occur.MUST);
                bq.add(new TermQuery(new Term("pos.1", "d")), BooleanClause.Occur.MUST);
                Query q2 = bq.build();

                TopDocs results = indexSearcher.search(q2,Integer.MAX_VALUE);
                ScoreDoc[] hits = results.scoreDocs;
                for (ScoreDoc hit : hits)
                {
                    Document doc = indexSearcher.doc(hit.doc);
                    words.add(doc.get("word"));
                }
            } catch ( IOException e) {
                e.printStackTrace();
            }
        }
        return words;
    }
}
