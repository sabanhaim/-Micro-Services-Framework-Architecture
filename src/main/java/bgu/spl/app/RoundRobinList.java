package bgu.spl.app;

import java.util.List;
import java.util.LinkedList;

/** 
 * This class holds a linked list with the given items, and returns them in a round-robin fashion:
 * Suppose A and B exist, and now A was returned. Now suppose a C was added.
 * The next order of returns should be B, C, A 
 * @note This class doesn't take care of synchronization. That is the caller's responsibility
 */
public class RoundRobinList<T> {
	private List<T> items;
	private int currentIndex;
	
	public RoundRobinList() {
		items = new LinkedList<>();
		currentIndex = -1;
	}
	
	/** 
	 * Add the element to the list, if it doesn't already exist
	 */
	public void add(T element) {
		if (!items.contains(element)) {
			items.add(element);
		}
	}
	
	/** 
	 * Removes the element from the list
	 */
	public void remove(T element) {
		int indexRemoved = items.indexOf(element);
		if (indexRemoved != -1 && currentIndex > indexRemoved) {
			// If our current index is after the removed index, we need to update it.
			currentIndex--;
		}
		items.remove(element);
	}
	
	/** 
	 * @return The next element in the list
	 */
	public T getNext() {
		if (isEmpty()) {
			return null;
		}
		
		currentIndex++;
		if (currentIndex == items.size()) {
			currentIndex = 0;
		}
		
		return items.get(currentIndex);
	}
	
	/**
	 * @return true if the list is empty
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}
}
