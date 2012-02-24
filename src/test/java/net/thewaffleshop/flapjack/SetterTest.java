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

import net.thewaffleshop.flapjack.annotations.Setter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for @Setter annotation
 *
 * @author Robert Hollencamp
 */
public class SetterTest
{
	@Setter
	private String instanceVar;

	@Setter
	private String staticVar;

	@Setter
	private boolean booleanVar;

	@Setter(rejectNull = true)
	private String rejectNullVar;

	/**
	 * Test setter on an instance field
	 */
	@Test
	public void testInstanceSetter()
	{
		this.setInstanceVar("instance");
		Assert.assertEquals("instance", instanceVar);

		this.setInstanceVar("var");
		Assert.assertEquals("var", instanceVar);
	}

	/**
	 * Boolean fields do not get a special name on the setter; make sure they get the regular name
	 */
	@Test
	public void testBoolean()
	{
		this.setBooleanVar(true);
		Assert.assertEquals(true, booleanVar);

		this.setBooleanVar(false);
		Assert.assertEquals(false, booleanVar);
	}

	/**
	 * Test reject null will reject null value
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRejectNullRejects() {
		setRejectNullVar(null);
	}

	/**
	 * Test reject null accepts valid value
	 */
	@Test
	public void testRejectNullAccepts() {
		setRejectNullVar("not null");
		Assert.assertEquals("not null", rejectNullVar);
	}
}
