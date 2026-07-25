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

package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.SiftModel;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.helper.FileNamePattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PhaseIndicator(phase = ProcessingPhase.DEPENDENCY_ANALYSIS)
public class FileCollisionAnalyser extends ModelHandlerBase {

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

        // A SiftingAppender instantiates its nested appender once per discriminator
        // value at runtime, so the nested appender escapes the static collision maps
        // handled below. If its file/fileNamePattern does not reference the
        // discriminator key, every instance resolves to the same target and their
        // output silently collides. Detect that at model-analysis time (see #1041).
        Model siftModel = firstSubModelOfClass(appenderModel, SiftModel.class);
        if (siftModel != null) {
            checkSiftingAppenderForCollision(mic, appenderModel, siftModel, appenderName);
            return;
        }

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

        if(!matchingModels.isEmpty()) {
            ImplicitModel implicitModel = (ImplicitModel) matchingModels.get(0);
            String bodyValue = mic.subst(implicitModel.getBodyText());


            Map<String, String> collisionMap = getCollisionMapByKey(mic, mapKey);

            Optional<Map.Entry<String, String>> collision = collisionMap.entrySet()
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
                collisionMap.put(appenderName, bodyValue);
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

    static public final String SIFT_COLLISION_MESSAGE_0 =
            "The nested appender of SiftingAppender [%s] does not reference the discriminator key [%s] in its 'file'/'fileNamePattern'. ";
    static public final String SIFT_COLLISION_MESSAGE_1 =
            "Every child appender will therefore write to the same file [%s] causing collisions. ";
    static public final String SIFT_COLLISION_MESSAGE_2 = "Consider embedding ${%s} in the file path.";

    /**
     * Detect the SiftingAppender variant of a file collision: the nested appender is
     * created once per discriminator value, so unless its file/fileNamePattern embeds a
     * reference to the discriminator key, all instances resolve to the same target.
     */
    private void checkSiftingAppenderForCollision(ModelInterpretationContext mic, AppenderModel appenderModel,
            Model siftModel, String appenderName) {

        String discriminatorKey = findDiscriminatorKey(appenderModel);
        if (discriminatorKey == null || discriminatorKey.isEmpty()) {
            // discriminator key not declared in XML (e.g. a discriminator with a built-in
            // default key); can't reason about the file pattern, so stay silent.
            return;
        }

        Model nestedAppenderModel = firstSubModelOfClass(siftModel, AppenderModel.class);
        if (nestedAppenderModel == null) {
            return;
        }

        List<String> fileValues = new ArrayList<>();
        collectBodyText(nestedAppenderModel, "file", fileValues);
        collectBodyText(nestedAppenderModel, "fileNamePattern", fileValues);

        if (fileValues.isEmpty()) {
            // nested appender writes to no file (e.g. ConsoleAppender); nothing to collide.
            return;
        }

        String keyReference = "${" + discriminatorKey;
        boolean referencesKey = fileValues.stream().anyMatch(v -> v.contains(keyReference));
        if (!referencesKey) {
            String resolvedTarget = mic.subst(fileValues.get(0));
            addWarn(String.format(SIFT_COLLISION_MESSAGE_0, appenderName, discriminatorKey));
            addWarn(String.format(SIFT_COLLISION_MESSAGE_1, resolvedTarget));
            addWarn(String.format(SIFT_COLLISION_MESSAGE_2, discriminatorKey));
        }
    }

    private Model firstSubModelOfClass(Model parent, Class<? extends Model> modelClass) {
        return parent.getSubModels().stream().filter(modelClass::isInstance).findFirst().orElse(null);
    }

    private String findDiscriminatorKey(AppenderModel appenderModel) {
        Optional<Model> discriminator = appenderModel.getSubModels().stream()
                .filter(m -> "discriminator".equalsIgnoreCase(m.getTag())).findFirst();

        if (discriminator.isEmpty()) {
            return null;
        }

        for (Model child : discriminator.get().getSubModels()) {
            if ("key".equalsIgnoreCase(child.getTag())) {
                String bodyText = child.getBodyText();
                if (bodyText != null) {
                    return bodyText.trim();
                }
            }
        }
        return null;
    }

    /**
     * Collect the raw body text of every {@code <tagName>} element nested (one or two levels
     * deep) inside the given appender model. Raw text is used on purpose: the discriminator
     * key is only bound as a substitution property at runtime, so it must be matched textually.
     */
    private void collectBodyText(Model appenderModel, String tagName, List<String> out) {
        Stream<Model> level1 = appenderModel.getSubModels().stream();
        Stream<Model> level2 = appenderModel.getSubModels().stream().flatMap(child -> child.getSubModels().stream());
        Stream.concat(level1, level2).filter(m -> tagPredicate(m, tagName)).map(Model::getBodyText)
                .filter(b -> b != null).forEach(out::add);
    }
}
