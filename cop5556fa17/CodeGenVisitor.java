package cop5556fa17;

import java.util.ArrayList;
import java.util.HashMap;

//import com.sun.org.apache.bcel.internal.generic.CodeExceptionGen;
//import com.sun.org.apache.bcel.internal.generic.PUSH;
//import com.sun.org.apache.xpath.internal.operations.Bool;
//import com.sun.org.apache.xpath.internal.operations.Neg;
//import com.sun.xml.internal.bind.v2.model.runtime.RuntimeTypeInfo;
//import jdk.nashorn.internal.runtime.ECMAException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	public class SymbolTable {

		HashMap<String, Declaration> hm = new HashMap<>();

		public void insert(String name, Declaration astNode) {
			hm.put(name,  astNode);
		}

		public Declaration lookupType(String name) {
			return hm.get(name);
		}
	}
	SymbolTable st = new SymbolTable();

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;



	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		//PreDefined variables
		cw.visitField(ACC_STATIC, "DEF_X", "I", null, 256).visitEnd();
		cw.visitField(ACC_STATIC, "DEF_Y", "I", null, 256).visitEnd();
		cw.visitField(ACC_STATIC, "Z", "I", null, 16777215).visitEnd();
		cw.visitField(ACC_STATIC, "x", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "y", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "X", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "Y", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "r", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "a", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "R", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "A", "I", null, 0).visitEnd();

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");

		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);

		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);

		//terminate construction of main method
		mv.visitEnd();

		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		Type tu = declaration_Variable.type;
		if (tu == Type.INTEGER) {
			cw.visitField(ACC_STATIC, declaration_Variable.name, "I", null, 0).visitEnd();
		} else if (tu == Type.BOOLEAN) {
			cw.visitField(ACC_STATIC, declaration_Variable.name, "Z", null, false).visitEnd();
		}
		if (declaration_Variable.e != null) {
			declaration_Variable.e.visit(this, arg);
			if (tu == Type.INTEGER) {
				mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, "I");
			} else if (tu == Type.BOOLEAN) {
				mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, "Z");
			}
		}
		return declaration_Variable.name;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);

		if (expression_Binary.type == Type.INTEGER) {
			switch (expression_Binary.op) {
				case OP_PLUS:
					mv.visitInsn(IADD);
					return null;
				case OP_MINUS:
					mv.visitInsn(ISUB);
					return null;
				case OP_TIMES:
					mv.visitInsn(IMUL);
					return null;
				case OP_DIV:
					mv.visitInsn(IDIV);
					return null;
				case OP_MOD:
					mv.visitInsn(IREM);
					return null;
			}
		}
		Label t = new Label();
		Label out = new Label();
		switch (expression_Binary.op) {
			case OP_AND:
				mv.visitInsn(IAND);
				mv.visitJumpInsn(GOTO, out);
				break;
			case OP_OR:
				mv.visitInsn(IOR);
				mv.visitJumpInsn(GOTO, out);
				break;
			case OP_EQ:
				mv.visitJumpInsn(IF_ICMPEQ, t);
				break;
			case OP_NEQ:
				mv.visitJumpInsn(IF_ICMPNE, t);
				break;
			case OP_LT:
				mv.visitJumpInsn(IF_ICMPLT, t);
				break;
			case OP_GT:
				mv.visitJumpInsn(IF_ICMPGT, t);
				break;
			case OP_LE:
				mv.visitJumpInsn(IF_ICMPLE, t);
				break;
			case OP_GE:
				mv.visitJumpInsn(IF_ICMPGE, t);
				break;
			default:
				throw new Exception("This shouldn't happen");
		}
		mv.visitLdcInsn(false);
		mv.visitJumpInsn(GOTO, out);
		mv.visitLabel(t);
		mv.visitLdcInsn(true);
		mv.visitLabel(out);

//		throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.type);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		expression_Unary.e.visit(this, arg);
		if (expression_Unary.type == Type.INTEGER) {
			if (expression_Unary.op == Scanner.Kind.OP_MINUS) mv.visitInsn(INEG);
			else if(expression_Unary.op == Scanner.Kind.OP_EXCL) {
				mv.visitLdcInsn(Integer.MAX_VALUE);
				mv.visitInsn(IXOR);
			}
		} else if (expression_Unary.type == Type.BOOLEAN && expression_Unary.op == Scanner.Kind.OP_EXCL) {
			Label t = new Label();
			Label out = new Label();
			mv.visitJumpInsn(IFEQ, t);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, out);
			mv.visitLabel(t);
			mv.visitLdcInsn(true);
			mv.visitLabel(out);
		}
//		throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.type);
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		if (index.isCartesian()) {
			index.e0.visit(this, arg);
			index.e1.visit(this, arg);
			return null;
		} else {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");

			/** No need to recalculate x and y. Converting from cartesian to polar and back with integers is inconsistent.
			index.e0.visit(this, arg);
			index.e1.visit(this, arg);
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, "RuntimeFunctions.className"cart_x", "(II)I", false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, "RuntimeFunctions.className"cart_y", "(II)I", false);
			**/
		}

		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, "Ljava/awt/image/BufferedImage;");
		expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"getPixel", "(Ljava/awt/image/BufferedImage;II)I", false);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
//		throw new UnsupportedOperationException();
		Label Out = new Label();
		Label FBranch = new Label();
		expression_Conditional.condition.visit(this, arg);
		mv.visitJumpInsn(IFEQ, FBranch);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, Out);
		mv.visitLabel(FBranch);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitLabel(Out);
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		cw.visitField(ACC_STATIC, declaration_Image.name, "Ljava/awt/image/BufferedImage;", null, null).visitEnd();
		st.insert(declaration_Image.name, declaration_Image);
		if (declaration_Image.source != null) {
			declaration_Image.source.visit(this, arg);
			if (declaration_Image.xSize != null && declaration_Image.ySize != null) {
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			} else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", "(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/awt/image/BufferedImage;", false);
		} else {
			if (declaration_Image.xSize != null && declaration_Image.ySize != null) {
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);
			} else {
				mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
				mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", "(II)Ljava/awt/image/BufferedImage;", false);
		}
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, "Ljava/awt/image/BufferedImage;");
		return declaration_Image.type;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
		//throw new UnsupportedOperationException();
	}



	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		//throw new UnsupportedOperationException();
		return source_CommandLineParam.type;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {

		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		return null;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {

		cw.visitField(ACC_STATIC, declaration_SourceSink.name, "Ljava/lang/String;", null, null).visitEnd();
		if (declaration_SourceSink.source != null) {
			declaration_SourceSink.source.visit(this,arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name, "Ljava/lang/String;");
		}
		return null;
		//throw new UnsupportedOperationException();
	}



	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		mv.visitLdcInsn(expression_IntLit.value);
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if (expression_FunctionAppWithExprArg.function == Scanner.Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className,"abs", "(I)I", false);
		} else if (expression_FunctionAppWithExprArg.function == Scanner.Kind.KW_log) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", "(I)I", false);
		} else {
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		if (expression_FunctionAppWithIndexArg.function == Scanner.Kind.KW_cart_x) {
			if (expression_FunctionAppWithIndexArg.arg.isCartesian())
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", "(II)I", false);
			else {
				mv.visitInsn(POP2);
				mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			}
		} else if (expression_FunctionAppWithIndexArg.function == Scanner.Kind.KW_cart_y) {
			if (expression_FunctionAppWithIndexArg.arg.isCartesian())
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", "(II)I", false);
			else {
				mv.visitInsn(POP2);
				mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			}
		} else if (expression_FunctionAppWithIndexArg.function == Scanner.Kind.KW_polar_r) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", "(II)I", false);
		} else if (expression_FunctionAppWithIndexArg.function == Scanner.Kind.KW_polar_a) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", "(II)I", false);
		} else {
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		switch (expression_PredefinedName.kind) {
			case KW_x:
				mv.visitFieldInsn(GETSTATIC, className, "x", "I");
				break;
			case KW_y:
				mv.visitFieldInsn(GETSTATIC, className, "y", "I");
				break;
			case KW_Y:
				mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
				break;
			case KW_X:
				mv.visitFieldInsn(GETSTATIC, className, "X", "I");
				break;
			case KW_r:
				mv.visitFieldInsn(GETSTATIC, className, "r", "I");
				break;
			case KW_R:
				mv.visitFieldInsn(GETSTATIC, className, "R", "I");
				break;
			case KW_a:
				mv.visitFieldInsn(GETSTATIC, className, "a", "I");
				break;
			case KW_A:
				mv.visitFieldInsn(GETSTATIC, className, "A", "I");
				break;
			case KW_DEF_X:
				mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
				break;
			case KW_DEF_Y:
				mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
				break;
			case KW_Z:
				mv.visitFieldInsn(GETSTATIC, className, "Z", "I");
				break;
			default:
				break;
		}

		return expression_PredefinedName.kind.name();
		//throw new UnsupportedOperationException();
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {

		if (statement_Out.type == Type.INTEGER) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
		} else if (statement_Out.type == Type.BOOLEAN) {
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		} else if (statement_Out.type == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Ljava/awt/image/BufferedImage;");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.IMAGE);
			statement_Out.sink.visit(this, arg);
		}
		//CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.type);
		return statement_Out.type;
//		throw new UnsupportedOperationException();
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 *
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean
	 *  to convert String to actual type.
	 *
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		statement_In.source.visit(this, arg);
		if (statement_In.type == Type.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
		} else if (statement_In.type == Type.BOOLEAN) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
		} else if (statement_In.type == Type.IMAGE) {
			Declaration_Image d = ((Declaration_Image)st.lookupType(statement_In.name));
			if (d.xSize != null && d.ySize != null) {
				d.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				d.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			} else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}

			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Ljava/awt/image/BufferedImage;");
		}
		//throw new UnsupportedOperationException();
		return statement_In.type;
	}


	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		if (statement_Assign.type == Type.INTEGER || statement_Assign.type == Type.BOOLEAN) {
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		} else if (statement_Assign.type == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", "(Ljava/awt/image/BufferedImage;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, "X", "I");

			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", "(Ljava/awt/image/BufferedImage;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");

			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", "(II)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, "R", "I");

			mv.visitLdcInsn(0);
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", "(II)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, "A", "I");

			Label innerCond = new Label();
			Label outerCond = new Label();
			Label endWhile = new Label();
			Label out = new Label();

			mv.visitLdcInsn(0);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			mv.visitLabel(outerCond);
			mv.visitLdcInsn(0);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");

			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "X", "I");
			mv.visitJumpInsn(IF_ICMPGE, out);
			mv.visitLabel(innerCond);
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			mv.visitJumpInsn(IF_ICMPGE, endWhile);

			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitInsn(DUP2);

			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", "(II)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, "r", "I");

			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", "(II)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, "a", "I");
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this,arg);

			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitLdcInsn(1);
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");

			mv.visitJumpInsn(GOTO, innerCond);
			mv.visitLabel(endWhile);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitLdcInsn(1);
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");

			mv.visitJumpInsn(GOTO, outerCond);
			mv.visitLabel(out);
		}
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {

		if (lhs.type == Type.INTEGER) {
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
		} else if (lhs.type == Type.BOOLEAN) {
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
		} else if (lhs.type == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, "Ljava/awt/image/BufferedImage;");
			lhs.index.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", "(ILjava/awt/image/BufferedImage;II)V", false);
		}
		return lhs.type;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {

		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"makeFrame", "(Ljava/awt/image/BufferedImage;)Ljavax/swing/JFrame;", false);
		mv.visitInsn(POP);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {

		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, "Ljava/lang/String;");
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		mv.visitLdcInsn(expression_BooleanLit.value);
//		throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return expression_BooleanLit.value;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
//		throw new UnsupportedOperationException();
		if (expression_Ident.type == Type.BOOLEAN) {
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
		} else if (expression_Ident.type == Type.INTEGER) {
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
		}
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.type);
		return null;
	}

}
