package uk.me.andrewhunt.crossword;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
    private static final int MAX_WORD_LENGTH = 20;
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
     *     IMPLEMENTED:
     *         abnisr       -- find anagrams of abnisr
     *         abnisr7      -- find words of 7 letters that include all the letters abnisr
     *         abnisr5      -- find all the words of 5 letters that you can make from the letters abnisr
     *         abnisr5:.a...-- All anagrams of 5 letters long, using the the letters abnisr, with a as the second letter
     *         abnir6:.r    -- Same as above, but the xword section only specifies as many characters as it needs to
     *         :br....      -- find all 6 letter words that start with br
     *
     *
     *     NOT YET IMPLEMENTED
     *         abnisr7-9    -- find all the 7,8 and 9 letter words that include all the letters abnisr
     *         abnisr..     -- find words of 8 letters that include all the letters abnisr
     *         abnisr5-8    -- Same as abnisr7-9, but this includes words shorter than the number of letters provided
     *         abnisr7-     -- All words 7 letters or longer that include all the letters abnisr
     *         abnisr-9     -- All words 9 letters or shorter that include all the letters abnisr
     *         abnisr5-     -- Same as abnisr5-, but including words shorter than the number of provided letters
     *         abnisr-9     -- Same as abnisr-9, but including words shorter than the number of provided letters
     *         abnisr7-9:br -- find all the 7,8 and 9 letter words that include all the letters abnisr and that start with br
     *         abnisr5-8:.a -- AHAHAHAHHAHA - the pinnacle of all acheivement!
     *              find all words whose second letter is a , and include all the letters abnisr if they are 6,7 or 8 letters long, or can be made out of the letters abnisr if they are 5 letters long
     * @return ArrayList of strings - the found words - currently in alphabetical order, but this is not guaranteed in the long run
     * @throws IOException
     */
    public ArrayList<String> search(String searchTerm) throws IOException, InvalidSearchTermException
    {
        // Set up essentially global variables
        searchTerm = searchTerm.toLowerCase();
        ArrayList<String> words = new ArrayList<String>();
        BooleanQuery.Builder bq = new BooleanQuery.Builder();
        String xword = "";
        String anagram = "";
        int lowerWordLength = 0;
        int upperWordLength = 0;

        if (searchTerm.equals(null) || searchTerm.equals(""))
        {
            words.add("");
            return words;
        }

        // Step 1 - split the search term into the anagram part and the Xword part
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

        // Step 2 - What sort of an anagram is it?
        int partialDelimtier = anagram.indexOf("-");
        if (partialDelimtier != -1)
        // Partial anagram
        {
            if (partialDelimtier == anagram.length())
            // Lower bounded range - no upper bound
            {
                upperWordLength = MAX_WORD_LENGTH;
            }
            else
            // Set the upper bound
            {
                String upperBound = anagram.substring(anagram.indexOf("-")+1);
                upperWordLength = Integer.parseInt(upperBound);
            }
            anagram = anagram.substring(0,anagram.indexOf("-"));
        }

        // Is there a lower bound?
        Pattern p = Pattern.compile("^([a-z]+)([0-9]+)");
        Matcher m = p.matcher(anagram);
        if (m.find())
        // Partial anagram with a lower bound
        {
            anagram = m.group(1);
            lowerWordLength = Integer.parseInt(m.group(2));
        }


        // Run some basic checks on the input data
        // TODO: Improve the integrity checking of searchterm
        if ((lowerWordLength > upperWordLength) && (upperWordLength != 0))
        {
            throw new InvalidSearchTermException(searchTerm + " - search term has invalid upper and lower anagram bounds.");
        }
        if ((upperWordLength < 0) || (lowerWordLength < 0))
        {
            throw new InvalidSearchTermException(searchTerm + " - bounds are negative.");
        }


        // Step 3 - Start Building the query
        if ((!anagram.equals("")) && ((lowerWordLength == 0) && (upperWordLength == 0)) )
        // Normal Anagram
        {
            bq = addAnagramQuery(bq, anagram);
        }
        if (!xword.equals(""))
        // Normal Xword
        {
            if ((upperWordLength == 0) && (lowerWordLength == 0))
            // No partial anagram, so no need to worry about the length of the target word being different to the number of characters in our Xword pattern
            {
                bq = addXwordQuery(bq, xword);
            }
            else
            // Partial anagram is in play, so we need to defer to the length specified there to determine how big the target word is
            // e.g. abnisr5:.r  - the xword component is only 2 characters long, but we have specifically requested a 5 letter word
            {
                bq = addXwordQueryWithoutLength(bq, xword);
            }
        }
        if (lowerWordLength != 0)
        // Partial with lower bounded range
        {
            if (upperWordLength != 0)
            {
                bq = addPartialQuery(bq,anagram,lowerWordLength,upperWordLength);
            }
            else
            // Simple partial with single worod length
            {
                bq = addPartialQuery(bq, anagram, lowerWordLength);
            }
        }
        else if(upperWordLength != 0)
        {
            bq = addPartialQuery(bq,anagram,1,upperWordLength);
        }
        else
        // Shouldn't have been able to get here, because upperWordLength should have been set to MAX if there was no provided upper bound
        {
            throw new InvalidSearchTermException(searchTerm + "Problem with upper bounds");
        }

        // Step 4 - check and adjust the requested length of the word

        // Build and run the query
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
        bq = addXwordQueryWithoutLength(bq, xwordString);

        bq.add(new WildcardQuery(new Term("pos." + (xwordString.length()), "*")), BooleanClause.Occur.MUST);
        bq.add(new WildcardQuery(new Term("pos." + (xwordString.length() + 1), "*")), BooleanClause.Occur.MUST_NOT);

        return bq;
    }
    private BooleanQuery.Builder addXwordQueryWithoutLength(BooleanQuery.Builder bq, String xwordString) {
        String parts[] = xwordString.split("");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(".")) {
                continue;
            }
//            System.out.println(parts[i]);
            bq.add(new TermQuery(new Term("pos." + (i + 1), parts[i])), BooleanClause.Occur.MUST);
        }
        return bq;
    }

    private BooleanQuery.Builder addAnagramQuery(BooleanQuery.Builder bq, String anagramString) {
        String angArray[] = anagramString.split("");
        Arrays.sort(angArray);
        bq.add(new TermQuery(new Term("alphaSort", String.join("", angArray))), BooleanClause.Occur.MUST);
        return bq;
    }
    private BooleanQuery.Builder addPartialQuery(BooleanQuery.Builder bq, String anagramString, int lowerWordLength)
    {
        String angArray[] = anagramString.split("");
        Arrays.sort(angArray);
        anagramString =  String.join("", angArray);

        // Partial query - specify how long the resultant word must be
        //   If the resultant word is shorter than the anagramString,we need to iterate through
        //   If the resultant word is longer, we just add in pos.x clauses
        if (anagramString.length() == lowerWordLength)
        {
            // trivial case - just send it to the standard anagram query
            return addAnagramQuery(bq, anagramString);
        }
        if (anagramString.length() < lowerWordLength)
        {
            bq.add(new TermQuery(new Term("meme", anagramString)), BooleanClause.Occur.MUST);
            bq.add(new WildcardQuery(new Term("pos."+lowerWordLength, "*")), BooleanClause.Occur.MUST);
            bq.add(new WildcardQuery(new Term("pos."+(lowerWordLength+1), "*")), BooleanClause.Occur.MUST_NOT);

            return bq;

        }
        if (anagramString.length() > lowerWordLength)
        {
            BooleanQuery.Builder bqShould = new BooleanQuery.Builder();

            HashMap<String, Integer> subMemes = new HashMap<String, Integer>();
            String[] parts = anagramString.split("");
            Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(parts.length, lowerWordLength);
            while (iterator.hasNext())
            {
                final int[] myint = iterator.next();
                String meme = "";
                for (int count = 0; count < myint.length; count++)
                {
                    meme += parts[myint[count]];
                }
                String toSort[] = meme.split("");
                Arrays.sort(toSort);
                meme = String.join("", toSort);
                bqShould.add(new TermQuery(new Term("alphaSort", meme)), BooleanClause.Occur.SHOULD);
                bq.add(new WildcardQuery(new Term("pos."+lowerWordLength, "*")), BooleanClause.Occur.MUST);
                bq.add(new WildcardQuery(new Term("pos."+(lowerWordLength+1), "*")), BooleanClause.Occur.MUST_NOT);
            }
            BooleanQuery.Builder compoundBuilder = new BooleanQuery.Builder();
            compoundBuilder.add(bqShould.build(), BooleanClause.Occur.MUST);
            if (bq.build().clauses().size() > 0)
            {
                compoundBuilder.add(bq.build(),BooleanClause.Occur.MUST);
            }
            return compoundBuilder;
        }
        return bq;
    }


    private BooleanQuery.Builder addPartialQuery(BooleanQuery.Builder bq, String anagramString, int lowerWordLength, int upperWordLength)
    {
        BooleanQuery.Builder compoundBuilder = new BooleanQuery.Builder();

        for (int i = lowerWordLength; i <= upperWordLength; i++)
        {
            BooleanQuery.Builder bqShould = new BooleanQuery.Builder();
            bqShould =  addPartialQuery(bqShould,anagramString,i);
            compoundBuilder.add(bqShould.build(), BooleanClause.Occur.SHOULD);
        }
        if (bq.build().clauses().size() > 0)
        {
            compoundBuilder.add(bq.build(),BooleanClause.Occur.MUST);
        }
        return compoundBuilder;
    }

}
