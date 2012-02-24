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
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Log;
import java.util.Map;
import java.util.regex.Pattern;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;
import javax.tools.JavaFileObject;

/**
 *
 * @author Robert Hollencamp
 */
public abstract class Handler
{
	private static final Pattern PRIMITIVE_TYPE_NAME_PATTERN = Pattern.compile("^(boolean|byte|short|int|long|float|double|char)$");

	JavacElements elements;
	TreeMaker treeMaker;
	Map<String, Object> arguments;
	JCAnnotation annotation;
	JCCompilationUnit compilationUnit;
	Log log;

	public abstract String getAnnotationClassName();

	public abstract void handle(final JCVariableDecl field, final JCClassDecl clazz);

	protected TreeMaker getTreeMaker() {
		return null;
	}

	/**
	 * @todo access level
	 * @param field
	 * @param clazz
	 * @return
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
	 * Access the value of an annotation argument
	 *
	 * @param name
	 * @return
	 */
	protected Object getArgument(final String name) {
		return arguments.get(name);
	}

	/**
	 * Given an expression, see if it is a primitive type
	 *
	 * @param expr
	 * @return
	 */
	protected boolean isPrimitive(final JCExpression expr) {
		String typeName = expr.toString();
		return PRIMITIVE_TYPE_NAME_PATTERN.matcher(typeName).matches();
	}

	/**
	 * In javac, dotted access of any kind, from {@code java.lang.String} to {@code var.methodName}
	 * is represented by a fold-left of {@code Select} nodes with the leftmost string represented by
	 * a {@code Ident} node. This method generates such an expression.
	 *
	 * For example, maker.Select(maker.Select(maker.Ident(NAME[java]), NAME[lang]), NAME[String]).
	 *
	 * @see com.sun.tools.javac.tree.JCTree.JCIdent
	 * @see com.sun.tools.javac.tree.JCTree.JCFieldAccess
	 *
	 * @param elems
	 * @return
	 */
	protected JCExpression chainDots(final String... elems)
	{
		JCExpression e = treeMaker.Ident(elements.getName(elems[0]));
		for (int i = 1 ; i < elems.length ; i++) {
			e = treeMaker.Select(e, elements.getName(elems[i]));
		}

		return e;
	}

	/**
	 * Add a warning message to the annotation
	 *
	 * @param message
	 */
	protected void addWarning(final String message) {
		final JavaFileObject oldSource = log.useSource(compilationUnit.sourcefile);
		log.warning(annotation.pos(), "proc.messager", message);
		log.useSource(oldSource);
	}

	/**
	 * Add an error message to the annotation
	 *
	 * @param message
	 */
	protected void addError(final String message) {
		final JavaFileObject oldSource = log.useSource(compilationUnit.sourcefile);
		log.error(annotation.pos(), "proc.messager", message);
		log.useSource(oldSource);
	}

	/**
	 * Apparently getting the void type is hard work; copy/pasta some magic from lombok
	 *
	 * @return
	 */
	protected Type getVoidType() {
		return new JCNoType(getCtcInt(TypeTags.class, "VOID"));
	}
	protected static int getCtcInt(Class<?> ctcLocation, String identifier) {
		try {
			return (Integer)ctcLocation.getField(identifier).get(null);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	private static class JCNoType extends Type implements NoType {
		JCNoType(int tag) {
			super(tag, null);
		}

		@Override
		public TypeKind getKind() {
			if (tag == getCtcInt(TypeTags.class, "VOID")) {
				return TypeKind.VOID;
			}
			if (tag == getCtcInt(TypeTags.class, "NONE")) {
				return TypeKind.NONE;
			}
			throw new RuntimeException("Unexpected tag: " + tag);
		}

		@Override
		public <R, P> R accept(TypeVisitor<R, P> v, P p) {
			return v.visitNoType(this, p);
		}
	}
}
