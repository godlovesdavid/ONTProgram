import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

/**
 * Drag and drop handler. Part of display panel, but must be put in its own file.
 * @author david
 *
 */
class WordDragger extends TransferHandler
{
	ONTProgram program;
	int[] selectedindices = null;
	DataFlavor localObjectFlavor;
	Object[] transferedObjects = null;

	public WordDragger(ONTProgram program)
	{
		this.program = program;
		localObjectFlavor =
			new ActivationDataFlavor(Object[].class,
				DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
	}

	/**
	 * make something draggable/droppable.
	 */
	protected Transferable createTransferable(JComponent component)
	{
		VerseBox versecontainer = (VerseBox) component;
		selectedindices = versecontainer.getSelectedIndices();
		transferedObjects = versecontainer.getSelectedValuesList().toArray();
		return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
	}

	public boolean canImport(TransferSupport info)
	{
		if (!info.isDrop() || !info.isDataFlavorSupported(localObjectFlavor))
			return false;
		return true;
	}

	public int getSourceActions(JComponent component)
	{
		return MOVE;
	}

	public boolean importData(TransferSupport dropinfo)
	{
		//get drop info
		if (!canImport(dropinfo))
			return false;

		JList.DropLocation droplocation =
			(JList.DropLocation) dropinfo.getDropLocation();
		Object[] objects = null;
		try
		{
			objects =
				(Object[]) dropinfo.getTransferable().getTransferData(
					localObjectFlavor);
		}
		catch (UnsupportedFlavorException | IOException e)
		{
			e.printStackTrace();
		}
		VerseBox versebox = (VerseBox) dropinfo.getComponent();
		Verse verse = (Verse) versebox.getModel();

		//indices[0] is the first selected idx before move. it's the idx of the one being moved.
		//destination index before actual move. it's the idx of the one being moved to.
		//following word index is the index of the word immediately following source word.
		int sourceidx = selectedindices[0];
		int destidx = droplocation.getIndex();
		int followidx = selectedindices[selectedindices.length - 1] + 1;

		//drop has occurred.
		program.editor.turnOnBatchMode();

		//smart punctuation handling.
		if (!(destidx >= sourceidx && destidx <= selectedindices[selectedindices.length - 1] + 1)
			&& program.programdata.smartpunctuationmode)
			doSmartPunctuation(verse, sourceidx, destidx, followidx);

		//drag and dropping.
		if (selectedindices != null && objects != null)
			doDragAndDrop(objects, versebox, verse, sourceidx, destidx);

		//finish.
		program.editor.turnOffBatchMode();

		return true;
	}

	/**
	 * smart punctuation part: moves punctuation into the correct place automatically when moving word(s)
	 * @param verse
	 * @param sourceidx
	 * @param destidx
	 * @param followidx
	 */
	void doSmartPunctuation(Verse verse, int sourceidx, int destidx,
		int followidx)
	{
		//get the words: sourceword is the word being moved, destination word is the word moved to, and following word is 1 word after sourceword
		Word sourceword = verse.get(sourceidx);
		Word destword = verse.get(destidx);
		Word followingword;
		if (followidx < verse.size())
			followingword = verse.get(followidx);
		else
		{ //make a following word if it doesn't exist
			followingword = new Word(verse, "", "", "", "");
			verse.addElement(followingword);
		}

		//get start of source word for future purposes
		String sourcestart = startOf(sourceword);

		/*
		 * if moving to before in sentence
		 */
		if (destidx < sourceidx)
		{
			/*
			 * capitalize (depends on punct part)
			 */
			if (hasCapitalizingPunct(destword)
				|| (destidx == 0 && (isCapitalized(destword) && !isProperNoun(destword))))
			{
				capitalize(sourceword);
				if (!isProperNoun(destword))
					lowercase(destword);
			}

			/*
			 * move punctuation
			 */
			//get start part of word up until the first letter
			String deststart = startOf(destword);
			if (hasPunct(deststart))
			{
				//move it..
				change(destword, destword.translation.substring(deststart.length()));
				//to source unless it already has punct
				if (!hasPunct(sourcestart))
					change(sourceword, deststart + sourceword.translation.trim());
			}
			//else if source has punct
			else if (hasPunct(sourcestart))
				//del it
				change(sourceword, sourceword.translation.substring(sourcestart
					.length()));

			/*
			 * add/del spaces (depends on punct part)
			 */
			//if the word is moved to the start of sentence
			if (destidx == 0)
			{
				removeSpace(sourceword);
				addSpace(destword);
			}
			//else it must be elsewhere, which requires a space if it doesn't have it and starts with letter
			else
			{
				if (startsWithLetter(sourceword))
					addSpace(sourceword);
				else if (startsWithLetter(destword))
					addSpace(destword);
			}
		}
		/*
		 * if moving to later in sentence
		 */
		else
		{
			/*
			 * capitalize
			 */
			if (hasCapitalizingPunct(sourceword)
				|| (sourceidx == 0 && (isCapitalized(sourceword) && !isProperNoun(sourceword))))
			{
				capitalize(followingword);
				if (!isProperNoun(sourceword))
					lowercase(sourceword);
			}

			/*
			 * move punct
			 */
			String followstart = startOf(followingword);
			if (hasPunct(sourcestart))
			{  //move it..
				change(sourceword, sourceword.translation.substring(sourcestart
					.length()));
				//to following word unless it already has punct
				if (!hasPunct(followstart))
					change(followingword, sourcestart
						+ followingword.translation.trim());
			}
			else if (hasPunct(followstart))
				//del it
				change(followingword, followingword.translation
					.substring(followstart.length()));

			/*
			 * add/del spaces
			 */
			//if the word moved to is from the start of sentence
			if (sourceidx == 0)
			{
				removeSpace(followingword);
				addSpace(sourceword);
			}
			//moved to somewhere in middle of sentence, requiring space
			else
			{
				if (startsWithLetter(sourceword))
					addSpace(sourceword);
				else if (startsWithLetter(followingword))
					addSpace(followingword);
			}
		}
	}

	/**
	 * drag and drop part
	 * @param dropinfo
	 * @param versebox
	 * @param verse
	 * @param sourceidx
	 * @param destidx
	 */
	void doDragAndDrop(Object[] objects, VerseBox versebox, Verse verse,
		int sourceidx, int destidx)
	{
		//drag.
		for (int i = objects.length - 1; i >= 0; i--)
		{
			UserData.wordidx = selectedindices[i];
			program.editor.remove(verse.get(selectedindices[i]));
		}

		//offset destination if moving later in the verse.
		for (int i = 0; i < objects.length; i++)
			if (selectedindices[i] < destidx)
				destidx--;

		if (destidx < 0)
			destidx = 0;
		else if (destidx >= verse.size())
			destidx = verse.size() - 1;

		//drop.
		UserData.wordidx = destidx;
		for (int i = objects.length - 1; i >= 0; i--)
		{
			program.editor.add((Word) objects[i]);
			versebox.addSelectionInterval(destidx, destidx);
		}
	}

	String startOf(Word word)
	{
		return word.translation.replaceFirst(
			"^([,\\. \\?\"';:\\-\\]\\[\\)\\(]*).*", "$1");
	}

	boolean hasPunct(String string)
	{
		Matcher matcher =
			Pattern.compile("[,\\.\\?\"';:\\-\\]\\[\\)\\(]").matcher(string);
		return matcher.find();
	}

	boolean hasCapitalizingPunct(Word word)
	{
		return word.translation.contains(".") || word.translation.contains("!")
			|| word.translation.contains("?") || word.translation.contains("\"");
	}

	boolean startsWithLetter(Word word)
	{
		if (word.translation.isEmpty())
			return false;
		return Character.isAlphabetic(word.translation.charAt(0));
	}

	boolean isCapitalized(Word word)
	{
		Matcher matcher =
			Pattern.compile("\\b\\p{Upper}").matcher(word.translation);
		return matcher.find();
	}

	boolean isProperNoun(Word word)
	{
		//temporarily call all nouns proper til I figure out how to detect them all
		return word.morph.contains("n-") || word.morph.contains("p-");
	}

	void lowercase(Word word)
	{
		if (word.translation.isEmpty())
			return;
		String regex = "\\b\\p{Alpha}";
		Matcher matcher = Pattern.compile(regex).matcher(word.translation);
		if (matcher.find())
			change(word, word.translation.replaceFirst(regex, matcher.group()
				.toLowerCase()));
	}

	void capitalize(Word word)
	{
		if (word.translation.isEmpty())
			return;
		String regex = "\\b\\p{Alpha}";
		Matcher matcher = Pattern.compile(regex).matcher(word.translation);
		if (matcher.find())
			change(word, word.translation.replaceFirst(regex, matcher.group()
				.toUpperCase()));
	}

	void addSpace(Word word)
	{
		change(word, " " + word.translation);
	}

	void removeSpace(Word word)
	{
		change(word, word.translation.trim());
	}

	void change(Word word, String newtranslation)
	{
		program.editor.set(word, new Word(word.verse, newtranslation, word.greek,
			word.morph, word.strongs));
	}
}