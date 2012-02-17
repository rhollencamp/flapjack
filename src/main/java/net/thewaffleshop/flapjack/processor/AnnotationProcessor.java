package net.thewaffleshop.flapjack.processor;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author robert.hollencamp
 */
public class AnnotationProcessor extends AbstractProcessor
{
	private final HashMap<String, Handler> handlers = new HashMap<String, Handler>();

	private JavacProcessingEnvironment javacProcessingEnv;
	private Context context;
	private Log log;
	private Trees trees;

	/**
	 * Initialize
	 */
	@Override
	public synchronized void init(final ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);

		for (final Handler handler : ServiceLoader.<Handler>load(Handler.class, this.getClass().getClassLoader())) {
			handlers.put(handler.getAnnotationClassName(), handler);
		}

		trees = Trees.instance(processingEnv);
		javacProcessingEnv = (JavacProcessingEnvironment) processingEnv;
		context = javacProcessingEnv.getContext();
		log = Log.instance(context);
	}

	/**
	 * Process annotations
	 *
	 * @param annotations
	 * @param roundEnv
	 * @return
	 */
	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv)
	{
		for (Element element : roundEnv.getRootElements()) {
			TreePath path = trees.getPath(element);
			JCCompilationUnit unit = (JCCompilationUnit) path.getCompilationUnit();

			processCompilationUnit(unit, annotations);
		}

		return true;
	}

	/**
	 * Process a compilation unit
	 *
	 * @param compilationUnit
	 * @param annotations
	 */
	private void processCompilationUnit(
			final JCCompilationUnit compilationUnit,
			final Set<? extends TypeElement> annotations)
	{
		for (JCTree compilationUnitDef : compilationUnit.defs) {
			// skip imports and find our way down to the class declaration
			if (compilationUnitDef instanceof JCClassDecl) {
				final JCClassDecl classDecl = (JCClassDecl) compilationUnitDef;
				for (JCTree classDef : classDecl.defs) {
					// find all fields
					if (classDef instanceof JCVariableDecl) {
						final JCVariableDecl variableDecl = (JCVariableDecl) classDef;
						for (JCAnnotation annotation : variableDecl.mods.annotations) {
							processAnnotations(compilationUnit, classDecl, variableDecl, annotation, annotations);
						}
					}
				}
			}
		}
	}

	/**
	 * Process annotations on a field
	 *
	 * @param compilationUnit compilation unit containing class
	 * @param classDecl class node containing variable
	 * @param variableDecl variable node annotation is attached to
	 * @param annotation annotation node attached to variable
	 * @param annotations list of annotations we process
	 */
	private void processAnnotations(
			final JCCompilationUnit compilationUnit,
			final JCClassDecl classDecl,
			final JCVariableDecl variableDecl,
			final JCAnnotation annotation,
			final Set<? extends TypeElement> annotations)
	{
		for (TypeElement typeElement : annotations) {
			final Symbol symbol = (Symbol) typeElement;
			if (symbol.equals(annotation.type.tsym)) {
				Handler handler = handlers.get(typeElement.getQualifiedName().toString());
				handler.treeMaker = TreeMaker.instance(context);
				handler.elements = JavacElements.instance(context);
				handler.handle(variableDecl, classDecl);

				/**
				final JavaFileObject oldSource = log.useSource(compilationUnit.sourcefile);
				log.error(annotation.pos(), "proc.messager", "Hello World!");
				log.useSource(oldSource);
				*/
			}
		}
	}

	/**
	 * Return a list of supported annotations
	 *
	 * @return
	 */
	@Override
	public Set<String> getSupportedAnnotationTypes()
	{
		return handlers.keySet();
	}

	/**
	 * Return the source version that this processor supports
	 *
	 * We want to support all VMs, so just return the version of the current VM
	 *
	 * @return
	 */
	@Override
	public SourceVersion getSupportedSourceVersion()
	{
		return SourceVersion.latestSupported();
	}
}
