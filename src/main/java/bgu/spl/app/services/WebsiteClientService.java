package bgu.spl.app.services;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Phaser;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import bgu.spl.app.PurchaseSchedule;
import bgu.spl.app.ScheduleList;
import bgu.spl.app.messages.NewDiscountBroadcast;
import bgu.spl.app.messages.PurchaseOrderRequest;
import bgu.spl.app.messages.TickBroadcast;

/**
 * Represents a client of the store. See instructions
 */
public class WebsiteClientService extends TickListenerService {
	
	private ScheduleList<PurchaseSchedule> purchaseSchedules;
	private Set<String> wishList;

	public WebsiteClientService(String name, Phaser initializedPhaser, 
			List<PurchaseSchedule> purchaseSchedules, Set<String> wishList) {
		super(name, initializedPhaser);
		
		this.purchaseSchedules = new ScheduleList<>(purchaseSchedules);
		this.wishList = wishList;
	}

	@Override
	protected void initialize() {
		super.initialize();
		subscribeBroadcast(TickBroadcast.class, (b) -> handleTickBroadcast(b));
		subscribeBroadcast(NewDiscountBroadcast.class, (b) -> handleNewDiscountBroadcast(b));
	}
	
	@Override
	protected void handleTickBroadcast(TickBroadcast b) {
		super.handleTickBroadcast(b);
		Set<PurchaseSchedule> schedules = purchaseSchedules.getSchedulesForTick(b.getTick());
		for (PurchaseSchedule sched : schedules) {
			log("PurchaseSchedule occurred: " + sched);
			PurchaseOrderRequest order = new PurchaseOrderRequest(
					sched.getShoeType(), getName(), false, b.getTick());
			purchase(order);
		}
	}
	
	private void handleNewDiscountBroadcast(NewDiscountBroadcast b) {
		updateCurrentTick(b.getTick());
		if (wishList.contains(b.getShoeType())) {
			log("Wishlisted shoe got discount: " + b);
			PurchaseOrderRequest order = new PurchaseOrderRequest(
					b.getShoeType(), getName(), true, getCurrentTick());
			purchase(order);
		}
	}
	
	private void purchase(PurchaseOrderRequest order) {
		log("Sending purchase request: " + order);
		boolean isReqHandled = sendRequest(order, (receipt) -> {
			log("Purchase finished: " + receipt);
			if (receipt != null) {
				wishList.remove(order.getShoeType());
			}
		});
		if (!isReqHandled) {
			log("No handler found for PurchaseOrderRequest: " + order);
		}
	}
	

	/**
	 * Implementation of our own Gson deserializer, since our member purchaseSchedule is not 
	 * a simple list/array.
	 */
	public static class WebsiteClientServiceDeserializer implements JsonDeserializer<WebsiteClientService> {
		private Phaser initializedPhaser;
		
		public WebsiteClientServiceDeserializer(Phaser initializedPhaser) {
			this.initializedPhaser = initializedPhaser;
		}
		
		@Override
		public WebsiteClientService deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2)
				throws JsonParseException {
			Gson gson = new Gson();
			JsonObject jsonObj = arg0.getAsJsonObject();
			
			Type purchaseScheduleListType = new TypeToken<List<PurchaseSchedule>>() {}.getType();
			List<PurchaseSchedule> schedules = gson.fromJson(jsonObj.get("purchaseSchedule"), purchaseScheduleListType);
			
			Type wishListType = new TypeToken<Set<String>>() {}.getType();
			Set<String> wishList = gson.fromJson(jsonObj.get("wishList"), wishListType);
			
			return new WebsiteClientService(jsonObj.get("name").getAsString(), this.initializedPhaser, 
					schedules, wishList);
		}
	}
}
