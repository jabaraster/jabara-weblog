/**
 * 
 */
package info.jabara.weblog;

import jabara.general.ArgUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jabaraster
 */
public class WebLogContext {

    private final HttpServletRequest request;
    private final String             descriptor;

    /**
     * @param pRequest -
     * @param pDescriptor -
     */
    public WebLogContext(final HttpServletRequest pRequest, final String pDescriptor) {
        this.request = ArgUtil.checkNull(pRequest, "pRequest"); //$NON-NLS-1$ 
        this.descriptor = pDescriptor;
    }

    /**
     * @return the descriptor
     */
    public String getDescriptor() {
        return this.descriptor;
    }

    /**
     * @return the request
     */
    public HttpServletRequest getRequest() {
        return this.request;
    }
}
