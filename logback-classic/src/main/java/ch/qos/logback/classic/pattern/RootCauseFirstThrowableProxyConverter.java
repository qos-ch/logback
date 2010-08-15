package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-08-07, 14:07:10
 */
public class RootCauseFirstThrowableProxyConverter extends ExtendedThrowableProxyConverter {

    @Override
    protected String printThrowableToString(IThrowableProxy tp) {
        StringBuilder buf = new StringBuilder(2048);
        printRootCauseFirst(tp, buf);
        return buf.toString();
    }

    private void printRootCauseFirst(IThrowableProxy tp, StringBuilder buf) {
        if (tp.getCause() != null)
            printRootCauseFirst(tp.getCause(), buf);
        printRootCause(tp, buf);
    }

    private void printRootCause(IThrowableProxy tp, StringBuilder buf) {
        ThrowableProxyUtil.printFirstLineRootCauseFirst(buf, tp);
        buf.append(CoreConstants.LINE_SEPARATOR);
        StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        int commonFrames = tp.getCommonFrames();

        boolean unrestrictedPrinting = lengthOption > stepArray.length;
        int length = (unrestrictedPrinting) ? stepArray.length : lengthOption;


        int maxIndex = length;
        if (commonFrames > 0 && unrestrictedPrinting) {
            maxIndex -= commonFrames;
        }

        for (int i = 0; i < maxIndex; i++) {
            String string = stepArray[i].toString();
            buf.append(CoreConstants.TAB);
            buf.append(string);
            extraData(buf, stepArray[i]); // allow other data to be added
            buf.append(CoreConstants.LINE_SEPARATOR);
        }

    }


}
