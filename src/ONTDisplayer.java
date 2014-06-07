import java.awt.Font;

class ONTDisplayer
{
	ProgramData programdata;

	ONTDisplayer(ProgramData programdata)
	{
		this.programdata = programdata;
	}

	void showGreek()
	{
		programdata.showgreek = true;
	}

	void showStrongs()
	{
		programdata.showstrongs = true;
	}

	void showMorph()
	{
		programdata.showmorph = true;
	}

	void hideGreek()
	{
		programdata.showgreek = false;
	}

	void hideStrongs()
	{
		programdata.showstrongs = false;
	}

	void hideMorph()
	{
		programdata.showstrongs = false;
	}

	void setFont(Font font)
	{
		programdata.font = font;
	}
}
