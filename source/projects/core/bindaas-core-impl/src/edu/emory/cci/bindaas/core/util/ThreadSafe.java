package edu.emory.cci.bindaas.core.util;
/**
 * Marker interface for ThreadSafe classes that must also be cloneable
 * @author nadir
 *
 */
public interface ThreadSafe extends Cloneable{

	public Object clone();
}
