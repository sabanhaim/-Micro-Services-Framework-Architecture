package bgu.spl.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import bgu.spl.app.messages.ManufacturingOrderRequest;
import bgu.spl.app.messages.RestockRequest;

/** 
 * A container class for ManagementService, that contains the following info for each shoe type:
 * 1. The awaiting RestockRequests, and the amount of reserved shoes for each request
 * 2. The corresponding ManufacturingOrderRequests for each order 
 * @note This class doesn't take care of synchronization. That is the caller's responsibility
 */
public class StockOrderList extends LoggableObject {
	private class StockOrderInfo {
		/** The actual manufacture request. This is the order itself */
		public ManufacturingOrderRequest manufactureRequest;
		
		/** Contains the RestockRequests that are waiting for this order */
		public List<RestockRequest> restockRequests;
		
		public StockOrderInfo(RestockRequest restockRequest, 
				ManufacturingOrderRequest manufactureRequest) {
			this.manufactureRequest = manufactureRequest;
			this.restockRequests = new LinkedList<>();
			this.restockRequests.add(restockRequest);
		}
	}
	
	/** Maps between a shoe type and its awaiting orders */
	private Map<String, List<StockOrderInfo>> stockOrders;
	
	/** 
	 * Constructor. Initializes an empty order list
	 */
	public StockOrderList() {
		stockOrders = new HashMap<>();
	}
	
	/** 
	 * Tries to reserve a shoe from an existing order, if one exists
	 * @return True if succeeded, or false if a new order has to be made 
	 */
	public boolean tryToReserveFromExistingOrders(RestockRequest req) {
		List<StockOrderInfo> orders = stockOrders.get(req.getShoeType());
		if (orders != null) {
			for (StockOrderInfo order : orders) {
				if (order.restockRequests.size() < order.manufactureRequest.getAmount()) {
					order.restockRequests.add(req);
					return true;
				}
			}
		}
		
		return false;
	}
	 
    /** Adds a new order to the list */
	public void addOrder(RestockRequest req, ManufacturingOrderRequest order) {
		List<StockOrderInfo> orders = stockOrders.get(order.getShoeType());
		if (orders == null) {
			orders = new LinkedList<>();
			stockOrders.put(order.getShoeType(), orders);
		}
		orders.add(new StockOrderInfo(req, order));
	}
	
	/** 
	 * Given a ManufacturingOrderRequest, this function finds the order, removes it from
	 * the list, and returns the list of awaiting RestockRequests for that order
	 */
	public List<RestockRequest> removeOrder(ManufacturingOrderRequest manufactureRequest) throws IllegalStateException {
		List<StockOrderInfo> orders = stockOrders.get(manufactureRequest.getShoeType());
		for (StockOrderInfo order : orders) {
			if (order.manufactureRequest == manufactureRequest) {
				orders.remove(order);
				return order.restockRequests;
			}
		}
		throw new IllegalStateException();
	}
}
