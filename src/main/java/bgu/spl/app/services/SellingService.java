package bgu.spl.app.services;

import java.util.concurrent.Phaser;

import bgu.spl.app.Receipt;
import bgu.spl.app.Store;
import bgu.spl.app.Store.BuyResult;
import bgu.spl.app.messages.PurchaseOrderRequest;
import bgu.spl.app.messages.RestockRequest;
import bgu.spl.mics.Callback;

/**
 * Represents a seller in the store. For more explanations see the instructions
 */
public class SellingService extends TickListenerService {
	public SellingService(String name, Phaser initializedPhaser) {
		super(name, initializedPhaser);
	}

	@Override
	protected void initialize() {
		super.initialize();
		this.subscribeRequest(PurchaseOrderRequest.class, (request) ->  handlePurchaseOrderRequest(request));
	}
	
	private void handlePurchaseOrderRequest(PurchaseOrderRequest request) {
		updateCurrentTick(request.getTickCount());
		log("Handling PurchaseOrderRequest: " + request);
		BuyResult result = Store.getInstance().take(request.getShoeType(), request.isDiscountOnly());
		
		switch (result) {
		case REGULAR_PRICE:
		case DISCOUNTED_PRICE:
			completePurchase(request, result);
			break;
		case NOT_IN_STOCK:
		case NOT_ON_DISCOUNT:
			if (request.isDiscountOnly()) {
				// It doesn't really matter whether there is no shoes on stock, or no shoes
				// on discount. In both cases - issuing a restock request won't help.
				log("Wanted discountOnly shoe is not on discount. Completing PurchaseOrderRequest with null");
				complete(request, null);
				return;
			}
			
			Callback<Boolean> handleRestockAnswer = new Callback<Boolean>() {
				@Override
				public void call(Boolean res) {
					if (res) {
						log("RestockRequest succeeded for " + request + ". Handling the PurchaseOrderRequest now");
						completePurchase(request, result);
					} else {
						log("RestockRequest failed for " + request + ". Completing PurchaseOrderRequest with null");
						complete(request, null);
					}
				}
			};
			log("No shoe of required type. Issuing RestockRequest for " + request);
			RestockRequest restockReq = new RestockRequest(request.getShoeType(), getCurrentTick());
			if (!sendRequest(restockReq, handleRestockAnswer)) {
				log("No handler found for RestockRequest: " + restockReq);
				complete(request, null);
			}
			break;
		}
	}
	
	private void completePurchase(PurchaseOrderRequest request, BuyResult result) {
		Receipt receipt = new Receipt(getName(), request.getCustomer(), request.getShoeType(),
				result == BuyResult.DISCOUNTED_PRICE, getCurrentTick(), request.getTickCount(), 1);
		Store.getInstance().file(receipt);
		complete(request, receipt);
	}
}
