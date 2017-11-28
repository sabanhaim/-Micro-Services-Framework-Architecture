package bgu.spl.mics.tests;

import static org.junit.Assert.*;

import java.util.concurrent.Phaser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.app.services.TickListenerService;
import bgu.spl.mics.impl.MessageBusImpl;

public class TickListenerServiceTest {
	class MyTickListener extends TickListenerService {
		private boolean handleTickBroadcastCalled;

		public MyTickListener(Phaser initializedPhaser) {
			super("test", initializedPhaser);
			
			// Register ourselves too since we're going to subscribe to TickBroadcast by ourselves
			// as well
			this.initializedPhaser.register();
			
			this.handleTickBroadcastCalled = false;
		}
		
		@Override
		protected void initialize() {
			super.initialize();
			subscribeBroadcast(TickBroadcast.class, (b) -> handleTickBroadcast(b));
			this.initializedPhaser.arriveAndDeregister();
		}
		
		public int getCurrentTickCount() {
			return this.currentTickCount.get();
		}
		
		public boolean isHandleTickBroadcastCalled() {
			return handleTickBroadcastCalled;
		}
		
		@Override
		protected void handleTickBroadcast(TickBroadcast b) {
			super.handleTickBroadcast(b);
			handleTickBroadcastCalled = true;
		}
	}
	
	Phaser tickListenerInitializedPhaser;
	MyTickListener tickListener;
	Thread tickListenerRunner;
	
	@Before
	public void setUp() {
		tickListenerInitializedPhaser = new Phaser();
		tickListenerInitializedPhaser.register();
		tickListener = new MyTickListener(tickListenerInitializedPhaser);
		tickListenerRunner = new Thread(tickListener);
		tickListenerRunner.start();
		
		// Wait for the tick listener to finish initializing.
		tickListenerInitializedPhaser.arriveAndAwaitAdvance();
	}
	
	@After
	public void tearDown() {
		MessageBusImpl.getInstance().sendBroadcast(new TerminateBroadcast());
		
		try {
			tickListenerRunner.join(100);
		} catch (InterruptedException ex) {
			fail("Interrupted unexpectedly!");
		}
	}

	@Test
	public void testCurrentTickCount() {
		final int TEST_TICK = 5;
		TickBroadcast b = new TickBroadcast(TEST_TICK);
		MessageBusImpl.getInstance().sendBroadcast(b);
		
		// Wait for the message to be received
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			fail("Interrupted unexpectedly!");
		}
		
		assertEquals(tickListener.getCurrentTickCount(), TEST_TICK);
	}
	
	@Test
	public void testNewTickBroadcastHandler() {
		TickBroadcast b = new TickBroadcast(1);
		MessageBusImpl.getInstance().sendBroadcast(b);
		
		// Wait for the message to be received
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			fail("Interrupted unexpectedly!");
		}
		
		assertTrue(tickListener.isHandleTickBroadcastCalled());
	}
}
