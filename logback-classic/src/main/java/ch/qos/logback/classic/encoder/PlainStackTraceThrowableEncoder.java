package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

import static ch.qos.logback.classic.encoder.JsonEncoder.appenderMember;

class PlainStackTraceThrowableEncoder extends ThrowableEncoder {
    public static final String STACKTRACE_NAME_ATTRIBUTE = "stackTrace";

    @Override
    void encodeStackTrace(StringBuilder sb, IThrowableProxy itp) {
        appenderMember(sb, STACKTRACE_NAME_ATTRIBUTE, getOriginalStackTrace(itp));
    }

    private static String getOriginalStackTrace(IThrowableProxy throwableProxy) {
        StringBuilder sb = new StringBuilder();
        getOriginalStackTrace(throwableProxy, sb, 0);
        return sb.toString();
    }

    private static void getOriginalStackTrace(IThrowableProxy throwable, StringBuilder sb, int depth) {
        if (throwable == null) {
            return;
        }
        if (depth > 0) {
            sb.append("Caused by: ");
        }
        sb.append(throwable.getClassName()).append(": ").append(throwable.getMessage()).append(NEWLINE);
        for (StackTraceElementProxy step : throwable.getStackTraceElementProxyArray()) {
            sb.append(TAB).append(step.getSTEAsString()).append(NEWLINE);
        }
        getOriginalStackTrace(throwable.getCause(), sb, depth + 1);
    }
}
