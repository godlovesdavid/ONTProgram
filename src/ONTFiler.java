import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

class ONTFiler
{
	ProgramData programdata;
	Date date;
	BufferedReader reader;
	BufferedWriter writer, logger;

	ONTFiler(ProgramData programdata)
	{
		this.programdata = programdata;

		date = Calendar.getInstance().getTime();
	}

	enum Filetype
	{
		ont, ot, nt
	}

	/**
	 * count lines of a file.
	 */
	int countLines(String filename) throws IOException
	{
		InputStream inputstream =
			new BufferedInputStream(new FileInputStream(filename));
		byte[] c = new byte[1024];
		int count = 0;
		int readChars = 0;
		boolean empty = true;
		while ((readChars = inputstream.read(c)) != -1)
		{
			empty = false;
			for (int i = 0; i < readChars; ++i)
				if (c[i] == '\n')
					++count;
		}
		inputstream.close();
		return (count == 0 && !empty) ? 1 : count + 1;
	}

	/**
	 * translation-root search indexer
	 */
	void indexWordRoots()
	{
		UserData.words.clear();

		for (int bookidx = 0; bookidx < Verse.BIBLE.length; bookidx++)
			for (int chapidx = 0; chapidx < Verse.BIBLE[bookidx].length; chapidx++)
				for (Verse verse : UserData.verses[bookidx][chapidx])
					for (int wordidx = 0; wordidx < verse.size(); wordidx++)
					{
						Word word = verse.get(wordidx);
						if (!word.strongs.isEmpty())
						{
							//if this is a new strongs number to be added, add it.
							if (!UserData.words.containsKey(word.strongs))
								UserData.words.put(word.strongs,
									new HashMap<String, List<Word>>());

							//if this is a new root for this strongs, add it.
							if (!UserData.words.get(word.strongs).containsKey(
								word.root))
								UserData.words.get(word.strongs).put(word.root,
									new LinkedList<Word>());

							UserData.words.get(word.strongs).get(word.root).add(word);
						}
					}
	}

	/**
	 * Puts file info into verse array. Searches for words by user-given regex
	 * pattern, and puts user-given capture info from that regex into the
	 * appropriate info (Greek, strong's, etc.). Searches line by line, so
	 * carriage returns are limiters.
	 * @throws IOException 
	 */
	void open(String loadpath, String regex, String translationcapture,
		String greekcapture, String strongscapture, String morphcapture)
		throws IOException
	{
		programdata.progress = 0;

		//clear verses.
		UserData.verses = new Verse[66][][];
		for (int bookidx = 0; bookidx < Verse.BIBLE.length; bookidx++)
		{
			UserData.verses[bookidx] = new Verse[Verse.BIBLE[bookidx].length][];
			for (int chapidx = 0; chapidx < Verse.BIBLE[bookidx].length; chapidx++)
				UserData.verses[bookidx][chapidx] =
					new Verse[Verse.BIBLE[bookidx][chapidx]];
		}

		// actual number of lines can be different from 
		// what file type says, so we guess it.
		final int numlines = UserData.numverses = countLines(loadpath);
		if (numlines < ProgramData.NT_LINES)
		{
			throw new IOException()
			{
				public String getMessage()
				{
					return "load error: not enough lines in file to merit even a NT: "
						+ Integer.toString(numlines);
				}
			};
		}
		else if (numlines >= ProgramData.NT_LINES
			&& numlines < ProgramData.OT_LINES)
		{
			programdata.maxprogress = ProgramData.NT_LINES;
			UserData.filetype = Filetype.nt;
		}
		else if (numlines >= ProgramData.OT_LINES
			&& numlines < ProgramData.ONT_LINES)
		{
			programdata.maxprogress = ProgramData.OT_LINES;
			UserData.filetype = Filetype.ot;
		}
		else
		{
			programdata.maxprogress = ProgramData.ONT_LINES;
			UserData.filetype = Filetype.ont;
		}

		// lock file being read.
		FileInputStream inputstream = new FileInputStream(loadpath);
		inputstream.getChannel().tryLock(0L, Long.MAX_VALUE, true);
		reader = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));

		// determine which book to count from.
		int bookstartidx = UserData.filetype == Filetype.nt ? 39 : 0;
		int totalbooks =
			UserData.filetype == Filetype.ot ? 39 : Verse.BIBLE.length;

		// parse each word to be put into array of verses.
		for (int bookidx = bookstartidx; bookidx < totalbooks; bookidx++)
			for (int chapidx = 0; chapidx < Verse.BIBLE[bookidx].length; chapidx++)
				for (int verseidx = 0; verseidx < Verse.BIBLE[bookidx][chapidx]; verseidx++)
				{
					UserData.verses[bookidx][chapidx][verseidx] =
						new Verse(bookidx, chapidx, verseidx, reader.readLine(),
							regex, translationcapture, greekcapture, strongscapture,
							morphcapture);

					// update progress bar.
					programdata.progress++;
				}
		reader.close();
		inputstream.close();

		UserData.loadpath = loadpath;
		UserData.regex = regex;
		UserData.transcapture = translationcapture;
		UserData.greekcapture = greekcapture;
		UserData.strongscapture = strongscapture;
		UserData.morphcapture = morphcapture;
		UserData.bookidx =
			UserData.chapidx = UserData.verseidx = UserData.wordidx = 0;

		indexWordRoots();
	}

	void save() throws IOException
	{
		saveAs(UserData.loadpath);
	}

	/**
	 * save changes to file. 
	 * makes a temp file to store written info, then moves
	 * it to the destination directory afterwards.
	 * @throws IOException 
	 */
	void saveAs(String savepath) throws IOException
	{
		// file must be writable.
		File savefile = new File(savepath);
		if (savefile.exists() && !savefile.canWrite())
			throw new IOException()
			{
				public String getMessage()
				{
					return "file not writable";
				}
			};

		// make temporary file for writing.
		File tempfile = File.createTempFile("temp_ont", ".ont");
		writer =
			new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				tempfile), "UTF-8"));

		// write byte order mark for UTF-8.
		writer.write('\ufeff');

		logger =
			new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				"ONTeditor_log.txt", true), "UTF-8"));
		logger.append("\nAttempted to write the following on " + date + ":\n");

		int totallines = countLines(UserData.loadpath);

		// write only changed lines, and copy rest.
		reader =
			new BufferedReader(new InputStreamReader(new FileInputStream(
				UserData.loadpath), "UTF-8"));
		for (int linenum = 1; linenum <= totallines; linenum++)
		{
			String nextline = reader.readLine();

			// check if line is changed.
			if (UserData.changedverses.containsKey(linenum))
			{
				Verse changedverse = UserData.changedverses.get(linenum);

				// log to ONTeditor_log.txt.
				logger.append(linenum + "(" + Verse.BOOKS[changedverse.bookidx][0]
					+ changedverse.chapidx + ":" + changedverse.verseidx + "):"
					+ changedverse + "\n");

				// write changed line.
				writer.write(changedverse + "\n");
			}
			// write original line if not changed.
			else
				writer.write(nextline + "\n");
		}
		writer.close();
		reader.close();
		logger.close();

		// copy temporary file to savepath.
		Files.copy(tempfile.toPath(), Paths.get(savepath),
			StandardCopyOption.REPLACE_EXISTING);

		UserData.changedverses.clear();
		UserData.loadpath = savepath;
	}

	/**
	 * save settings and quit program
	 */
	void exit()
	{
		try
		{
			ObjectOutputStream output =
				new ObjectOutputStream(new FileOutputStream(
					ProgramData.SETTINGS_FILE));
			output.writeObject(programdata);
			output.close();
		}
		catch (IOException i)
		{
			i.printStackTrace();
		}
		programdata.mainframe.dispose();
	}
}
