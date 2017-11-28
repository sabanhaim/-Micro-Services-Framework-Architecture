package bgu.spl.app.services;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Phaser;

import bgu.spl.app.Receipt;
import bgu.spl.app.messages.ManufacturingOrderRequest;
import bgu.spl.app.messages.TickBroadcast;

/**
 * Represents our store's shoe factory. Handles ManufacturingOrderRequests
 */
public class ShoeFactoryService extends TickListenerService {
	private Queue<ManufacturingOrderRequest> awaitingOrders;
	
	/** 
	 * Contains the amount of ticks left to finish the current order, or -1 if 
	 * no order is being worked on. 
	 */
	private int ticksLeftForCurrentOrder;

	public ShoeFactoryService(String name, Phaser initializedPhaser) {
		super(name, initializedPhaser);
		awaitingOrders = new LinkedList<>();
		ticksLeftForCurrentOrder = -1;
	}
	
	@Override
	public void initialize() {
		super.initialize();
		subscribeBroadcast(TickBroadcast.class, (b) -> handleTickBroadcast(b));
		subscribeRequest(ManufacturingOrderRequest.class, (req) -> handleManufacturingOrderRequest(req));
	}
	
	@Override
	protected void handleTickBroadcast(TickBroadcast b) {
		super.handleTickBroadcast(b);
		synchronized (awaitingOrders) {
			if (!awaitingOrders.isEmpty()) {
				if (ticksLeftForCurrentOrder == 0) {
					ManufacturingOrderRequest finishedOrder = awaitingOrders.poll();
					log ("Finished ManufacturingOrderRequest: " + finishedOrder);
					
					Receipt receipt = new Receipt(getName(), "store", finishedOrder.getShoeType(),
							false, getCurrentTick(), finishedOrder.getTick(), finishedOrder.getAmount());
					
					complete(finishedOrder, receipt);
					
					moveToNextOrder();
				}
				ticksLeftForCurrentOrder--;
			}
		}
	}
	
	private void handleManufacturingOrderRequest(ManufacturingOrderRequest req) {
		updateCurrentTick(req.getTick());
		log("Added ManufacturingOrdeRequest to list: " + req);
		synchronized (awaitingOrders) {
			awaitingOrders.add(req);
			
			if (awaitingOrders.size() == 1) {
				moveToNextOrder();
			}
		}
	}
	
	private void moveToNextOrder() {
		if (awaitingOrders.isEmpty()) {
			ticksLeftForCurrentOrder = 0;
		} else {
			ManufacturingOrderRequest req = awaitingOrders.peek();
			log("Starting to work on: " + req);
			ticksLeftForCurrentOrder = req.getAmount();
		}
	}
}
