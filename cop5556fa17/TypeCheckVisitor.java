package cop5556fa17;


import cop5556fa17.AST.*;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}
		
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
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		if(st.lookupType(declaration_Variable.name) != null) {
			throw new SemanticException(declaration_Variable.firstToken, "Already Taken name");
		}else {
			if(declaration_Variable.e != null) declaration_Variable.e.visit(this, arg);
			st.insert(declaration_Variable.name, declaration_Variable);
			if(declaration_Variable.token.kind == Kind.KW_int) {
				declaration_Variable.type = Type.INTEGER;
			}
			else if(declaration_Variable.token.kind == Kind.KW_boolean) {
				declaration_Variable.type = Type.BOOLEAN;
			} else if(declaration_Variable.token.kind == Kind.KW_file) {
				declaration_Variable.type = Type.FILE;
			}
			if (declaration_Variable.e != null) {
				if (declaration_Variable.e.type != declaration_Variable.type) {
					throw new SemanticException(declaration_Variable.firstToken, "Type mismatch");
				}
			}
			return declaration_Variable.type;
		}
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);

		if (expression_Binary.e0.type == expression_Binary.e1.type) {
			if (expression_Binary.op == Kind.OP_EQ || expression_Binary.op == Kind.OP_NEQ) {
				expression_Binary.type = Type.BOOLEAN;
			}
			else if ( 	(
							expression_Binary.op == Kind.OP_GE ||
							expression_Binary.op == Kind.OP_GT ||
							expression_Binary.op == Kind.OP_LT ||
							expression_Binary.op == Kind.OP_LE
 						) && expression_Binary.e0.type == Type.INTEGER
					) {
				expression_Binary.type = Type.BOOLEAN;
			} else if (	(
							expression_Binary.op == Kind.OP_AND ||
							expression_Binary.op == Kind.OP_OR
						) && (
							expression_Binary.e0.type == Type.INTEGER ||
							expression_Binary.e0.type == Type.BOOLEAN
						)
					) {
				expression_Binary.type = expression_Binary.e0.type;
			} else if (	(
							expression_Binary.op == Kind.OP_DIV ||
							expression_Binary.op == Kind.OP_MINUS ||
							expression_Binary.op == Kind.OP_MOD ||
							expression_Binary.op == Kind.OP_PLUS ||
							expression_Binary.op == Kind.OP_POWER ||
							expression_Binary.op == Kind.OP_TIMES
						) && expression_Binary.e0.type == Type.INTEGER
					  ) {
				expression_Binary.type = Type.INTEGER;
			} else {
				throw new SemanticException(expression_Binary.firstToken, "Type mismatch");
			}
		} else {
			throw new SemanticException(expression_Binary.firstToken, "Type mismatch");
		}
		return expression_Binary.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		expression_Unary.e.visit(this, arg);

		if((expression_Unary.op == Kind.OP_EXCL) && (expression_Unary.e.type == TypeUtils.Type.BOOLEAN || expression_Unary.e.type == TypeUtils.Type.INTEGER)) {
			expression_Unary.type = expression_Unary.e.type;
		}else if((expression_Unary.op == Kind.OP_MINUS || expression_Unary.op == Kind.OP_PLUS) && expression_Unary.e.type == TypeUtils.Type.INTEGER) {
			expression_Unary.type = TypeUtils.Type.INTEGER;
		}else if(expression_Unary.op == Kind.LPAREN) {
			expression_Unary.type = expression_Unary.e.type;
		}else {
			throw new SemanticException(expression_Unary.firstToken, "Type mismatch");
		}
		return expression_Unary.type;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if (index.e0.type == Type.INTEGER && index.e1.type == Type.INTEGER) {
			if (index.e0 instanceof Expression_PredefinedName && index.e1 instanceof Expression_PredefinedName) {
				index.setCartesian( !(((Expression_PredefinedName)index.e0).kind == Kind.KW_r && ((Expression_PredefinedName)index.e1).kind == Kind.KW_a));
			} else {
				index.setCartesian(true);
			}
			return index.isCartesian();
		} else {
			throw new SemanticException(index.firstToken, "Index must be an integer");
		}
		//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		if (expression_PixelSelector.index != null) expression_PixelSelector.index.visit(this, arg);
		ASTNode astNode = st.lookupType(expression_PixelSelector.name);
		if (astNode != null) {
			if (astNode.type == Type.IMAGE) {
				expression_PixelSelector.type = Type.INTEGER;
				return expression_PixelSelector.type;
			} else if (expression_PixelSelector.index == null) {
				expression_PixelSelector.type = astNode.type;
				return expression_PixelSelector.type;
			} else {
				throw new SemanticException(expression_PixelSelector.firstToken, "Type mismatch");
			}
		} else {
			throw new SemanticException(expression_PixelSelector.firstToken, "Variable undeclared");
		}
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		expression_Conditional.condition.visit(this, arg);
		expression_Conditional.trueExpression.visit(this, arg);
		expression_Conditional.falseExpression.visit(this, arg);

		if(expression_Conditional.condition.type == TypeUtils.Type.BOOLEAN && expression_Conditional.trueExpression.type == expression_Conditional.falseExpression.type) {
			expression_Conditional.type = expression_Conditional.trueExpression.type;
			return expression_Conditional.type;
		}
		else {
			throw new SemanticException(expression_Conditional.firstToken, "Type mismatch");
		}
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		if(st.lookupType(declaration_Image.name) == null) {
			st.insert(declaration_Image.name, declaration_Image);
			declaration_Image.type = TypeUtils.Type.IMAGE;
			if (declaration_Image.xSize != null) {
				if (declaration_Image.ySize != null) {
					declaration_Image.ySize.visit(this, arg);
					declaration_Image.xSize.visit(this, arg);
					if (declaration_Image.xSize.type != Type.INTEGER || declaration_Image.ySize.type != Type.INTEGER) {
						throw new SemanticException(declaration_Image.firstToken, "Must be integer type");
					}
				} else {
					throw new SemanticException(declaration_Image.firstToken, "Need y size");
				}
			} else if (declaration_Image.ySize != null) {
				throw new SemanticException(declaration_Image.firstToken,"need x size");
			}
			if (declaration_Image.source != null) {
				declaration_Image.source.visit(this, arg);
			}
			return declaration_Image.type;
		}else
		throw new SemanticException(declaration_Image.firstToken, "Already Taken name");
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		if (isValidURL(source_StringLiteral.fileOrUrl)) {
			source_StringLiteral.type = Type.URL;
		} else {
			source_StringLiteral.type = Type.FILE;
		}
		return source_StringLiteral.type;

//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		source_CommandLineParam.paramNum.visit(this, arg);
		source_CommandLineParam.type = null;
		if (source_CommandLineParam.paramNum.type == Type.INTEGER) {
			return source_CommandLineParam.paramNum.type;
		} else {
			throw new SemanticException(source_CommandLineParam.firstToken, "Must be integer");
		}
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		ASTNode temp = st.lookupType(source_Ident.name);
		if (temp != null) {
			source_Ident.type = temp.type;
		} else {
			throw new SemanticException(source_Ident.firstToken, "Variable undeclared");
		}
		if (source_Ident.type == Type.FILE || source_Ident.type == Type.URL) {
			return source_Ident.type;
		} else {
			throw new SemanticException(source_Ident.firstToken, "Source must be a file or url");
		}
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		if(st.lookupType(declaration_SourceSink.name) != null) {
			throw new SemanticException(declaration_SourceSink.firstToken, "Already Taken name");
		} else {
			declaration_SourceSink.source.visit(this, arg);
			st.insert(declaration_SourceSink.name, declaration_SourceSink);
			if(declaration_SourceSink.token.kind == Kind.KW_url) {
				declaration_SourceSink.type = Type.URL;
			}
			else if(declaration_SourceSink.token.kind == Kind.KW_file) {
				declaration_SourceSink.type = Type.FILE;
			}
			if (declaration_SourceSink.source.type == declaration_SourceSink.type || declaration_SourceSink.source.type == null)
				return declaration_SourceSink.type;
			else
				throw new SemanticException(declaration_SourceSink.firstToken, "Type mismatch");
		}
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		expression_IntLit.type = TypeUtils.Type.INTEGER;
		return expression_IntLit.type;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if(expression_FunctionAppWithExprArg.arg.type == TypeUtils.Type.INTEGER) {
			expression_FunctionAppWithExprArg.type = TypeUtils.Type.INTEGER;
			return expression_FunctionAppWithExprArg.type;
		}else {
		throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "Not an Integer");
		}
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		expression_FunctionAppWithIndexArg.type = TypeUtils.Type.INTEGER;
		return expression_FunctionAppWithIndexArg.type;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		expression_PredefinedName.type = TypeUtils.Type.INTEGER;
		return expression_PredefinedName.type;
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		statement_Out.sink.visit(this, arg);
		Declaration temp = st.lookupType(statement_Out.name);
		statement_Out.setDec(temp);
		if  ( 	(temp != null) &&
				( 	(	(temp.type == Type.INTEGER || temp.type == Type.BOOLEAN) && statement_Out.sink.type == Type.SCREEN) ||
					(temp.type == Type.IMAGE && (statement_Out.sink.type == Type.FILE || statement_Out.sink.type == Type.SCREEN))
			  	)
			) {
			statement_Out.type = temp.type;
			return statement_Out.type;
		} else {
			throw new SemanticException(statement_Out.firstToken, "Variable undeclared");
		}

//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		statement_In.source.visit(this, arg);
		statement_In.setDec(st.lookupType(statement_In.name));
		if (statement_In.getDec() == null) {
			throw new SemanticException(statement_In.firstToken, "Variable undeclared");
		} else {
			statement_In.type = statement_In.getDec().type;
		}
		return statement_In.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		statement_Assign.lhs.visit(this, arg);
		statement_Assign.e.visit(this, arg);
		if (statement_Assign.lhs.type == Type.IMAGE && statement_Assign.e.type == Type.INTEGER && statement_Assign.lhs.index != null) {
			statement_Assign.setCartesian(statement_Assign.lhs.index.isCartesian());
			statement_Assign.type = statement_Assign.lhs.type;
			return statement_Assign.isCartesian();
		}
		else if (statement_Assign.lhs.type == statement_Assign.e.type) {
			if (statement_Assign.lhs.index != null) {
				statement_Assign.setCartesian(statement_Assign.lhs.index.isCartesian());
			}
			statement_Assign.type = statement_Assign.lhs.type;
			return statement_Assign.isCartesian();
		} else {
			throw new SemanticException(statement_Assign.firstToken, "Type mismatch");
		}

//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		if (lhs.index != null) lhs.index.visit(this, arg);
		ASTNode symb = st.lookupType(lhs.name);
		if (symb != null) {
			lhs.type = symb.type;
			return lhs.type;
		} else {
			throw new SemanticException(lhs.firstToken, "Variable undeclared");
		}

//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		sink_SCREEN.type = Type.SCREEN;
		return sink_SCREEN.type;
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		ASTNode temp = st.lookupType(sink_Ident.name);
		if (temp != null) {
			if (temp.type == Type.FILE) {
				sink_Ident.type = temp.type;
				return sink_Ident.type;
			} else {
				throw new SemanticException(sink_Ident.firstToken, "Must be file type");
			}
		} else {
			throw new SemanticException(sink_Ident.firstToken, "Variable undeclared");
		}
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		expression_BooleanLit.type = TypeUtils.Type.BOOLEAN;
		return expression_BooleanLit.type;
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		ASTNode astNode = st.lookupType(expression_Ident.name);
		if (astNode != null) {
			expression_Ident.type = astNode.type;
			return expression_Ident.type;
		} else {
			throw new SemanticException(expression_Ident.firstToken, "Variable undeclared");
		}
	}

	private boolean isValidURL(String url) {

		URL u = null;

		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}

		return true;
	}
}