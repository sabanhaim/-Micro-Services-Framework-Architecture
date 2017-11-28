package bgu.spl.mics.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import bgu.spl.app.Receipt;
import bgu.spl.app.ShoeStorageInfo;
import bgu.spl.app.Store;
import bgu.spl.app.Store.BuyResult;

public class StoreTest {
	@Test
	public void testSingletonInstance() {
		assertEquals(Store.getInstance(), Store.getInstance());
	} 
	
	@Test
	public void loadTest() {
		ShoeStorageInfo[] info = {new ShoeStorageInfo("A", 1), new ShoeStorageInfo("B", 2)};
		Store.getInstance().load(info);
		
		assertEquals(Store.getInstance().take(info[0].getShoeType(), false), BuyResult.REGULAR_PRICE);
		assertEquals(Store.getInstance().take(info[1].getShoeType(), false), BuyResult.REGULAR_PRICE);
		assertEquals(Store.getInstance().take(info[1].getShoeType(), false), BuyResult.REGULAR_PRICE);
		assertEquals(Store.getInstance().take(info[1].getShoeType(), false), BuyResult.NOT_IN_STOCK);
	}
	
	@Test
	 /** 
	  * Tests take() (and by extension, also addDiscount() and add())
	  */
	public void takeTest() {
		Store.getInstance().add("A", 4);
		assertEquals(Store.getInstance().take("A", true), BuyResult.NOT_ON_DISCOUNT);
		assertEquals(Store.getInstance().take("A", false), BuyResult.REGULAR_PRICE);
		
		Store.getInstance().addDiscount("A", 2);
		assertEquals(Store.getInstance().take("A", false), BuyResult.DISCOUNTED_PRICE);
		assertEquals(Store.getInstance().take("A", true), BuyResult.DISCOUNTED_PRICE);
		assertEquals(Store.getInstance().take("A", false), BuyResult.REGULAR_PRICE);
		
		assertEquals(Store.getInstance().take("A", false), BuyResult.NOT_IN_STOCK);
		assertEquals(Store.getInstance().take("A", true), BuyResult.NOT_IN_STOCK);
	}
	
	@Test
	public void fileReceiptTest() {
		Receipt r = new Receipt("A", "B", "C", true, 3, 4, 5);
		Store.getInstance().file(r);
		assertTrue(Store.getInstance().toString().contains(r.toString()));
	}

}
