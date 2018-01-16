package cop5556fa17;

import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa17.AST.ASTNode;
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
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Statement;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	Token consume() {
		t = scanner.nextToken();
		return t;
	}

	public Token match(Kind kind) throws SyntaxException {
	    Token matched = t;
		if (t.kind.equals(kind)) {
			consume();
			return matched;
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
	}
	
	Token statementToken = null;
	/**
	 * Main method called by compiler to parser input. Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	/**
	 * Program ::= IDENTIFIER ( Declaration SEMI | Statement SEMI )*
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	// void program() throws SyntaxException {
	// // t.kind.equals(IDENTIFIER);
	// throw new UnsupportedOperationException();
	// }

	Program program() throws SyntaxException {
		ArrayList<ASTNode> decsAndStatements = new ArrayList<>();
		Token firstToken = t;
		match(IDENTIFIER);
		while (t.kind.equals(KW_int) || t.kind.equals(KW_boolean) || t.kind.equals(KW_image) || t.kind.equals(KW_url)
				|| t.kind.equals(KW_file) || t.kind.equals(IDENTIFIER)) {
			if (t.kind.equals(KW_int) || t.kind.equals(KW_boolean) || t.kind.equals(KW_image) || t.kind.equals(KW_url)
					|| t.kind.equals(KW_file)) {
				decsAndStatements.add(declaration());
				match(SEMI);
			} else if (t.kind.equals(IDENTIFIER)) {
				decsAndStatements.add(statement());
				match(SEMI);
			} else
				throw new SyntaxException(t, "Exception because of " + t.kind);
		}
		return new Program(firstToken, firstToken, decsAndStatements);
	}

	 Declaration declaration() throws SyntaxException {
		if (t.kind.equals(KW_int) || t.kind.equals(KW_boolean)) {
			return VariableDeclaration();
		} else if (t.kind.equals(KW_image)) {
			return ImageDeclaration();
		} else if (t.kind.equals(KW_url) || t.kind.equals(KW_file)) {
			return SourceSinkDeclaration();
		} else
			throw new SyntaxException(t, "Exception because of " + t.kind);
	}

	Declaration VariableDeclaration() throws SyntaxException {
		Token firstToken = t;   
		Token name =  null; 
		Expression e = null;
		
		if (t.kind.equals(KW_int) || t.kind.equals(KW_boolean)) {
			VarType();
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
		name = t;
		match(IDENTIFIER);
		if (t.kind.equals(OP_ASSIGN)) {
			if (t.kind.equals(OP_ASSIGN)) {
				match(OP_ASSIGN);
				e = expression();//after writing the method expression
			} else {
				throw new SyntaxException(t, "Exception because of " + t.kind);
			}
		} else {
		}
		
		return new Declaration_Variable(firstToken, firstToken, name, e);
	}

	void VarType() throws SyntaxException {
		if (t.kind.equals(KW_int)) {
			match(KW_int);
		} else if (t.kind.equals(KW_boolean)) {
			match(KW_boolean);
		} else
			throw new SyntaxException(t, "Not VarType");
	}

	Declaration SourceSinkDeclaration() throws SyntaxException {
		Token firstToken = t; Token name = null; Source source = null;
		if (t.kind.equals(KW_url) || t.kind.equals(KW_file)) {
			SourceSinkType();
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
		name = t;
		match(IDENTIFIER);
		match(OP_ASSIGN);
		if (t.kind.equals(STRING_LITERAL) || t.kind.equals(OP_AT) || t.kind.equals(IDENTIFIER)) {
			source = source();
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
		return new Declaration_SourceSink(firstToken, firstToken, name, source);
	}

	 Source source() throws SyntaxException {
		Token firstToken = t;
		String fileOrUrl = "";
		Expression paramNum = null;
		Token op = null;
		if (t.kind.equals(STRING_LITERAL)) {
			fileOrUrl = t.getText();
			match(STRING_LITERAL);
			return new Source_StringLiteral(firstToken, fileOrUrl);
		} else if (t.kind.equals(OP_AT)) {
			match(OP_AT);
			paramNum = expression();
			return new Source_CommandLineParam(firstToken, paramNum);
		} else if (t.kind.equals(IDENTIFIER)) {
			op = t;
			match(IDENTIFIER);
			return new Source_Ident(firstToken, op);
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
	}

	void SourceSinkType() throws SyntaxException {
		if (t.kind.equals(KW_url)) {
			match(KW_url);
		} else if (t.kind.equals(KW_file)) {
			match(KW_file);
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
	}

	Declaration ImageDeclaration() throws SyntaxException {
		Token firstToken = t;
		Source source = null; Token name = null; Expression xSize = null; Expression ySize = null;
		match(KW_image);
		if (t.kind.equals(LSQUARE)) {
			if (t.kind.equals(LSQUARE)) {
				match(LSQUARE);
				xSize = expression();
				match(COMMA);
				ySize = expression();
				match(RSQUARE);
			}
		}
		name = t;
		match(IDENTIFIER);
		
		if (t.kind.equals(OP_LARROW)) {
			if (t.kind.equals(OP_LARROW)) {
				match(OP_LARROW);
				source = source();
			} else {
				throw new SyntaxException(t, "Exception because of " + t.kind);
			}
		}
			return new Declaration_Image(firstToken, xSize, ySize, name, source);
	}

	Statement statement() throws SyntaxException {
		statementToken = t;
		Token ident = match(IDENTIFIER);
		if (t.kind.equals(OP_LARROW)) {
			return ImageInStatement();
		} else if (t.kind.equals(OP_RARROW)) {
			return ImageOutStatement();
		} else if (t.kind.equals(OP_ASSIGN) || t.kind.equals(LSQUARE)) {
			return AssignmentStatement();
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
	}

	Statement AssignmentStatement() throws SyntaxException {
		LHS lhs = Lhs();
		Expression e = null;
//		if (t.kind.equals(LSQUARE)) {
//			match(LSQUARE);
//			LhsSelector();
//			if(t.kind.equals(RSQUARE)) {
//				match(RSQUARE);
//			}
//			else {
//				throw new SyntaxException(t, "Exception because of "+t.kind);
//			}
//		}
		if(t.kind.equals(OP_ASSIGN)) {
			match(OP_ASSIGN);
			e = expression();
		}
		else {
			throw new SyntaxException(t, "Exception because of "+t.kind);
		}
		return new Statement_Assign(lhs.firstToken, lhs, e);
	}

	Statement ImageInStatement() throws SyntaxException {
		Source source = null;
		match(OP_LARROW);
		source = source();
		return new Statement_In(statementToken, statementToken, source);
	}

	Statement ImageOutStatement() throws SyntaxException {
		Sink sink = null;
		match(OP_RARROW);
		sink = sink();
		return new Statement_Out(statementToken, statementToken, sink);
	}

	Sink sink() throws SyntaxException {
		Token firstToken = t;
		if (t.kind.equals(IDENTIFIER)) {
			match(IDENTIFIER);
			return new Sink_Ident(firstToken, firstToken);
		} else if (t.kind.equals(KW_SCREEN)) {
			match(KW_SCREEN);
			return new Sink_SCREEN(firstToken);
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
	}

	/**
	 * Expression ::= OrExpression OP_Q Expression OP_COLON Expression |
	 * OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental
	 * development.
	 * 
	 * @throws SyntaxException
	 */
	// void expression() throws SyntaxException {
	// // TODO implement this.
	// throw new UnsupportedOperationException();
	// }

	Index XySelector() throws SyntaxException {
        Token firstToken = t;       
        Expression e0 = new Expression_PredefinedName(firstToken, KW_x);
        match(KW_x);        
        match(COMMA);
        Expression e1 = new Expression_PredefinedName(firstToken, KW_y);
        match(KW_y);
        return new Index(firstToken, e0, e1);
	}

	Index RaSelector() throws SyntaxException {
		Token firstToken = t;		
		Expression e0 = new Expression_PredefinedName(firstToken, KW_r);
		match(KW_r);		
		match(COMMA);
		Expression e1 = new Expression_PredefinedName(firstToken, KW_a);
		match(KW_a);
		return new Index(firstToken, e0, e1);
	}

	Index LhsSelector() throws SyntaxException {
		Token firstToken = t;
		Index index = null;
		match(LSQUARE);
		if (t.kind.equals(KW_x)) {
			index = XySelector();
		} else if (t.kind.equals(KW_r)) {
		    index = RaSelector();
		} else {
			throw new SyntaxException(t, "Exception because of" + t.kind);
		}
		match(RSQUARE);
		return new Index(firstToken, index.e0, index.e1);
	}

	Token FunctionName() throws SyntaxException {
		if (t.kind.equals(KW_sin)) {
			return match(KW_sin);
		} else if (t.kind.equals(KW_cos)) {
		    return match(KW_cos);
		} else if (t.kind.equals(KW_atan)) {
		    return match(KW_atan);
		} else if (t.kind.equals(KW_abs)) {
		    return match(KW_abs);
		} else if (t.kind.equals(KW_cart_x)) {
		    return match(KW_cart_x);
		} else if (t.kind.equals(KW_cart_y)) {
		    return match(KW_cart_y);
		} else if (t.kind.equals(KW_polar_a)) {
		    return match(KW_polar_a);
		} else if (t.kind.equals(KW_polar_r)) {
		    return match(KW_polar_r);
		} else {
			throw new SyntaxException(t, "Exception because of" + t.kind);
		}
	}

	LHS Lhs() throws SyntaxException {
		Token firstToken = statementToken;
		Index index = null;

		if (t.kind.equals(LSQUARE)) {
			if (t.kind.equals(LSQUARE)) {
				match(LSQUARE);
				index = LhsSelector();
				match(RSQUARE);
			} else {
				throw new SyntaxException(t, "Exception because of " + t.kind);
			}
		}
		return new LHS(firstToken, firstToken, index);
	}

	Expression expression() throws SyntaxException {
		Expression condition = null; 
		Expression trueExpression = null;
		Expression falseExpression = null;
		
		condition = OrExpression();
		if (t.kind.equals(OP_Q)) {
			if (t.kind.equals(OP_Q)) {
				match(OP_Q);
				trueExpression = expression();
				match(OP_COLON);
				falseExpression = expression();
				return new Expression_Conditional(condition.firstToken, condition, trueExpression, falseExpression);
			} else {
				throw new SyntaxException(t, "Exception because of" + t.kind);
			}
		}
		return condition;
	}

	Expression OrExpression() throws SyntaxException {
		Expression e0 = null; 
		Token op = null; 
		Expression e1 = null;
		e0 = AndExpression();
		while (t.kind.equals(OP_OR)) {
			op = t;
			match(OP_OR);
			e1 = AndExpression();
			e0 = new Expression_Binary(e0.firstToken, e0, op, e1);
		}
		return e0;
	}

	Expression AndExpression() throws SyntaxException {
		Expression e0 = null; 
		Token op = null; 
		Expression e1 = null;
		e0 = EqExpression();
		while (t.kind.equals(OP_AND)) {
			op = t;
			match(OP_AND);
			e1 = EqExpression();
			e0 = new Expression_Binary(e0.firstToken, e0, op, e1);
		}
		return e0;
	}

	Expression EqExpression() throws SyntaxException { 
		Expression e0 = null; 
		Token op = null; 
		Expression e1 = null;
		e0 = RelExpression();
		while (t.kind.equals(OP_EQ) || t.kind.equals(OP_NEQ)) {
			if (t.kind.equals(OP_EQ)) {
				op = t;
				match(OP_EQ);	
			} else if (t.kind.equals(OP_NEQ)) {
				op = t;
				match(OP_NEQ);
			} else {
				throw new SyntaxException(t, "Exception because of " + t.kind);
			}
			e1 = RelExpression();
			e0 = new Expression_Binary(e0.firstToken, e0, op, e1);
		}
		return e0;
	}

	Expression RelExpression() throws SyntaxException { 
		Expression e0 = null; 
		Token op = null; 
		Expression e1 = null;
		e0 = AddExpression();
		while (t.kind.equals(OP_LT) || t.kind.equals(OP_GT) || t.kind.equals(OP_GE) || t.kind.equals(OP_LE)) {
			if (t.kind.equals(OP_LT)) {
				op = t;
				match(OP_LT);
			} else if (t.kind.equals(OP_GT)) {
				op = t;
				match(OP_GT);
			} else if (t.kind.equals(OP_LE)) {
				op = t;
				match(OP_LE);
			} else if (t.kind.equals(OP_GE)) {
				op = t;
				match(OP_GE);
			} else {
				throw new SyntaxException(t, "EXception because of " + t.kind);
			}
			e1 = AddExpression();
			e0 = new Expression_Binary(e0.firstToken, e0, op, e1);
		}
		return e0;
	}

	Expression AddExpression() throws SyntaxException {
		Expression e0 = null; 
		Token op = null; 
		Expression e1 = null;
		e0 = MultExpression();
		while (t.kind.equals(OP_PLUS) || t.kind.equals(OP_MINUS)) {
			if (t.kind.equals(OP_PLUS)) {
				op = t;
				match(OP_PLUS);
			} else if (t.kind.equals(OP_MINUS)) {
				op = t;
				match(OP_MINUS);
			} else {
				throw new SyntaxException(t, "Exception because of " + t.kind);
			}
			e1 = MultExpression();
			e0 = new Expression_Binary(e0.firstToken, e0, op, e1);
		}
		return e0;		
	}

	Expression MultExpression() throws SyntaxException { 
		Expression e0 = null; 
		Token op = null; 
		Expression e1 = null;
		e0 = UnaryExpression();
		while (t.kind.equals(OP_TIMES) || t.kind.equals(OP_DIV) || t.kind.equals(OP_MOD)) {
			if (t.kind.equals(OP_TIMES)) {
				op = t;
				match(OP_TIMES);
			} else if (t.kind.equals(OP_DIV)) {
				op = t;
				match(OP_DIV);
			} else if (t.kind.equals(OP_MOD)) {
				op = t;
				match(OP_MOD);
			} else {
				throw new SyntaxException(t, "Exception because of " + t.kind);
			}
			e1 = UnaryExpression();
			e0 = new Expression_Binary(e0.firstToken, e0, op, e1);
		}
		return e0;
	}

	Expression UnaryExpression() throws SyntaxException { 
		Token op = null; 
		Expression e = null;
		if (t.kind.equals(OP_PLUS)) {
			op = t;
			match(OP_PLUS);
			e = UnaryExpression();
			return new Expression_Unary(op, op, e);
		} else if (t.kind.equals(OP_MINUS)) {
			op = t;
			match(OP_MINUS);
			e = UnaryExpression();
			return new Expression_Unary(op, op, e);
		} else if (t.kind.equals(OP_EXCL) || t.kind.equals(IDENTIFIER) || t.kind.equals(KW_x) || t.kind.equals(KW_y) || t.kind.equals(KW_r) || t.kind.equals(KW_a) || t.kind.equals(KW_X) || t.kind.equals(KW_Y) || t.kind.equals(KW_Z) || t.kind.equals(KW_A) || t.kind.equals(KW_R) || t.kind.equals(KW_DEF_X) || t.kind.equals(KW_DEF_Y) || t.kind.equals(INTEGER_LITERAL) || t.kind.equals(LPAREN) || t.kind.equals(BOOLEAN_LITERAL) || t.kind.equals(KW_sin) || t.kind.equals(KW_cos) || t.kind.equals(KW_atan) || t.kind.equals(KW_abs) || t.kind.equals(KW_cart_x) || t.kind.equals(KW_cart_y) || t.kind.equals(KW_polar_a) || t.kind.equals(KW_polar_r)) {
			return UnaryExpressionNotPlusMinus();
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
	}

	Expression UnaryExpressionNotPlusMinus() throws SyntaxException {
		Token firstToken = t; 
		Expression e = null;
		if (t.kind.equals(OP_EXCL)) {
			match(OP_EXCL);
			e = UnaryExpression();
			return new Expression_Unary(firstToken, firstToken, e);
		} else if (t.kind.equals(INTEGER_LITERAL)) {
			return Primary();
		} else if (t.kind.equals(LPAREN)) {
			return Primary();
		} else if (t.kind.equals(BOOLEAN_LITERAL)) {
			return Primary();
		} else if (t.kind.equals(KW_sin)) {
			return Primary();
		} else if (t.kind.equals(KW_cos)) {
			return Primary();
		} else if (t.kind.equals(KW_atan)) {
			return Primary();
		} else if (t.kind.equals(KW_abs)) {
			return Primary();
		} else if (t.kind.equals(KW_cart_x)) {
			return Primary();
		} else if (t.kind.equals(KW_cart_y)) {
			return Primary();
		} else if (t.kind.equals(KW_polar_a)) {
			return Primary();
		} else if (t.kind.equals(KW_polar_r)) {
			return Primary();
		} else if (t.kind.equals(KW_x)) {
			match(KW_x);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(KW_y)) {
			match(KW_y);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(KW_r)) {
			match(KW_r);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(KW_a)) {
			match(KW_a);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(KW_X)) {
			match(KW_X);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(KW_Y)) {
			match(KW_Y);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(KW_Z)) {
			match(KW_Z);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(KW_A)) {
			match(KW_A);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(KW_R)) {
			match(KW_R);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(KW_DEF_X)) {
			match(KW_DEF_X);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(KW_DEF_Y)) {
			match(KW_DEF_Y);
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind.equals(IDENTIFIER)) {
			return IdentOrPixelSelectorExpression();
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
	}

	Expression Primary() throws SyntaxException {
		Token firstToken = t; 
		if (t.kind.equals(INTEGER_LITERAL)) {
		    int value = t.intVal();
			match(INTEGER_LITERAL);
			return new Expression_IntLit(firstToken, value);
		} else if (t.kind.equals(LPAREN)) {
			match(LPAREN);
			Expression e = expression();
			match(RPAREN);
			return new Expression_Unary(firstToken, firstToken, e);
		} else if (t.kind.equals(KW_sin)) {
			return FunctionApplication();
		} else if (t.kind.equals(KW_cos)) {
			return FunctionApplication();
		} else if (t.kind.equals(KW_atan)) {
			return FunctionApplication();
		} else if (t.kind.equals(KW_abs)) {
			return FunctionApplication();
		} else if (t.kind.equals(KW_cart_x)) {
			return FunctionApplication();
		} else if (t.kind.equals(KW_cart_y)) {
			return FunctionApplication();
		} else if (t.kind.equals(KW_polar_a)) {
			return FunctionApplication();
		} else if (t.kind.equals(KW_polar_r)) {
			return FunctionApplication();
		} else if (t.kind.equals(BOOLEAN_LITERAL)) {
			Token matched = match(BOOLEAN_LITERAL);
			boolean value2 = Boolean.parseBoolean(matched.getText());
			return new Expression_BooleanLit(firstToken, value2);
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
	}

	Expression IdentOrPixelSelectorExpression() throws SyntaxException {
		Token ident = match(IDENTIFIER);
		if (t.kind.equals(LSQUARE)) {
			if (t.kind.equals(LSQUARE)) {
				match(LSQUARE);
				Index index = Selector();
				match(RSQUARE);
				return new Expression_PixelSelector(ident, ident, index);
			} else {
				throw new SyntaxException(t, "Exception because of " + t.kind);
			}
		}
		return new Expression_Ident(ident, ident);
	}

	Expression FunctionApplication() throws SyntaxException {
		Token op = null;
		Expression arg = null;
		Index i = null;
		op = FunctionName();
		if (t.kind.equals(LPAREN)) {
			match(LPAREN);
			arg = expression();
			match(RPAREN);
			return new Expression_FunctionAppWithExprArg(op, op.kind, arg);
		} else if (t.kind.equals(LSQUARE)) {
			match(LSQUARE);
			i = Selector();
			match(RSQUARE);
			return new Expression_FunctionAppWithIndexArg(op, op.kind, i);
		} else {
			throw new SyntaxException(t, "Exception because of " + t.kind);
		}
	}

	Index Selector() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		e0 = expression();
		match(COMMA);
		e1 = expression();
		return new Index(e0.firstToken, e0, e1);
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}