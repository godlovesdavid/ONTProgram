ONTProgram
==========

By David Hsu, currently in alpha phase

What it is:
Bible translation editor tool for the Word Software, with GUI, which allows easy transposing of word order in Bible verses.

How to use it: 
1. click the open button
2. select .ont/.nt/.ot file
3. enter the regular expression that describes one greek word, along with capture info (like $1) for translation, greek, strong's number, or morphology fields. 
4. may select multiple words and move them around. 
5. may also double click each word to edit them (along with their other aspects).


Current bugs:
-regular expression dialog not showing text field locations properly, but sitll works
-going to a new Bible position not right
-cannot move items to very end of verse
-moving end of verse word can result in missing text
-when running program on restart of computer, loading may not function correctly. Must close and re-open, and the rest of the time will work.
-no auto resizing of verses to fit window. So must scroll horizontally.
-moving toolbar may cause program to not function properly.
