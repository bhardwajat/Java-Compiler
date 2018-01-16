/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Scanner {

	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {

		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}

		public int getPos() {
			return pos;
		}

	}

	public static enum State {

		START, AFTER_EQ, IN_IDENT, IN_DIGIT, IN_SLIT, IN_CMNT
	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/*
																																 * r
																																 */, KW_R/*
																																			 * R
																																			 */, KW_a/*
																																						 * a
																																						 */, KW_A/*
																																									 * A
																																									 */, KW_Z/*
																																												 * Z
																																												 */, KW_DEF_X/*
																																																 * DEF_X
																																																 */, KW_DEF_Y/*
																																																				 * DEF_Y
																																																				 */, KW_SCREEN/*
																																																								 * SCREEN
																																																								 */, KW_cart_x/*
																																																												 * cart_x
																																																												 */, KW_cart_y/*
																																																																 * cart_y
																																																																 */, KW_polar_a/*
																																																																				 * polar_a
																																																																				 */, KW_polar_r/*
																																																																								 * polar_r
																																																																								 */, KW_abs/*
																																																																											 * abs
																																																																											 */, KW_sin/*
																																																																														 * sin
																																																																														 */, KW_cos/*
																																																																																	 * cos
																																																																																	 */, KW_atan/*
																																																																																				 * atan
																																																																																				 */, KW_log/*
																																																																																							 * log
																																																																																							 */, KW_image/*
																																																																																											 * image
																																																																																											 */, KW_int/*
																																																																																														 * int
																																																																																														 */, KW_boolean/*
																																																																																																		 * boolean
																																																																																																		 */, KW_url/*
																																																																																																					 * url
																																																																																																					 */, KW_file/*
																																																																																																								 * file
																																																																																																								 */, OP_ASSIGN/*
																																																																																																												 * =
																																																																																																												 */, OP_GT/*
																																																																																																															 * >
																																																																																																															 */, OP_LT/*
																																																																																																																		 * <
																																																																																																																		 */, OP_EXCL/*
																																																																																																																					 * !
																																																																																																																					 */, OP_Q/*
																																																																																																																								 * ?
																																																																																																																								 */, OP_COLON/*
																																																																																																																												 * :
																																																																																																																												 */, OP_EQ/*
																																																																																																																															 * ==
																																																																																																																															 */, OP_NEQ/*
																																																																																																																																		 * !=
																																																																																																																																		 */, OP_GE/*
																																																																																																																																					 * >=
																																																																																																																																					 */, OP_LE/*
																																																																																																																																								 * <=
																																																																																																																																								 */, OP_AND/*
																																																																																																																																											 * &
																																																																																																																																											 */, OP_OR/*
																																																																																																																																														 * |
																																																																																																																																														 */, OP_PLUS/*
																																																																																																																																																	 * +
																																																																																																																																																	 */, OP_MINUS/*
																																																																																																																																																					 * -
																																																																																																																																																					 */, OP_TIMES/*
																																																																																																																																																									 * *
																																																																																																																																																									 */, OP_DIV/*
																																																																																																																																																												 * /
																																																																																																																																																												 */, OP_MOD/*
																																																																																																																																																															 * %
																																																																																																																																																															 */, OP_POWER/*
																																																																																																																																																																			 * **
																																																																																																																																																																			 */, OP_AT/*
																																																																																																																																																																						 * @
																																																																																																																																																																						 */, OP_RARROW/*
																																																																																																																																																																										 * ->
																																																																																																																																																																										 */, OP_LARROW/*
																																																																																																																																																																														 * <-
																																																																																																																																																																														 */, LPAREN/*
																																																																																																																																																																																	 * (
																																																																																																																																																																																	 */, RPAREN/*
																																																																																																																																																																																				 * )
																																																																																																																																																																																				 */, LSQUARE/*
																																																																																																																																																																																							 * [
																																																																																																																																																																																							 */, RSQUARE/*
																																																																																																																																																																																										 * ]
																																																																																																																																																																																										 */, SEMI/*
																																																																																																																																																																																													 * ;
																																																																																																																																																																																													 */, COMMA/*
																																																																																																																																																																																																 * ,
																																																																																																																																																																																																 */, EOF;
	}

	/**
	 * Class to represent Tokens.
	 *
	 * This is defined as a (non-static) inner class which means that each Token
	 * instance is associated with a specific Scanner instance. We use this when
	 * some token methods access the chars array in the associated Scanner.
	 *
	 *
	 * @author Beverly Sanders
	 *
	 */
	public class Token {

		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			} else {
				return String.copyValueOf(chars, pos, length);
			}
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the enclosing "
		 * characters and convert escaped characters to the represented character. For
		 * example the two characters \ t in the char array should be converted to a
		 * single tab character in the returned String
		 *
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		public String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial
				// and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); // for completeness, line termination
						// chars not allowed in String
						// literals
						break;
					case 'n':
						sb.append('\n'); // for completeness, line termination
						// chars not allowed in String
						// literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition: This Token is an INTEGER_LITERAL
		 *
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {

			// String format = "%-20s%-10s%-10s%-10s%-10s%-10s";

			// return String.format(format, kind, String.copyValueOf(chars, pos, length),
			// pos, length, line, pos_in_line);

			return "[" + kind + "," + String.copyValueOf(chars, pos, length) + "," + pos + "," + length + "," + line
					+ "," + pos_in_line + "]";
		}

		/**
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 *
		 * Both the equals and hashCode method were generated by eclipse
		 *
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object is the same class and
		 * all fields are equal.
		 *
		 * Overriding this creates an obligation to override hashCode.
		 *
		 * Both hashCode and equals were generated by eclipse.
		 *
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (kind != other.kind) {
				return false;
			}
			if (length != other.length) {
				return false;
			}
			if (line != other.line) {
				return false;
			}
			if (pos != other.pos) {
				return false;
			}
			if (pos_in_line != other.pos_in_line) {
				return false;
			}
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is associated with.
		 *
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/**
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.
	 */
	static final char EOFchar = 0;

	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;

	/**
	 * An array of characters representing the input. These are the characters from
	 * the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input
		// string
		// terminated
		// with
		// null
		// char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
	}

	/**
	 * Method to scan the input and create a list of Tokens.
	 *
	 * If an error is encountered during scanning, throw a LexicalException.
	 *
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		/* TODO Replace this with a correct and complete implementation!!! */
		State state = State.START;

		int line = 1;
		int posInLine = 1;
		int pos = 0;
		int length = 0;

		Map<String, Kind> hm = new HashMap<>();
		hm.put("sin", Kind.KW_sin);
		hm.put("true", Kind.BOOLEAN_LITERAL);
		hm.put("false", Kind.BOOLEAN_LITERAL);
		hm.put("int", Kind.KW_int);
		hm.put("x", Kind.KW_x);
		hm.put("y", Kind.KW_y);
		hm.put("X", Kind.KW_X);
		hm.put("Y", Kind.KW_Y);
		hm.put("image", Kind.KW_image);
		hm.put("a", Kind.KW_a);
		hm.put("A", Kind.KW_A);
		hm.put("r", Kind.KW_r);
		hm.put("R", Kind.KW_R);
		hm.put("Z", Kind.KW_Z);
		hm.put("log", Kind.KW_log);
		hm.put("DEF_X", Kind.KW_DEF_X);
		hm.put("DEF_Y", Kind.KW_DEF_Y);
		hm.put("cart_x", Kind.KW_cart_x);
		hm.put("cart_y", Kind.KW_cart_y);
		hm.put("image", Kind.KW_image);
		hm.put("polar_a", Kind.KW_polar_a);
		hm.put("polar_r", Kind.KW_polar_r);
		hm.put("abs", Kind.KW_abs);
		hm.put("cos", Kind.KW_cos);
		hm.put("atan", Kind.KW_atan);
		hm.put("boolean", Kind.KW_boolean);
		hm.put("url", Kind.KW_url);
		hm.put("file", Kind.KW_file);
		hm.put("SCREEN", Kind.KW_SCREEN);
		StringBuilder sb = new StringBuilder();

		while (pos <= chars.length - 1) {
			char ch = chars[pos];
			switch (state) {
			case START: {
				ch = chars[pos];
				length = 1; // every time you are on start length will be one
				switch (ch) {
				case '(': {
					tokens.add(new Token(Kind.LPAREN, pos, 1, line, posInLine));
				}
					break;
				case ')': {
					tokens.add(new Token(Kind.RPAREN, pos, 1, line, posInLine));
				}
					break;
				case '[': {
					tokens.add(new Token(Kind.LSQUARE, pos, 1, line, posInLine));
				}
					break;
				case ']': {
					tokens.add(new Token(Kind.RSQUARE, pos, 1, line, posInLine));
				}
					break;
				case ',': {
					tokens.add(new Token(Kind.COMMA, pos, 1, line, posInLine));
				}
					break;
				case ';': {
					tokens.add(new Token(Kind.SEMI, pos, 1, line, posInLine));
				}
					break;
				case ':': {
					tokens.add(new Token(Kind.OP_COLON, pos, 1, line, posInLine));
				}
					break;
				case '|': {
					tokens.add(new Token(Kind.OP_OR, pos, 1, line, posInLine));
				}
					break;
				case '&': {
					tokens.add(new Token(Kind.OP_AND, pos, 1, line, posInLine));
				}
					break;
				case '@': {
					tokens.add(new Token(Kind.OP_AT, pos, 1, line, posInLine));
				}
					break;
				case '/': {
					if (chars[pos + 1] == '/') {
						state = State.IN_CMNT;
					} else {
						tokens.add(new Token(Kind.OP_DIV, pos, 1, line, posInLine));
					}
				}
					break;
				case '+': {
					tokens.add(new Token(Kind.OP_PLUS, pos, 1, line, posInLine));
				}
					break;
				case '?': {
					tokens.add(new Token(Kind.OP_Q, pos, 1, line, posInLine));
				}
					break;
				case '%': {
					tokens.add(new Token(Kind.OP_MOD, pos, 1, line, posInLine));
				}
					break;
				case '\n': {
					line++;
					posInLine = 0; // the position in line starts from 1 so having zero here will ingore the \n
				}
					break;
				case '=': {
					if (chars[pos + 1] == '=') {
						tokens.add(new Token(Kind.OP_EQ, pos, 2, line, posInLine));
						pos++;
						posInLine++;
					} else {
						tokens.add(new Token(Kind.OP_ASSIGN, pos, 1, line, posInLine));
					}
				}
					break;
				case '!': {
					if (chars[pos + 1] == '=') {
						tokens.add(new Token(Kind.OP_NEQ, pos, 2, line, posInLine));
						pos++;
						posInLine++;
					} else {
						tokens.add(new Token(Kind.OP_EXCL, pos, 1, line, posInLine));
					}
				}
					break;
				case '*': {
					if (chars[pos + 1] == '*') {
						tokens.add(new Token(Kind.OP_POWER, pos, 2, line, posInLine));
						pos++;
						posInLine++;
					} else {
						tokens.add(new Token(Kind.OP_TIMES, pos, 1, line, posInLine));
					}
				}
					break;
				case '<': {
					if (chars[pos + 1] == '=') {
						tokens.add(new Token(Kind.OP_LE, pos, 2, line, posInLine));
						pos++;
						posInLine++;
					} else if (chars[pos + 1] == '-') {
						tokens.add(new Token(Kind.OP_LARROW, pos, 2, line, posInLine));
						pos++;
						posInLine++;
					} else {
						tokens.add(new Token(Kind.OP_LT, pos, 1, line, posInLine));
					}
				}
					break;
				case '>': {
					if (chars[pos + 1] == '=') {
						tokens.add(new Token(Kind.OP_GE, pos, 2, line, posInLine));
						pos++;
						posInLine++;
					} else {
						tokens.add(new Token(Kind.OP_GT, pos, 1, line, posInLine));
					}
				}
					break;
				case '-': {
					if (chars[pos + 1] == '>') {
						tokens.add(new Token(Kind.OP_RARROW, pos, 2, line, posInLine));
						pos++;
						posInLine++;
					}
					else {
						tokens.add(new Token(Kind.OP_MINUS, pos, 1, line, posInLine));
					}
				}
					break;
				case '\r': {
					if (chars[pos + 1] == '\n') {
						pos++;
						posInLine = 0;
						line--;
					}
					line++;
					posInLine = 0; // the position in line starts from 1 so having zero here will ingore the \n
				}
					break;
				case EOFchar: {

				}
					break;
				case ' ': {

				}
					break;
				default: {
					if (Character.isJavaIdentifierStart(ch)) {
						sb = sb.append(Character.toString(ch));
						state = State.IN_IDENT;
					} else if (Character.isDigit(ch)) {
						if (ch == '0') {
							tokens.add(new Token(Kind.INTEGER_LITERAL, pos, 1, line, posInLine));

						} else {
							state = State.IN_DIGIT;
						}
					} else if (ch == '\"') {
						state = State.IN_SLIT;
					} else if(ch == '~' || ch == '^' || ch == '#') {
						throw new LexicalException("Not a valid character", pos);
					}
				}
				}
			}
				break;
			case IN_IDENT: {
				if ((Character.isWhitespace(chars[pos]) || pos == chars.length - 1 || !(Character.isJavaIdentifierStart(ch) || Character.isDigit(ch)) ) && hm.containsKey(sb.toString())) {
					tokens.add(new Token(hm.get(sb.toString()), pos - 1, 1, line, posInLine - 1));
					sb.setLength(0);
					pos--;posInLine--;
					state = State.START;
				} 
				else {
					if (Character.isJavaIdentifierStart(ch) || Character.isDigit(ch)) {
						sb = sb.append(Character.toString(ch));
						if (hm.containsKey(sb.toString()) && ((Character.isWhitespace(chars[pos + 1]))
								|| pos + 1 == chars.length - 1 || !(Character.isJavaIdentifierPart(chars[pos + 1])))) {
							tokens.add(new Token(hm.get(sb.toString()), pos - length, length + 1, line,
									posInLine - length));
							sb.setLength(0);
							state = State.START;
						}
						length++;

					} else {
						tokens.add(new Token(Kind.IDENTIFIER, pos - length, length, line, posInLine - length));
						sb.setLength(0);
						pos--;
						posInLine--;
						state = State.START;

					}
				}
			}
				break;
			case IN_DIGIT: {
				if (Character.isDigit(ch)) {
					length++;

				} else {
					try {
						if ((Integer.valueOf(String.copyValueOf(chars, pos - length, length)) > 2147483647)) {

							throw new NumberFormatException();
						}
					} catch (NumberFormatException e) {
						throw new LexicalException("Your integer literal is beyond the permissible limit", pos);
					}

					tokens.add(new Token(Kind.INTEGER_LITERAL, pos - length, length, line, posInLine - length));
					pos--;
					posInLine--;
					state = State.START;
				}
			}
				break;
			case IN_SLIT: {
				if(ch == '\n') {
					throw new LexicalException("Invalid sequence in String Literal", pos);
				}
				else if (ch != '\"') {
						if (ch == EOFchar) {
							throw new LexicalException("Unclosed String Literal", pos);
						}
						if (chars[pos] == '\\') {
							pos++;
							length++;
							posInLine++;
						}

						length++;
				}
				else {
					tokens.add(new Token(Kind.STRING_LITERAL, pos - length, length + 1, line, posInLine - length));
					state = State.START;
					}
			}
				break;
			case IN_CMNT: {
				if (!(Character.isWhitespace(ch)) || ch == ' ') {
					//pos++;
					//posInLine++;
				} else {
					if(ch == '\r' && chars[pos+1] == '\n') {

					}
					else {
					line++;
					posInLine = 0;
					state = State.START;
					}
				}
			}
				break;
			}
			pos++;
			posInLine++;
		}
		tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
		return this;

	}

	/**
	 * Returns true if the internal interator has more Tokens
	 *
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that the next
	 * call to nextToken will return the next token in the list.
	 *
	 * It is the callers responsibility to ensure that there is another Token.
	 *
	 * Precondition: hasTokens()
	 *
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}

	/**
	 * Returns the next Token, but does not update the internal iterator. This means
	 * that the next call to nextToken or peek will return the same Token as
	 * returned by this methods.
	 *
	 * It is the callers responsibility to ensure that there is another Token.
	 *
	 * Precondition: hasTokens()
	 *
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}

	/**
	 * Resets the internal iterator so that the next call to peek or nextToken will
	 * return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");

		// String format = "%-20s%-10s%-10s%-10s%-10s%-10s";
		//
		// sb.append(String.format(format, "KIND", "TEXT", "POS", "LENGTH", "LINE",
		// "POS_IN_LINE")).append('\n');
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}