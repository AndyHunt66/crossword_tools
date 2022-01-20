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



#### Crossword only
An ordered list of full stops (periods) and letters, where full stops indicate an unkonwn letter and the given letters are in the correct position in the word. All preceeded by a colon.

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


Valid search terms:
*        abnisr       -- find anagrams of abnisr
         abnisr7      -- find words of 7 letters that include all the letters abnisr
         abnisr5      -- find all the words of 5 letters that you can make from the letters abnisr
         abnisr7-9    -- find all the 7,8 and 9 letter words that include all the letters abnisr
         abnisr3-5    -- find all the 3,4 and 5 letter words that you can make from the letters abnisr
         abnisr5-8    -- Same as abnisr7-9, but this includes words shorter than the number of letters provided
         abnisr7-     -- All words 7 letters or longer that include all the letters abnisr
         abnisr5-     -- Same as abnisr7-, but including words shorter than the number of provided letters
         abnisr-9     -- All words 9 letters or shorter that include all the letters abnisr
         abnisr..     -- find words of 8 letters that include all the letters abnisr. INCOMPATIBLE WITH  abnisr7 or :.a
         abnisr5:.a...-- All anagrams of 5 letters long, using the the letters abnisr, with a as the second letter
         abnir6:.r    -- Same as above, but the xword section only specifies as many characters as it needs to
         :br....      -- find all 6 letter words that start with br
## Console
- `java uk.me.andrewhunt.crossword.Main console ./index_directory`

Provides a command line to input any valid search term.

## Diff
- `java uk.me.andrewhunt.crossword.Main diff ./words1.txt ./words2.txt`
- `java uk.me.andrewhunt.crossword.Main diff ./words1.txt ./words2.txt ./output.txt`

Display a list of all the words in `words2.txt` that are not in `words1.txt`.

Optionally, write the alphabetically sorted, concatenated contents of both files to `output.txt` 



There is currently very little defensive checking anywhere as to if files exist, or directories exist, or if the input is valid etc. etc.

## Future To-Do

- Multiple partial anagrams 
 e.g. Given `asnirb` and either a range of word lengths, or a minimum or a maximum word length, find every X-letter word 
asnirb7-9 - all 7 to 9 letter words containing all 6 letters

- Multiple word anagrams. E.g. given `abcdiklnoww` and specifying `2` words, return `black window` 

- Full Android /  iOS version


- Proper licenses and acknowledgement of Lucene