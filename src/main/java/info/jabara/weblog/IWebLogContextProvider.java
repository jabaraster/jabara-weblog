/**
 * 
 */
package info.jabara.weblog;

import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jabaraster
 */
public interface IWebLogContextProvider {

    /**
     * @return -
     */
    WebLogContext get();

    /**
     * このメソッドで取得した{@link Writer}オブジェクトは、呼び出し元でクローズして下さい.
     * 
     * @param pExceptionId ファイル名に使われるため、使用できる文字には制限があります.
     * @return -
     */
    Writer getExceptionLogWriter(final String pExceptionId);

    /**
     * @return -
     */
    Writer getTraceLogWriter();

    /**
     * 
     */
    void finishThread();

    /**
     * @param pCaller -
     * @return -
     */
    boolean isEnabled(final Class<?> pCaller);

    /**
     * @return -
     */
    boolean isOutputToConsole();

    /**
     * @return -
     */
    boolean isMethodOutput();

    /**
     * @return -
     */
    boolean isLineNumberOutput();

    /**
     * @param pRequest -
     */
    void startThread(final HttpServletRequest pRequest);
}
