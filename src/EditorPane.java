import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * actually, it's a popup editor.
 * @author david
 *
 */
class EditorPane extends JPopupMenu
{
	JTextField translationfield, greekfield, strongsfield, morphfield;
	ONTProgram program;
	Word word;

	EditorPane(final ONTProgram program)
	{
		this.program = program;

		translationfield = new JTextField();
		greekfield = new JTextField();
		strongsfield = new JTextField();
		morphfield = new JTextField();

		translationfield.addActionListener(makeFieldListener());
		greekfield.addActionListener(makeFieldListener());
		strongsfield.addActionListener(makeFieldListener());
		morphfield.addActionListener(makeFieldListener());
		add(translationfield);
		add(greekfield);
		add(strongsfield);
		add(morphfield);

		addPopupMenuListener(new PopupMenuListener()
		{
			/**
			 * save changes to word when clicking outside of popup menu.
			 */
			public void popupMenuCanceled(PopupMenuEvent e)
			{
				acceptChanges();
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
			}
		});
	}

	void acceptChanges()
	{
		setVisible(false);
		program.editor.set(word, new Word(word.verse, translationfield.getText(),
			greekfield.getText(), strongsfield.getText(), morphfield.getText()));
		//program.refreshViews();
	}

	ActionListener makeFieldListener()
	{
		return new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					acceptChanges();
				}
				catch (IllegalArgumentException | SecurityException ex)
				{
					ex.printStackTrace();
					program.errorpane.showError(ex.getMessage());
				}
			}
		};
	}

	/**
	 * shows the popup menu. note that must supply the word before showing.
	 * @param word
	 * @param container
	 * @param x
	 * @param y
	 */
	void show(Word word, Component container, int x, int y)
	{
		this.word = word;
		translationfield.setColumns(word.translation.length() < 6 ? 6 : word.translation.length());
		translationfield.setText(word.translation);
		greekfield.setText(word.greek);
		strongsfield.setText(word.strongs);
		morphfield.setText(word.morph);
		for (Component c : getComponents())
			c.setFont(program.programdata.font);
		super.show(container, x, y);
		translationfield.requestFocus();
	}
}
