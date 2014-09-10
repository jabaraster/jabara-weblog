/**
 * 
 */
package info.jabara.weblog;

import info.jabara.weblog.IRequestMonitor.IListener;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jabaraster
 */
public class WebLogFilter implements Filter {

    @SuppressWarnings("synthetic-access")
    private final RequestMonitor requestMonitor = new RequestMonitor();

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // 処理なし
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(final ServletRequest pRequest, final ServletResponse pResponse, final FilterChain pChain) throws IOException,
            ServletException {

        final IListener listener = this.requestMonitor.getListener();
        listener.onStart((HttpServletRequest) pRequest);
        try {
            pChain.doFilter(pRequest, pResponse);

        } catch (IOException | ServletException e) {
            WebLog.tracer(WebLogFilter.class).exception(e);
            throw e;
        } catch (RuntimeException | Error e) {
            WebLog.tracer(WebLogFilter.class).exception(e);
            throw e;
        } catch (final Throwable e) {
            WebLog.tracer(WebLogFilter.class).exception(e);
            throw new ServletException(e);
        } finally {
            listener.onFinish();
        }
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(@SuppressWarnings("unused") final FilterConfig pFilterConfig) {
        WebLog.init(this.requestMonitor);
    }

    private static class RequestMonitor implements IRequestMonitor {

        private IListener listener;

        /**
         * @see info.jabara.weblog.IRequestMonitor#setRequestListener(info.jabara.weblog.IRequestMonitor.IListener)
         */
        @Override
        public void setRequestListener(final IListener pListener) {
            this.listener = pListener;
        }

        IListener getListener() {
            return this.listener;
        }
    }
}
