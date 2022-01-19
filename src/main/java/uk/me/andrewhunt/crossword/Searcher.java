package uk.me.andrewhunt.crossword;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     *     searchTerm format is [[String anagramString][int wordlength][- int biggestWordLength]][:xwordstring]
     *     e.g.
     *         abnisr       -- find anagrams of abnisr
     *         abnisr..     -- find words of 8 letters that include all the letters abnisr
     *         abnisr5      -- find all the words of 5 letters that you can make from the letters abnisr
     *         abnisr7      -- find words of 7 letters that include all the letters abnisr
     *         abnisr7-9    -- find all the 7,8 and 9 letter words that include all the letters abnisr
     *         abnisr5-8    -- Holy shit....
     *         :br....      -- find all 6 letter words that start with br
     *         abnisr7-9:br -- find all the 7,8 and 9 letter words that include all the letters abnisr and that start with br
     *         abnisr5-8:.a -- AHAHAHAHHAHA :
     *         find all words whose second letter is a , and include all the letters abnisr if they are 6,7 or 8 letters long, or can be made out of the letters abnisr if they are 5 letters long
     * @return ArrayList of strings - the found words - currently in alphabetical order, but this is not guaranteed in the long run
     * @throws IOException
     */
    public ArrayList<String> search(String searchTerm) throws IOException, InvalidSearchTermException
    {
        searchTerm = searchTerm.toLowerCase();
        ArrayList<String> words = new ArrayList<String>();
        BooleanQuery.Builder bq = new BooleanQuery.Builder();
        String xword = "";
        String anagram = "";
        int lowerWordLength = 0;
        int upperWordLength = 0;

        String regex = "^(.)*(:)(.)*$";
        Pattern pattern = Pattern.compile(regex);
        boolean containsXword = pattern.matcher(searchTerm).matches();
        if (containsXword)
        {
           xword = searchTerm.substring(searchTerm.indexOf(":")+1);
           anagram = searchTerm.substring(0,searchTerm.indexOf(":"));
        }
        else
        {
            anagram = searchTerm;
        }

        if (anagram.indexOf("-") != -1)
        // Partial anagram, with range of word lengths defined
        {
            String upperBound = anagram.substring(anagram.indexOf("-")+1);
            upperWordLength = Integer.parseInt(upperBound);
            anagram = anagram.substring(0,anagram.indexOf("-"));
        }
        Pattern p = Pattern.compile("^([a-z]+)([0-9]+)");
        Matcher m = p.matcher(anagram);
        if (m.find())
        // Partial anagram with a lower bound
        {
            anagram = m.group(1);
            lowerWordLength = Integer.parseInt(m.group(2));
        }

        // Run some basic checks on the input data
        if (lowerWordLength > upperWordLength)
        {
            throw new InvalidSearchTermException(searchTerm + " - search term has invalid upper and lower anagram bounds.");
        }
        if ((upperWordLength < 0) || (lowerWordLength < 0))
        {
            throw new InvalidSearchTermException(searchTerm + " - bounds are negative.");
        }

        if (!anagram.equals(""))
        {
            bq = addAnagramQuery(bq, anagram);
        }
        if (!xword.equals(""))
        {
            bq = addXwordQuery(bq, xword);
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

    private BooleanQuery.Builder addPartialAnagramQuery(BooleanQuery.Builder bq, String partialString)
    {
        String parts[] = partialString.split("");
        Arrays.sort(parts);
        bq.add(new TermQuery(new Term("meme", String.join("", parts))), BooleanClause.Occur.MUST);
        return bq;
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
