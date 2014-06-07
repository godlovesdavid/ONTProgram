import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class NavigatorPane extends JToolBar implements ChangeListener
{
	ONTProgram program;

	JButton openbutton, savebutton, saveasbutton, searchbutton;
	JToggleButton togglesmartpunct;
	JButton prevchapbutton, nextchapbutton, nextbookbutton, prevbookbutton;
	JTextField jumpfield;
	static Pattern jumptoregexpattern = Pattern
		.compile("(\\d*[a-zA-Z ]+)(\\d*)[:\\. ]*(\\d*)");

	NavigatorPane(final ONTProgram program)
	{
		this.program = program;
		setRollover(true);

		savebutton = new JButton();
		openbutton = new JButton();
		saveasbutton = new JButton();
		searchbutton = new JButton();
		togglesmartpunct = new JToggleButton();
		prevchapbutton = new JButton("<chap");
		nextchapbutton = new JButton("chap>");
		prevbookbutton = new JButton("<book");
		nextbookbutton = new JButton("book>");
		jumpfield = new JTextField("verse");

		openbutton.setText("open");
		openbutton.setFocusable(false);
		openbutton.setHorizontalTextPosition(SwingConstants.CENTER);
		openbutton.setVerticalTextPosition(SwingConstants.BOTTOM);
		openbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (program.filechooser.showOpenDialog(NavigatorPane.this) == JFileChooser.APPROVE_OPTION)
					program.loadfilepane.show(program.filechooser.getSelectedFile()
						.getAbsolutePath());
			}
		});
		add(openbutton);

		savebutton.setText("save");
		savebutton.setFocusable(false);
		savebutton.setHorizontalTextPosition(SwingConstants.CENTER);
		savebutton.setVerticalTextPosition(SwingConstants.BOTTOM);
		savebutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					program.filer.save();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					program.errorpane.showError(e.getMessage());
				}
			}
		});
		add(savebutton);

		saveasbutton.setText("save as");
		saveasbutton.setFocusable(false);
		saveasbutton.setHorizontalTextPosition(SwingConstants.CENTER);
		saveasbutton.setVerticalTextPosition(SwingConstants.BOTTOM);
		saveasbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (program.filechooser.showSaveDialog(NavigatorPane.this) == JFileChooser.APPROVE_OPTION)
					try
					{
						program.filer.saveAs(program.filechooser.getSelectedFile()
							.getAbsolutePath());
						program.refreshViews();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
						program.errorpane.showError(e1.getMessage());
					}
			}
		});
		add(saveasbutton);

		searchbutton.setText("search");
		searchbutton.setFocusable(false);
		searchbutton.setHorizontalTextPosition(SwingConstants.CENTER);
		searchbutton.setVerticalTextPosition(SwingConstants.BOTTOM);
		add(searchbutton);
		add(new JToolBar.Separator());
		togglesmartpunct.setSelected(program.programdata.smartpunctuationmode);
		togglesmartpunct.setText("smart punctuation");
		togglesmartpunct.setFocusable(false);
		togglesmartpunct.setHorizontalTextPosition(SwingConstants.CENTER);
		togglesmartpunct.setVerticalTextPosition(SwingConstants.BOTTOM);
		togglesmartpunct.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.programdata.smartpunctuationmode =
					togglesmartpunct.isSelected();
			}
		});
		add(togglesmartpunct);
		add(new JToolBar.Separator());
		prevbookbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.navigator.rollBook(false);
				program.refreshViews();
			}
		});
		add(prevbookbutton);
		nextbookbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.navigator.rollBook(true);
				program.refreshViews();
			}
		});
		add(nextbookbutton);
		jumpfield.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//get regex pattern
				Matcher matcher = jumptoregexpattern.matcher(jumpfield.getText());
				matcher.find();
				if (!matcher.matches())
					return;
				else
				{
					//set current verse to found verse
					program.navigator.set(matcher.group(1), matcher.group(2)
						.isEmpty() ? 1 : Integer.parseInt(matcher.group(2)), matcher
						.group(3).isEmpty() ? 1 : Integer.parseInt(matcher.group(3)));
					program.refreshViews();
				}
			}
		});
		add(jumpfield);
		prevchapbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.navigator.rollChapter(false);
				program.refreshViews();
			}
		});
		add(prevchapbutton);
		nextchapbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.navigator.rollChapter(true);
				program.refreshViews();
			}
		});
		add(nextchapbutton);
		setBounds(0, 0, 660, 40);
	}

	/**
	 * refresh method simply updates the text that shows what verse we're at
	 */
	public void stateChanged(ChangeEvent e)
	{
		jumpfield
			.setText(Verse.BOOKS[UserData.bookidx][Verse.BOOKS[UserData.bookidx].length - 1]
				+ " "
				+ Integer.toString(UserData.chapidx + 1)
				+ ":"
				+ Integer.toString(UserData.verseidx + 1));
	}
}
