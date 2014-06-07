import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.northwestern.at.morphadorner.corpuslinguistics.lemmatizer.Lemmatizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.lemmatizer.LemmatizerFactory;

/**
 * Word class containing translation, greek, strongs tag, morph tag as text.
 * @author david
 *
 */
class Word implements Serializable
{
	static Lemmatizer lemmatizer = LemmatizerFactory.newLemmatizer();
	Verse verse;
	//warning: don't directly edit translation field
	String translation, greek, strongs, morph;
	String root;

	/**
	 * constructor of Word. auto lemmatizes.
	 * @param translation the translation of the info
	 * @param greek the greek word
	 * @param strongs the strongs number
	 * @param morph the morphology info
	 */
	Word(Verse verse, String translation, String greek, String strongs,
		String morph)
	{
		this.translation = (translation == null ? "" : translation);
		this.greek = (greek == null ? "" : greek);
		this.strongs = (strongs == null ? "" : strongs);
		this.morph = (morph == null ? "" : morph);
		this.verse = verse;

		root = parseRoot(translation);
	}

	public String toString()
	{
		return translation + greek
			+ (strongs.isEmpty() ? "" : "<WG" + strongs + ">")
			+ (morph.isEmpty() ? "" : "<WT" + morph + ">");
	}

	/**
	 * give lemma of translation
	 * @param translation
	 * @return
	 */
	static String parseRoot(String translation)
	{
		Matcher matcher =
			Pattern.compile("[^ ]++$").matcher(
				translation.trim().replaceAll("<[^>]+>[^<]+<[^>]+>", "")
					.replaceAll("[\\.,!;:?$\\-\\)\\(\\*\"'\\]\\[\\{\\}<>]", "")
					.toLowerCase());

		if (matcher.find())
			return lemmatizer.lemmatize(matcher.group());

		return "";
	}
}