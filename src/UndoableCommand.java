import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * undoable/redoable command or edit
 */
abstract class UndoableCommand
{
	//for jumping purposes
	int bookidx, chapidx, verseidx, wordidx;

	UndoableCommand(int bookidx, int chapidx, int verseidx, int wordidx)
	{
		this.bookidx = bookidx;
		this.chapidx = chapidx;
		this.verseidx = verseidx;
		this.wordidx = wordidx;
	}

	abstract void undo();

	abstract void redo();
}

/**
 * this command adds word to a verse
 */
class AddWordCommand extends UndoableCommand
{
	Verse verse;
	int wordidx;
	Word word;

	AddWordCommand(Verse verse, int wordidx, Word word)
	{
		super(verse.bookidx, verse.chapidx, verse.verseidx, wordidx);
		this.verse = verse;
		this.word = word;
		this.wordidx = wordidx;

		redo();
	}

	void undo()
	{
		ChangeWordCommand.removeFromRootIndex(word);
		verse.remove(wordidx);
	}

	void redo()
	{
		verse.add(wordidx, word);
		ChangeWordCommand.addToRootIndex(word);
	}
}

/**
 * this command removes a word from a verse
 */
class RemoveWordCommand extends UndoableCommand
{
	Verse verse;
	Word word;
	int wordidx;

	RemoveWordCommand(Verse verse, int wordidx)
	{
		super(verse.bookidx, verse.chapidx, verse.verseidx, wordidx);
		this.verse = verse;
		this.wordidx = wordidx;
		word = verse.get(wordidx);

		redo();
	}

	void undo()
	{
		verse.add(wordidx, word);
		ChangeWordCommand.addToRootIndex(word);
	}

	void redo()
	{
		ChangeWordCommand.removeFromRootIndex(word);
		verse.remove(wordidx);
	}
}

/**
 * this command can change a word's contents
 */
class ChangeWordCommand extends UndoableCommand
{
	Word word;
	Word oldcontents, newcontents;

	ChangeWordCommand(Word word, Word newcontents)
	{
		super(newcontents.verse.bookidx, newcontents.verse.chapidx,
			newcontents.verse.verseidx, word.verse.indexOf(word));
		oldcontents =
			new Word(word.verse, word.translation, word.greek, word.strongs,
				word.morph);
		this.newcontents = newcontents;
		this.word = word;

		redo();
	}

	void undo()
	{
		//remove word from root index
		removeFromRootIndex(word);

		//change its contents to old word's contents
		for (Field field : Word.class.getDeclaredFields())
			try
			{
				field.set(word, field.get(oldcontents));
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}

		//add it back to root index
		addToRootIndex(word);
	}

	void redo()
	{
		//remove word from root index
		removeFromRootIndex(word);

		//change its contents to new word's contents
		for (Field field : Word.class.getDeclaredFields())
			try
			{
				field.set(word, field.get(newcontents));
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}

		//add it back to root index
		addToRootIndex(word);
	}

	static void removeFromRootIndex(Word word)
	{
		if (word.strongs.isEmpty())
			return;

		//remove word from root list
		UserData.words.get(word.strongs).get(word.root).remove(word);

		//if that was the last word with root,
		if (UserData.words.get(word.strongs).get(word.root).isEmpty())
		{
			//remove root entry.
			UserData.words.get(word.strongs).remove(word.root);

			//if that was the last strongs,
			if (UserData.words.get(word.strongs).isEmpty())
				//remove the strongs entry.
				UserData.words.remove(word.strongs);
		}
	}

	/**
	 * do the above reversed
	 * @param word
	 */
	static void addToRootIndex(Word word)
	{
		if (word.strongs.isEmpty())
			return;

		//if this is a new strongs number to be added
		if (!UserData.words.containsKey(word.strongs))
			//add it
			UserData.words.put(word.strongs, new HashMap<String, List<Word>>());

		//if this is a new root for this strongs
		if (!UserData.words.get(word.strongs).containsKey(word.root))
			//add it
			UserData.words.get(word.strongs)
				.put(word.root, new LinkedList<Word>());

		//put word in new root list
		UserData.words.get(word.strongs).get(word.root).add(word);
	}
}

/**
 * composite command executing a list of commands' undo/redo functions.
 * somewhat different from other commands in that its stored commands
 * have already been executed, so don't need to do so in the constructor.
 */
class CompositeCommand extends UndoableCommand
{
	//first on list was first executed
	List<UndoableCommand> commands;

	CompositeCommand(List<UndoableCommand> commands)
	{
		super(commands.get(commands.size() - 1).bookidx, commands.get(commands
			.size() - 1).chapidx, commands.get(commands.size() - 1).verseidx,
			commands.get(commands.size() - 1).wordidx);
		this.commands = commands;
	}

	void undo()
	{
		for (int i = commands.size() - 1; i >= 0; i--)
			commands.get(i).undo();
	}

	void redo()
	{
		for (UndoableCommand command : commands)
			command.redo();
	}
}

/**
 * this command can change a word's fields
 */
class NavigateCommand extends UndoableCommand
{
	int oldbookidx, oldchapidx, oldverseidx;
	int newbookidx, newchapidx, newverseidx;

	NavigateCommand(int oldbookidx, int oldchapidx, int oldverseidx,
		int newbookidx, int newchapidx, int newverseidx)
	{
		super(newbookidx, newchapidx, newverseidx, 0);
		this.oldbookidx = oldbookidx;
		this.oldchapidx = oldchapidx;
		this.oldverseidx = oldverseidx;
		this.newbookidx = newbookidx;
		this.newchapidx = newchapidx;
		this.newverseidx = newverseidx;
	}

	void undo()
	{
		bookidx = oldbookidx;
		chapidx = oldchapidx;
		verseidx = oldverseidx;
	}

	void redo()
	{
		bookidx = newbookidx;
		chapidx = newchapidx;
		verseidx = newverseidx;
	}
}