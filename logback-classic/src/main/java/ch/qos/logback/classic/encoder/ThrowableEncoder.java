package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.spi.IThrowableProxy;

import static ch.qos.logback.classic.encoder.JsonEncoder.CLASS_NAME_ATTR_NAME;
import static ch.qos.logback.classic.encoder.JsonEncoder.CLOSE_OBJ;
import static ch.qos.logback.classic.encoder.JsonEncoder.COMMON_FRAMES_COUNT_ATTR_NAME;
import static ch.qos.logback.classic.encoder.JsonEncoder.MESSAGE_ATTR_NAME;
import static ch.qos.logback.classic.encoder.JsonEncoder.OPEN_OBJ;
import static ch.qos.logback.classic.encoder.JsonEncoder.QUOTE;
import static ch.qos.logback.classic.encoder.JsonEncoder.QUOTE_COL;
import static ch.qos.logback.classic.encoder.JsonEncoder.THROWABLE_ATTR_NAME;
import static ch.qos.logback.classic.encoder.JsonEncoder.VALUE_SEPARATOR;
import static ch.qos.logback.classic.encoder.JsonEncoder.appenderMember;
import static ch.qos.logback.classic.encoder.JsonEncoder.appenderMemberWithIntValue;
import static ch.qos.logback.classic.encoder.JsonEncoder.jsonEscape;
import static ch.qos.logback.classic.encoder.JsonEncoder.nullSafeStr;
import static ch.qos.logback.core.model.ModelConstants.NULL_STR;

abstract class ThrowableEncoder {

    static final String NEWLINE = "\\n";
    static final String TAB = "\\t";

    private static final String CYCLIC_THROWABLE_ATTR_NAME = "cyclic";

    static void appendThrowableProxy(StringBuilder sb, IThrowableProxy throwableProxy, boolean withPlainStackTrace) {
        ThrowableEncoder throwableEncoder = withPlainStackTrace ? new PlainStackTraceThrowableEncoder() : new ArrayThrowableEncoder();
        throwableEncoder.appendThrowableProxy(sb, THROWABLE_ATTR_NAME, throwableProxy);
    }

    protected void appendThrowableProxy(StringBuilder sb, String attributeName, IThrowableProxy itp) {
        // in the nominal case, attributeName != null. However, attributeName will be null for suppressed
        // IThrowableProxy array, in which case no attribute name is needed
        if (attributeName != null) {
            sb.append(QUOTE).append(attributeName).append(QUOTE_COL);
            if (itp == null) {
                sb.append(NULL_STR);
                return;
            }
        }

        sb.append(OPEN_OBJ);

        appenderMember(sb, CLASS_NAME_ATTR_NAME, nullSafeStr(itp.getClassName()));

        sb.append(VALUE_SEPARATOR);
        appenderMember(sb, MESSAGE_ATTR_NAME, jsonEscape(itp.getMessage()));

        if (itp.isCyclic()) {
            sb.append(VALUE_SEPARATOR);
            appenderMember(sb, CYCLIC_THROWABLE_ATTR_NAME, jsonEscape("true"));
        }

        if (itp.getCommonFrames() != 0) {
            sb.append(VALUE_SEPARATOR);
            appenderMemberWithIntValue(sb, COMMON_FRAMES_COUNT_ATTR_NAME, itp.getCommonFrames());
        }

        sb.append(VALUE_SEPARATOR);
        encodeStackTrace(sb, itp);
        sb.append(CLOSE_OBJ);
    }

    abstract void encodeStackTrace(StringBuilder sb, IThrowableProxy itp);
}
