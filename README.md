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
- The word list included in https://www.powerlanguage.co.uk/wordle/ (only 5 letter words)

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
- Partial anagrams
e.g. given `asnir` and a specified length of `6` , output every 6-letter word that includes an anagram of `asnir`
```
arains
asarin
bairns
brains
arcsin
cairns
dinars
drains
nadirs
ranids
etc.etc.etc......
```

- Full Android /  iOS version


- Proper licenses and acknowledgement of Lucene