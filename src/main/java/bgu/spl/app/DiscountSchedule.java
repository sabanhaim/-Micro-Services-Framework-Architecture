package bgu.spl.app;

public class DiscountSchedule extends Schedule {
	private final String shoeType;
	private final int amount;
	
	public DiscountSchedule(String shoeType, int amount, int tick) {
		super(tick);
		this.shoeType = shoeType;
		this.amount = amount;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getAmount() {
		return amount;
	}
}
