package g0dkar.utils.jspTags;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.markdownj.MarkdownProcessor;

import g0dkar.utils.StringUtils;

/**
 * Tag to {@link MarkdownProcessor turn Markdown into HTML}
 * @author g0dkar
 *
 */
public class MarkdownTag extends SimpleTagSupport {
	@Override
	public void doTag() throws JspException, IOException {
		final StringWriter sw = new StringWriter();
		getJspBody().invoke(sw);
		
		final String innerValue = sw.toString();
		
		if (innerValue != null) {
			getJspContext().getOut().append(StringUtils.markdown(innerValue));
		}
	}
}
