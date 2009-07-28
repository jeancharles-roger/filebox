package org.kawane.filebox.boost;

/**
 * <p>
 * A class that implements this interface can be serialized and deserialezed by a {@link JBoost} instance.
 * </p>
 *
 * @author Jean-Charles Roger (jeancharles.roger@geensys.com)
 *
 */
public interface BoostObject {

	public void writeToBoost(Boost boost);
	
	public void readFromBoost(Boost boost);
	
	public boolean equals(Object obj);
	
}
