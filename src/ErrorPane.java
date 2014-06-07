import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class ErrorPane extends JInternalFrame
{
	JLabel errorlabel;
	JTextArea msgarea;
	JButton closeerrorbutton;
	ONTProgram program;

	ErrorPane(ONTProgram program)
	{
		setLayout(null);
		this.program = program;
		/*
		 * error panel
		 */
		setBounds(100, 100, 300, 200);
		errorlabel = new JLabel("ERROR");
		JScrollPane errormsgscroller = new JScrollPane();
		errormsgscroller
			.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		errormsgscroller
			.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		msgarea = new JTextArea();
		closeerrorbutton = new JButton("ok");
		add(errorlabel);
		errorlabel.setBounds(92, 0, 60, 14);
		msgarea.setColumns(20);
		msgarea.setRows(5);
		msgarea.setLineWrap(true);
		msgarea.setFont(ProgramData.MONOSPACED_FONT);
		errormsgscroller.setViewportView(msgarea);
		add(errormsgscroller);
		errormsgscroller.setBounds(0, 20, 231, 96);
		closeerrorbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				hide();
			}
		});
		add(closeerrorbutton);
		closeerrorbutton.setBounds(188, 122, 43, 23);
		msgarea.setEditable(false);
	}

	/**
	 * simply shows an error message
	 * @param txt
	 */
	void showError(String txt)
	{
		msgarea.setText(txt);
		revalidate();
		repaint();
		show();
	}
}
