package bgu.spl.app.messages;

import bgu.spl.app.LoggableObject;
import bgu.spl.mics.Broadcast;

/**
 * Is sent by the ManagementService when a new discount is being applied
 */
public class NewDiscountBroadcast extends LoggableObject implements Broadcast {
	private final String shoeType;
	private final int discountAmount;
	private final int tick;
	
	public NewDiscountBroadcast(String shoeType, int discountAmount, int tick) {
		super();
		this.shoeType = shoeType;
		this.discountAmount = discountAmount;
		this.tick = tick;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getDiscountAmount() {
		return discountAmount;
	}

	public int getTick() {
		return tick;
	}	
	
}
