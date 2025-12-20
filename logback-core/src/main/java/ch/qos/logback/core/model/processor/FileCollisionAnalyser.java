/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 *  Copyright (C) 1999-2025, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *     or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.helper.FileNamePattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PhaseIndicator(phase = ProcessingPhase.DEPENDENCY_ANALYSIS)
public class FileCollisionAnalyser extends ModelHandlerBase {

    // Key: appender name, Value: file path
    final static String FA_FILE_COLLISION_MAP_KEY = "FA_FILE_COLLISION_MAP_KEY";

    // Key: appender name, Value: FileNamePattern
    Map<String, FileNamePattern> RFA_FILENAME_COLLISTION_MAP = new HashMap<>();


    public FileCollisionAnalyser(Context context) {
        super(context);
    }

    @Override
    protected Class<AppenderModel> getSupportedModelClass() {
        return AppenderModel.class;
    }


    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        AppenderModel appenderModel = (AppenderModel) model;

        String originalClassName = appenderModel.getClassName();
        String className = mic.getImport(originalClassName);

        String appenderName = appenderModel.getName();

        if (!fileAppenderOrRollingFileAppender(className)) {
            return;
        }

        String tagName0 = "file";
        checkForCollisions(mic, MapKey.FILE_COLLISION_MAP_KEY, appenderModel, appenderName, tagName0);

        String tagName1 = "fileNamePattern";
        checkForCollisions(mic, MapKey.RFA_FILENAME_COLLISION_MAP, appenderModel, appenderName, tagName1);
    }

    private static boolean fileAppenderOrRollingFileAppender(String className) {
        return FileAppender.class.getName().equals(className) || RollingFileAppender.class.getName().equals(className);
    }


    boolean tagPredicate(Model model, String tagName) {
        return (model instanceof ImplicitModel) && tagName.equals(model.getTag());
    }

    enum MapKey {
        FILE_COLLISION_MAP_KEY, RFA_FILENAME_COLLISION_MAP
    }

    private void checkForCollisions(ModelInterpretationContext mic, MapKey mapKey, AppenderModel appenderModel, String appenderName, final String tagName) {


        Stream<Model> streamLevel1 = appenderModel.getSubModels().stream();
        Stream<Model> streamLevel2 = appenderModel.getSubModels().stream().flatMap(child -> child.getSubModels().stream());

        List<Model> matchingModels = Stream.concat(streamLevel1, streamLevel2).filter(m -> tagPredicate(m, tagName)).collect(Collectors.toList());

        //List<Model> matchingModels = appenderModel.getSubModels().stream().filter(m -> tagPredicate(m, tagName)).collect(Collectors.toList());

        if(!matchingModels.isEmpty()) {
            ImplicitModel implicitModel = (ImplicitModel) matchingModels.get(0);
            String bodyValue = mic.subst(implicitModel.getBodyText());


            Map<String, String> faileCollisionMap = getCollisionMapByKey(mic, mapKey);

            Optional<Map.Entry<String, String>> collision = faileCollisionMap.entrySet()
                    .stream()
                    .filter(entry -> bodyValue.equals(entry.getValue()))
                    .findFirst();

            if (collision.isPresent()) {
                addErrorForCollision(tagName, appenderName, collision.get().getKey(), bodyValue);
                appenderModel.markAsHandled();
                appenderModel.deepMarkAsSkipped();
            } else {
                // add to collision map if and only if no collision detected
                // reasoning: single entry is as effective as multiple entries for collision detection
                faileCollisionMap.put(appenderName, bodyValue);
            }
        }
    }

    private Map<String, String> getCollisionMapByKey(ModelInterpretationContext mic, MapKey mapKey) {
        Map<String, String> map = (Map<String, String>) mic.getObjectMap().get(mapKey.name());
        if(map == null) {
            map = new HashMap<>();
            mic.getObjectMap().put(mapKey.name(), map);
        }
        return map;
    }


    static public final String COLLISION_DETECTED = "Collision detected. Skipping initialization of appender named [%s]";
    static public final String COLLISION_MESSAGE = "In appender [%s] option '%s' has the same value '%s' as that set for appender [%s] defined earlier";
    private void addErrorForCollision(String optionName, String appenderName, String previousAppenderName, String optionValue) {
        addError(String.format(COLLISION_DETECTED, appenderName));
        addError(String.format(COLLISION_MESSAGE, appenderName, optionName, optionValue, previousAppenderName));
    }
}
