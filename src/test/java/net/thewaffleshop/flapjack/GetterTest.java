/*
 *   Copyright 2012 Robert Hollencamp
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.thewaffleshop.flapjack;

import junit.framework.Assert;
import net.thewaffleshop.flapjack.annotations.Getter;
import org.junit.Test;

/**
 * Unit tests on @Getter annotation
 *
 * @author Robert Hollencamp
 */
public class GetterTest
{
	@Getter
	private String instanceVar;

	@Getter
	private static String staticVar;

	@Getter
	private boolean boolVar;

	/**
	 * Test instance field getter
	 */
	@Test
	public void testInstanceGetter()
	{
		instanceVar = "instance";
		Assert.assertEquals("instance", this.getInstanceVar());

		instanceVar = "var";
		Assert.assertEquals("var", this.getInstanceVar());
	}

	/**
	 * boolean fields get a special named getter; make sure a boolean field gets special name
	 */
	@Test
	public void testBoolean()
	{
		boolVar = true;
		Assert.assertEquals(true, this.isBoolVar());

		boolVar = false;
		Assert.assertEquals(false, this.isBoolVar());
	}
}
