import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

class ONTSearcher
{
	ProgramData programdata;

	ONTSearcher(ProgramData programdata)
	{
		this.programdata = programdata;
	}

	/**
	 * search for a word. returns the verse containing the word and the index of the word
	 * @param field which field of the word, ie. translation, greek, morph, or 
	 * @param string
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	SearchResultsList search(String field, String string)
		throws NoSuchFieldException, SecurityException, IllegalArgumentException,
		IllegalAccessException
	{
		if (field == "strongs")
			return searchStrongs(string);

		// determine whether to count from book 1 or book 40, which is of
		// the New Testament
		int bookstartidx = UserData.filetype == ONTFiler.Filetype.nt ? 39 : 0;
		int totalbooks =
			UserData.filetype == ONTFiler.Filetype.ot ? 39 : Verse.BIBLE.length;

		Field wordfield = Word.class.getDeclaredField(field);
		SearchResultsList list = new SearchResultsList(wordfield, string);
		for (int bookidx = bookstartidx; bookidx < totalbooks; bookidx++)
			for (int chapidx = 0; chapidx < UserData.verses[bookidx].length; chapidx++)
				for (int verseidx = 0; verseidx < UserData.verses[bookidx][chapidx].length; verseidx++)
					for (int wordidx = 0; wordidx < UserData.verses[bookidx][chapidx][verseidx]
						.size(); wordidx++)
					{
						Verse verse = UserData.verses[bookidx][chapidx][verseidx];
						if (wordfield.get(verse.get(wordidx)).toString().contains(
							string))
							list.addElement(verse, wordidx);
					}

		list.sort(0, list.getSize() - 1);

		return list;
	}

	/**
	 * search strongs index
	 * @param string
	 * @return search results list
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	SearchResultsList searchStrongs(String strongs) throws NoSuchFieldException,
		SecurityException
	{
		SearchResultsList list =
			new SearchResultsList(Word.class.getDeclaredField("strongs"), strongs);

		for (List<Word> roots : UserData.words.get(strongs).values())
			for (Word word : roots)
				list.addElement(word.verse, word.verse.indexOf(word));

		list.sort(0, list.getSize() - 1);

		return list;
	}

}

/**
 * list that holds the search results.
 * @author david
 *
 */
class SearchResultsList extends DefaultListModel<Verse>
{
	Field searchfield;
	String searchstring;
	List<Integer> wordindices;

	SearchResultsList(Field searchfield, String searchstring)
	{
		this.searchfield = searchfield;
		this.searchstring = searchstring;
		wordindices = new ArrayList<Integer>();
	}

	void addElement(Verse verse, int wordidx)
	{
		addElement(verse);
		wordindices.add(wordidx);
	}

	public boolean removeElement(Object obj)
	{
		if (super.removeElement(obj)
			&& wordindices.remove((Integer) indexOf(obj)))
			return true;
		return false;
	}

	public Verse remove(int i)
	{
		wordindices.remove(i);
		return super.remove(i);
	}

	public void clear()
	{
		wordindices.clear();
		super.clear();
	}

	int giveFoundWordIndex(int selectedIndex)
	{
		return wordindices.get(selectedIndex);
	}

	String giveSearchField()
	{
		if (searchfield != null)
			return searchfield.getName();
		return "";
	}

	/**
	 * sort a section of list.
	 * @param start start idx
	 * @param end end idx
	 */
	void sort(int start, int end)
	{
		if (start < end)
		{
			//partition.
			Verse verse = get(end);
			int i = start - 1;
			for (int j = start; j < end; j++)
				if (get(j).compareTo(verse) <= 0)
				{
					i = i + 1;

					//swap.
					Verse tempverse = get(i);
					Integer tempwordidx = wordindices.get(i);
					set(i, get(j));
					wordindices.set(i, wordindices.get(j));
					set(j, tempverse);
					wordindices.set(j, tempwordidx);
				}

			//swap.
			Verse tempverse = get(i + 1);
			Integer tempwordidx = wordindices.get(i + 1);
			set(i + 1, get(end));
			wordindices.set(i + 1, wordindices.get(end));
			set(end, tempverse);
			wordindices.set(end, tempwordidx);

			int q = i + 1;

			sort(start, q - 1);
			sort(q + 1, end);
		}
	}
}
