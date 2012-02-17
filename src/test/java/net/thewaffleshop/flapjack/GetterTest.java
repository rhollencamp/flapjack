package net.thewaffleshop.flapjack;

import junit.framework.Assert;
import net.thewaffleshop.flapjack.annotations.Getter;
import org.junit.Test;

/**
 *
 * @author robert.hollencamp
 */
public class GetterTest
{
	@Getter
	private final String instanceVar = "instance";

	@Getter
	private static final String staticVar = "static";

	@Getter
	private final boolean bool = true;

	@Test
	public void testInstanceGetter()
	{
		Assert.assertEquals("instance", this.getInstanceVar());
	}

	@Test
	public void testBoolean()
	{
		Assert.assertEquals(true, this.isBool());
	}
}
