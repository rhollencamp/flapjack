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
	@Override
	public String getAnnotationClassName()
	{
		return Setter.class.getName();
	}

	@Override
	public void handle(final JCVariableDecl field, final JCClassDecl clazz)
	{
		if (setterExists(field, clazz)) {
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
		// method body
		final JCAssign assign = treeMaker.Assign(createFieldAccessor(field, clazz), treeMaker.Ident(field));

		// add null check
		final List<JCStatement> bodyStatements;
		if (getArgument("rejectNull") == Boolean.TRUE) {
			bodyStatements = List.<JCStatement>of(generateNullCheck(field), treeMaker.Exec(assign));
		} else {
			bodyStatements = List.<JCStatement>of(treeMaker.Exec(assign));
		}

		return treeMaker.Block(0, bodyStatements);
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
	 * Check and see if a setter method already exists for this field
	 *
	 * @param field
	 * @param clazz
	 * @return
	 */
	private boolean setterExists(final JCVariableDecl field, final JCClassDecl clazz)
	{
		final String setterName = generateSetterName(field);

		for (final JCTree classDef : clazz.defs) {
			if (classDef instanceof JCMethodDecl) {
				final JCMethodDecl method = (JCMethodDecl) classDef;
				final String methodName = method.name.toString();
				if (methodName.equals(setterName)) {
					return true;
				}
			}
		}

		return false;
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
