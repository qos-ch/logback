package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

import static ch.qos.logback.classic.encoder.JsonEncoder.CAUSE_ATTR_NAME;
import static ch.qos.logback.classic.encoder.JsonEncoder.CLASS_NAME_ATTR_NAME;
import static ch.qos.logback.classic.encoder.JsonEncoder.CLOSE_ARRAY;
import static ch.qos.logback.classic.encoder.JsonEncoder.CLOSE_OBJ;
import static ch.qos.logback.classic.encoder.JsonEncoder.METHOD_NAME_ATTR_NAME;
import static ch.qos.logback.classic.encoder.JsonEncoder.OPEN_ARRAY;
import static ch.qos.logback.classic.encoder.JsonEncoder.OPEN_OBJ;
import static ch.qos.logback.classic.encoder.JsonEncoder.QUOTE;
import static ch.qos.logback.classic.encoder.JsonEncoder.QUOTE_COL;
import static ch.qos.logback.classic.encoder.JsonEncoder.STEP_ARRAY_NAME_ATTRIBUTE;
import static ch.qos.logback.classic.encoder.JsonEncoder.SUPPRESSED_ATTR_NAME;
import static ch.qos.logback.classic.encoder.JsonEncoder.VALUE_SEPARATOR;
import static ch.qos.logback.classic.encoder.JsonEncoder.appenderMember;
import static ch.qos.logback.classic.encoder.JsonEncoder.appenderMemberWithIntValue;
import static ch.qos.logback.classic.encoder.JsonEncoder.nullSafeStr;

class ArrayThrowableEncoder extends ThrowableEncoder {

    private static final String FILE_NAME_ATTR_NAME = "fileName";
    private static final String LINE_NUMBER_ATTR_NAME = "lineNumber";

    @Override
    void encodeStackTrace(StringBuilder sb, IThrowableProxy itp) {
        appendSTEPArray(sb, itp.getStackTraceElementProxyArray(), itp.getCommonFrames());

        IThrowableProxy cause = itp.getCause();
        if (cause != null) {
            sb.append(VALUE_SEPARATOR);
            appendThrowableProxy(sb, CAUSE_ATTR_NAME, cause);
        }

        IThrowableProxy[] suppressedArray = itp.getSuppressed();
        if (suppressedArray != null && suppressedArray.length != 0) {
            sb.append(VALUE_SEPARATOR);
            sb.append(QUOTE).append(SUPPRESSED_ATTR_NAME).append(QUOTE_COL);
            sb.append(OPEN_ARRAY);
            boolean first = true;
            for (IThrowableProxy suppressedITP : suppressedArray) {
                if (first) {
                    first = false;
                } else {
                    sb.append(VALUE_SEPARATOR);
                }
                appendThrowableProxy(sb, null, suppressedITP);
            }
            sb.append(CLOSE_ARRAY);
        }
    }

    private void appendSTEPArray(StringBuilder sb, StackTraceElementProxy[] stepArray, int commonFrames) {
        sb.append(QUOTE).append(STEP_ARRAY_NAME_ATTRIBUTE).append(QUOTE_COL).append(OPEN_ARRAY);

        int len = stepArray != null ? stepArray.length : 0;

        if (commonFrames >= len) {
            commonFrames = 0;
        }

        for (int i = 0; i < len - commonFrames; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);

            StackTraceElementProxy step = stepArray[i];

            sb.append(OPEN_OBJ);
            StackTraceElement ste = step.getStackTraceElement();

            appenderMember(sb, CLASS_NAME_ATTR_NAME, nullSafeStr(ste.getClassName()));
            sb.append(VALUE_SEPARATOR);

            appenderMember(sb, METHOD_NAME_ATTR_NAME, nullSafeStr(ste.getMethodName()));
            sb.append(VALUE_SEPARATOR);

            appenderMember(sb, FILE_NAME_ATTR_NAME, nullSafeStr(ste.getFileName()));
            sb.append(VALUE_SEPARATOR);

            appenderMemberWithIntValue(sb, LINE_NUMBER_ATTR_NAME, ste.getLineNumber());
            sb.append(CLOSE_OBJ);

        }

        sb.append(CLOSE_ARRAY);
    }
}
