package bgu.spl.app.services;

import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.mics.MicroService;

/** 
 * Helpful abstract class for MicroServices that wish to be notified of the current
 * tick count. 
 */
public abstract class TickListenerService extends MicroService {
	
	protected AtomicInteger currentTickCount;
	
	/** 
	 * This phaser is given by our caller and is meant for it to know when we have finished
	 * registering for the timer's messages.
	 */
	protected Phaser initializedPhaser;

	public TickListenerService(String name, Phaser initializedPhaser) {
		super(name);
		currentTickCount = new AtomicInteger(1);
		this.initializedPhaser = initializedPhaser;
		initializedPhaser.register();
	}

	/** 
	 * If you need more logic on your initialize, override this but remember to call this with
	 * "super.initialize()"
	 */
	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, (b) -> handleTickBroadcast(b));
		subscribeBroadcast(TerminateBroadcast.class, (b) -> terminate());
		initializedPhaser.arriveAndDeregister();
	}
	
	/** 
	 * The handler for TickBroadcasts. If you wish for more logic on your inherited class,
	 * you can override this (and subscribe your callback in your initialize()). But
	 * be sure to call this handler from your handler with "super.handleTickBroadcast".
	 */
	protected void handleTickBroadcast(TickBroadcast b) {
		currentTickCount.set(b.getTick());
	}
	
	/**
	 * @return The current tick count
	 */
	protected int getCurrentTick() {
		return currentTickCount.get();
	}
	
	/** 
	 * Used to update the current tick count when we get messages other than TickBroadcast,
	 * to make sure our current tick is as updated as possible
	 */
	protected void updateCurrentTick(int newTick) {
		synchronized (currentTickCount) {
			currentTickCount.set(Math.max(newTick, getCurrentTick()));
		}
	}
}
