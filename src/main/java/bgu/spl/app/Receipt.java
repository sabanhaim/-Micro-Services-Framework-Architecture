package bgu.spl.app;

public class Receipt extends LoggableObject {
	private final String seller;
	private final String customer;
	private final String shoeType;
	private final boolean discount;
	private final int issuedTick;
	private final int requestTick;
	private final int amountSold;
	
	public Receipt(String seller, String customer, String shoeType, boolean discount, 
			       int issuedTick, int requestTick, int amountSold) {
		this.seller = seller;
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount = discount;
		this.issuedTick = issuedTick;
		this.requestTick = requestTick;
		this.amountSold = amountSold;
	}
	
	
	
	public String getSeller() {
		return seller;
	}



	public String getCustomer() {
		return customer;
	}



	public String getShoeType() {
		return shoeType;
	}



	public boolean isDiscount() {
		return discount;
	}



	public int getIssuedTick() {
		return issuedTick;
	}



	public int getRequestTick() {
		return requestTick;
	}



	public int getAmountSold() {
		return amountSold;
	}
}
