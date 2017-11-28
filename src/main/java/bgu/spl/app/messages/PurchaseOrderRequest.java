package bgu.spl.app.messages;

import bgu.spl.app.LoggableObject;
import bgu.spl.app.Receipt;
import bgu.spl.mics.Request;

/**
 * Is sent by a WebsiteClientService when he wishes to buy a shoe
 */
public class PurchaseOrderRequest extends LoggableObject implements Request<Receipt> {
	private final String shoeType;
	private final String customer;
	private final boolean discountOnly;
	private final int tickCount;
	
	public PurchaseOrderRequest(String shoeType, String customer, boolean discountOnly, int tickCount) {
		super();
		this.shoeType = shoeType;
		this.customer = customer;
		this.discountOnly = discountOnly;
		this.tickCount = tickCount;
	}
	
	public String getShoeType() {
		return shoeType;
	}

	public String getCustomer() {
		return customer;
	}

	public boolean isDiscountOnly() {
		return discountOnly;
	}
	
	public int getTickCount() {
		return tickCount;
	}
}
