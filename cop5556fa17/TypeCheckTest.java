package cop5556fa17;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeCheckVisitor.SemanticException;

import static cop5556fa17.Scanner.Kind.*;

public class TypeCheckTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	
	/**
	 * Scans, parses, and type checks given input String.
	 * 
	 * Catches, prints, and then rethrows any exceptions that occur.
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		try {
			Scanner scanner = new Scanner(input).scan();
			ASTNode ast = new Parser(scanner).parse();
			show(ast);
			ASTVisitor v = new TypeCheckVisitor();
			ast.visit(v, null);
		} catch (Exception e) {
			show(e);
			throw e;
		}
	}

	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSmallest() throws Exception {
		String input = "n"; //Smallest legal program, only has a name
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); // Create a parser
		ASTNode ast = parser.parse(); // Parse the program
		TypeCheckVisitor v = new TypeCheckVisitor();
		String name = (String) ast.visit(v, null);
		show("AST for program " + name);
		show(ast);
	}



	
	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	 @Test
	 public void testDec1() throws Exception {
	 String input = "prog int k = 42;";
	 typeCheck(input);
	 }
	 
	 /**
	  * This program does not declare k. The TypeCheckVisitor should
	  * throw a SemanticException in a fully implemented assignment.
	  * @throws Exception
	  */
	 @Test
	 public void testUndec() throws Exception {
	 String input = "prog k = 42;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }

	@Test
	public void testDeclTypeStoF() throws Exception {
		String input = "prog file k = @3;";
		typeCheck(input);
	}

	@Test
	public void testDecTypeBtoI() throws Exception {
		String input = "prog int k = true;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	@Test
	public void testDecTypeItoB() throws Exception {
		String input = "prog boolean k = 123;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testImgDecAssg() throws Exception {
		String input = "prog image[3,5] myImg; myImg->SCREEN;";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testIntDecAssg() throws Exception {
		String input = "prog int myInt; myInt = 8 + 9 * 3;";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testBoolDecAssg() throws Exception {
		String input = "prog boolean myBool; myBool = 8 == 1;";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testBinaryExpression() throws Exception {
		String input = "prog boolean myBool = 8 == 1 | 9 != 1;";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testExpressionUnary() throws Exception {
		String input = "prog boolean myBool; myBool = 8 == -1 + 2;";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testExpressionConditional() throws Exception {
		String input = "prog int myInt = true | false ? 8 : 10;";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testDeclSourceSinkString() throws Exception {
		String input = "prog file mySource = \"filename.txt\";";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testDeclSourceSinkIdent() throws Exception {
		String input = "prog file myFile = \"filename.txt\"; file mySource = myFile;";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testStateIn() throws Exception {
		String input = "prog file myFile = \"file1.txt\";myFile <- \"file2.txt\";";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testIndex() throws Exception {
		String input = "prog int myInt = sin[R,A];";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testExpArg() throws Exception {
		String input = "prog int myInt = sin(1 + 2);";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testPixSel() throws Exception {
		String input = "prog image myImg <- \"1234\";int myInt = myImg[1,2];";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testNested() throws Exception {
		String input = "prog int myInt = (3+3-(8*9))-1;";
		typeCheck(input);
	}

	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testprog10() throws Exception {
		String input = "prog10//args:false,false\nboolean v1;\nv1 <- @ 0;\nboolean v2;\nv2 <- @ 1;\nboolean output = !v1 ? v1 : v2;\n";
		typeCheck(input);
	}
}
