package net.thewaffleshop.flapjack;

import junit.framework.Assert;
import net.thewaffleshop.flapjack.annotations.Getter;
import org.junit.Test;

/**
 *
 * @author robert.hollencamp
 */
public class FooTest {

	@Getter
	private String foo;

	@Test
	public void testStuff() {
		Assert.assertEquals(this, this);
	}
}
