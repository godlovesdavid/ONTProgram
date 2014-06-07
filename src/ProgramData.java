import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JFrame;

class ProgramData implements Serializable
{
	Font font;
	boolean showgreek, showstrongs, showmorph, smartpunctuationmode;
	int progress, maxprogress; //for progress bar
	Dimension dimensions;
	transient JFrame mainframe; //not serializable

	static File SETTINGS_FILE = new File("settings.ser");
	static Font MONOSPACED_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	static Font SANSSERIF_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	static Font SERIF_FONT = new Font(Font.SERIF, Font.PLAIN, 12);
	Font GREEKUNCIAL_FONT;
	static int NT_LINES = 7957;
	static int OT_LINES = 23145;
	static int ONT_LINES = 31102;

	ProgramData()
	{
		//initialize custom font.
		try
		{
			GREEKUNCIAL_FONT =
				Font.createFont(Font.TRUETYPE_FONT, new File("GreekUncial.otf"))
					.deriveFont(12f);
		}
		catch (FontFormatException | IOException e)
		{
			e.printStackTrace();
			GREEKUNCIAL_FONT = null;
		}

		showgreek = showstrongs = showmorph = true;
		smartpunctuationmode = false;
		font = GREEKUNCIAL_FONT;
		progress = 0;
		maxprogress = 1;

		mainframe = new JFrame("ONT editor");
		mainframe.setSize(dimensions = new Dimension(1280, 800));
	}
}