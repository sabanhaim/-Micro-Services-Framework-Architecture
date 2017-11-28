package bgu.spl.app;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ShoeStorageInfo extends LoggableObject {
	private final String shoeType;
	private int amountOnStorage;
	private int discountedAmount;
	
	/**
	 * Constructor - initializes discountedAmount to 0.
	 */
	public ShoeStorageInfo(String shoeType, int amountOnStorage) {
		this(shoeType, amountOnStorage, 0);
	}
	
	/**
	 * Constructor
	 */
	public ShoeStorageInfo(String shoeType, int amountOnStorage, int discountedAmount) {
		this.shoeType = shoeType;
		this.amountOnStorage = amountOnStorage;
		this.discountedAmount = discountedAmount;
	}
	 
	public String getShoeType() {
		return shoeType;
	}
	
	public int getAmountOnStorage() {
		return amountOnStorage;
	}
	
	public int getDiscountedAmount() {
		return discountedAmount;
	}
	
	public boolean isOnDiscount() {
		return discountedAmount > 0;
	}
	
	public boolean isOut() {
		return amountOnStorage == 0;
	}
	
	/**
	 * Decrements the amount of the shoe. Also decrements the discountedAmount if the shoe is on discount.
	 */
	public void decrementAmount() {
		if (this.isOnDiscount()) {
			discountedAmount--;
		}
		amountOnStorage--;
	}
	
	/**
	 * Adds the given amount to the shoe
	 * @param amount to add
	 */
	public void addAmount(int amount) {
		amountOnStorage += amount;
	}
	
	/** 
	 * Adds a discount to a given amount of shoes
	 * @param amount of shoes to add discount to
	 */
	public void addDiscount(int amount) {
		discountedAmount = Math.min(discountedAmount + amount, amountOnStorage);
	}
	
	
	/**
	 * Implementation of our own Gson deserializer, since our members have a slightly different
	 * name than the ones on the json, and giving them different names than those specified on the
	 * instructions is probably not allowed.
	 */
	public static class ShoeStorageInfoDeserializer implements JsonDeserializer<ShoeStorageInfo> {
		@Override
		public ShoeStorageInfo deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2)
				throws JsonParseException {
			JsonObject jsonObj = arg0.getAsJsonObject();
			return new ShoeStorageInfo(jsonObj.get("shoeType").getAsString(), 
									   jsonObj.get("amount").getAsInt());
		}
	}
}
