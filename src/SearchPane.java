import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

class SearchPane extends JInternalFrame implements ChangeListener
{
	ONTProgram program;

	JTextField searchfield, regexfield, replacefield;
	JComboBox<String> searchcombo, lemmacombo, replacecombo;
	SearchResultsBox resultsbox;
	Map<String, List<Word>> rootsandtheirwords;
	List<Word> selectedwords;
	SearchResultsList resultslist;
	EditorPane leftclickmenu; //editor
	HTMLEditorKit htmlkit; //for verse drawer

	//right click menu
	JPopupMenu rightclickmenu;
	JMenuItem gotoversemenuitem;

	SearchPane(final ONTProgram program)
	{
		setBounds(0, 40, 400, 600);
		this.program = program;

		String[] wordfields = new String[]
		{ "translation", "greek", "strongs", "morph" };
		searchfield = new JTextField();
		resultsbox = new SearchResultsBox(program);
		lemmacombo = new JComboBox<String>();
		searchcombo = new JComboBox<String>();
		replacecombo = new JComboBox<String>();
		replacefield = new JTextField();
		regexfield = new JTextField();
		JLabel replacelabel = new JLabel();
		JLabel rootlabel = new JLabel();
		JLabel categorylabel = new JLabel();
		JLabel categoryreplacelabel = new JLabel();
		JLabel regexlabel = new JLabel();
		JLabel searchlabel = new JLabel();
		selectedwords = new LinkedList<Word>();
		leftclickmenu = new EditorPane(program);
		resultslist = new SearchResultsList(null, "");
		rightclickmenu = new JPopupMenu();
		gotoversemenuitem = new JMenuItem("go to verse");
		JScrollPane scroller = new JScrollPane(resultsbox);

		htmlkit = new HTMLEditorKit();
		StyleSheet css = htmlkit.getStyleSheet();
		css.addRule("strong {color: red;}");

		//setClosable(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setIconifiable(true);
		setMaximizable(true);
		setResizable(true);
		setTitle("search");
		setVisible(true);
		GridBagLayout displaypanelLayout = new GridBagLayout();
		displaypanelLayout.columnWeights = new double[]
		{ 1.0 };
		displaypanelLayout.rowWeights = new double[]
		{ 1.0 };
		getContentPane().setLayout(displaypanelLayout);
		GridBagConstraints gridBagConstraints;

		replacecombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				revalidate();
				repaint();
			}
		});
		regexfield.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent arg0)
			{
			}

			public void insertUpdate(DocumentEvent arg0)
			{
				revalidate();
				repaint();
			}

			public void removeUpdate(DocumentEvent arg0)
			{
				insertUpdate(null);
			}
		});
		resultsbox.setCellRenderer(makeVerseDrawer());
		replacecombo.setModel(new DefaultComboBoxModel<String>(wordfields));
		gotoversemenuitem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.navigator.set(resultsbox.getSelectedValue().bookidx,
					resultsbox.getSelectedValue().chapidx, resultsbox
						.getSelectedValue().verseidx, resultslist
						.giveFoundWordIndex(resultsbox.getSelectedIndex()));
				program.refreshViews();
			}
		});
		rightclickmenu.add(gotoversemenuitem);

		searchcombo.setModel(new DefaultComboBoxModel<String>(wordfields));
		searchfield.setColumns(20);
		searchfield.addActionListener(new ActionListener()
		{
			/**
			 * search action
			 */
			public void actionPerformed(ActionEvent evt)
			{
				search((String) searchcombo.getSelectedItem(), searchfield
					.getText());
			}
		});

		/**
		 * on selecting of verses in box, set them to be the ones to be replaced
		 */
		resultsbox.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				selectedwords.clear();
				int[] selectedindices = resultsbox.getSelectedIndices();
				for (int selectedindex : selectedindices)
					selectedwords.add(resultslist.get(selectedindex).get(
						resultslist.wordindices.get(selectedindex)));
			}
		});

		lemmacombo.addActionListener(new ActionListener()
		{
			/**
			 * on selecting a root from list of roots.
			 */
			public void actionPerformed(ActionEvent e)
			{
				//set words to replace to be word list of selected root
				selectedwords =
					new LinkedList<Word>(rootsandtheirwords.get(lemmacombo
						.getSelectedItem()));

				//select all those verses in the search box
				//NOTE: there is a bug where if a verse has 2 or more of our found words, and those words are different roots, it may highlight incorrectly. but the chance of this situation is pretty rare
				int[] indices = new int[selectedwords.size()];
				int indicesidx = 0;
				boolean[] alreadyaddedverseindices =
					new boolean[resultslist.size()];
				for (Word word : selectedwords)
					for (int verseidx = 0; verseidx < resultslist.size(); verseidx++)
						if (!alreadyaddedverseindices[verseidx]
							&& resultslist.get(verseidx).contains(word))
						{
							indices[indicesidx++] = verseidx;
							alreadyaddedverseindices[verseidx] = true;
							break;
						}

				//debug: show what the root is for each found word is
				//				for (int i = 0; i < searchedlist.size(); i++)
				//					System.out.println("root of searched list at index " + i
				//						+ " is " + searchedlist.giveFoundWord(i).root);

				resultsbox.setSelectedIndices(indices);
			}
		});
		replacefield.setColumns(20);
		replacefield.addActionListener(new ActionListener()
		{
			/**
			 * replace action
			 */
			public void actionPerformed(ActionEvent evt)
			{
				//replace all words at one time
				program.editor.turnOnBatchMode();
				for (int i = 0; i < selectedwords.size(); i++)
				{
					Word oldword = selectedwords.get(i);
					Word newword = null;

					//replace all text if no regex given
					String fieldtoreplace = (String) replacecombo.getSelectedItem();
					if (fieldtoreplace == "translation")
						newword =
							new Word(oldword.verse, oldword.translation.replaceAll(
								regexfield.getText().isEmpty() ? ".+" : regexfield
									.getText(), replacefield.getText()), oldword.greek,
								oldword.strongs, oldword.morph);
					else if (fieldtoreplace == "greek")
						newword =
							new Word(oldword.verse, oldword.translation, oldword.greek
								.replaceAll(regexfield.getText().isEmpty() ? ".+"
									: regexfield.getText(), replacefield.getText()),
								oldword.strongs, oldword.morph);
					else if (fieldtoreplace == "strongs")
						newword =
							new Word(oldword.verse, oldword.translation,
								oldword.greek, oldword.strongs.replaceAll(regexfield
									.getText().isEmpty() ? ".+" : regexfield.getText(),
									replacefield.getText()), oldword.morph);
					else if (fieldtoreplace == "morph")
						newword =
							new Word(oldword.verse, oldword.translation,
								oldword.greek, oldword.strongs, oldword.morph
									.replaceAll(regexfield.getText().isEmpty() ? ".+"
										: regexfield.getText(), replacefield.getText()));

					program.editor.set(oldword, newword);
				}
				program.editor.turnOffBatchMode();

				program.refreshViews();
			}
		});
		resultsbox.addMouseListener(new MouseAdapter()
		{
			/**
			 * popup editor
			 */
			public void mousePressed(MouseEvent e)
			{
				//with doubleclick, edit selected word
				if (e.getClickCount() == 2)
				{
					int listidx = resultsbox.locationToIndex(e.getPoint());
					leftclickmenu.show(resultsbox.getModel().getElementAt(listidx)
						.get(resultslist.wordindices.get(listidx)), resultsbox, e
						.getX(), e.getY());
				}
			}

			//with right click, show menu
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					//select item in versecontainer (JList)
					resultsbox.setSelectedIndex(resultsbox.locationToIndex(e
						.getPoint()));

					//show menu
					rightclickmenu.show(resultsbox, e.getX(), e.getY());
				}
			}
		});

		searchlabel.setText("Search for");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(searchlabel, gridBagConstraints);

		categorylabel.setText("In Category");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(categorylabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(searchfield, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weighty = 100.0;
		getContentPane().add(scroller, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(lemmacombo, gridBagConstraints);

		replacefield.setColumns(20);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 8;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(replacefield, gridBagConstraints);

		categoryreplacelabel.setText("Category to replace");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(categoryreplacelabel, gridBagConstraints);

		searchcombo.setModel(new DefaultComboBoxModel<String>(wordfields));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		getContentPane().add(searchcombo, gridBagConstraints);

		rootlabel.setText("Lemma Selector");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(rootlabel, gridBagConstraints);

		replacecombo.setModel(new DefaultComboBoxModel<String>(wordfields));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(replacecombo, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 7;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(regexfield, gridBagConstraints);

		regexlabel.setText("Regex (empty to replace all)");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 7;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(regexlabel, gridBagConstraints);

		replacelabel.setText("Replacement");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 8;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(replacelabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(new JSeparator(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(new JSeparator(), gridBagConstraints);
	}

	/**
	 * simple verse drawer with only translation text and morph
	 * @return
	 */
	ListCellRenderer<? super Verse> makeVerseDrawer()
	{
		return new ListCellRenderer<Verse>()
		{
			public Component getListCellRendererComponent(
				JList<? extends Verse> jlist, Verse verse, int verseindex,
				boolean isselected, boolean cellHasFocus)
			{
				SearchResultsList list = (SearchResultsList) jlist.getModel();
				JLayeredPane cell = new JLayeredPane();
				cell.setLayout(null);

				//declare num of panes to be 1 per word
				Component[] panes = new Component[verse.size()];

				//get found word index
				int foundidx = list.giveFoundWordIndex(verseindex);

				//get mid point of jlist
				int midpoint = jlist.getWidth() / 2;

				//take that - (searched word cell width) / 2
				panes[foundidx] = makePane(verse, isselected, foundidx, true);
				int startx =
					midpoint - panes[foundidx].getPreferredSize().width / 2;

				//take that - total width of all cells before searched word cell
				int totalwidth = 0;
				for (int i = 0; i < foundidx; i++)
				{
					panes[i] = makePane(verse, isselected, i, false);
					totalwidth += panes[i].getPreferredSize().width;
				}
				startx = startx - totalwidth;

				//now you're at the very left. so draw the words left to right
				int prevwidth = panes[0].getPreferredSize().width;
				int prevx = startx - prevwidth;
				int height = panes[0].getPreferredSize().height;
				for (int i = 0; i < panes.length; i++)
				{
					if (i > foundidx)
						panes[i] = makePane(verse, isselected, i, false);
					panes[i].setBounds(prevx = prevx + prevwidth, 0, prevwidth =
						panes[i].getPreferredSize().width, height);
					cell.add(panes[i], 1, i);
				}

				//write the verse label
				JLabel reflabel =
					new JLabel(Verse.BOOKS[verse.bookidx][0]
						+ Integer.toString(verse.chapidx + 1) + ":"
						+ Integer.toString(verse.verseidx + 1));
				cell.add(reflabel, 2, 0);
				reflabel.setBounds(0, 0, 100, 20);

				//set panel final size according to words and return it
				cell.setPreferredSize(new Dimension(jlist.getWidth(), height));
				return cell;
			}

			Component makePane(Verse verse, boolean isselected, int wordidx,
				boolean issearchedword)
			{
				JTextComponent pane;
				String txt = "";
				if (issearchedword)
				{
					pane = new JTextPane();
					pane.setDocument(new BatchDocument());
					((JTextPane) pane).setEditorKit(htmlkit);
					((JTextPane) pane).setContentType("text/html");
				}
				else
				{
					pane = new JTextArea();
					pane.setDocument(new PlainDocument());
				}

				Word word = verse.get(wordidx);
				String selectedfield = (String) replacecombo.getSelectedItem();

				//write translation (highlight regex)
				txt =
					(isselected && issearchedword && selectedfield == "translation"
						? word.translation.replaceAll("("
							+ (regexfield.getText().isEmpty() ? ".+" : regexfield
								.getText()) + ")", "<strong>$1</strong>")
						: word.translation);

				//write greek
				if (program.programdata.showgreek)
					txt +=
						(issearchedword ? "<br/>" : "\n")
							+ (isselected && issearchedword
								&& selectedfield == "greek" ? word.greek.replaceAll("("
								+ (regexfield.getText().isEmpty() ? ".+" : regexfield
									.getText()) + ")", "<strong>$1</strong>")
								: word.greek);

				//write strongs
				if (program.programdata.showstrongs)
					txt +=
						(issearchedword ? "<br/>" : "\n")
							+ (isselected && issearchedword
								&& selectedfield == "strongs" ? word.strongs
								.replaceAll("("
									+ (regexfield.getText().isEmpty() ? ".+"
										: regexfield.getText()) + ")",
									"<strong>$1</strong>") : word.strongs);

				//write morphology
				if (program.programdata.showmorph)
					txt +=
						(issearchedword ? "<br/>" : "\n")
							+ (isselected && issearchedword
								&& selectedfield == "morph" ? word.morph.replaceAll("("
								+ (regexfield.getText().isEmpty() ? ".+" : regexfield
									.getText()) + ")", "<strong>$1</strong>")
								: word.morph);

				//set selection color.
				if (issearchedword)
				{
					pane.setForeground(Color.black);
					pane.setBackground(isselected ? Color.darkGray : Color.white);
				}
				else
				{
					pane.setForeground(Color.gray);
					pane.setBackground(isselected ? Color.darkGray : Color.white);
				}

				pane.setText(txt);

				return pane;
			}
		};
	}

	//	
	//	/**
	//	 * manually map from translation root to list of words (i.e., not from index).
	//	 * @return
	//	 */
	//	Map<String, List<Word>> mapRootsToWords(SearchedList verses)
	//	{
	//		Map<String, List<Word>> map = new HashMap<String, List<Word>>();
	//		for (int i = 0; i < verses.size(); i++)
	//		{
	//			//get root.
	//			String root =
	//				giveRoot(verses.get(i).get(verses.wordindices.get(i)).translation);
	//
	//			//make root key if not contains.
	//			if (!map.containsKey(root))
	//				map.put(root, new LinkedList<Word>());
	//
	//			//put new word for this root entry.
	//			map.get(root).add(verses.get(i).get(verses.wordindices.get(i)));
	//		}
	//		return map;
	//	}

	/**
	 * search function, can be called by other classes
	 * @param field
	 * @param text
	 */
	void search(final String field, final String text)
	{
		searchfield.setText(text); //since might be being searched by another class
		try
		{
			// collect list of results.
			resultslist = program.searcher.search(field, text);
		}
		catch (NoSuchFieldException | SecurityException
			| IllegalArgumentException | IllegalAccessException ex)
		{
			ex.printStackTrace();
			program.errorpane.showError(ex.getMessage());
		}

		new SwingWorker<Void, Void>()
		{
			protected Void doInBackground()
			{
				//show results in box.
				resultsbox.setModel(resultslist);
				return null;
			}
		}.execute();

		stateChanged(null);
	}

	/**
	 * refresh
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (UserData.verses == null)
			return;

		//update word root choices (if last search was for strongs).
		if (resultslist.giveSearchField() == "strongs")
		{
			rootsandtheirwords = UserData.words.get(searchfield.getText()); //use map from index
			//		rootsandtheirwords = mapRootsToWords(foundverses); //manually map
			lemmacombo.setModel(new DefaultComboBoxModel<String>(
				rootsandtheirwords.keySet().toArray(new String[0])));
			lemmacombo.setEnabled(true);
		}
		else
			lemmacombo.setEnabled(false);

		revalidate();
		repaint();
	}
}

class SearchResultsBox extends JList<Verse>
{
	ONTProgram program;

	SearchResultsBox(ONTProgram program)
	{
		this.program = program;
		setSelectionForeground(Color.orange);
		setForeground(Color.black);
		//		setFont(program.programdata.GREEKUNCIAL_FONT.deriveFont(8f));
		getSelectionModel().setSelectionMode(
			ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setBorder(BorderFactory.createLineBorder(Color.black));
	}

	/**
	 * for optimization purposes
	 *
	 */
	ListCellRenderer<? super Verse> makeVerseDrawerEnglishOnly()
	{
		return new ListCellRenderer<Verse>()
		{
			public Component getListCellRendererComponent(
				JList<? extends Verse> jlist, Verse verse, int verseindex,
				boolean isselected, boolean cellHasFocus)
			{
				JTextArea txtarea = new JTextArea();
				txtarea.setDocument(new PlainDocument());
				txtarea.setText(verse.toEnglish());
				txtarea.setForeground(isselected ? Color.orange : Color.black);
				return txtarea;
			}
		};
	}
}