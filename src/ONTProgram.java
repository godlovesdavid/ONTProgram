import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class ONTProgram
{
	// data.
	ProgramData programdata;

	// functions.
	ONTFiler filer;
	ONTEditor editor;
	ONTNavigator navigator;
	ONTDisplayer displayer;
	ONTSearcher searcher;

	// displays.
	Menubar menubar;
	NavigatorPane navigatorpane;
	DisplayPane displaypane;
	SearchPane searchpane;
	ErrorPane errorpane;
	JFileChooser filechooser;
	LoadFilePane loadfilepane;
	SaveBeforeClosePane savebeforeclosepane;
	ProgressPane progresspane;
	JDesktopPane desktop;
	List<ChangeListener> changelisteners;

	ONTProgram()
	{
		initData();

		initFunctions();

		initDisplays();

		initKeyboard();
	}

	/**
	 * loads settings from last close
	 */
	void initData()
	{
		if (ProgramData.SETTINGS_FILE.exists())
		{
			try
			{
				ObjectInputStream input =
					new ObjectInputStream(new FileInputStream(
						ProgramData.SETTINGS_FILE));
				programdata = (ProgramData) input.readObject();
				programdata.mainframe = new JFrame(); //because it's not serializable
				programdata.mainframe.setSize(programdata.dimensions);
				input.close();
			}
			catch (IOException | ClassNotFoundException c)
			{
				c.printStackTrace();
			}
		}
		else
			programdata = new ProgramData();
	}

	void initFunctions()
	{
		filer = new ONTFiler(programdata);
		editor = new ONTEditor(programdata);
		displayer = new ONTDisplayer(programdata);
		searcher = new ONTSearcher(programdata);
		navigator = new ONTNavigator(programdata);
	}

	void initDisplays()
	{
		navigatorpane = new NavigatorPane(this);
		errorpane = new ErrorPane(this);
		loadfilepane = new LoadFilePane(this);
		savebeforeclosepane = new SaveBeforeClosePane(this);
		displaypane = new DisplayPane(this);
		searchpane = new SearchPane(ONTProgram.this);
		changelisteners = new ArrayList<ChangeListener>();
		navigatorpane = new NavigatorPane(this);
		menubar = new Menubar(this);
		progresspane = new ProgressPane(this);
		filechooser = new JFileChooser(new File(System.getProperty("user.dir")));

		//change listeners
		changelisteners.add(searchpane);
		changelisteners.add(navigatorpane);
		changelisteners.add(displaypane);
		refreshViews();

		programdata.mainframe.setJMenuBar(menubar);
		desktop = new JDesktopPane();
		desktop.setLayout(null);
		desktop.add(navigatorpane);
		desktop.add(displaypane);
		desktop.add(searchpane);
		desktop.add(savebeforeclosepane);
		desktop.add(loadfilepane);
		desktop.add(errorpane);
		desktop.add(progresspane);

		GroupLayout layout =
			new GroupLayout(programdata.mainframe.getContentPane());
		programdata.mainframe.getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
			GroupLayout.Alignment.LEADING).addComponent(desktop,
			GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
			GroupLayout.Alignment.LEADING).addComponent(desktop,
			GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE));

		programdata.mainframe
			.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		programdata.mainframe.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				if (!UserData.changedverses.isEmpty())
					savebeforeclosepane.show();
				else
				{
					filer.exit();
				}
			}
		});
		programdata.mainframe.setVisible(true);
	}

	//	void loadLastLoaded()
	//	{
	//		if (!settings.loadhistory.isEmpty())
	//		{
	//			System.out.println(settings.loadhistory.get(0));
	//			try
	//			{
	//				filer
	//					.open(
	//						(String) settings.loadhistory.keySet().toArray()[settings.loadhistory
	//							.size() - 1], activesession.regex,
	//						activesession.transcapture, activesession.greekcapture,
	//						activesession.strongscapture, activesession.morphcapture);
	//			}
	//			catch (IOException ex)
	//			{
	//				ex.printStackTrace();
	//			}
	//		}
	//	}

	void initKeyboard()
	{
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.addKeyEventDispatcher(new KeyEventDispatcher()
			{
				public boolean dispatchKeyEvent(KeyEvent e)
				{
					if (e.getID() == KeyEvent.KEY_PRESSED)
					{
						// f4 is jump-to-verse field focus.
						if (e.getID() == KeyEvent.KEY_PRESSED
							&& e.getKeyCode() == KeyEvent.VK_F4)
						{
							navigatorpane.jumpfield.requestFocus();
							navigatorpane.jumpfield.selectAll();
						}
						//control-z is undo.
						if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z)
						{
							editor.undo();
							refreshViews();
						}
						//control-y is redo.
						if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y)
						{
							editor.redo();
							refreshViews();
						}
						//control-s is save.
						if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S)
						{
							try
							{
								filer.save();
							}
							catch (IOException e1)
							{
								e1.printStackTrace();
							}
						}
						//alt-left is go back
						if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_LEFT)
						{
							navigator.goBack();
							refreshViews();
						}
						//alt-right is go forward
						if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_RIGHT)
						{
							navigator.goForward();
							refreshViews();
						}
					}
					return false;
				}
			});
	}

	Verse[] giveCurrentChapter()
	{
		return UserData.verses[UserData.bookidx][UserData.chapidx];
	}

	Verse giveCurrentVerse()
	{
		return UserData.verses[UserData.bookidx][UserData.chapidx][UserData.verseidx];
	}

	Word giveCurrentWord()
	{
		return UserData.verses[UserData.bookidx][UserData.chapidx][UserData.verseidx]
			.get(UserData.wordidx);
	}

	void refreshViews()
	{
		for (ChangeListener listener : changelisteners)
			listener.stateChanged(new ChangeEvent(this));
		programdata.mainframe.setTitle("ONT Program - " + UserData.loadpath);
	}

	/**
	 * runner of program
	 * @param args
	 */
	public static void main(String[] args) throws ClassNotFoundException,
		InstantiationException, IllegalAccessException,
		UnsupportedLookAndFeelException
	{
		// skin the window
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			if ("Nimbus".equals(info.getName()))
			{
				UIManager.setLookAndFeel(info.getClassName());
				break;
			}

		// make editor instance
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new ONTProgram();
			}
		});
	}
}
