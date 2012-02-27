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
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import net.thewaffleshop.flapjack.annotations.Setter;

/**
 *
 * @author Robert Hollencamp
 */
public class HandlerSetter extends Handler
{
	/**
	 * Return fully qualified name of annotations we support
	 *
	 * This class supports {@link Setter}
	 *
	 * @return
	 */
	@Override
	public String getAnnotationClassName()
	{
		return Setter.class.getName();
	}

	/**
	 * Generate code for Setter
	 *
	 * @param field
	 * @param clazz
	 */
	@Override
	public void handle(final JCVariableDecl field, final JCClassDecl clazz)
	{
		final String setterName = generateSetterName(field);

		// if setter already exists we have nothing to do
		if (methodExists(setterName, clazz)) {
			return;
		}

		// parameters
		JCVariableDecl param = treeMaker.VarDef(
				treeMaker.Modifiers(Flags.FINAL, List.<JCAnnotation>nil()),
				field.name,
				field.vartype,
				null);

		final Name methodName = elements.getName(generateSetterName(field));
		final JCExpression returnType = treeMaker.Type(getVoidType());
		final JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
		final JCBlock body = generateBody(field, clazz);
		final JCMethodDecl methodDef = treeMaker.MethodDef(
				modifiers,
				methodName,
				returnType,
				List.<JCTypeParameter>nil(),        // generic parameters
				List.of(param),                     // parameters
				List.<JCExpression>nil(),           // throws clauses
				body,
				null);                              // annotation method default value
		clazz.defs = clazz.defs.append(methodDef);
	}

	/**
	 * Generate setter method body
	 *
	 * @param field
	 * @param clazz
	 * @return
	 */
	private JCBlock generateBody(final JCVariableDecl field, final JCClassDecl clazz) {
		final JCExpression rhs =
				(getArgument("useClone") == Boolean.TRUE)
				? callClone(field)
				: treeMaker.Ident(field);

		// method body
		final JCAssign assign = treeMaker.Assign(
				createFieldAccessor(field, clazz),
				rhs);

		// generate body statements to assign field value
		List<JCStatement> bodyStatements = List.<JCStatement>of(treeMaker.Exec(assign));

		// if we want to reject null values, prepend null check
		if (getArgument("rejectNull") == Boolean.TRUE) {
			bodyStatements = bodyStatements.prepend(generateNullCheck(field));
		}

		return treeMaker.Block(0, bodyStatements);
	}

	/**
	 * Generate call to parameter's clone method
	 *
	 * @param field
	 * @return
	 */
	private JCExpression callClone(final JCVariableDecl field)
	{
		// call clone
		JCMethodInvocation cloneCall = treeMaker.Apply(
				List.<JCExpression>nil(),
				treeMaker.Select(
						treeMaker.Ident(field),
						elements.getName("clone")),
				List.<JCExpression>nil());

		// clone returns java.lang.Object; cast to correct type
		return treeMaker.TypeCast(field.vartype, cloneCall);
	}

	/**
	 * Generate a null check statement
	 *
	 * @param field
	 * @return
	 */
	private JCStatement generateNullCheck(final JCVariableDecl field) {
		// primitive types can not be null
		if (isPrimitive(field.vartype)) {
			addWarning("Primitive variable types can not be null");
			return null;
		}

		Name fieldName = field.name;
		JCExpression npe = chainDots("java", "lang", "IllegalArgumentException");
		JCNewClass exception = treeMaker.NewClass(null, List.<JCExpression>nil(), npe, List.<JCExpression>of(treeMaker.Literal(fieldName.toString())), null);
		JCStatement throwStatement = treeMaker.Throw(exception);

		return treeMaker.If(treeMaker.Binary(getCtcInt(JCTree.class, "EQ"), treeMaker.Ident(fieldName), treeMaker.Literal(getCtcInt(TypeTags.class, "BOT"), null)), throwStatement, null);
	}

	/**
	 * Generate the name of the setter method
	 *
	 * @param field
	 * @return
	 */
	private String generateSetterName(final JCVariableDecl field)
	{
		final String fieldName = field.name.toString();
		return "set"
				+ Character.toUpperCase(fieldName.charAt(0))
				+ (fieldName.length() > 1 ? fieldName.substring(1) : "");
	}
}
