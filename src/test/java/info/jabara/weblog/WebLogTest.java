/**
 * 
 */
package info.jabara.weblog;

import info.jabara.weblog.IRequestMonitor.IListener;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import mockit.Mocked;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jabaraster
 */
public class WebLogTest {

    @Mocked
    private HttpServletRequest request;

    private IListener          listener;

    /**
     * 
     */
    @SuppressWarnings("static-method")
    @Test
    public void _exception() {
        WebLog.get().exception(new Exception("テスト用の例外です.")); //$NON-NLS-1$
    }

    /**
     * 
     */
    @SuppressWarnings({ "nls" })
    @Test
    @Ignore
    public void _tracer() {
        final int threadCount = 3;
        final ExecutorService worker = Executors.newFixedThreadPool(threadCount);
        final Runnable task = new Runnable() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {
                WebLogTest.this.listener.onStart(WebLogTest.this.request);
                WebLog.get() //
                        .trace("{0}の日付は{1,date,yyyy/MM/dd}です.", "今日", Calendar.getInstance().getTime());
                WebLog.tracer(WebLog.class) //
                        .trace("{0}の日付は{1,date,yyyy/MM/dd}です.", "昨日", Calendar.getInstance().getTime());
                WebLogTest.this.listener.onFinish();
            }
        };
        for (int i = 0; i < threadCount; i++) {
            worker.submit(task);
        }

        WebLog.get() //
                .trace("{0}の日付は{1,date,yyyy/MM/dd}です.", "今日", Calendar.getInstance().getTime());
        WebLog.tracer(WebLog.class) //
                .trace("{0}の日付は{1,date,yyyy/MM/dd}です.", "昨日", Calendar.getInstance().getTime());
    }

    /**
     * 
     */
    @Before
    public void setUp() {
        WebLog.init(new IRequestMonitor() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void setRequestListener(final IListener pListener) {
                WebLogTest.this.listener = pListener;
                pListener.onStart(WebLogTest.this.request);
            }
        });
    }

    /**
     * 
     */
    @After
    public void tearDown() {
        this.listener.onFinish();
    }
}
