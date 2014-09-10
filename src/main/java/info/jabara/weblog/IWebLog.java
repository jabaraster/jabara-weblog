/**
 * 
 */
package info.jabara.weblog;

import java.text.MessageFormat;

/**
 * @author jabaraster
 */
public interface IWebLog {

    /**
     * @param pMessage -
     * @param pValues -
     * @see MessageFormat
     */
    void trace(final String pMessage, final Object... pValues);

    /**
     * @param pException -
     */
    void exception(final Throwable pException);
}
