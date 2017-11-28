package bgu.spl.mics.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.app.ShoeStorageInfo;
import bgu.spl.app.ShoeStorageInfo.ShoeStorageInfoDeserializer;
import bgu.spl.app.Store;
import bgu.spl.app.services.ManagementService;
import bgu.spl.app.services.SellingService;
import bgu.spl.app.services.ShoeFactoryService;
import bgu.spl.app.services.TimeService;
import bgu.spl.app.services.WebsiteClientService;
import bgu.spl.app.services.WebsiteClientService.WebsiteClientServiceDeserializer;
import bgu.spl.app.services.ManagementService.ManagementServiceDeserializer;
import bgu.spl.mics.MicroService;

/**
 * Represents the "main" of our program. Executes the ShoeStore according to a given 
 * json input file
 *
 */
public class ShoeStoreRunner {
	/**
	 * A class that holds all of the fields necessary for correct parsing of the json input file.
	 */
	public static class ExecutionFileInfo {
		public static class ServicesInfo {
			public TimeService time;
			public ManagementService manager;
			public List<WebsiteClientService> customers;
			public int factories;
			public int sellers;
		}
		
		public ShoeStorageInfo[] initialStorage;
		public ServicesInfo services;
	}
	
	/** All of the services in the execution, except for the TimeService. */
	private List<MicroService> services;
	
	/** 
	 * The TimeService. It has to be saved separately so that we can make sure to start
	 * it only after the other services have finished initializing.
	 */
	private TimeService timeService;
	
	/** 
	 * We use this phaser to make sure all of the services have initialized before running
	 * the TimeService. 
	 * We've chosen a Phaser instead of a CountDownLatch because we don't want to count the
	 * number of services, and only then initialize them. We want to do everything in one go,
	 * and Phaser allows this - the counter can be incremented and decremented dynamically.
	 */
	private Phaser servicesInitializedPhaser;
	
	/** Initializes the store and the services according to the given info */
	public ShoeStoreRunner(ExecutionFileInfo info, Phaser servicesInitializedPhaser) {
		this.services = new LinkedList<MicroService>();
		this.timeService = info.services.time;
		this.servicesInitializedPhaser = servicesInitializedPhaser;
		
		services.add(info.services.manager);
		services.addAll(info.services.customers);
		
		for (int i = 1; i <= info.services.factories; i++) {
			services.add(new ShoeFactoryService("factory " + i, servicesInitializedPhaser));
		}
		
		for (int i = 1; i <= info.services.sellers; i++) {
			services.add(new SellingService("seller " + i, servicesInitializedPhaser));
		}
		
		Store.getInstance().load(info.initialStorage);
	}
	
	/**
	 * Runs the store. Note that we've used an infinite timeout for the execution, but the execution
	 * will terminate gracefully after the duration stated in the json input file.
	 */
	public void run() {
		servicesInitializedPhaser.register();
		
		ExecutorService executor = Executors.newFixedThreadPool(services.size() + 1);
		for (MicroService m : services) {
			// One of the services may be null if for example no manager was set.
			if (m != null) {
				executor.execute(m);
			}
		}
		
		servicesInitializedPhaser.arriveAndAwaitAdvance();
		
		executor.execute(timeService);
		
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			assert false;
		}
		executor.shutdownNow();
		
		System.out.println();
		Store.getInstance().print();
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("No input file found. Exiting..");
			return;
		}
		final String jsonPath = args[0];
		BufferedReader br = new BufferedReader(new FileReader(jsonPath));
		
		Phaser servicesInitializedPhaser = new Phaser();
		
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ManagementService.class, 
				new ManagementServiceDeserializer(servicesInitializedPhaser));
		builder.registerTypeAdapter(WebsiteClientService.class, 
				new WebsiteClientServiceDeserializer(servicesInitializedPhaser));
		builder.registerTypeAdapter(ShoeStorageInfo.class, new ShoeStorageInfoDeserializer());
		
		Gson gson = builder.create();
		ExecutionFileInfo info = gson.fromJson(br, ExecutionFileInfo.class);
		
		ShoeStoreRunner s = new ShoeStoreRunner(info, servicesInitializedPhaser);
		s.run();
	}
}
