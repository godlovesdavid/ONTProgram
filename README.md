ONTProgram
==========

By David Hsu, currently in alpha phase

What it is:
Bible translation editor tool for the Word Software, with GUI, which allows easy transposing of word order in Bible verses.

How to use it: 
1. place ONTProgram.jar and GreekUncial.otf in some directory
2. open ONTProgram.jar
3. click the open button
4. select .ont/.nt/.ot file
5. enter the regular expression that describes one greek word, along with capture info (like $1) for translation, greek, strong's number, or morphology fields. 
6. may select multiple words and move them around. 
7. may also double click each word to edit them (along with their other aspects).

Current bugs:
-regular expression dialog not showing text field locations properly, but sitll works
-going to a new Bible position not right
-cannot move items to very end of verse
-moving end of verse word can result in missing text
-when running program on restart of computer, loading may not function correctly. Must close and re-open, and the rest of the time will work.
-no auto resizing of verses to fit window. So must scroll horizontally.
-moving toolbar may cause program to not function properly.


This program uses Natural Language Processing technology from Northwestern U, called Morphadorner.

MorphAdorner License:
The MorphAdorner client and server source code and data files fall under the following NCSA style license. Some of the incorporated code and data fall under different licenses as noted in the section third-party licenses below.

Copyright © 2006-2013 by Northwestern University. All rights reserved.

Developed by:
Academic and Research Technologies
Northwestern University
http://www.it.northwestern.edu/about/departments/at/ 
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal with the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimers.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimers in the documentation and/or other materials provided with the distribution.
Neither the names of Academic and Research Technologies, Northwestern University, nor the names of its contributors may be used to endorse or promote products derived from this Software without specific prior written permission.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
