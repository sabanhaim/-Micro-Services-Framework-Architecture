package bgu.spl.app;

/**
 * Contains an abstract schedule - a tick count.
 */
public class Schedule extends LoggableObject {
	private final int tick;
	
	public Schedule(int tick) {
		this.tick = tick;
	}
	
	public int getTick() {
		return tick;
	}
}
