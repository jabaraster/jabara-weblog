/**
 * 
 */
package info.jabara.weblog;

import jabara.general.ArgUtil;
import jabara.general.ExceptionUtil;
import jabara.general.MapUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * @author jabaraster
 */
class Tracer implements IWebLog {

    private static final FastDateFormat  _dateFormatter = FastDateFormat.getInstance("yyyy/MM/dd HH-mm-ss.SSS"); //$NON-NLS-1$

    private final Class<?>               caller;
    private final IWebLogContextProvider contextProvider;

    private final boolean                lineNumberOutput;
    private final boolean                methodOutput;
    private final boolean                outputToConsole;
    private final boolean                enabled;

    private static final String          ENC            = "UTF-8";                                              //$NON-NLS-1$

    Tracer(final Class<?> pCaller, final IWebLogContextProvider pContextProvider) {
        ArgUtil.checkNull(pCaller, "pCaller"); //$NON-NLS-1$
        ArgUtil.checkNull(pContextProvider, "pContextProvider"); //$NON-NLS-1$
        this.caller = pCaller;
        this.contextProvider = pContextProvider;

        this.lineNumberOutput = pContextProvider.isLineNumberOutput();
        this.methodOutput = pContextProvider.isMethodOutput();
        this.outputToConsole = pContextProvider.isOutputToConsole();
        this.enabled = pContextProvider.isEnabled(pCaller);
    }

    /**
     * @see info.jabara.weblog.IWebLog#exception(java.lang.Throwable)
     */
    @Override
    public void exception(final Throwable pException) {
        if (pException == null) {
            return;
        }

        final String exceptionMessage = buildExceptionMessage(pException);
        final StackTraceElement stack = getCallerCallerStack();
        if (this.outputToConsole) {
            printToConsole(stack, exceptionMessage);
        }
        final String exceptionId = toHash(exceptionMessage);
        writeTraceLog(stack, "例外発生", MapUtil.<String, String> m("exceptionId", exceptionId)); //$NON-NLS-1$ //$NON-NLS-2$
        writeExceptionLog(exceptionMessage, exceptionId);
    }

    /**
     * @see info.jabara.weblog.IWebLog#trace(java.lang.String, java.lang.Object[])
     */
    @Override
    public void trace(final String pMessage, final Object... pValues) {
        if (!this.enabled) {
            return;
        }
        final String message = buildMessage(pMessage, pValues);
        final StackTraceElement stack = getCallerCallerStack();
        if (this.outputToConsole) {
            printToConsole(stack, message);
        }

        writeTraceLog(stack, message, null);
    }

    private String buildLog(final StackTraceElement pStack, final String pMessage, final Map<String, String> pExtension) {
        final WebLogContext context = this.contextProvider.get();
        final HttpServletRequest request = context.getRequest();

        @SuppressWarnings("nls")
        final Map<String, Object> logs = MapUtil.m( //
                "caller", this.caller.getName() //
                , "time", _dateFormatter.format(Calendar.getInstance().getTime()) //
                , "message", pMessage //
                , "req", request.getMethod() + " " + request.getRequestURI() //
                , "host", request.getRemoteHost() //
                , "thread", Thread.currentThread().getName() //
                , "requestId", this.contextProvider.get().getDescriptor() //
                );
        if (this.lineNumberOutput) {
            logs.put("lineNumber", Integer.valueOf(pStack.getLineNumber())); //$NON-NLS-1$
        }
        if (this.methodOutput) {
            logs.put("method", pStack.getMethodName()); //$NON-NLS-1$
        }
        if (pExtension != null) {
            for (final Map.Entry<String, String> entry : pExtension.entrySet()) {
                logs.put(entry.getKey(), entry.getValue());
            }
        }

        return toLtsv(logs);
    }

    private StackTraceElement getCallerCallerStack() {
        if (this.lineNumberOutput || this.methodOutput) {
            return new Throwable().getStackTrace()[2];
        }
        return null;
    }

    @SuppressWarnings("nls")
    private void printToConsole(final StackTraceElement pStack, final String message) {

        final StringBuilder callerPlace = new StringBuilder();
        callerPlace.append(this.caller.getName());
        if (this.methodOutput) {
            callerPlace.append("#").append(pStack.getMethodName());
        }
        final String callerPlaceS = StringUtils.leftPad(StringUtils.right(new String(callerPlace), 30), 30);

        final StringBuilder sb = new StringBuilder();
        sb.append("[").append(this.contextProvider.get().getDescriptor()) //
                .append("][") //
                .append(callerPlaceS).append("]");
        if (this.lineNumberOutput) {
            sb.append("[").append(pStack.getLineNumber()).append("行目] ");
        }
        sb.append(message);

        System.out.println(new String(sb));
    }

    @SuppressWarnings("nls")
    private void writeExceptionLog(final String pExceptionMessage, final String pExceptionId) {
        final String time = _dateFormatter.format(Calendar.getInstance().getTime());
        final StringBuilder sb = new StringBuilder();
        sb.append("[").append(time).append("]") //
                .append("[Request ID:").append(this.contextProvider.get().getDescriptor()).append("]") //
                .append("[Exception ID:").append(pExceptionId).append("] ") //
                .append(pExceptionMessage);

        try (final Writer writer = this.contextProvider.getExceptionLogWriter(pExceptionId)) {
            writer.write(new String(sb));
            writer.write("\n"); //$NON-NLS-1$
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTraceLog(final StackTraceElement pStack, final String message, final Map<String, String> pExtension) {
        try {
            @SuppressWarnings("resource")
            final Writer writer = this.contextProvider.getTraceLogWriter();
            writer.write(buildLog(pStack, message, pExtension));
            writer.write("\n"); //$NON-NLS-1$
            writer.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("nls")
    static String toLtsv(final Map<String, Object> pLogs) {
        final StringBuilder sb = new StringBuilder();
        final String SEPARATOR = "\t";
        for (final Map.Entry<String, Object> entry : pLogs.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            sb.append(key).append(":").append(String.valueOf(value).replaceAll(":", "").replaceAll("\\t", ""));
            sb.append(SEPARATOR);
        }
        sb.delete(sb.length() - 1, sb.length());
        return new String(sb);
    }

    private static String buildExceptionMessage(final Throwable pException) {
        try {
            final ByteArrayOutputStream memory = new ByteArrayOutputStream();

            final String msg = pException.getMessage();
            if (msg != null) {
                memory.write((msg + "\n").getBytes(ENC)); //$NON-NLS-1$
            }

            final PrintStream ps = new PrintStream(memory, false, ENC);
            pException.printStackTrace(ps);
            ps.flush();

            return new String(memory.toByteArray(), ENC);

        } catch (final IOException e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    private static String buildMessage(final String pMessage, final Object[] pValues) {
        try {
            return MessageFormat.format(pMessage, pValues);

        } catch (final IllegalArgumentException e) {
            final StringBuilder sb = new StringBuilder(pMessage);
            for (final Object value : pValues) {
                sb.append(",").append(value); //$NON-NLS-1$
            }
            return new String(sb);
        }
    }

    private static String toHash(final String pExceptionMessage) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
            digest.update(pExceptionMessage.getBytes(ENC));
            return toHexString(digest.digest());

        } catch (final NoSuchAlgorithmException e) {
            throw ExceptionUtil.rethrow(e);
        } catch (final UnsupportedEncodingException e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    private static String toHexString(final byte[] by) {
        final StringBuilder sb = new StringBuilder();
        for (final int b : by) {
            sb.append(Character.forDigit(b >> 4 & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return new String(sb);
    }
}
