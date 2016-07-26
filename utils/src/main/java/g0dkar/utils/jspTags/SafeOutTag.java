package g0dkar.utils.jspTags;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import g0dkar.utils.StringUtils;

/**
 * {@link SimpleTag} that strips all "bad" HTML from the {@code value} attribute and writes it at the JSP
 * @author g0dkar
 *
 */
public class SafeOutTag extends SimpleTagSupport {
	@Override
	public void doTag() throws JspException, IOException {
		final Boolean strip = (Boolean) getJspContext().getAttribute("strip");
		String value = (String) getJspContext().getAttribute("value");
		
		if (value == null) {
			final StringWriter sw = new StringWriter();
			getJspBody().invoke(sw);
			value = sw.toString();
			
			if (value == null) {
				value = (String) getJspContext().getAttribute("default");
			}
		}
		
		if (value != null) {
			getJspContext().getOut().append(strip != null && strip ? StringUtils.stripHTML(value) : StringUtils.cleanHTML(value));
		}
	}
}
