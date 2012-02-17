package net.thewaffleshop.flapjack.processor;

import net.thewaffleshop.flapjack.annotations.Setter;

/**
 *
 * @author robert.hollencamp
 */
public class HandlerSetter implements Handler
{
	@Override
	public String getAnnotationClassName()
	{
		return Setter.class.getName();
	}

	@Override
	public void handle()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
