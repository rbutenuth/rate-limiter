package de.codecentric.limiter.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Manage the retry-after intervals for several resources, identified by arbitrary ID.  
 */
public class WaitTimeStorage {
	private Map<String, WaitEntry> id2timeMap = new HashMap<>();
	private SortedMap<Long, Map<String, WaitEntry>> waitTime2EntryMap = new TreeMap<>();
	
	public synchronized void storeWaitTime(String id, long millisSinceEpoch) {
		WaitEntry oldEntry = id2timeMap.get(id);
		if (oldEntry != null)
			waitTime2EntryMap.get(oldEntry.millisSinceEpoch).remove(id);
		WaitEntry entry = new WaitEntry(id, millisSinceEpoch);
		id2timeMap.put(id, entry);
		Map<String, WaitEntry> map = waitTime2EntryMap.get(millisSinceEpoch);
		if (map == null) {
			map = new HashMap<>();
			waitTime2EntryMap.put(millisSinceEpoch, map);
		}
		map.put(id, entry);

		// Remove entries with a wait time in the past (or now).
		// This will remove the just added entry, when the time is in the past, too.
		long now = System.currentTimeMillis();
		for (Iterator<Entry<Long, Map<String, WaitEntry>>> iterator = waitTime2EntryMap.entrySet().iterator(); iterator.hasNext();) {
			Entry<Long, Map<String, WaitEntry>> e = iterator.next();
			if (e.getKey() <= now) {
				iterator.remove();
				for (String id2remove : e.getValue().keySet()) {
					id2timeMap.remove(id2remove);
				}
			} else
				break;
		}
	}
	
	public synchronized void removeWaitTime(String id) {
		WaitEntry oldEntry = id2timeMap.remove(id);
		if (oldEntry != null) {
			Map<String, WaitEntry> entryMapForTime = waitTime2EntryMap.get(oldEntry.millisSinceEpoch);
			entryMapForTime.remove(oldEntry.id);
			if (entryMapForTime.isEmpty()) {
				waitTime2EntryMap.remove(oldEntry.millisSinceEpoch);
			}
		}
	}
	
	public synchronized Optional<Long> retrieveWaitTime(String id) {
		WaitEntry waitEntry = id2timeMap.get(id);
		if (waitEntry != null) {
			long waitUntil = waitEntry.millisSinceEpoch;
			if (waitUntil < System.currentTimeMillis()) {
				// in the past, so no longer interesting
				removeWaitTime(id);
				return Optional.empty();
			}
			return Optional.of(waitUntil);
		} else {
			return Optional.empty();
		}
	}

	private static class WaitEntry {
		final String id;
		final long millisSinceEpoch;
		
		public WaitEntry(String id, long millisSinceEpoch) {
			this.id = id;
			this.millisSinceEpoch = millisSinceEpoch;
		}
	}
}
