package net.thewaffleshop.flapjack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author robert.hollencamp
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Setter
{
}
