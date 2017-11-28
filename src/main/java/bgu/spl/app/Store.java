package bgu.spl.app;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A singleton that represents the shoe store 
 */
public class Store extends LoggableObject {
	/** A map between shoe type, and its storage info */
	private Map<String, ShoeStorageInfo> shoes;
	
	/** 
	 * A list of the receipts, sorted by the issued tick count. Note that
	 * this is not really a "set" - there is no protection against multiple insertions
	 * of the same receipt. For now this is enough as our program never files the same
	 * receipt more than once 
	 */
	private SortedSet<Receipt> receipts;
	
	/** The singleton holder of our class */
	private static class SingletonHolder {
		private static Store instance = new Store();
	}
	
	/** Contains a result for a purchase attempt */
	public enum BuyResult {
		NOT_IN_STOCK,
		NOT_ON_DISCOUNT,
		REGULAR_PRICE,
		DISCOUNTED_PRICE
	}
	
	/**
	 * Constructor. Initializes an empty store
	 */
	public Store() {
		Comparator<Receipt> compareByIssuedTick = new Comparator<Receipt>() {
			@Override
			public int compare(Receipt r1, Receipt r2) {
				 if(r1.getIssuedTick() == r2.getIssuedTick())
					 // If the issued tick is equal - return a random one of them.
		             return 1;
		         return r1.getIssuedTick() < r2.getIssuedTick() ? -1 : 1;
			}
		};
		
		shoes = new HashMap<>();
		receipts = new TreeSet<>(compareByIssuedTick);
	}
	
	/**
	 * Returns the singleton instance of the store
	 */
	public static Store getInstance() {
		return SingletonHolder.instance;
	}
	
	/**
	 * Loads the given shoes to the store
	 * @param storage The storage to load
	 */
	public void load(ShoeStorageInfo[] storage) {
		for (ShoeStorageInfo s : storage) {
			add(s.getShoeType(), s.getAmountOnStorage());
		}
	}
	
	/**
	 * Attempts to take the given shoe from the store
	 * @param shoeType The type of shoe to take
	 * @param onlyDiscount Only take the shoe if it's on discount
	 * @return A BuyResult according to the state of the wanted shoe
	 */
	public BuyResult take(String shoeType, boolean onlyDiscount) {
		synchronized (shoes) {
			ShoeStorageInfo info = shoes.get(shoeType);
			if (info == null) {
				return BuyResult.NOT_IN_STOCK;
			} else {
				boolean isOnDiscount = info.isOnDiscount();
				if (!isOnDiscount && onlyDiscount) {
					return BuyResult.NOT_ON_DISCOUNT;
				}
				
				log("Removing shoe of type: " + shoeType);
				info.decrementAmount();
				if (info.isOut()) {
					shoes.remove(shoeType);
				}
				
				if (isOnDiscount) {
					return BuyResult.DISCOUNTED_PRICE;
				} else {
					return BuyResult.REGULAR_PRICE;
				}
			}
		}
	}
	
	/**
	 * Adds the given amount of shoes to the store
	 * @param shoeType The type of shoe to add
	 * @param amount The amount of shoes to add
	 */
	public void add(String shoeType, int amount) {
		if (amount <= 0) {
			return;
		}
		
		synchronized (shoes) {
			ShoeStorageInfo info = shoes.get(shoeType);
			log("Adding " + amount + " shoes to " + shoeType);
			if (info == null) {
				shoes.put(shoeType, new ShoeStorageInfo(shoeType, amount));
			} else {
				info.addAmount(amount);
			}
		}
	}
	
	/**
	 * Adds discount to the given shoe type
	 * @param shoeType The type of shoe
	 * @param amount The amount of shoes to discount
	 */
	public void addDiscount(String shoeType, int amount) {
		if (amount <= 0) {
			return;
		}
		
		synchronized (shoes) {
			ShoeStorageInfo info = shoes.get(shoeType);
			if (info == null) {
				log("Failed to add " + amount + " discount to " + shoeType + ": shoe doesn't exist");
			} else {
				log("Adding " + amount + " discount to " + shoeType);
				info.addDiscount(amount);
			}
		}
	}
	
	/**
	 * Files the given receipt in the store
	 * @param receipt The receipt to file in the store 
	 */
	public void file(Receipt receipt) {
		synchronized (receipts) {
			log("filing receipt: " + receipt);
			receipts.add(receipt);
		}
	}
	
	/**
	 * Prints the contents of the store (receipts and shoes)
	 */
	public void print() {
		synchronized (shoes) {
			String shoesInfo = "Storage: \n[\n";
			shoesInfo += shoes.values().stream().map(ShoeStorageInfo::toString).collect(Collectors.joining("\n"));
			shoesInfo += "\n]";
			System.out.println(shoesInfo);
		}
		
		synchronized (receipts) {
			String receiptsInfo = "Receipts (" + receipts.size() + "): \n[\n";
			receiptsInfo += receipts.stream().map(Receipt::toString).collect(Collectors.joining("\n"));
			receiptsInfo += "\n]";
			System.out.println(receiptsInfo);
			System.out.println("No. of receipts: " + receipts.size());
		}
	}
	
	private void log(String msg) {
		System.out.println("Store: " + msg);
	}
}
