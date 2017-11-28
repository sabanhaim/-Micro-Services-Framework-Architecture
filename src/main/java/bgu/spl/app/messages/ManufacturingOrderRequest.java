package bgu.spl.app.messages;

import bgu.spl.app.LoggableObject;
import bgu.spl.app.Receipt;
import bgu.spl.mics.Request;

/**
 * Represents a ManufacturingOrderRequest as explained in the instructions
 */
public class ManufacturingOrderRequest extends LoggableObject implements Request<Receipt> {
	private final String shoeType;
	private final int amount;
	private final int tick;
	
	public ManufacturingOrderRequest(String shoeType, int amount, int tick) {
		super();
		this.shoeType = shoeType;
		this.amount = amount;
		this.tick = tick;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getAmount() {
		return amount;
	}
	
	public int getTick() {
		return tick;
	}
}
