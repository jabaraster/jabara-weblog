/**
 * 
 */
package info.jabara.weblog;

import jabara.general.ArgUtil;
import jabara.general.ExceptionUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jabaraster
 */
public final class WebLog {

    private final static IWebLogContextProvider _contextProvider = new WebLogContextProviderImpl();

    private WebLog() {
        // 処理なし
    }

    /**
     * @param pRequestMonitor -
     */
    public static void init(final IRequestMonitor pRequestMonitor) {
        pRequestMonitor.setRequestListener(new IRequestMonitor.IListener() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void onFinish() {
                _contextProvider.finishThread();
            }

            @SuppressWarnings("synthetic-access")
            @Override
            public void onStart(final HttpServletRequest pRequest) {
                _contextProvider.startThread(pRequest);
            }
        });
    }

    /**
     * pCallerを推測して{@link #tracer(Class)}を呼び出します.
     * 
     * @return -
     */
    public static IWebLog get() {
        try {
            final String callerClassName = new Throwable().getStackTrace()[1].getClassName();
            return tracer(Class.forName(callerClassName));
        } catch (final ClassNotFoundException e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    /**
     * @param pCaller -
     * @return -
     */
    public static IWebLog tracer(final Class<?> pCaller) {
        ArgUtil.checkNull(pCaller, "pCaller"); //$NON-NLS-1$
        return new Tracer(pCaller, _contextProvider);
    }
}
