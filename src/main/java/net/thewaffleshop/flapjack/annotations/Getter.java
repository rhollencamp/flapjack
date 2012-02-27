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
package net.thewaffleshop.flapjack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Robert Hollencamp
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Getter
{
	/**
	 * Return clone of instance field.
	 *
	 * Useful for mutable types like {@code Date} - you do not want to return a reference to a
	 * mutable type because the state of the object can be modified using said reference. This
	 * causes a clone of the instance field to be returned from the getter.
	 *
	 * @todo determine if object type implements Cloneable at compile time
	 *
	 * @return
	 */
	public boolean useClone() default false;
}
