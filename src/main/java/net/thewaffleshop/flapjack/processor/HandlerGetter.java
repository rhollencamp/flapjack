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
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
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
	 * Return fully qualified name of annotations we support.
	 *
	 * This class supports {@link Getter}
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
		final String getterName = generateGetterName(field);

		// if getter method already exists, we have nothing to do
		if (methodExists(getterName, clazz)) {
			return;
		}

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
				generateBody(field, clazz),
				null);                       // annotation method default value
		clazz.defs = clazz.defs.append(methodDef);
	}

	/**
	 * Generate body of getter method
	 *
	 * @param field
	 * @param clazz
	 * @return
	 */
	private JCBlock generateBody(final JCVariableDecl field, final JCClassDecl clazz)
	{
		JCFieldAccess fieldAccess = createFieldAccessor(field, clazz);

		// are we returning a clone or the instance itself?
		JCExpression retValue;
		if (getArgument("useClone") == Boolean.TRUE) {
			retValue = generateCloneStatement(fieldAccess, field);
		} else {
			retValue = fieldAccess;
		}

		return treeMaker.Block(0, List.<JCStatement>of(treeMaker.Return(retValue)));
	}

	/**
	 * Generate a clone method call for the given expression
	 *
	 * @param var expression to clone result of
	 * @param field type to cast result to
	 * @return
	 */
	private JCTypeCast generateCloneStatement(final JCExpression var, final JCVariableDecl field)
	{
		// call clone
		JCTree.JCMethodInvocation cloneCall = treeMaker.Apply(
				List.<JCExpression>nil(),
				treeMaker.Select(
						var,
						elements.getName("clone")),
				List.<JCExpression>nil());

		// clone returns java.lang.Object; cast to correct type
		return treeMaker.TypeCast(field.vartype, cloneCall);
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
