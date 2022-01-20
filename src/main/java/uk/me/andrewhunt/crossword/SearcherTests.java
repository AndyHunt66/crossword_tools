package uk.me.andrewhunt.crossword;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SearcherTests
{
    Searcher searcher;

    @BeforeEach
    void setUp() throws IOException
    {
        searcher = new Searcher("./index");
    }

    @Test
    @DisplayName("Anagram - Basic")
        //  abnisr       -- find anagrams of abnisr
    void basicAnagram() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("brains");
        ArrayList<String> response = new ArrayList<>();
        response.add("bairns");
        response.add("brains");

        assertEquals(response, results);
    }

    @Test
    @DisplayName("Anagram - Basic with leading space")
        //  abnisr       -- find anagrams of abnisr
    void basicAnagramLeadingSpace() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search(" brains");
        ArrayList<String> response = new ArrayList<>();
        response.add("bairns");
        response.add("brains");

        assertEquals(response, results);
    }


    @Test
    @DisplayName("Anagram - Letters Given < target word")
    // abnisr7      -- find words of 7 letters that include all the letters abnisr
    public void subPartialAnagram() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr7");
        assertEquals(results.size(), 8);
        for (String result : results)
        {
            assertEquals(result.length(), 7);
        }
    }

    @Test
    @DisplayName("Anagram - Letters Given > target word")
    // abnisr5      -- find all the words of 5 letters that you can make from the letters abnisr
    public void superPartialAnagram() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr5");
        assertEquals(results.size(), 22);
        for (String result : results)
        {
            assertEquals(result.length(), 5);
        }
    }

    @Test
    @DisplayName("Anagram - Target range greater than letters given")
    // abnisr7-9    -- find all the 7,8 and 9 letter words that include all the letters abnisr
    public void subRangePartialAnagram() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr7-9");
        assertTrue(results.contains("siberians"));
        for (String result : results)
        {
            assertTrue(result.length() >= 7 && result.length() <= 9);
        }
    }

    @Test
    @DisplayName("Anagram - Target range smaller than letters given")
    // abnisr3-5    -- find all the 3,4 and 5 letter words that you can make from the letters abnisr
    public void superRangePartialAnagram() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr3-5");
        assertTrue(results.contains("brain"));
        for (String result : results)
        {
            assertTrue(result.length() >= 3 && result.length() <= 5);
        }
    }

    @Test
    @DisplayName("Anagram - Target range smaller and larger than letters given")
    // abnisr5-8    -- Same as abnisr7-9, but this includes words shorter than the number of letters provided
    public void superAndSubRangePartialAnagram() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr5-8");
        assertTrue(results.contains("brain"));
        assertTrue(results.contains("siberian"));
        for (String result : results)
        {
            assertTrue(result.length() >= 5 && result.length() <= 8);
        }
    }

    @Test
    @DisplayName("Anagram - Open ended target range greater than letters given")
    // abnisr7-     -- All words 7 letters or longer that include all the letters abnisr
    public void openSubRangePartialAnagram() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr7-");
        assertTrue(results.contains("siberians"));
        for (String result : results)
        {
            assertTrue(result.length() >= 7);
        }
    }

    @Test
    @DisplayName("Anagram - Open ended target range less than letters given")
    // abnisr-9     -- All words 9 letters or shorter that include all the letters abnisr
    public void openSuperRangePartialAnagram() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr-9");
        assertTrue(results.contains("siberians"));
        assertTrue(results.contains("urbanised"));
        assertTrue(results.contains("is"));

        for (String result : results)
        {
            assertTrue(result.length() <= 9);
        }
    }

    @Test
    @DisplayName("Anagram - Open top-ended target range, starting from less than letters given")
    // abnisr5-     -- Same as abnisr7-, but including words shorter than the number of provided letters
    public void lowOpenSubRangePartialAnagram() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr5-");
        assertTrue(results.contains("brain"));
        assertTrue(results.contains("signboards"));
        assertFalse(results.contains("is"));

        for (String result : results)
        {
            assertTrue(result.length() >= 5);
        }
    }

    @Test
    @DisplayName("Anagram - with placeholder dots")
    // abnisr..     -- find words of 8 letters that include all the letters abnisr. INCOMPATIBLE WITH  abnisr7 or :.a
    public void dottedAnagram() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr..");
        assertTrue(results.contains("rinsable"));
        assertFalse(results.contains("signboards"));
        assertFalse(results.contains("is"));

        for (String result : results)
        {
            assertTrue(result.length() == 8);
        }
    }

    @Test
    @DisplayName("Anagram plus Xword - simple")
    // abnisr5:.a...-- All anagrams of 5 letters long, using the letters abnisr, with a as the second letter
    public void simpleAnagramXword() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr5:.a...");
        assertTrue(results.contains("basin"));
        assertFalse(results.contains("signboards"));
        assertFalse(results.contains("is"));

        for (String result : results)
        {
            assertTrue(result.length() == 5);
        }
    }

    @Test
    @DisplayName("Anagram with Xword shorter than the target word - single target length")
    // abnir6:.r    -- Same as abnisr5:.a... but the xword section only specifies as many characters as it needs to
    public void AnagramWithShortXword() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnir6:.r");
        assertTrue(results.contains("brains"));
        assertFalse(results.contains("bairns"));
        assertFalse(results.contains("signboards"));
        assertFalse(results.contains("is"));

        for (String result : results)
        {
            assertTrue(result.length() == 6);
        }
    }


    @Test
    @DisplayName("Xword - basic")
    // :br....      -- find all 6 letter words that start with br
    public void xwordSimple() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search(":br....");
        assertTrue(results.contains("brains"));
        assertFalse(results.contains("bairns"));
        assertFalse(results.contains("signboards"));
        assertFalse(results.contains("is"));

        for (String result : results)
        {
            assertTrue(result.length() == 6);
        }
    }

    @Test
    @DisplayName("Anagram with Xword shorter than the target word - range of target lengths")
    // abnisr7-9:br - find all the 7,8 and 9 letter words that include all the letters abnisr and that start with br
    public void AnagramWithShortXwordRange() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr7-9:br");
        assertTrue(results.contains("brisant"));
        assertFalse(results.contains("brains"));
        assertFalse(results.contains("broadlings"));
        assertFalse(results.contains("is"));

        for (String result : results)
        {
            assertTrue(result.length() >= 7 && result.length() <= 9);
        }
    }

    @Test
    @DisplayName("Anagram with Xword shorter than the target word - range of target lengths, including shorter than the anagram")
    // abnisr5-8:.a -- AHAHAHAHHAHA - the pinnacle of all acheivement!
    //                 find all words whose second letter is a , and include all the letters abnisr if they are 6,7 or 8 letters long, or can be made out of the letters abnisr if they are 5 letters long
    public void AnagramWithShortXwordRangePlusShortTargets() throws InvalidSearchTermException, IOException
    {
        ArrayList<String> results = searcher.search("abnisr5-8:.a");
        assertFalse(results.contains("brisant"));
        assertFalse(results.contains("brains"));
        assertTrue(results.contains("bairns"));

        assertFalse(results.contains("broadlings"));
        assertFalse(results.contains("is"));

        for (String result : results)
        {
            assertTrue(result.length() >= 5 && result.length() <= 8);
        }
    }

    public void SearcherTests() throws InvalidSearchTermException, IOException
    {
    }

}
