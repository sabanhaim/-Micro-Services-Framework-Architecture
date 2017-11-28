package bgu.spl.app.services;

import java.util.Timer;
import java.util.TimerTask;

import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.mics.MicroService;

public class TimeService extends MicroService {
	
	private static final String SERVICE_NAME = "timer";
	
	/** Contains the number of milliseconds between each tick */
	private int speed;
	
	/** The number of ticks until the service stops */
	private int duration;
	
	/** Contains the number of clock ticks that have passed */  
	private int tickCount;
	 
	/** The timer used for broadcasting */ 
	private transient Timer executionTimer;
	
	public TimeService() {
		this(0, 0);
	}

	public TimeService(int speed, int duration) {
		super(SERVICE_NAME);
		this.speed = speed;
		this.duration = duration;
		this.tickCount = 1;
		this.executionTimer = new Timer();
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, (b) -> terminate());
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (tickCount > duration) {
					executionTimer.cancel();
					sendBroadcast(new TerminateBroadcast());
				} else {
					log("tick " + tickCount);
					sendBroadcast(new TickBroadcast(tickCount));
				}
				
				tickCount++;
			}
		};
		
		executionTimer.scheduleAtFixedRate(task, 0, speed);
	}
}
