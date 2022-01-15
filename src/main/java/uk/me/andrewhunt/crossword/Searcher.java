package uk.me.andrewhunt.crossword;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
    private IndexReader reader;
    private IndexSearcher indexSearcher;

    public Searcher(String dirPath) throws IOException {
        this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(dirPath)));
        this.indexSearcher = new IndexSearcher(reader);
    }

    /***
     *
     * @param searchTerm
     *     searchTerm format is [anagramString][:xwordstring]
     * @return ArrayList of strings - the found words - currently in alphabetical order, but this is not guaranteed in the long run
     * @throws IOException
     */
    public ArrayList<String> search(String searchTerm) throws IOException {
        ArrayList<String> words = new ArrayList<String>();
        BooleanQuery.Builder bq = new BooleanQuery.Builder();
        int delimiter = searchTerm.indexOf(":");
        if (delimiter == -1)
        //   Pure anagram
        {
            bq = addAnagramQuery(bq, searchTerm);
        } else if (delimiter == 0)
        // Pure XWord
        {
            bq = addXwordQuery(bq, searchTerm.substring(delimiter + 1));
        } else
        // Hybrid Anagram and XWord
        {
            String anagramString = searchTerm.substring(0, delimiter);
            String xwordString = searchTerm.substring(delimiter + 1);
            bq = addAnagramQuery(bq, anagramString);
            bq = addXwordQuery(bq, xwordString);
        }
        Query q2 = bq.build();

        TopDocs results = indexSearcher.search(q2, Integer.MAX_VALUE);
        ScoreDoc[] hits = results.scoreDocs;
        for (ScoreDoc hit : hits) {
            Document doc = indexSearcher.doc(hit.doc);
            words.add(doc.get("word"));
        }

        return words;
    }

    private BooleanQuery.Builder addXwordQuery(BooleanQuery.Builder bq, String xwordString) {
        String parts[] = xwordString.split("");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(".")) {
                continue;
            }
//            System.out.println(parts[i]);

            bq.add(new TermQuery(new Term("pos." + (i + 1), parts[i])), BooleanClause.Occur.MUST);
        }
        bq.add(new WildcardQuery(new Term("pos." + (parts.length), "*")), BooleanClause.Occur.MUST);
        bq.add(new WildcardQuery(new Term("pos." + (parts.length + 1), "*")), BooleanClause.Occur.MUST_NOT);

        return bq;
    }

    private BooleanQuery.Builder addAnagramQuery(BooleanQuery.Builder bq, String anagramString) {
        String angArray[] = anagramString.split("");
        Arrays.sort(angArray);
        bq.add(new TermQuery(new Term("alphaSort", String.join("", angArray))), BooleanClause.Occur.MUST);
        return bq;
    }

}
