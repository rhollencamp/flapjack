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
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import net.thewaffleshop.flapjack.annotations.Getter;

/**
 *
 * @author Robert Hollencamp
 */
public class HandlerGetter extends Handler
{
	/**
	 * Return the class name of the Getter annotation
	 *
	 * @return
	 */
	@Override
	public String getAnnotationClassName()
	{
		return Getter.class.getName();
	}

	/**
	 * Handle a Getter annotation on a field
	 *
	 * @todo do we need annotations (not null, nullable) in modifiers?
	 *
	 * @param field
	 * @param clazz
	 */
	@Override
	public void handle(final JCVariableDecl field, final JCClassDecl clazz)
	{
		if (getterExists(field, clazz)) {
			return;
		}

		final List<JCStatement> bodyStatements = List.<JCStatement>of(treeMaker.Return(createFieldAccessor(field, clazz)));
		final JCBlock block = treeMaker.Block(0, bodyStatements);
		final Name methodName = elements.getName(generateGetterName(field));
		final JCExpression returnType = field.vartype;
		final JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
		final JCMethodDecl methodDef = treeMaker.MethodDef(
				modifiers,
				methodName,
				returnType,
				List.<JCTypeParameter>nil(), // generic parameters
				List.<JCVariableDecl>nil(),  // parameters
				List.<JCExpression>nil(),    // throws clauses
				block,
				null);                       // annotation method default value
		clazz.defs = clazz.defs.append(methodDef);
	}

	/**
	 * Check and see if a getter method already exists for the given field
	 *
	 * @param field
	 * @param clazz
	 * @return
	 */
	private boolean getterExists(final JCVariableDecl field, final JCClassDecl clazz)
	{
		final String getterName = generateGetterName(field);

		for (final JCTree classDef : clazz.defs) {
			if (classDef instanceof JCMethodDecl) {
				final JCMethodDecl method = (JCMethodDecl) classDef;
				final String methodName = method.name.toString();
				if (methodName.equals(getterName)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Generate the name of the getter method
	 *
	 * @param field
	 * @return
	 */
	private String generateGetterName(final JCVariableDecl field)
	{
		final String prefix =
				"boolean".equals(field.vartype.toString())
				? "is"
				: "get";

		final String fieldName = field.name.toString();
		return prefix
				+ Character.toUpperCase(fieldName.charAt(0))
				+ (fieldName.length() > 1 ? fieldName.substring(1) : "");
	}
}
