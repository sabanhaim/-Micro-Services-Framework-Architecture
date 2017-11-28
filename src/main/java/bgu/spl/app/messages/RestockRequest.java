package bgu.spl.app.messages;

import bgu.spl.app.LoggableObject;
import bgu.spl.mics.Request;

/**
 * Represents a RestockRequest as explained in the instructions
 */
public class RestockRequest extends LoggableObject implements Request<Boolean> {
	/** The type of the shoe to order */
	private final String shoeType;
	private final int tick;

	public RestockRequest(String shoeType, int tick) {
		super();
		this.shoeType = shoeType;
		this.tick = tick;
	}

	public String getShoeType() {
		return shoeType;
	}
	
	public int getTick() {
		return this.tick;
	}
}
