package net.thewaffleshop.flapjack.processor;

import net.thewaffleshop.flapjack.annotations.Getter;

/**
 *
 * @author robert.hollencamp
 */
public class HandlerGetter implements Handler
{
	/**
	 * Return the class name of the Getter annotation
	 *
	 * @return
	 */
	@Override
	public String getAnnotationClassName()
	{
		return Getter.class.getName();
	}

	@Override
	public void handle()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
