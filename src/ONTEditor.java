import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class ONTEditor
{
	private Stack<UndoableCommand> undostack, redostack;
	private List<UndoableCommand> commandbatch; //not init'd in constructor yet
	private boolean joinedcommandmode;
	ProgramData programdata;

	ONTEditor(ProgramData programdata)
	{
		this.programdata = programdata;
		undostack = new Stack<UndoableCommand>();
		redostack = new Stack<UndoableCommand>();
	}

	/**
	 * given book, chapter, and verse indices, return line number in the ONT
	 * file.
	 */
	int verseToLine(int bookidx, int chapidx, int verseidx)
	{
		int bookstartidx = 0;
		if (UserData.filetype == ONTFiler.Filetype.nt)
			bookstartidx = 39;

		int versessum = 0;
		for (int i = bookstartidx; i <= bookidx; i++)
			for (int j = 0; j < Verse.BIBLE[i].length; j++)
				if (i == bookidx && j == chapidx)
				{
					versessum += verseidx + 1;
					break;
				}
				else
					versessum += Verse.BIBLE[i][j];
		return versessum;
	}

	/**
	 * mode for multiple undoes at a time to be possible.
	 */
	void turnOnBatchMode()
	{
		joinedcommandmode = true;
		commandbatch = new ArrayList<UndoableCommand>();
	}

	void turnOffBatchMode()
	{
		joinedcommandmode = false;
		undostack.push(new CompositeCommand(commandbatch));
	}

	void remove(Word word)
	{
		pushOntoUndoStack(new RemoveWordCommand(word.verse, UserData.wordidx));
	}

	void add(Word word)
	{
		pushOntoUndoStack(new AddWordCommand(word.verse,  UserData.wordidx, word));
	}

	void set(Word oldword, Word newword)
	{
		//		//cancel if all fields are same
		//		boolean changed = false;
		//		try
		//		{
		//			for (Field field : Word.class.getDeclaredFields())
		//				if (field.get(oldword).equals(field.get(newword)))
		//				{
		//					changed = true;
		//					System.out.println(field.get(oldword) + " " + field.get(newword));
		//					break;
		//				}
		//		}
		//		catch (IllegalArgumentException | IllegalAccessException e)
		//		{
		//			e.printStackTrace();
		//		}
		//		if (changed)
		pushOntoUndoStack(new ChangeWordCommand(oldword, newword));
	}

	/**
	 * pushes command onto undoable stack.
	 * also marks verse as changed and clears redostack.
	 * @param command
	 */
	void pushOntoUndoStack(UndoableCommand command)
	{
		if (joinedcommandmode)
			commandbatch.add(command);
		else
			undostack.push(command);
		UserData.changedverses.put(verseToLine(command.bookidx, command.chapidx,
			command.verseidx),
			UserData.verses[command.bookidx][command.chapidx][command.verseidx]);
		redostack.clear();
	}

	/**
	 * use stack to undo.
	 */
	void undo()
	{
		if (!undostack.empty())
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
	}

	/**
	 * use stack to redo.
	 */
	void redo()
	{
		if (!redostack.empty())
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

	void cut()
	{
	}

	void copy()
	{
	}

	void paste()
	{
	}
}
