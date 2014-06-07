import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class SaveBeforeClosePane extends JInternalFrame implements ChangeListener
{
	//save-before-closing frame
	JButton cancelbutton;
	JButton nosavebutton;
	JButton savebutton;
	JLabel savelabel;
	ONTProgram program;

	SaveBeforeClosePane(final ONTProgram program)
	{
		super("save?", false, true, false);
		this.program = program;

		setLayer(2);
		savelabel = new JLabel("Save before close?");
		nosavebutton = new JButton("No save");
		savebutton = new JButton("Save");
		cancelbutton = new JButton("Cancel");
		nosavebutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				program.filer.exit();
			}
		});
		savebutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					program.filer.saveAs(UserData.loadpath);
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
					program.errorpane.showError(ex.getMessage());
				}
				program.filer.exit();
			}
		});
		cancelbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				hide();
			}
		});
		savelabel.setBounds(73, 11, 116, 37);
		savebutton.setBounds(10, 54, 70, 23);
		nosavebutton.setBounds(73, 54, 80, 23);
		cancelbutton.setBounds(150, 54, 70, 23);
		setBounds(82, 108, 246, 122);
		setLayout(null);
		setClosable(true);
		setMaximizable(true);
		setResizable(true);
		setVisible(false);
		add(savelabel);
		add(savebutton);
		add(nosavebutton);
		add(cancelbutton);
	}

	public void stateChanged(ChangeEvent e)
	{
	}
}
