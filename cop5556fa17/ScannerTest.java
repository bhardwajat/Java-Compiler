/**
 * /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
 */

package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;

import static cop5556fa17.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(scanner.new Token(kind, pos, length, line, pos_in_line), t);
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token check(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}

	/**
	 * Simple test case with a (legal) empty program
	 *   
	 * @throws LexicalException
	 */
//	@Test
//	public void testEmpty() throws LexicalException {
//		String input = "";  //The input is the empty string.  This is legal
//		show(input);        //Display the input 
//		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
//		show(scanner);   //Display the Scanner
//		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
//	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, as we will want to do 
	 * later, the end of line character would be inserted by the text editor.
	 * Showing the input will let you check your input is what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testRandom() throws LexicalException {
		String input = /*"abc\ndef";"\"\\\\\"\"ab\"";*/"//Checking comment\r\n%123";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_MOD, 20, 1, 2, 1);
		checkNext(scanner, INTEGER_LITERAL, 21, 3, 2, 2);
//		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdentAndComment() throws LexicalException {
		String input = "def //Checking comment\nv2";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);
		checkNext(scanner, IDENTIFIER, 23, 2, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testSept() throws LexicalException {
		String input = "(([];\n,]";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, LPAREN, 1, 1, 1, 2);
		checkNext(scanner, LSQUARE, 2, 1, 1, 3);
		checkNext(scanner, RSQUARE, 3, 1, 1, 4);
		checkNext(scanner, SEMI, 4, 1, 1, 5);
		checkNext(scanner, COMMA, 6, 1, 2, 1);
		checkNext(scanner, RSQUARE, 7, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testOpt() throws LexicalException {
		String input = "+<-->**!=\n=&|@";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_PLUS, 0, 1, 1, 1);
		checkNext(scanner, OP_LARROW, 1, 2, 1, 2);
		checkNext(scanner, OP_RARROW, 3, 2, 1, 4);
		checkNext(scanner, OP_POWER, 5, 2, 1, 6);
		checkNext(scanner, OP_NEQ, 7, 2, 1, 8);
		checkNext(scanner, OP_ASSIGN, 10, 1, 2, 1);
		checkNext(scanner, OP_AND, 11, 1, 2, 2);
		checkNext(scanner, OP_OR, 12, 1, 2, 3);
		checkNext(scanner, OP_AT, 13, 1, 2, 4);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testOpt2() throws LexicalException {
		String input = "<>!?:%== <=>=-*/<-";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_LT, 0, 1, 1, 1);
		checkNext(scanner, OP_GT, 1, 1, 1, 2);
		checkNext(scanner, OP_EXCL, 2, 1, 1, 3);
		checkNext(scanner, OP_Q, 3, 1, 1, 4);
		checkNext(scanner, OP_COLON, 4, 1, 1, 5);
		checkNext(scanner, OP_MOD, 5, 1, 1, 6);
		checkNext(scanner, OP_EQ, 6, 2, 1, 7);
		checkNext(scanner, OP_LE, 9, 2, 1, 10);
		checkNext(scanner, OP_GE, 11, 2, 1, 12);
		checkNext(scanner, OP_MINUS, 13, 1, 1, 14);
		checkNext(scanner, OP_TIMES, 14, 1, 1, 15);
		checkNext(scanner, OP_DIV, 15, 1, 1, 16);
		checkNext(scanner, OP_LARROW, 16, 2, 1, 17);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdent() throws LexicalException {
		String input = "abc$_01+sin%false";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 7, 1, 1);
		checkNext(scanner, OP_PLUS, 7, 1, 1, 8);
		checkNext(scanner, KW_sin, 8, 3, 1, 9);
		checkNext(scanner, OP_MOD, 11, 1, 1, 12);
		checkNext(scanner, BOOLEAN_LITERAL, 12, 5, 1, 13);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdent2() throws LexicalException {
		String input = "abc+sin%\nfalse";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);
		checkNext(scanner, OP_PLUS, 3, 1, 1, 4);
		checkNext(scanner, KW_sin, 4, 3, 1, 5);
		checkNext(scanner, OP_MOD, 7, 1, 1, 8);
		checkNext(scanner, BOOLEAN_LITERAL, 9, 5, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testDigit() throws LexicalException {
		String input = "001000";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 1, 1, 2);
		checkNext(scanner, INTEGER_LITERAL, 2, 4, 1, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteral() throws LexicalException {
		String input = "\"abcdef\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 8, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteralEscSeq1() throws LexicalException {
		String input = "\"ab\\\\pc\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 8, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteralEscSeq2() throws LexicalException {
		String input = "\"ab\\\\pc\"+";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 8, 1, 1);
		checkNext(scanner, OP_PLUS, 8, 1, 1, 9);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteralEscSeq3() throws LexicalException {
		String input = "\"a\tpc\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 6, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testCombination1() throws LexicalException {
		String input = "\f//#**Comment**#\n_abc$ image\t9011\rtrue\"stringlit\";>=?";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER,17, 5, 2, 1);
		checkNext(scanner, KW_image,23, 5, 2, 7);
		checkNext(scanner, INTEGER_LITERAL,29, 4, 2, 13);
		checkNext(scanner, BOOLEAN_LITERAL,34, 4, 3, 1);
		checkNext(scanner, STRING_LITERAL,38, 11, 3, 5);
		checkNext(scanner, SEMI,49, 1, 3, 16);
		checkNext(scanner, OP_GE,50, 2, 3, 17);
		checkNext(scanner, OP_Q,52, 1, 3, 19);		
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteralValidEscSeq() throws LexicalException {
		String input = "\"ab\\'bc\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL,0, 8, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteralandIdentifier() throws LexicalException {
		String input = "ga\"apple\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 2, 1, 1);
		checkNext(scanner, STRING_LITERAL,2, 7, 1, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteralValidEscSeq2() throws LexicalException {
		String input = "ab;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 2, 1, 1);
		checkNext(scanner, SEMI,2,1, 1, 3);
//		checkNext(scanner, KW_A,2, 1, 1, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteralValidEscSeq3() throws LexicalException {
		String input = "/ /// Hoping this is /// still in comment. \r\n / //";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_DIV, 0, 1, 1, 1);
		checkNext(scanner, OP_DIV, 46, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testComment2() throws LexicalException {
		String input = "//Checking comment\r\n%123";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_MOD, 20, 1, 2, 1);
		checkNext(scanner, INTEGER_LITERAL, 21, 3, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testComment3() throws LexicalException {
		String input = "       \r    \n";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testKw() throws LexicalException {
		String input = "boolean image atan cos y Y z Z a A r R";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_boolean,0,7,1,1);
		checkNext(scanner, KW_image,8,5,1,9);
		checkNext(scanner, KW_atan,14, 4, 1, 15);
		checkNext(scanner, KW_cos, 19, 3, 1, 20);
		checkNext(scanner, KW_y, 23,1,1,24);
		checkNext(scanner, KW_Y,25,1,1,26);
		checkNext(scanner, IDENTIFIER,27,1,1,28);
		checkNext(scanner, KW_Z,29,1,1,30);
		checkNext(scanner, KW_a,31,1,1,32);
		checkNext(scanner, KW_A,33,1,1,34);
		checkNext(scanner, KW_r,35,1,1,36);
		checkNext(scanner, KW_R,37,1,1,38);
		checkNextIsEOF(scanner);
	}
	
	
	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it a String literal
	 * that is missing the closing ".  
	 * 
	 * Note that the outer pair of quotation marks delineate the String literal
	 * in this test program that provides the input to our Scanner.  The quotation
	 * mark that is actually included in the input must be escaped, \".
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failUnclosedStringLiteral() throws LexicalException {
//		String input = /* "\" greetings  "; "(\"\"a\")";*/"beginIdent // comment has illegal char ^#~ is totally legal \n endIdent";
//		String input = "(\"\\a\")";
		String input = "\" greetings  ";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  
			show(e);
			assertEquals(13,e.getPos());
			throw e;
		}
	}


}
