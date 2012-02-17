package net.thewaffleshop.flapjack.processor;

/**
 *
 * @author robert.hollencamp
 */
public interface Handler
{
	public String getAnnotationClassName();

	public void handle();
}
