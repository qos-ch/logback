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
package ch.qos.logback.core.spi;

import ch.qos.logback.core.CoreConstants;

import java.util.*;

/**
 * An abstract implementation of the ComponentTracker interface. Derived classes must implement
 * {@link #buildComponent(String)}, {@link #processPriorToRemoval(Object)}, and {@link #isComponentStale(Object)}
 * methods as appropriate for their component type.
 *
 * @param <C> component type
 *
 * @author Tommy Becker
 * @author Ceki Gulcu
 * @author David Roussel
 */
abstract public class AbstractComponentTracker<C> implements ComponentTracker<C> {
    private static final boolean ACCESS_ORDERED = true;

    // Components in lingering state last 10 seconds
    final public static long LINGERING_TIMEOUT = 10 * CoreConstants.MILLIS_IN_ONE_SECOND;

    /**
     * The minimum amount of time that has to elapse between successive removal iterations.
     */
    final public static long WAIT_BETWEEN_SUCCESSIVE_REMOVAL_ITERATIONS = CoreConstants.MILLIS_IN_ONE_SECOND;

    protected int maxComponents = DEFAULT_MAX_COMPONENTS;
    protected long timeout = DEFAULT_TIMEOUT;

    // an access ordered map. Least recently accessed element will be removed after a 'timeout'
    LinkedHashMap<String, Entry<C>> liveMap = new LinkedHashMap<String, Entry<C>>(32, .75f, ACCESS_ORDERED);

    // an access ordered map. Least recently accessed element will be removed after LINGERING_TIMEOUT
    LinkedHashMap<String, Entry<C>> lingerersMap = new LinkedHashMap<String, Entry<C>>(16, .75f, ACCESS_ORDERED);
    long lastCheck = 0;

    /**
     * Stop or clean the component.
     *
     * @param component
     */
    abstract protected void processPriorToRemoval(C component);

    /**
     * Build a component based on the key.
     *
     * @param key
     * @return
     */
    abstract protected C buildComponent(String key);

    /**
     * Components can declare themselves stale. Such components may be
     * removed before they time out.
     *
     * @param c
     * @return
     */
    protected abstract boolean isComponentStale(C c);

    public int getComponentCount() {
        return liveMap.size() + lingerersMap.size();
    }

    /**
     * Get an entry from the liveMap, if not found search the lingerersMap.
     *
     * @param key
     * @return
     */
    private Entry<C> getFromEitherMap(String key) {
        Entry<C> entry = liveMap.get(key);
        if (entry != null)
            return entry;
        else {
            return lingerersMap.get(key);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Note that this method is synchronized.</p>
     *
     * @param key {@inheritDoc}
     * @return {@inheritDoc}
     *
     */
    public synchronized C find(String key) {
        Entry<C> entry = getFromEitherMap(key);
        if (entry == null)
            return null;
        else
            return entry.component;
    }

    /**
     *  {@inheritDoc}
     *
     * <p>Note that this method is atomic, i.e. synchronized.</p>
     *
     * @param key {@inheritDoc}
     * @param timestamp {@inheritDoc}
     * @return {@inheritDoc}
     */
    public synchronized C getOrCreate(String key, long timestamp) {
        Entry<C> entry = getFromEitherMap(key);
        if (entry == null) {
            C c = buildComponent(key);
            entry = new Entry<C>(key, c, timestamp);
            // new entries go into the main map
            liveMap.put(key, entry);
        } else {
            entry.setTimestamp(timestamp);
        }
        return entry.component;
    }

    /**
     * Mark component identified by 'key' as having reached its end-of-life.
     *
     * @param key
     */
    public void endOfLife(String key) {
        Entry<C> entry = liveMap.remove(key);
        if (entry == null)
            return;
        lingerersMap.put(key, entry);
    }

    /**
     * Clear (and detach) components which are stale. Components which have not
     * been accessed for more than a user-specified duration are deemed stale.
     *
     * @param now
     */
    public synchronized void removeStaleComponents(long now) {
        if (isTooSoonForRemovalIteration(now))
            return;
        removeExcedentComponents();
        removeStaleComponentsFromMainMap(now);
        removeStaleComponentsFromLingerersMap(now);
    }

    private void removeExcedentComponents() {
        genericStaleComponentRemover(liveMap, 0, byExcedent);
    }

    private void removeStaleComponentsFromMainMap(long now) {
        genericStaleComponentRemover(liveMap, now, byTimeout);
    }

    private void removeStaleComponentsFromLingerersMap(long now) {
        genericStaleComponentRemover(lingerersMap, now, byLingering);
    }

    private void genericStaleComponentRemover(LinkedHashMap<String, Entry<C>> map, long now, RemovalPredicator<C> removalPredicator) {
        Iterator<Map.Entry<String, Entry<C>>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Entry<C>> mapEntry = iter.next();
            Entry<C> entry = mapEntry.getValue();
            if (removalPredicator.isSlatedForRemoval(entry, now)) {
                iter.remove();
                C c = entry.component;
                processPriorToRemoval(c);
            } else {
                break;
            }
        }
    }

    private RemovalPredicator<C> byExcedent = new RemovalPredicator<C>() {
        public boolean isSlatedForRemoval(Entry<C> entry, long timestamp) {
            return (liveMap.size() > maxComponents);
        }
    };

    private RemovalPredicator<C> byTimeout = new RemovalPredicator<C>() {
        public boolean isSlatedForRemoval(Entry<C> entry, long timestamp) {
            return isEntryStale(entry, timestamp);
        }
    };
    private RemovalPredicator<C> byLingering = new RemovalPredicator<C>() {
        public boolean isSlatedForRemoval(Entry<C> entry, long timestamp) {
            return isEntryDoneLingering(entry, timestamp);
        }
    };

    private boolean isTooSoonForRemovalIteration(long now) {
        if (lastCheck + WAIT_BETWEEN_SUCCESSIVE_REMOVAL_ITERATIONS > now) {
            return true;
        }
        lastCheck = now;
        return false;
    }

    private boolean isEntryStale(Entry<C> entry, long now) {
        // stopped or improperly started appenders are considered stale
        // see also http://jira.qos.ch/browse/LBCLASSIC-316
        C c = entry.component;
        if (isComponentStale(c))
            return true;

        return ((entry.timestamp + timeout) < now);
    }

    private boolean isEntryDoneLingering(Entry<C> entry, long now) {
        return ((entry.timestamp + LINGERING_TIMEOUT) < now);
    }

    public Set<String> allKeys() {
        HashSet<String> allKeys = new HashSet<String>(liveMap.keySet());
        allKeys.addAll(lingerersMap.keySet());
        return allKeys;
    }

    public Collection<C> allComponents() {
        List<C> allComponents = new ArrayList<C>();
        for (Entry<C> e : liveMap.values())
            allComponents.add(e.component);
        for (Entry<C> e : lingerersMap.values())
            allComponents.add(e.component);

        return allComponents;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getMaxComponents() {
        return maxComponents;
    }

    public void setMaxComponents(int maxComponents) {
        this.maxComponents = maxComponents;
    }

    // ================================================================
    private interface RemovalPredicator<C> {
        boolean isSlatedForRemoval(Entry<C> entry, long timestamp);
    }

    // ================================================================
    private static class Entry<C> {
        String key;
        C component;
        long timestamp;

        Entry(String k, C c, long timestamp) {
            this.key = k;
            this.component = c;
            this.timestamp = timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            @SuppressWarnings("unchecked")
            final Entry<C> other = (Entry<C>) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (component == null) {
                if (other.component != null)
                    return false;
            } else if (!component.equals(other.component))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "(" + key + ", " + component + ")";
        }
    }
}
