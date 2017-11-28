package bgu.spl.app.services;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Phaser;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import bgu.spl.app.DiscountSchedule;
import bgu.spl.app.Receipt;
import bgu.spl.app.ScheduleList;
import bgu.spl.app.StockOrderList;
import bgu.spl.app.Store;
import bgu.spl.app.messages.ManufacturingOrderRequest;
import bgu.spl.app.messages.NewDiscountBroadcast;
import bgu.spl.app.messages.RestockRequest;
import bgu.spl.app.messages.TickBroadcast;

/**
 * Represents the ManagementService, as explained in the instructions
 */
public class ManagementService extends TickListenerService {
	private static final String SERVICE_NAME = "manager";
	
	/** Contains the awaiting stock orders */
	private StockOrderList awaitingOrders;
	
	/** Maps between a tick, and the list of DiscountSchedules to execute at that tick */
	private ScheduleList<DiscountSchedule> discountSchedules;

	/** 
	 * The constructor
	 * @param initializedPhaser A phaser that should be arrived at and deregistered when we've finished initializing.
	 * @param discountSchedules The list of discount schedules
	 */
	public ManagementService(Phaser initializedPhaser, List<DiscountSchedule> discountSchedules) {
		super(SERVICE_NAME, initializedPhaser);
		
		// We want to register ourselves as well, since we're going to subscribe to the Timer
		// messages by ourselves.
		this.initializedPhaser.register();
		
		this.awaitingOrders = new StockOrderList();
		this.discountSchedules = new ScheduleList<>(discountSchedules);
	}

	@Override
	protected void initialize() {
		super.initialize();
		subscribeBroadcast(TickBroadcast.class, (b) -> handleTickBroadcast(b));
		subscribeRequest(RestockRequest.class, (req) -> handleRestockRequest(req));
		initializedPhaser.arriveAndDeregister();
	}
	
	@Override
	protected void handleTickBroadcast(TickBroadcast b) {
		super.handleTickBroadcast(b);
		Set<DiscountSchedule> schedules = discountSchedules.getSchedulesForTick(b.getTick());
		for (DiscountSchedule schedule : schedules) {
			log("DiscountSchedule occurred: " + schedule);
			Store.getInstance().addDiscount(schedule.getShoeType(), schedule.getAmount());
			NewDiscountBroadcast discountMsg = new NewDiscountBroadcast(
					schedule.getShoeType(), 
					schedule.getAmount(),
					getCurrentTick());
			log("Sending NewDiscountBroadcast: " + discountMsg);
			sendBroadcast(discountMsg);
		}
	}
	
	private void handleRestockRequest(RestockRequest req) {
		updateCurrentTick(req.getTick());
		synchronized (awaitingOrders) {
			log("Handling RestockRequest: " + req);
			if (awaitingOrders.tryToReserveFromExistingOrders(req)) {
				log("Successfully reserved from an existing order");
			} else {
				// No awaiting stock order with free shoes was found. Make a new order
				ManufacturingOrderRequest order = new ManufacturingOrderRequest(
						req.getShoeType(), getCurrentTick() % 5 + 1, getCurrentTick());
				
				log("Failed to reserve from an existing order. Issuing a ManufacturingOrderRequest: " + order);
				if (sendRequest(order, (receipt) -> handleManufacturingOrderCompleted(order, receipt))) {
					awaitingOrders.addOrder(req, order);
				} else {
					log("No handler found for ManufacturingOrderRequest: " + order);
					complete(req, false);
				}
			}
		}
	}
	
	private void handleManufacturingOrderCompleted(ManufacturingOrderRequest req,
											       Receipt receipt) {
		log("Manufacturing request completed. Request: " + req + ". Receipt: " + receipt);
		Store.getInstance().file(receipt);
		
		// Synchronize awaitingOrders until the actual shoes were added to the store. Or else,
		// a race could occur.
		List<RestockRequest> requests;
		synchronized (awaitingOrders) {
			requests = awaitingOrders.removeOrder(req);
		
			int shoesToAdd = req.getAmount() - requests.size();
			if (shoesToAdd > 0) {
				Store.getInstance().add(req.getShoeType(), shoesToAdd);
			}
		}
		
		for (RestockRequest r : requests) {
			log("Completing RestockRequest: " + r);
			complete(r, true);
		}
	}
	
	/**
	 * Implementation of our own Gson deserializer, since our member discountSchedules is not 
	 * a simple list/array.
	 */
	public static class ManagementServiceDeserializer implements JsonDeserializer<ManagementService> {
		Phaser initializedPhaser;
		
		public ManagementServiceDeserializer(Phaser initializedPhaser) {
			this.initializedPhaser = initializedPhaser;
		}
		
		@Override
		public ManagementService deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2)
				throws JsonParseException {
			Gson gson = new Gson();
			Type discountScheduleListType = new TypeToken<List<DiscountSchedule>>() {}.getType();
			List<DiscountSchedule> schedules = gson.fromJson(arg0.getAsJsonObject().get("discountSchedule"), 
					discountScheduleListType);
			return new ManagementService(this.initializedPhaser, schedules);
		}
	}
}
