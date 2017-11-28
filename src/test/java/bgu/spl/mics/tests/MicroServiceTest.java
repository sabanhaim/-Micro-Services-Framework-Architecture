package bgu.spl.mics.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.impl.MessageBusImpl;

/**
 * Some basic tests of the MicroService. 
 */
public class MicroServiceTest {
	
	class MockBroadcast implements Broadcast {
	}
	
	class MockRequest implements Request<Integer> {
	}
	
	class MyMicroService extends MicroService {
		public static final String SERVICE_NAME = "test"; 
		public static final int REQUEST_RETURN_VALUE = 3;
		
		public boolean isInitCalled;
		public boolean isBroadcastReceived;
		public boolean isRequestReceived;
		public int requestResult;
		
		public MyMicroService() {
			super(SERVICE_NAME);
			isInitCalled = false;
			isBroadcastReceived = false;
			isRequestReceived = false;
			requestResult = 0;
		}

		@Override
		protected void initialize() {
			isInitCalled = true;
			subscribeBroadcast(MockBroadcast.class, (b) -> isBroadcastReceived = true);
			subscribeRequest(MockRequest.class, (r) -> {
				isRequestReceived = true;
				complete(r, REQUEST_RETURN_VALUE);
			});
		}
		
		public void mySendBroadcast(MockBroadcast b) {
			sendBroadcast(b);
		}
		
		public void mySendRequest(MockRequest req) {
			sendRequest(req, (n) -> requestResult = n);
		}
	}
	
	private MyMicroService service;
	private Thread serviceRunner;
	
	@Before
	public void setUp() {
		service = new MyMicroService();
		serviceRunner = new Thread(service);
		serviceRunner.start();
		
		// Give the service some time to run and initialize
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			fail("Interrupted unexpectedly!");
		}
	}
	
	@After
	public void tearDown() {
		MessageBusImpl.getInstance().sendBroadcast(new TerminateBroadcast());
		
		try {
			serviceRunner.join(100);
		} catch (InterruptedException ex) {
			fail("Interrupted unexpectedly!");
		}
	}

	@Test
	public void testGetName() {
		assertEquals(service.getName(), MyMicroService.SERVICE_NAME);
	}
	
	@Test
	public void testInitializeCalled() {
		assertTrue(service.isInitCalled);
	}
	
	/** Tests both sendBroadcast and registering for a broadcast callback */
	@Test
	public void testBroadcast() {
		MockBroadcast b = new MockBroadcast();
		service.mySendBroadcast(b);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			fail("Interrupted unexpectedly!");
		}
		
		assertTrue(service.isBroadcastReceived);
	}
	
	// For now we haven't tested requests, but it isn't necessary according to the instructions
}
