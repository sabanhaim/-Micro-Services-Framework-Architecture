package bgu.spl.mics.tests;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.impl.MessageBusImpl;

@SuppressWarnings({ "rawtypes"})
public class MessageBusImplTest {
	
	private static final int MESSAGE_WAIT_TIMEOUT = 100;
	
	private MessageBusImpl messageBus;
	private MockMicroService mockService;
	private MockMicroService mockService2;
	private MockMicroService mockService3;
	
	private class MockRequest<T> implements Request {
	}
	
	private class MockBroadcast implements Broadcast {
	}
	
	private class MockMicroService extends MicroService {
		
		public MockMicroService(String name) {
			super(name);
		}

		@Override
		protected void initialize() {
		}
	}
	
	private void assertGotMessage(MicroService service, Message msg) throws Exception {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<Message> future = exec.submit(() -> messageBus.awaitMessage(service));
		assertEquals(future.get(MESSAGE_WAIT_TIMEOUT, TimeUnit.MILLISECONDS), msg);
	}
	
	private void assertGotMessageType(MicroService service, Class<? extends Message> msgType) throws Exception {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<Message> future = exec.submit(() -> messageBus.awaitMessage(service));
		assertEquals(future.get(MESSAGE_WAIT_TIMEOUT, TimeUnit.MILLISECONDS).getClass(), msgType);
	}

	@Before
	public void setUp() {
		messageBus = MessageBusImpl.getInstance(); 
		
		mockService = new MockMicroService("mock");
		mockService2 = new MockMicroService("mock2");
		mockService3 = new MockMicroService("mock3");
		
		messageBus.register(mockService);
		messageBus.register(mockService2);
		messageBus.register(mockService3);
	}
	
	@After
	public void tearDown() {
		messageBus.unregister(mockService);
		messageBus.unregister(mockService2);
		messageBus.unregister(mockService3);
	}
	
	@Test
	public void testSingletonInstance() {
		assertEquals(MessageBusImpl.getInstance(), MessageBusImpl.getInstance());
	}
	
	@Test public void testRegisterUnregister() {
		MicroService service = new MockMicroService("testRegisterMock");
		messageBus.register(service);
		
	}
	
    /** Tests subscribeBroadcast, and by extension, sendRequest */
	@Test
	public void testSubscribeRequesst() throws Exception {
		MockRequest<Integer> req = new MockRequest<>();
		messageBus.subscribeRequest(MockRequest.class, mockService);
		messageBus.sendRequest(req, mockService);
		assertGotMessage(mockService, req);
	}
	
    /** Tests subscribeBroadcast, and by extension, sendBroadcast */
	@Test
	public void testSubscribeBroadcast() throws Exception {		
		messageBus.subscribeBroadcast(MockBroadcast.class, mockService);
		messageBus.subscribeBroadcast(MockBroadcast.class, mockService2);
		
		MockBroadcast br = new MockBroadcast();
		messageBus.sendBroadcast(br);
		
		assertGotMessage(mockService, br);
		assertGotMessage(mockService2, br);
	}
	
	@Test
	public void testComplete() throws Exception {
 		MockRequest<Integer> req = new MockRequest<>();
		messageBus.subscribeRequest(req.getClass(), mockService2);
		messageBus.sendRequest(req, mockService);
		assertGotMessage(mockService2, req);
		
		messageBus.complete(req, new Integer(5));
		assertGotMessageType(mockService, RequestCompleted.class);
	}
	
	/** 
	 * Test the following scenario:
	 * A and B are subscribed for a request.
	 * Now a request is sent, and then after assigning it to A, C subscribed
	 * to the request. Now the order of receiving should be BCA and not BAC 
	 */
	@Test
	public void testRequestRoundRobinHandling() throws Exception {
		MockRequest<Integer> req = new MockRequest<>();
		MockRequest<Integer> req2 = new MockRequest<>();
		MockRequest<Integer> req3 = new MockRequest<>();
		MockRequest<Integer> req4 = new MockRequest<>();
		
		messageBus.subscribeRequest(MockRequest.class, mockService);
		messageBus.subscribeRequest(MockRequest.class, mockService2);
		
		messageBus.sendRequest(req, mockService);
		assertGotMessage(mockService, req);
		
		messageBus.subscribeRequest(MockRequest.class, mockService3);
		
		messageBus.sendRequest(req2, mockService);
		assertGotMessage(mockService2, req2);
		messageBus.sendRequest(req3, mockService);
		assertGotMessage(mockService3, req3);
		messageBus.sendRequest(req4, mockService);
		assertGotMessage(mockService, req4);
	}
	
	// Note: Adding tests for register, unregister and awaitMessage is not needed since our tests already 
	//       use them and thus test that they work correctly.
}
