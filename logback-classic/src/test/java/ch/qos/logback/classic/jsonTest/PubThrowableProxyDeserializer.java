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

package ch.qos.logback.classic.jsonTest;

import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.spi.PubThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PubThrowableProxyDeserializer  extends StdDeserializer<PubThrowableProxy>  {

    protected PubThrowableProxyDeserializer() {
        this(null);
    }
    protected PubThrowableProxyDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public PubThrowableProxy deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        return jsonNodeThrowableProxy(node);
    }

    static StackTraceElementProxy[] EMPTY_STEP_ARRAY = new StackTraceElementProxy[0];
    static PubThrowableProxy[] EMPTY_PTP_ARRAY = new PubThrowableProxy[0];

    private static PubThrowableProxy jsonNodeThrowableProxy(JsonNode node) {
        JsonNode classNameJN = node.get(JsonEncoder.CLASS_NAME_ATTR_NAME);
        JsonNode messageJN = node.get(JsonEncoder.MESSAGE_ATTR_NAME);
        JsonNode stepArrayJN = node.get(JsonEncoder.STEP_ARRAY_NAME_ATTRIBUTE);
        JsonNode causeJN = node.get(JsonEncoder.CAUSE_ATTR_NAME);
        JsonNode commonFramesCountJN = node.get(JsonEncoder.COMMON_FRAMES_COUNT_ATTR_NAME);

        JsonNode suppressedJN = node.get(JsonEncoder.SUPPRESSED_ATTR_NAME);

        PubThrowableProxy ptp = new PubThrowableProxy();
        ptp.setClassName(classNameJN.textValue());
        ptp.setMessage(messageJN.textValue());

        List<StackTraceElementProxy> stepList = stepNodeToList(stepArrayJN);
        ptp.setStackTraceElementProxyArray(stepList.toArray(EMPTY_STEP_ARRAY));

        if(commonFramesCountJN != null) {
            int commonFramesCount = commonFramesCountJN.asInt();
            ptp.setCommonFramesCount(commonFramesCount);
        }

        if(causeJN != null) {
            PubThrowableProxy cause = jsonNodeThrowableProxy(causeJN);
            ptp.setCause(cause);
        }

        if(suppressedJN != null) {
            //System.out.println("suppressedJN "+suppressedJN);
            List<PubThrowableProxy>  ptpList = suppressedNodeToList(suppressedJN);
            System.out.println("iiiiiiiiiiii");
            System.out.println("ptpList="+ptpList);

            ptp.setSuppressed(ptpList.toArray(EMPTY_PTP_ARRAY));
        }

        System.out.println("xxxxxxxxxxxxx");
        System.out.println(ptp.getSuppressed());

        return ptp;
    }

    private static List<StackTraceElementProxy> stepNodeToList(JsonNode stepArrayJN) {
        List<StackTraceElementProxy> stepList = new ArrayList<>();
        for(JsonNode jsonNode: stepArrayJN) {
            StackTraceElementProxy step = STEPDeserializer.jsonNodeToSTEP(jsonNode);
            stepList.add(step);
        }
        return stepList;
    }

    private static List<PubThrowableProxy> suppressedNodeToList(JsonNode ptpArrayJN) {
        List<PubThrowableProxy> ptpList = new ArrayList<>();
        for(JsonNode jsonNode: ptpArrayJN) {
            //System.out.println("---in  suppressedNodeToList seeing "+jsonNode);
            PubThrowableProxy ptp = jsonNodeThrowableProxy(jsonNode);
            //System.out.println("--in  suppressedNodeToList ptp="+ptp);
            ptpList.add(ptp);
        }
        return ptpList;
    }
}
