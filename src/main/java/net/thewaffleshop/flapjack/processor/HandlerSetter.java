package net.thewaffleshop.flapjack.processor;

import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import net.thewaffleshop.flapjack.annotations.Setter;

/**
 *
 * @author robert.hollencamp
 */
public class HandlerSetter extends Handler
{
	@Override
	public String getAnnotationClassName()
	{
		return Setter.class.getName();
	}

	@Override
	public void handle(final JCVariableDecl field, final JCClassDecl clazz)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
