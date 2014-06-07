import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class LoadFilePane extends JInternalFrame implements ChangeListener
{
	JLabel instructionlabel1, instructionlabel2, instructionlabel3,
		instructionlabel4, instructionlabel5, instructionlabel6,
		instructionlabel7, instructionlabel8;
	JButton okbutton, cancelbutton;
	JTextField regexfield, strongscapturefield, transcapturefield,
		greekcapturefield, morphcapturefield;
	ONTProgram program;
	String loadpath;

	LoadFilePane(final ONTProgram program)
	{
		super(null);
		setTitle("opening file");
		this.program = program;
		setBounds(100, 100, 600, 400);

		setLayer(1);
		okbutton = new JButton("ok");
		cancelbutton = new JButton("cancel");
		regexfield = new JTextField();
		regexfield.setFont(ProgramData.MONOSPACED_FONT);
		transcapturefield = new JTextField();
		transcapturefield.setFont(ProgramData.MONOSPACED_FONT);
		greekcapturefield = new JTextField();
		greekcapturefield.setFont(ProgramData.MONOSPACED_FONT);
		strongscapturefield = new JTextField();
		strongscapturefield.setFont(ProgramData.MONOSPACED_FONT);
		morphcapturefield = new JTextField();
		morphcapturefield.setFont(ProgramData.MONOSPACED_FONT);
		instructionlabel1 =
			new JLabel(
				"Enter regex (Java flavor) describing a word of a verse, indicating captures (searches line by line):");
		instructionlabel2 = new JLabel("(example: <wt>(.*?)<W[HG](\\d+)>)");
		instructionlabel3 =
			new JLabel(
				"Enter the capture(s) that go into each part of the word element:");
		instructionlabel4 = new JLabel("Translation");
		instructionlabel5 = new JLabel("Greek");
		instructionlabel6 = new JLabel("Strong's number");
		instructionlabel7 = new JLabel("Morphology");
		instructionlabel8 = new JLabel("(example: $1)");

		instructionlabel2.setForeground(Color.gray);
		instructionlabel8.setForeground(Color.gray);
		/*
		 * ok button to load file
		 */
		okbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				hide();
				program.progresspane.showProgress();

				//try to load the file out of the typed info
				new Thread(new Runnable()
				{
					public void run()
					{
						try
						{
							//open file
							program.filer.open(loadpath, regexfield.getText(),
								transcapturefield.getText(), greekcapturefield
									.getText(), strongscapturefield.getText(),
								morphcapturefield.getText());
						}
						catch (IOException ex)
						{
							ex.printStackTrace();
							program.errorpane.showError("Regex pattern error:"
								+ ex.getMessage());
						}

						program.refreshViews();
					}
				}).start();
			}
		});
		cancelbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				hide();
			}
		});
		add(okbutton);
		okbutton.setBounds(311, 242, 73, 23);
		add(instructionlabel1);
		instructionlabel1.setBounds(0, 0, 550, 26);
		add(instructionlabel2);
		instructionlabel2.setBounds(0, 30, 463, 26);
		add(regexfield);
		regexfield.setBounds(0, 60, 500, 30);
		add(instructionlabel3);
		instructionlabel3.setBounds(0, 99, 463, 23);
		add(instructionlabel4);
		instructionlabel4.setBounds(0, 131, 89, 14);
		add(instructionlabel5);
		instructionlabel5.setBounds(0, 169, 89, 14);
		add(instructionlabel6);
		instructionlabel6.setBounds(0, 207, 89, 14);
		add(instructionlabel7);
		instructionlabel7.setBounds(0, 246, 89, 14);
		add(instructionlabel8);
		instructionlabel8.setBounds(311, 131, 152, 14);
		add(cancelbutton);
		cancelbutton.setBounds(390, 242, 73, 23);
		add(transcapturefield);
		transcapturefield.setBounds(93, 128, 212, 28);
		add(greekcapturefield);
		greekcapturefield.setBounds(93, 166, 212, 28);
		add(strongscapturefield);
		strongscapturefield.setBounds(93, 204, 212, 28);
		morphcapturefield.setBounds(93, 250, 212, 28);
		add(morphcapturefield);
		stateChanged(null);
	}

	void show(String loadpath)
	{
		this.loadpath = loadpath;
		show();
	}

	public void stateChanged(ChangeEvent e)
	{
		regexfield.setText(UserData.regex);
		transcapturefield.setText(UserData.transcapture);
		greekcapturefield.setText(UserData.greekcapture);
		strongscapturefield.setText(UserData.strongscapture);
		morphcapturefield.setText(UserData.morphcapture);
	}
}
