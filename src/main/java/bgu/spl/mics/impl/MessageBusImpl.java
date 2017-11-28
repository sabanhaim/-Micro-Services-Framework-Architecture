package bgu.spl.mics.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.app.RoundRobinList;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

/**
 * The MessageBus. Used to send messages between services
 * For all overridden method explanations, see MessageBus
 */
@SuppressWarnings({ "rawtypes"})
public class MessageBusImpl implements MessageBus {
	
	/** The singleton holder for our class */ 
	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}
	
	/** 
	 * Contains the listening MicroServices for every Request. For each request, the
	 * listener is chosen in a round-robin fashion as explained in the instructions.
	 */
	private Map<Class<? extends Request>, RoundRobinList<MicroService>> requestListeners;
	
	/** Contains the Message queues for all of the MicroServices */ 
	private Map<MicroService, BlockingQueue<Message>> messageQueues;
	
	/** Contains the listening Microservices for every Broadcast */
	private Map<Class<? extends Broadcast>, Set<MicroService>> broadcastListeners;
	
	/** 
	 * Maps between the requests that are awaiting completion, with their
	 * requester MicroService. 
	 */
	private Map<Request<?>, MicroService> awaitingRequests;
	
	private MessageBusImpl() {
		this.messageQueues = new HashMap<>();
		this.requestListeners = new HashMap<>();
		this.broadcastListeners = new HashMap<>();
		this.awaitingRequests = new HashMap<>();
	}
	
	public static MessageBusImpl getInstance() { 
		return SingletonHolder.instance; 
	}

	@Override
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		synchronized (requestListeners) {
			RoundRobinList<MicroService> listeners = requestListeners.get(type);
			if (listeners == null) {
				listeners = new RoundRobinList<>();
				requestListeners.put(type, listeners);
			}
			listeners.add(m);
			log(m.getName() + " subscribed for " + type.getName());
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (broadcastListeners) {
			Set<MicroService> listeners = broadcastListeners.get(type);
			if (listeners == null) {
				listeners = new HashSet<>();
				broadcastListeners.put(type, listeners);
			}
			listeners.add(m);
			log(m.getName() + " subscribed for " + type);
		}
	}

	@Override
	public <T> void complete(Request<T> r, T result) {
		MicroService requester = null;
		synchronized (awaitingRequests) {
			requester = awaitingRequests.remove(r);
			sendMessage(requester, new RequestCompleted<T>(r, result));
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (broadcastListeners) {
			Set<MicroService> listeners = broadcastListeners.get(b.getClass());
			if (listeners != null) {
				for (MicroService m : listeners) {
					sendMessage(m, b);
				}
			}
		}
	}

	@Override
	public boolean sendRequest(Request<?> r, MicroService requester) {
		MicroService handler = null;
		synchronized (requestListeners) {
			RoundRobinList<MicroService> listeners = requestListeners.get(r.getClass());
			if (listeners == null) {
				return false;
			} else {
				handler = listeners.getNext();
			} 
		}
		
		synchronized (awaitingRequests) {
			awaitingRequests.put(r, requester);
		}
		sendMessage(handler, r);
		return true;
	}

	@Override
	public void register(MicroService m) {
		synchronized(messageQueues) {
			messageQueues.put(m, new LinkedBlockingQueue<Message>());
			log(m.getName() + " registered");
		}
	}

	@Override
	public void unregister(MicroService m) {
		log(m.getName() + " unregistering...");
		
		synchronized(messageQueues) {
			messageQueues.remove(m);
		}
		
		synchronized (requestListeners) {
			Iterator<RoundRobinList<MicroService>> iterator = requestListeners.values().iterator();
			while (iterator.hasNext()) {
				RoundRobinList<MicroService> list = iterator.next();
				list.remove(m);
				if (list.isEmpty()) {
					iterator.remove();
				}
			}
		}
		
		synchronized (broadcastListeners) {
			Iterator<Set<MicroService>> iterator = broadcastListeners.values().iterator();
			while (iterator.hasNext()) {
				Set<MicroService> set = iterator.next();
				set.remove(m);
				if (set.isEmpty()) {
					iterator.remove();
				}
			}
		}
		
		log(m.getName() + " unregistered");
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// We don't synchronize here - the only way the queue can be removed
		// is by calling unregister, but that means the same service called 
		// unregister() and awaitMessage() at the same time, which is impossible.
		BlockingQueue<Message> messages = messageQueues.get(m);
		if (messages == null) {
			throw new IllegalStateException();
		}
		
		return messages.take();
	}
	
	/** 
	 * A helper function that appends the given message to the given MicroService
	 */
	private void sendMessage(MicroService service, Message m) {
		Queue<Message> q = messageQueues.get(service);
		
		// If the queue has just been unregistered - the message is lost
		if (q != null) {
			// Note that BlockingQueue is thread-safe
			q.add(m);
		}
	}
	
	private void log(final String msg) {
    	System.out.println("MessageBus: " + msg);
	}
}
