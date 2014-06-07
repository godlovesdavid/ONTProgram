import java.util.Stack;

public class ONTNavigator
{
	ProgramData programdata;

	Stack<UndoableCommand> undostack, redostack;

	ONTNavigator(ProgramData programdata)
	{
		this.programdata = programdata;

		undostack = new Stack<UndoableCommand>();
		redostack = new Stack<UndoableCommand>();
	}

	void rollBook(boolean up)
	{
		if (up)
		{
			UserData.bookidx = (UserData.bookidx + 1) % Verse.BIBLE.length;
		}
		else
		{
			UserData.bookidx =
				(UserData.bookidx - 1 + Verse.BIBLE.length) % Verse.BIBLE.length;
		}

		UserData.chapidx = UserData.verseidx = UserData.wordidx = 0;
	}

	void rollChapter(boolean up)
	{
		if (up)
		{
			if (UserData.chapidx == Verse.BIBLE[UserData.bookidx].length - 1)
			{
				rollBook(true);
				return;
			}
			else
			{
				UserData.chapidx =
					(UserData.chapidx + 1) % Verse.BIBLE[UserData.bookidx].length;
			}
		}
		else
		{
			if (UserData.chapidx == 0)
			{
				rollBook(false);
			}

			UserData.chapidx =
				(UserData.chapidx - 1 + Verse.BIBLE[UserData.bookidx].length)
					% Verse.BIBLE[UserData.bookidx].length;
		}

		UserData.verseidx = UserData.wordidx = 0;
	}

	void rollVerse(boolean up)
	{
		if (up)
		{
			if (UserData.verseidx == Verse.BIBLE[UserData.bookidx][UserData.chapidx] - 1)
			{
				rollChapter(true);
				return;
			}
			else
			{
				UserData.verseidx =
					(UserData.verseidx + 1)
						% Verse.BIBLE[UserData.bookidx][UserData.chapidx];
			}
		}
		else
		{
			if (UserData.verseidx == 0)
			{
				rollChapter(false);
			}

			UserData.verseidx =
				(UserData.verseidx - 1 + Verse.BIBLE[UserData.bookidx][UserData.chapidx])
					% Verse.BIBLE[UserData.bookidx][UserData.chapidx];
		}

		UserData.wordidx = 0;
	}

	void rollWord(boolean up)
	{
		if (up)
		{
			if (UserData.wordidx == UserData.verses[UserData.bookidx][UserData.chapidx][UserData.verseidx]
				.size() - 1)
			{
				rollVerse(true);
				return;
			}
			else
			{
				UserData.wordidx =
					(UserData.wordidx + 1)
						% UserData.verses[UserData.bookidx][UserData.chapidx][UserData.verseidx]
							.size();
			}
		}
		else
		{
			if (UserData.wordidx == 0)
			{
				rollVerse(false);
			}
			UserData.wordidx =
				(UserData.wordidx - 1 + UserData.verses[UserData.bookidx][UserData.chapidx][UserData.verseidx]
					.size())
					% UserData.verses[UserData.bookidx][UserData.chapidx][UserData.verseidx]
						.size();
		}
	}

	/**
	 * Go to a specific place (zero-based).
	 */
	void set(int bookidx, int chapidx, int verseidx, int wordidx)
	{
		// remember position.
		int oldbookidx = UserData.bookidx;
		int oldchapidx = UserData.chapidx;
		int oldverseidx = UserData.verseidx;

		//get book.
		UserData.bookidx =
			bookidx >= UserData.verses.length ? UserData.verses.length - 1
				: bookidx;

		// get chapter.
		UserData.chapidx =
			chapidx >= UserData.verses[UserData.bookidx].length
				? UserData.verses[UserData.bookidx].length - 1 : chapidx;

		// get verse.
		UserData.verseidx =
			verseidx >= UserData.verses[UserData.bookidx][UserData.chapidx].length
				? UserData.verses[UserData.bookidx][UserData.chapidx].length - 1
				: verseidx;

		//get word.
		UserData.wordidx =
			wordidx >= UserData.verses[UserData.bookidx][UserData.chapidx][UserData.verseidx]
				.size()
				? UserData.verses[UserData.bookidx][UserData.chapidx][UserData.verseidx]
					.size() - 1 : wordidx;

		//save position.
		undostack.push(new NavigateCommand(oldbookidx, oldchapidx, oldverseidx,
			UserData.bookidx, UserData.chapidx, UserData.verseidx));
	}

	/**
	 * Go to a specific verse in readable form.
	 */
	void set(String bookname, int chapnum, int versenum)
	{
		// remove spaces from book.
		bookname = bookname.replaceAll(" ", "");

		// look up book index.
		int bookidx = 0;
		for (int i = 0; i < Verse.BOOKS.length; i++)
			for (int j = 0; j < Verse.BOOKS[i].length; j++)
				if (Verse.BOOKS[i][j].equalsIgnoreCase(bookname))
				{
					bookidx = i;
					break;
				}

		set(bookidx, chapnum - 1, versenum - 1, 0);
	}

	void goBack()
	{
		UndoableCommand command = undostack.pop();
		command.undo();
		redostack.push(command);

		//jump to commanded area.
		UserData.bookidx = command.bookidx;
		UserData.chapidx = command.chapidx;
		UserData.verseidx = command.verseidx;
		UserData.wordidx = command.wordidx;
	}

	void goForward()
	{
		UndoableCommand command = redostack.pop();
		command.redo();
		undostack.push(command);

		//jump to commanded area.
		UserData.bookidx = command.bookidx;
		UserData.chapidx = command.chapidx;
		UserData.verseidx = command.verseidx;
		UserData.wordidx = command.wordidx;
	}
}
