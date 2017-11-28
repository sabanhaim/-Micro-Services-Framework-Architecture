package bgu.spl.app.messages;

import bgu.spl.app.LoggableObject;
import bgu.spl.mics.Broadcast;

/**
 * Is sent on every tick by the TimeService
 */
public class TickBroadcast extends LoggableObject implements Broadcast {
	private final int tick;

	public TickBroadcast(int tick) {
		super();
		this.tick = tick;
	}

	public int getTick() {
		return tick;
	}
	
}
