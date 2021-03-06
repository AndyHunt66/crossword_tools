# crossword_tools

Crossword tools is the start of an engine for implementing crossword clue solvers and anagram machines

Based on Apache Lucene, one of the aims is to be tiny tiny tiny, but powerful.

The end-goal is to have a fully-featured mobile app, but that's a long way off.

Current capabilities:
## Index
- `java uk.me.andrewhunt.crossword.Main index ./index_directory ./words.txt`
- `java uk.me.andrewhunt.crossword.Main index ./index_directory ./words.txt append`

Create a new index from the words file, in the specified directory

Words.txt is one alpha-only word per line. 

If `append` is added, words.txt will be added to the index, otherwise a new index is created, overwriting what was there before.

The word list I use currently is a mashup of
- https://github.com/dwyl/english-words/blob/master/words_alpha.txt
- The word lists included in https://www.powerlanguage.co.uk/wordle/ (only 5 letter words)

## Search
- `java uk.me.andrewhunt.crossword.Main search ./index_directory search_term`

Search_term can be one of three formats :
#### Anagram only
A list of letters to generate anagrams of.

E.g. `gasnir` will produce
```
grains
grasni
rasing
sangir
```
The anagram letters can be followed by a number which specifies the length of the target words.

E.g. `gasnir5` will show all 5-letter words that can be made out of the letters supplied, while `gasnir7` will show all 7-letter words that include all the letters supplied. These can be combined to provide a range, such as `gasnir5-8` and if you want either the upper or lower bound to be unspecified, just leave it out.

So`gasnir-7` will show all 1,2,3,4,5 and 6-letter words that can be made out of the letters given, plus all 7-letter words that include all the given letters. `gasnir9-` will return all the words of 9 letters or more that include all the given letters (up to a maximum word length, which is currently set to 20) 


#### Crossword only
An ordered list of full stops (periods) and letters, where full stops indicate an unknown letter and the given letters are in the correct position in the word. All preceded by a colon.
If used in conjunction with an anagram which specifies how many letters should be in the target word, (see Hybrid mode below), you don't need to specify all the full stops - just enough to count up to the last specified letter.

E.g. `:.r..ns` will produce:
```
arains
brains
brawns
drains
grains
prawns
trains
```

#### Hybrid Mode
A simple concatenation of anagram and crossword, joined with a logical boolean AND. In other words, what anagram of these letters fits into this crossword template.

E.g. `gasnir:.r..ns` produces `grains`

#### Negative Letters
Any of the above modes can be suffixed by `~<letters>` to exclude any word which contains any of the letters specified

E.g `asnir6:.rai~bt` produces 
```
arains
drains
grains
```
(but not `brains` or `trains`)



Valid example search terms:
```
         abnisr       -- anagrams of abnisr
         abnisr7      -- words of 7 letters that include all the letters abnisr
         abnisr5      -- words of 5 letters that you can make from the letters abnisr
         abnisr7-9    -- 7,8 and 9 letter words that include all the letters abnisr
         abnisr3-5    -- 3,4 and 5 letter words that you can make from the letters abnisr
         abnisr5-8    -- Same as abnisr7-9, but this includes words shorter than the number of letters provided
         abnisr7-     -- words 7 letters or longer that include all the letters abnisr
         abnisr5-     -- Same as abnisr7-, but including words shorter than the number of provided letters
         abnisr-9     -- words 9 letters or shorter that include all the letters abnisr
         abnisr..     -- words of 8 letters that include all the letters abnisr. INCOMPATIBLE WITH  abnisr7 or :.a - use abnisr8 instead 
         abnisr5:.a...-- anagrams of 5 letters long, using the the letters abnisr, with a as the second letter
         abnir6:.r    -- Same as above, but the xword section only specifies as many characters as it needs to
         :br....      -- 6 letter words that start with br
         abnisr7-9:br -- 7,8 and 9 letter words that include all the letters abnisr and that start with br
         abnisr5-8:.a -- Like abnisr7-9:br but including words shorter than the number of anagram letters provided
         abnis6~r     -- 6-letter words that include abnis but no word with r in it - this probably won't cope with duplicated letters very well
         asnir5-7:.ai~bt -- 5,6 or 7-letter words with a and i as the 2nd and 3rd letters, that either include all the letters asnir or can be made from those letters, with no words that include the letters b or t
```
## Console
- `java uk.me.andrewhunt.crossword.Main console ./index_directory`

Provides a command line to input any valid search term.

## Diff
- `java uk.me.andrewhunt.crossword.Main diff ./words1.txt ./words2.txt`
- `java uk.me.andrewhunt.crossword.Main diff ./words1.txt ./words2.txt ./output.txt`

Display a list of all the words in `words2.txt` that are not in `words1.txt`.

Optionally, write the alphabetically sorted, concatenated contents of both files to `output.txt` 



There is currently very little defensive checking anywhere as to if files exist, or directories exist, or if the input is valid etc. etc.


## Java Version
The project is currently built to target jdk 8. Because of that Lucene 8.11.1 is being used. Lucene 9 needs jdk 11, and I'm constrained for a couple of other reasons to jdk8. It should compile just fine with 11, but there will be one or two warnings about deprecated methods. 

## Future To-Do
- Multiple word anagrams. E.g. given `abcdiklnoww` and specifying `2` words, return `black window` 

- Full Android /  iOS version

- Proper licenses and acknowledgement of Lucene

