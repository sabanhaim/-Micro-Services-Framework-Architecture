package bgu.spl.app;

import com.google.gson.Gson;

/**
 * Implements the object's toString() with Gson, which basically prints the entire contents 
 * of the object. If you don't want some field to be logged, declare it as transient.
 * As many classes as possible should inherit from this class to ease logging.
 */
public abstract class LoggableObject {
	public String toString() {
		return new Gson().toJson(this);
	}
}
