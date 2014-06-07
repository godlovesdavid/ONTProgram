import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontFormatException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

class DisplayPane extends JInternalFrame implements ChangeListener
{
	ONTProgram program;

	//verses panel
	JPanel versespanel;
	VerseBox[] verseboxes;
	HTMLEditorKit htmlkit;
	JScrollPane scroller;

	DisplayPane(final ONTProgram program)
	{
		this.program = program;
		setMaximizable(true);
		setResizable(true);
		setVisible(true);
		setTitle("display");
		setBounds(0, 40, program.programdata.mainframe.getWidth(),
			program.programdata.mainframe.getHeight());
		getContentPane().setLayout(
			new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		versespanel = new JPanel();
		versespanel.setLayout(new BoxLayout(versespanel, BoxLayout.Y_AXIS));
		htmlkit = new HTMLEditorKit();
		StyleSheet css = htmlkit.getStyleSheet();
		css.addRule("strong {color: red;}");
		css.addRule("em {color: #808080;}");

		//make and fill up the verse containers intially
		verseboxes = new VerseBox[176]; //176 is max num of verses for any book in the bible
		for (int i = 0; i < verseboxes.length; i++)
			verseboxes[i] = new VerseBox(program);

		//resize all verseboxes on resize. bugged: doesn't shrink til you scroll
//		addComponentListener(new ComponentAdapter()
//		{
//			public void componentResized(ComponentEvent e)
//			{
//				for (VerseBox box : verseboxes)
//					box.setPreferredSize(new Dimension(getWidth(), box.getHeight()));
//				revalidate();
//				repaint();
//			}
//		});

		scroller = new JScrollPane(versespanel);
		//		scroller
		//			.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getVerticalScrollBar().setUnitIncrement(30);
		add(scroller);
	}

	/**
	 * scroll to current verse.
	 */
	void scrollToVerse()
	{
		Rectangle viewrect = scroller.getViewport().getViewRect();

		//get place to scroll to.
		Rectangle scrollto = verseboxes[UserData.verseidx].getBounds();

		//don't scroll if area already in view.
		if (viewrect.contains(scrollto.getLocation()))
			return;

		//offset scrollto to just enough to be viewed if the verse is below viewing area.
		if (scrollto.y > viewrect.y + viewrect.height)
			scrollto.setLocation(scrollto.x, scrollto.y - (viewrect.height - 200)); //donno how to get verse box height yet, so use 200 instead

		//move there.
		scroller.getViewport().setViewPosition(scrollto.getLocation());
	}

	/**
	 * refresh this view. like hitting refresh button in your internet browser, because the data has been updated
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (UserData.verses == null)
			return;

		Verse[] currentchapter = program.giveCurrentChapter();

		//remove verse boxes
		versespanel.removeAll();

		//verse boxes point to updated verses
		for (int i = 0; i < currentchapter.length; i++)
		{
			verseboxes[i].setFont(program.programdata.font);
			verseboxes[i].setVerse(currentchapter[i]);
			versespanel.add(verseboxes[i]);
		}

		// highlight the verse
		verseboxes[UserData.verseidx].setSelectedIndex(UserData.wordidx);

		// scroll there
		scrollToVerse();
	}
}

/**
 * verse container for verse (list of words). can open menus by right click or double clicking.
 * @author david
 *
 */
class VerseBox extends JList<Word>
{
	ListSelectionListener selectionlistener;
	ONTProgram program;
	EditorPane leftclickmenu;
	//save selected word here so when user won't accidentally edit somewhere else while editing (?)
	Word wordbeingedited;

	//right click menu
	JPopupMenu rightclickmenu;
	JMenu changefontmenu;
	JMenuItem showgreekmenuitem, showstrongsmenuitem, showmorphmenuitem,
		font1menuitem, font2menuitem, font3menuitem, font4menuitem,
		addwordmenuitem, delwordmenuitem, uppercasemenuitem;

	/**
	 * verse container with verse
	 * @param verse
	 * @throws IOException 
	 * @throws FontFormatException 
	 */
	VerseBox(ONTProgram program, Verse verse) throws FontFormatException,
		IOException
	{
		this(program);
		setVerse(verse);
	}

	VerseBox(final ONTProgram program)
	{
		setBorder(BorderFactory.createLineBorder(Color.black));
		this.program = program;

		leftclickmenu = new EditorPane(program);
		rightclickmenu = new JPopupMenu();

		uppercasemenuitem = new JMenuItem("capitalize greek");
		uppercasemenuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.editor.set(wordbeingedited, new Word(wordbeingedited.verse,
					wordbeingedited.translation,
					wordbeingedited.greek.toUpperCase(), wordbeingedited.strongs,
					wordbeingedited.morph));
			}
		});

		//showing greek/strongs/morph
		showgreekmenuitem =
			new JMenuItem(program.programdata.showgreek == true ? "Hide Greek"
				: "Show Greek");
		showgreekmenuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.programdata.showgreek = !program.programdata.showgreek;
				((JMenuItem) e.getSource())
					.setText(program.programdata.showgreek == true ? "hide greek"
						: "show greek");
				program.refreshViews();
			}
		});
		showstrongsmenuitem =
			new JMenuItem(program.programdata.showstrongs == true ? "Hide Strongs"
				: "Show Strongs");
		showstrongsmenuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.programdata.showstrongs = !program.programdata.showstrongs;
				((JMenuItem) e.getSource())
					.setText(program.programdata.showstrongs == true
						? "hide strongs" : "show strongs");
				program.refreshViews();
			}
		});
		showmorphmenuitem =
			new JMenuItem(program.programdata.showmorph == true ? "Hide Morph"
				: "Show Morph");
		showmorphmenuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.programdata.showmorph = !program.programdata.showmorph;
				((JMenuItem) e.getSource())
					.setText(program.programdata.showmorph == true ? "hide morph"
						: "show morph");
				program.refreshViews();
			}
		});

		//add word menu item
		addwordmenuitem = new JMenuItem("Add word");
		addwordmenuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.editor.add(new Word(program.giveCurrentVerse(), "", "", "",
					""));
			}
		});

		delwordmenuitem = new JMenuItem("Delete word");
		delwordmenuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.editor.remove(program.giveCurrentWord());
			}
		});

		//changing font submenu
		changefontmenu = new JMenu("Change font");
		font1menuitem = new JMenuItem("GreekUncial");
		font1menuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				program.programdata.font = program.programdata.GREEKUNCIAL_FONT;
				program.refreshViews();
			}
		});
		//		font2menuitem = new JMenuItem("Monospaced"); //monospaced seems bugged for ge 1.1
		//		font2menuitem.addActionListener(new ActionListener()
		//		{
		//			public void actionPerformed(ActionEvent arg0)
		//			{
		//				program.programdata.font = ProgramData.MONOSPACED_FONT;
		//				program.refreshViews();
		//			}
		//		});
		font3menuitem = new JMenuItem("Sans Serif");
		font3menuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				program.programdata.font = ProgramData.SANSSERIF_FONT;
				program.refreshViews();
			}
		});
		font4menuitem = new JMenuItem("Serif");
		font4menuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				program.programdata.font = ProgramData.SERIF_FONT;
				program.refreshViews();
			}
		});
		changefontmenu.add(font1menuitem);
		//		changefontmenu.add(font2menuitem);
		changefontmenu.add(font3menuitem);
		changefontmenu.add(font4menuitem);
		JMenuItem searchstrongsmenuitem =
			new JMenuItem("search for same strongs");
		searchstrongsmenuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				program.searchpane.search("strongs", VerseBox.this
					.getSelectedValue().strongs);
			}
		});

		//add all of above into main menu
		rightclickmenu.add(searchstrongsmenuitem);
		rightclickmenu.addSeparator();
		rightclickmenu.add(addwordmenuitem);
		rightclickmenu.add(delwordmenuitem);
		rightclickmenu.add(uppercasemenuitem);
		rightclickmenu.addSeparator();
		rightclickmenu.add(showgreekmenuitem);
		rightclickmenu.add(showstrongsmenuitem);
		rightclickmenu.add(showmorphmenuitem);
		rightclickmenu.addSeparator();
		rightclickmenu.add(changefontmenu);

		//lay out Words left to right
		setAlignmentX(LEFT_ALIGNMENT);
		setLayoutOrientation(JList.HORIZONTAL_WRAP);

		//resize on parent window resize.
		//		addHierarchyBoundsListener(new HierarchyBoundsListener()
		//		{
		//			public void ancestorMoved(HierarchyEvent arg0)
		//			{
		//			}
		//
		//			public void ancestorResized(HierarchyEvent e)
		//			{
		//				setPreferredSize(new Dimension(getRootPane().getWidth(),
		//					getHeight()));
		//				getRootPane().revalidate();
		//				getRootPane().repaint();
		//				System.out.println("got here");
		//			}
		//		});

		setVisibleRowCount(0);

		//renderer for each draggable cell.
		setCellRenderer(makeVerseDrawer());

		//initial font
		setFont(program.programdata.GREEKUNCIAL_FONT);

		//manager of drag and drop
		setTransferHandler(new WordDragger(program));
		setDropMode(DropMode.INSERT);
		setDragEnabled(true);

		//selection options
		setSelectionBackground(Color.gray);
		setSelectionForeground(Color.orange);
		getSelectionModel().setSelectionMode(
			ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectionlistener = makeSelectionListener();

		addMouseListener(new MouseAdapter()
		{
			//right click popup menu
			public void mousePressed(MouseEvent e)
			{
				//with doubleclick, edit selected word
				if (e.getClickCount() == 2)
					showLeftClickMenu(e.getPoint());
			}

			public void mouseReleased(MouseEvent e)
			{
				//rightclick menu
				if (e.isPopupTrigger())
				{
					//select item in versecontainer (JList)
					setSelectedIndex(locationToIndex(e.getPoint()));

					//show menu
					rightclickmenu.show(VerseBox.this, e.getX(), e.getY());
				}
			}
		});
	}

	public void setSelectedIndex(int index)
	{
		super.setSelectedIndex(index);
		UserData.wordidx = index;
	}

	public void setSelectedIndices(int[] indices)
	{
		super.setSelectedIndices(indices);
		UserData.wordidx = indices[0];
	}

	/**
	 * show left click menu given point clicked
	 * @param point point clicked
	 */
	void showLeftClickMenu(Point point)
	{
		leftclickmenu.show(wordbeingedited =
			getModel().getElementAt((locationToIndex(point))), VerseBox.this,
			(int) point.getX(), (int) point.getY());
	}

	/** 
	 * Sets current verse and word to selected verse and word,
	 * and ensures only one JList can have its element(s) be selected at a time
	 */
	ListSelectionListener makeSelectionListener()
	{
		return new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				//make sure only this jlist is being selected
				if (getValueIsAdjusting())
					return;
				if (!getSelectionModel().isSelectionEmpty())
					for (VerseBox versecontainer : program.displaypane.verseboxes)
						if (versecontainer != VerseBox.this)
							versecontainer.getSelectionModel().clearSelection();

				//set current verse/word to selected verse/word
				program.navigator.set(UserData.bookidx, UserData.chapidx,
					((Verse) getModel()).verseidx, getSelectedIndex() < 0 ? 0
						: getSelectedIndex());

				//refresh panels as necessary
				program.navigatorpane.stateChanged(null);
				program.searchpane.stateChanged(null);
			}
		};
	}

	/**
	 * renderer of each cell of this jlist.
	 * @return
	 */
	ListCellRenderer<? super Word> makeVerseDrawer()
	{
		/**
		 *  note that we can't actually input anything into the textpane
		 *  after it is rendered.
		 */
		return new ListCellRenderer<Word>()
		{
			public Component getListCellRendererComponent(
				final JList<? extends Word> list, final Word value,
				final int index, final boolean isSelected,
				final boolean cellHasFocus)
			{
				//draw word text
				String txt = "<p style=\"text-align:center\">" + value.translation;
				txt = txt.replaceAll("<RF>[^<]*<Rf>", "<strong>*</strong>");
				txt = txt.replaceAll("<FI>([^<]*)<Fi>", "<em>$1</em>");
				txt = txt.replaceAll("<TS1>[^<]*<Ts1>", "<strong>TITLE</strong>");

				//write greek
				if (program.programdata.showgreek)
					txt += "<br/>" + value.greek;

				//write strongs
				if (program.programdata.showstrongs)
					txt += "<br/>" + value.strongs;

				//write morphology
				if (program.programdata.showmorph)
					txt += "<br/>" + value.morph;

				txt += "</p>";

				JTextPane textpane = new JTextPane();
				textpane.setDocument(new BatchDocument());
				textpane.setEditorKit(program.displaypane.htmlkit);

				//set selection color.
				textpane.setForeground(isSelected ? list.getSelectionForeground()
					: list.getForeground());
				textpane.setBackground(isSelected ? list.getSelectionBackground()
					: list.getBackground());

				//add the html text to pane
				textpane.setContentType("text/html");
				textpane.setText(txt);
				textpane.setFont(getFont());

				//add a label indicating verse number
				if (index == 0)
				{
					JLabel versenumlabel =
						new JLabel(Integer
							.toString(((Verse) getModel()).verseidx + 1));
					textpane.add(versenumlabel);
					versenumlabel.setBounds(0, 0, 30, 30);
				}
				return textpane;
			}
		};
	}

	/**
	 * not just set the model of this container, but also make sure the layout is correct
	 * @param verse
	 */
	void setVerse(Verse verse)
	{
		//need to remove selectionlistener first so that it doesn't auto select things while we reset a verse
		removeListSelectionListener(selectionlistener);

		setModel(verse);

		//size must be reset
		setSize(program.programdata.mainframe.getSize());

		//add the listener back again
		addListSelectionListener(selectionlistener);

		revalidate();
		repaint();
	}
}
