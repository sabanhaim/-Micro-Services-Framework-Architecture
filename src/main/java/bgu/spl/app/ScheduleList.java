package bgu.spl.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains a list of scheduled actions, each of which has to be done at a certain tick count
 */
public class ScheduleList<T extends Schedule> {
	/** Maps between a tick count and its list of actions */ 
	private Map<Integer, Set<T>> schedules;
	
	/**
	 * Initializes the inner schedules map according to the given schedule list
	 */
	public ScheduleList(List<T> scheduleList) {
		this.schedules = new HashMap<>();
		
		for (T schedule : scheduleList) {
			Set<T> schedulesForTick = schedules.get(schedule.getTick());
			if (schedulesForTick == null) {
				schedulesForTick = new HashSet<>();
				schedules.put(schedule.getTick(), schedulesForTick);
			}
			schedulesForTick.add(schedule);
		}
	}
	
	/** 
	 * Returns a set of schedules for the given tick
	 * If no schedules are set for that tick, an empty set is returned.
	 */
	public Set<T> getSchedulesForTick(int tick) {
		Set<T> scheduleList = schedules.get(tick);
		if (scheduleList == null) {
			return Collections.emptySet();
		} else {
			return scheduleList;
		}
	}
}
