/**
 * 
 */
package info.jabara.weblog;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jabaraster
 */
public interface IRequestMonitor {

    /**
     * @param pListener -
     */
    void setRequestListener(final IListener pListener);

    /**
     * @author jabaraster
     */
    public interface IListener {
        /**
         * @param pRequest -
         */
        void onStart(final HttpServletRequest pRequest);

        /**
         * 
         */
        void onFinish();
    }
}
