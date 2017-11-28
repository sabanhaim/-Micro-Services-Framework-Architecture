package bgu.spl.app;

public class PurchaseSchedule extends Schedule {
	private final String shoeType;
	
	PurchaseSchedule(String shoeType, int tick) {
		super(tick);
		this.shoeType = shoeType;
	}

	public String getShoeType() {
		return shoeType;
	}
}
