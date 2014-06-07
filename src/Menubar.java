import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class Menubar extends JMenuBar
{
	ONTProgram program;

	JMenuItem about, content, copy, cut, delete, open, paste, exit, saveAs,
		save;
	JMenu editMenu, fileMenu, helpMenu;

	Menubar(final ONTProgram program)
	{
		this.program = program;

		fileMenu = new JMenu();
		open = new JMenuItem();
		save = new JMenuItem();
		saveAs = new JMenuItem();
		exit = new JMenuItem();
		editMenu = new JMenu();
		cut = new JMenuItem();
		copy = new JMenuItem();
		paste = new JMenuItem();
		delete = new JMenuItem();
		helpMenu = new JMenu();
		content = new JMenuItem();
		about = new JMenuItem();
		fileMenu.setMnemonic('f');
		fileMenu.setText("File");

		open.setMnemonic('o');
		open.setText("Open");
		open.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (program.filechooser.showOpenDialog(Menubar.this) == JFileChooser.APPROVE_OPTION)
					program.loadfilepane.show(program.filechooser.getSelectedFile()
						.getAbsolutePath());
			}
		});
		fileMenu.add(open);

		save.setMnemonic('s');
		save.setText("Save");
		save.addActionListener(new ActionListener()
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
		fileMenu.add(save);

		saveAs.setMnemonic('a');
		saveAs.setText("Save As ...");
		saveAs.setDisplayedMnemonicIndex(5);
		saveAs.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (program.filechooser.showSaveDialog(Menubar.this) == JFileChooser.APPROVE_OPTION)
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
		fileMenu.add(saveAs);

		exit.setMnemonic('x');
		exit.setText("Exit");
		exit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (!UserData.changedverses.isEmpty())
					program.savebeforeclosepane.show();
				else
				{
					program.filer.exit();
				}
			}
		});
		fileMenu.add(exit);

		add(fileMenu);

		editMenu.setMnemonic('e');
		editMenu.setText("Edit");

		cut.setMnemonic('t');
		cut.setText("Cut");
		editMenu.add(cut);

		copy.setMnemonic('y');
		copy.setText("Copy");
		editMenu.add(copy);

		paste.setMnemonic('p');
		paste.setText("Paste");
		editMenu.add(paste);

		delete.setMnemonic('d');
		delete.setText("Delete");
		delete.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.editor.remove(program.giveCurrentWord());
			}
		});
		editMenu.add(delete);

		add(editMenu);

		helpMenu.setMnemonic('h');
		helpMenu.setText("Help");

		content.setMnemonic('c');
		content.setText("Contents");
		helpMenu.add(content);

		about.setMnemonic('a');
		about.setText("About");
		helpMenu.add(about);

		add(helpMenu);
	}
}
