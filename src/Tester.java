import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextPane;

import edu.northwestern.at.morphadorner.corpuslinguistics.inflector.InflectorFactory;
import edu.northwestern.at.morphadorner.corpuslinguistics.inflector.Person;
import edu.northwestern.at.morphadorner.corpuslinguistics.inflector.VerbTense;
import edu.northwestern.at.morphadorner.corpuslinguistics.lemmatizer.LemmatizerFactory;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.PartOfSpeech;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.PartOfSpeechTagsFactory;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.simplerulebased.SimpleRuleBasedTagger;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.suffix.SuffixTagger;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.unigram.UnigramTagger;

class Tester
{
	public static void main(String[] args) throws Exception
	{
		//		System.out.println(12. / 100);
		Verse verse = new Verse(1, 5, 3, "verse");
		Verse verse1 = new Verse(2, 1, 2, "verse1");
		Verse verse2 = new Verse(4, 0, 0, "verse2");
		Verse verse3 = new Verse(1, 5, 4, "verse3");
		//				Word word1 = new Word(verse, "testing", "1", "1", "1");
		//				Word word2 = new Word(verse, "2", "2", "2", "2");
		//				Word word3 = word1;
		//		UndoableCommand command = new ChangeWordCommand(word1, word2);
		//		command.undo();
		//		command.redo();
		//		command.undo();
		//
		//		int x, y, z = y = x = 0;
		//		LinkedList<Entry<Verse, Integer>> list =
		//			new LinkedList<Entry<Verse, Integer>>();
		//		list.add();
		//		System.out.println(x);
		//
		//		Map<Verse, Integer> map = new HashMap<Verse, Integer>();
		//		map.put(verse, 1);
		//		map.put(verse, 2);

		//		HashMap<String, List>[] x;
		//x = new HashMap<String, List>[2];
		//		Map<String, Integer> map = new HashMap<String, Integer>();
		//		map.put("test", 5);
		//		map.put("test2", 2);
		//		map.put("test3", 3);
		//
		//		Matcher matcher =
		//			Pattern.compile("[^ ]++$").matcher(
		//				word1.translation.trim().replaceAll(
		//					"[\\.,!;:?\\-\\)\\(\\*\"'\\]\\[\\{\\}<>]", "").toLowerCase());
		//		matcher.find();
		//		System.out.println(LemmatizerFactory.newLemmatizer().lemmatize(
		//			matcher.group()));
		//		UnigramTagger tagger = new UnigramTagger();
		//		SuffixTagger tagger2 = new SuffixTagger();
		//		InflectorFactory.newInflector().conjugate("try",
		//			VerbTense.PAST_PARTICIPLE, Person.FIRST_PERSON_SINGULAR);
		//		PartOfSpeech pos = new PartOfSpeech();
		//		pos.setLemmaWordClass("verb");
		//		SimpleRuleBasedTagger tagger1 = new SimpleRuleBasedTagger();
		//		tagger1.tagWord("lost");
		//		System.out.println(PartOfSpeechTagsFactory.newPartOfSpeechTags()
		//			.getLemmaWordClass(tagger1.getMostCommonTag("lost")));
		Field field = Word.class.getDeclaredField("translation");
		//		JTextPane pane = new JTextPane();
		//		pane.setText("<color=red>hi!</color>");

		SearchResultsList list = new SearchResultsList(field, null);
		list.addElement(verse, 0);
		list.addElement(verse1, 0);
		list.addElement(verse2, 0);
		list.addElement(verse3, 0);
		quicksort(list, 0, list.size() - 1);
		//		System.out.println(list);

		System.out.println(new Verse(0, 0, 0,
			"He sent from on high, he took me; he drew me out of many waters.",
			"([ ,.;'\"?!—\\-:)(]*.+?)(?=[ ,.;:'\"?!—\\-)(]|$)", "$1", "",
			"", "").toEnglish());
	}

	static void quicksort(SearchResultsList list, int start, int end)
	{
		if (start < end)
		{
			//partition.
			Verse verse = list.get(end);
			int i = start - 1;
			for (int j = start; j < end; j++)
			{
				if (list.get(j).compareTo(verse) <= 0)
				{
					i = i + 1;

					//swap
					Verse temp = list.get(i);
					list.set(list.indexOf(list.get(i)), list.get(j));
					list.set(list.indexOf(list.get(j)), temp);
				}
			}

			//swap
			Verse temp = list.get(i + 1);
			list.set(list.indexOf(list.get(i + 1)), list.get(end));
			list.set(list.indexOf(list.get(end)), temp);

			int q = i + 1;

			quicksort(list, start, q - 1);
			quicksort(list, q + 1, end);
		}
	}

	//	static void test()
	//	{
	//		System.out.println("test");
	//	}
}
