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
package net.thewaffleshop.flapjack.processor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

/**
 *
 * @author Robert Hollencamp
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

	/**
	 * Apparently getting the void type is hard work; copy/pasta some magic from lombok
	 *
	 * @return
	 */
	protected Type getVoidType() {
		return new JCNoType(getCtcInt(TypeTags.class, "VOID"));
	}
	private static int getCtcInt(Class<?> ctcLocation, String identifier) {
		try {
			return (Integer)ctcLocation.getField(identifier).get(null);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	private static class JCNoType extends Type implements NoType {
		public JCNoType(int tag) {
			super(tag, null);
		}

		@Override
		public TypeKind getKind() {
			if (tag == getCtcInt(TypeTags.class, "VOID")) return TypeKind.VOID;
			if (tag == getCtcInt(TypeTags.class, "NONE")) return TypeKind.NONE;
			throw new AssertionError("Unexpected tag: " + tag);
		}

		@Override
		public <R, P> R accept(TypeVisitor<R, P> v, P p) {
			return v.visitNoType(this, p);
		}
	}
}
