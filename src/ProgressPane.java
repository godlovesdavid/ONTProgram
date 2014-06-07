import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JProgressBar;

/**
 * show progress bar of something
 * @author david
 *
 */
public class ProgressPane extends JInternalFrame
{
	JProgressBar progressbar;
	ONTProgram program;

	ProgressPane(ONTProgram program)
	{
		this.program = program;

		setTitle("progress");
		getContentPane().setLayout(
			new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		setLayer(2);
		setBounds(100, 100, 200, 150);
		add(progressbar = new JProgressBar(0, 100));
	}

	/**
	 * show progress based on the value of program.programdata.progress
	 */
	void showProgress()
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				setVisible(true);

				progressbar.setMaximum(program.programdata.maxprogress);

				while (program.programdata.progress < program.programdata.maxprogress)
				{
					progressbar.setValue(program.programdata.progress);
					try
					{
						//dont update too often
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}

				setVisible(false);
			}
		}).start();
	}
}
