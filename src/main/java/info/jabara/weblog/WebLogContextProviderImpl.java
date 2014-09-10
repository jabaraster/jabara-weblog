/**
 * 
 */
package info.jabara.weblog;

import jabara.general.ArgUtil;
import jabara.general.ExceptionUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * @author jabaraster
 */
public class WebLogContextProviderImpl implements IWebLogContextProvider {

    private static final FastDateFormat      _dateFormatter        = FastDateFormat.getInstance("yyyyMMdd-HHmmss-SSS"); //$NON-NLS-1$

    private static final Path                logDirectory          = Paths.get(".", "logs");                           //$NON-NLS-1$ //$NON-NLS-2$
    private static final Path                traceLogDirectory     = Paths.get(logDirectory.toString(), "trace");      //$NON-NLS-1$
    private static final Path                exceptionLogDirectory = Paths.get(logDirectory.toString(), "exception");  //$NON-NLS-1$

    private final ThreadLocal<WebLogContext> contexts              = new ThreadLocal<>();
    private final ThreadLocal<Writer>        writers               = new ThreadLocal<>();

    /**
     * @see info.jabara.weblog.IWebLogContextProvider#finishThread()
     */
    @Override
    public void finishThread() {
        this.contexts.set(null);

        try {
            this.writers.get().close();
        } catch (final IOException e) {
            // 無視
        }
        this.writers.set(null);
    }

    /**
     * @see info.jabara.weblog.IWebLogContextProvider#get()
     */
    @Override
    public WebLogContext get() {
        return this.contexts.get();
    }

    /**
     * @see info.jabara.weblog.IWebLogContextProvider#getExceptionLogWriter(java.lang.String)
     */
    @Override
    public Writer getExceptionLogWriter(final String pExceptionId) {
        ArgUtil.checkNullOrEmpty(pExceptionId, "pExceptionId"); //$NON-NLS-1$

        final String time = _dateFormatter.format(Calendar.getInstance());
        final Path logFilePath = Paths.get(exceptionLogDirectory.toString(), time + "-" + pExceptionId + ".log"); //$NON-NLS-1$ //$NON-NLS-2$

        OutputStream target = null;
        try {
            target = new FileOutputStream(logFilePath.toFile());
        } catch (final FileNotFoundException e) {
            throw ExceptionUtil.rethrow(e);
        }
        return new BufferedWriter(new OutputStreamWriter(target, Charset.forName("UTF-8"))); //$NON-NLS-1$
    }

    /**
     * @see info.jabara.weblog.IWebLogContextProvider#getTraceLogWriter()
     */
    @Override
    public Writer getTraceLogWriter() {
        return this.writers.get();
    }

    /**
     * @see info.jabara.weblog.IWebLogContextProvider#isEnabled(java.lang.Class)
     */
    @Override
    public boolean isEnabled(final Class<?> pCaller) {
        return true; // TODO 設定をどこからか読み込むようにしたい.
    }

    /**
     * @see info.jabara.weblog.IWebLogContextProvider#isLineNumberOutput()
     */
    @Override
    public boolean isLineNumberOutput() {
        return true; // TODO 設定をどこからか読み込むようにしたい.
    }

    /**
     * @see info.jabara.weblog.IWebLogContextProvider#isMethodOutput()
     */
    @Override
    public boolean isMethodOutput() {
        return true; // TODO 設定をどこからか読み込むようにしたい.
    }

    /**
     * @see info.jabara.weblog.IWebLogContextProvider#isOutputToConsole()
     */
    @Override
    public boolean isOutputToConsole() {
        return true; // TODO 設定をどこからか読み込むようにしたい.
    }

    /**
     * @see info.jabara.weblog.IWebLogContextProvider#startThread(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public void startThread(final HttpServletRequest pRequest) {
        try {
            Files.createDirectories(WebLogContextProviderImpl.traceLogDirectory);
            Files.createDirectories(WebLogContextProviderImpl.exceptionLogDirectory);
        } catch (final IOException e) {
            throw ExceptionUtil.rethrow(e);
        }

        final OutputFile outputFile = createNewFile();
        this.contexts.set(new WebLogContext(pRequest, outputFile.getDescriptor()));
        this.writers.set(createWriter(outputFile));
    }

    private static OutputFile createNewFile() {
        try {
            final int retryCount = 3;
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < retryCount; i++) {
                final String descriptor = _dateFormatter.format(Calendar.getInstance().getTime()) + "-" + randomNumber(random); //$NON-NLS-1$
                final String fileName = descriptor + ".log"; //$NON-NLS-1$
                final File file = new File(traceLogDirectory.toFile(), fileName);
                try {
                    Files.createFile(file.toPath());
                    return new OutputFile(file, descriptor);
                } catch (final FileAlreadyExistsException e) {
                    continue; // リトライする
                }
            }
            throw new IllegalStateException("ログファイルを作成出来ませんでした."); //$NON-NLS-1$

        } catch (final IOException e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    private static BufferedWriter createWriter(final OutputFile pOutputFile) {
        try {
            final OutputStream target = new FileOutputStream(pOutputFile.getPath());
            return new BufferedWriter(new OutputStreamWriter(target, Charset.forName("UTF-8"))); //$NON-NLS-1$

        } catch (final FileNotFoundException e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    private static String randomNumber(final ThreadLocalRandom pRandom) {
        return new DecimalFormat("0000").format(pRandom.nextInt(10000) + 1); //$NON-NLS-1$
    }

    private static class OutputFile {
        private final File   path;
        private final String descriptor;

        OutputFile(final File pPath, final String pDescriptor) {
            this.path = pPath;
            this.descriptor = pDescriptor;
        }

        String getDescriptor() {
            return this.descriptor;
        }

        File getPath() {
            return this.path;
        }
    }
}
