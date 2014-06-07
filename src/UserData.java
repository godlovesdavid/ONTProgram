import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * static fields if you want only one load instance at a time
 * @author roger
 *
 */
class UserData implements Serializable
{
	//position
	static int bookidx = 0;
	static int chapidx = 0;
	static int verseidx = 0;
	static int wordidx = 0;

	//regex settings
	static String regex =
		"(<(?:RF|FI|TS1)>[^<]*<(?:Rf|Fi|Ts1)>)|([^Α-Ω]*?)([Α-Ω ]*)<WG([^>]*)>(?:<WT([^>]*)>)?|([^Α-Ω]+)$";
	static String transcapture = "$1$2$6";
	static String greekcapture = "$3";
	static String strongscapture = "$4";
	static String morphcapture = "$5";

	//for saving purposes
	static String loadpath = "";
	static Map<Integer, Verse> changedverses = new TreeMap<Integer, Verse>();

	//map from strongs to translation root to words
	static Map<String, Map<String, List<Word>>> words =
		new HashMap<String, Map<String, List<Word>>>();

	//init'd during program run
	static Verse[][][] verses;
	static ONTFiler.Filetype filetype;
	static int numverses;
}