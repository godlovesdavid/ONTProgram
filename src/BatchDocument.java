import java.util.ArrayList;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;

/**
 * DefaultDocument subclass that supports batching inserts.
 */
public class BatchDocument extends DefaultStyledDocument
{
	/**
	 * EOL tag that we re-use when creating ElementSpecs
	 */
	private static final char[] EOL_ARRAY =
	{ '\n' };

	/**
	 * Batched ElementSpecs
	 */
	private ArrayList<ElementSpec> batch = null;

	public BatchDocument()
	{
		batch = new ArrayList<ElementSpec>();
	}

	/**
	 * Adds a String (assumed to not contain linefeeds) for 
	 * later batch insertion.
	 */
	public void appendBatchString(String str, AttributeSet a)
	{
		// We could synchronize this if multiple threads 
		// would be in here. Since we're trying to boost speed, 
		// we'll leave it off for now.

		// Make a copy of the attributes, since we will hang onto 
		// them indefinitely and the caller might change them 
		// before they are processed.
		a = a.copyAttributes();
		char[] chars = str.toCharArray();
		batch.add(new ElementSpec(a, ElementSpec.ContentType, chars, 0, str
			.length()));
	}

	/**
	 * Adds a linefeed for later batch processing
	 */
	public void appendBatchLineFeed(AttributeSet a)
	{
		// See sync notes above. In the interest of speed, this 
		// isn't synchronized.

		// Add a spec with the linefeed characters
		batch.add(new ElementSpec(a, ElementSpec.ContentType, EOL_ARRAY, 0, 1));

		// Then add attributes for element start/end tags. Ideally 
		// we'd get the attributes for the current position, but we 
		// don't know what those are yet if we have unprocessed 
		// batch inserts. Alternatives would be to get the last 
		// paragraph element (instead of the first), or to process 
		// any batch changes when a linefeed is inserted.
		Element paragraph = getParagraphElement(0);
		AttributeSet pattr = paragraph.getAttributes();
		batch.add(new ElementSpec(null, ElementSpec.EndTagType));
		batch.add(new ElementSpec(pattr, ElementSpec.StartTagType));
	}

	public void processBatchUpdates(int offs) throws BadLocationException
	{
		// As with insertBatchString, this could be synchronized if
		// there was a chance multiple threads would be in here.
		ElementSpec[] inserts = new ElementSpec[batch.size()];
		batch.toArray(inserts);

		// Process all of the inserts in bulk
		super.insert(offs, inserts);
	}
}