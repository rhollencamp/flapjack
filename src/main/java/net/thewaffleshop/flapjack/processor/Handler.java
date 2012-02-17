package net.thewaffleshop.flapjack.processor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;

/**
 *
 * @author robert.hollencamp
 */
public abstract class Handler
{
	JavacElements elements;
	TreeMaker treeMaker;

	public abstract String getAnnotationClassName();

	public abstract void handle(final JCVariableDecl field, final JCClassDecl clazz);

	protected TreeMaker getTreeMaker() {
		return null;
	}

	/**
	 * @todo access level
	 * @param field
	 */
	protected JCFieldAccess createFieldAccessor(final JCVariableDecl field, final JCClassDecl clazz) {
		JCIdent ident;
		if ((field.mods.flags & Flags.STATIC) == 0) {
			ident = treeMaker.Ident(elements.getName("this"));
		} else {
			ident = treeMaker.Ident(clazz.name);
		}

		return treeMaker.Select(ident, field.name);
	}
}
