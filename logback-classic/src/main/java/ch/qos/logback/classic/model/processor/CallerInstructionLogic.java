/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.qos.logback.core.CoreConstants.CODES_URL;

/**
 * Detects contradictory caller-data extraction instructions across appenders.
 *
 * <p>During configuration analysis, each appender is associated with an
 * {@link Instruction} describing whether it wants caller data extracted and
 * how. This class checks a map of appender name to instruction for
 * combinations that cannot work together at runtime and returns the
 * corresponding {@link Status} messages.</p>
 *
 * <p>Compatibility rules:</p>
 * <ul>
 *   <li>{@link Instruction#DIRECT_WANT} may appear alone.</li>
 *   <li>{@link Instruction#PREPROCESS_WANT} may coexist with
 *       {@link Instruction#DIRECT_WANT}.</li>
 *   <li>{@link Instruction#DO_NOT_WANT} must not coexist with
 *       {@link Instruction#PREPROCESS_WANT}.</li>
 *   <li>{@link Instruction#DO_NOT_WANT} must not coexist with
 *       {@link Instruction#DIRECT_WANT}.</li>
 *   <li>{@link Instruction#PREPROCESS_WANT} alone is not a valid
 *       configuration.</li>
 * </ul>
 *
 * @since 1.6.2
 * @see CallerContradictionAnalyser
 * @see CallerContradictionWarnAnalyser
 */
public class CallerInstructionLogic {

    static final String CALLER_CONTRADICTION_ANCHOR = "#callerContradiction";
    static final String CALLER_CONTRADICTION_URL = CODES_URL + CALLER_CONTRADICTION_ANCHOR;
    static final String WARNING_MSG_TEMPLATE = "appenders named %s instruct against caller extraction info while other appenders named %s instruct in favor of caller extraction";
    static final String LONE_PREPROCESS_WANT_MSG_TEMPLATE = "appenders named %s instruct preprocessing of caller extraction info but no appender instructs in favor of caller extraction";
    static final String NO_CONTRADICTIONS_MSG = "No contradictions in caller extraction instruction were detected";

    /**
     * How an appender relates to caller-data extraction.
     */
    enum Instruction {
        /**
         * Caller data should be extracted during preprocessing (for example by
         * an {@code AsyncAppender} with {@code includeCallerData} set to
         * {@code true}) so that nested appenders can use it.
         */
        PREPROCESS_WANT,

        /**
         * Caller data should not be extracted (for example an
         * {@code AsyncAppender} with {@code includeCallerData} false or
         * absent, the default).
         */
        DO_NOT_WANT,

        /**
         * The appender itself requires caller data, typically because its
         * layout pattern uses a caller-data converter such as {@code %C},
         * {@code %M}, {@code %L}, {@code %F}, {@code %l}, or
         * {@code %caller}.
         */
        DIRECT_WANT,
    }

    /**
     * Checks the given appender instructions for contradictions.
     *
     * <p>The map maps appender names to the caller-inclusion instruction
     * gathered during analysis of the configuration model. Contradictions
     * are reported as {@link WarnStatus} entries; if none are found, a
     * single {@link InfoStatus} is returned. When one or more contradiction
     * warnings are produced, an additional warning pointing to
     * {@link #CALLER_CONTRADICTION_URL} is appended.</p>
     *
     * @param appenderNameToInstructionMap map of appender name to its
     *        {@link Instruction}; must not be {@code null}
     * @return a non-empty list of status objects describing the outcome of
     *         the contradiction check
     */
    public List<Status> contradiction(Map<String, Instruction> appenderNameToInstructionMap) {
        List<String> preprocessWantList   = new ArrayList<>();
        List<String> doNotWantList = new ArrayList<>();
        List<String> directWantList   = new ArrayList<>();

        for (Map.Entry<String, Instruction> e : appenderNameToInstructionMap.entrySet()) {
            switch (e.getValue()) {
                case PREPROCESS_WANT:
                    preprocessWantList.add(e.getKey());
                    break;
                case DO_NOT_WANT:
                    doNotWantList.add(e.getKey());
                    break;
                case DIRECT_WANT:
                    directWantList.add(e.getKey());
                    break;
            }
        }

        List<Status> result = new ArrayList<>();

        // DIRECT_WANT elements can exist alone
        // one or more PREPROCESS_WANT elements can coexist one or more DIRECT_WANT elements
        // DO_NOT_WANT cannot be allowed to coexist with PREPROCESS_WANT;
        // DO_NOT_WANT cannot be allowed to coexist with DIRECT_WANT;
        // PREPROCESS_WANT alone is not allowed.
        // DO_NOT_WANT and PREPROCESS_WANT are contradictory

        if (!doNotWantList.isEmpty() && !preprocessWantList.isEmpty()) {
            String msg = String.format(
                    WARNING_MSG_TEMPLATE,
                    String.join(", ", doNotWantList),
                    String.join(", ", preprocessWantList));

            result.add(new WarnStatus(msg, this));
        }

        if (!doNotWantList.isEmpty() && !directWantList.isEmpty()) {
            String msg = String.format(
                    WARNING_MSG_TEMPLATE,
                    String.join(", ", doNotWantList),
                    String.join(", ", directWantList));

            result.add(new WarnStatus(msg, this));
        }

        // PREPROCESS_WANT alone (without DIRECT_WANT) is not allowed
        if (!preprocessWantList.isEmpty() && directWantList.isEmpty() && doNotWantList.isEmpty()) {
            String msg = String.format(
                    LONE_PREPROCESS_WANT_MSG_TEMPLATE,
                    String.join(", ", preprocessWantList));
            result.add(new WarnStatus(msg, this));
        }

        if (result.isEmpty()) {
            result.add(new InfoStatus(NO_CONTRADICTIONS_MSG, this));
        } else {
            result.add(new WarnStatus("See "+CALLER_CONTRADICTION_URL+" for details", this));
        }

        return result;
    }



}
