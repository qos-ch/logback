/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.issue.lbcore224;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reduce a file consisting of lock and unlock operations by removing matching lock/unlocks.
 */
public class Reduce {

    static int NA = -1;

    enum OperationType {
        LOCK, UNLOCK
    }

    public static void main(final String[] args) throws IOException {
        final File inputFile = new File(args[0]);
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("Missing file [" + args[0] + "]");
        }
        final List<String> lines = readFile(inputFile);
        System.out.println("Lines count=" + lines.size());
        final List<Structure> structuredLines = structure(lines);
        final List<Structure> reduction = reduce(structuredLines);
        if (reduction.isEmpty()) {
            System.out.println("Reduction is EMPTY as it should be.");
        } else {
            System.out.println("Non-empty reduction!!! WTF?");
            System.out.println(reduction);
        }

    }

    private static List<String> readFile(final File inputFile) throws IOException {
        BufferedReader reader = null;
        final List<String> lines = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                }
            }
        }
        return lines;
    }

    private static List<Structure> reduce(final List<Structure> structuredLines) {
        final List<Structure> matching = new ArrayList<>();
        int lockIndex = 0;
        while (lockIndex < structuredLines.size()) {
            lockIndex = findNearestLock(structuredLines, lockIndex);
            if (lockIndex == NA) {
                break;
            }
            final int unlockIndex = findNearestUnlockInSameThread(structuredLines, lockIndex);
            if (unlockIndex != NA) {
                matching.add(structuredLines.get(lockIndex));
                matching.add(structuredLines.get(unlockIndex));
            }
            lockIndex++;
        }
        System.out.println("matching list size: " + matching.size());
        final List<Structure> reduction = new ArrayList<>();
        for (final Structure s : structuredLines) {
            if (!matching.contains(s)) {
                reduction.add(s);
            }
        }
        return reduction;

    }

    private static int findNearestLock(final List<Structure> reduction, final int index) {
        for (int i = index; i < reduction.size(); i++) {
            final Structure s = reduction.get(i);
            if (s.operationType == OperationType.LOCK) {
                return i;
            }
        }
        return NA;
    }

    private static int findNearestUnlockInSameThread(final List<Structure> reduction, final int lockIndex) {
        final int firstCandidateIndex = lockIndex + 1;
        final Structure lockStructure = reduction.get(lockIndex);
        for (int i = firstCandidateIndex; i < reduction.size(); i++) {
            final Structure s = reduction.get(i);
            if (s.operationType == OperationType.UNLOCK && lockStructure.thread.equals(s.thread)) {
                return i;
            }
        }
        return NA;
    }

    static List<Structure> structure(final List<String> lines) {
        final List<Structure> structuredLines = new ArrayList<>();
        final Pattern p = Pattern.compile("(\\d{2,5})\\ +(.*) (LOCK|UNLOCK)");

        for (final String line : lines) {
            final Matcher m = p.matcher(line);
            if (m.matches()) {
                final String relTime = m.group(1);
                final String t = m.group(2);
                final String opStr = m.group(3);
                final Structure structure = buildStructure(relTime, t, opStr);
                structuredLines.add(structure);
            } else {
                System.out.println("NON MATCHING LINE: [" + line + "]");
            }

        }
        return structuredLines;
    }

    private static Structure buildStructure(final String relTime, final String t, final String opStr) {
        final long r = Long.parseLong(relTime);
        OperationType operationType;
        if (opStr.equals("LOCK")) {
            operationType = OperationType.LOCK;
        } else if (opStr.equals("UNLOCK")) {
            operationType = OperationType.UNLOCK;
        } else {
            throw new IllegalArgumentException(opStr + " is not LOCK|UNLOCK");
        }
        return new Structure(r, t, operationType);
    }

    static class Structure {
        long time;
        String thread;
        OperationType operationType;

        Structure(final long time, final String thread, final OperationType operationType) {
            this.time = time;
            this.thread = thread;
            this.operationType = operationType;
        }

        @Override
        public String toString() {
            return "Structure{" + "time=" + time + ", thread='" + thread + '\'' + ", operationType=" + operationType + '}';
        }
    }

}
