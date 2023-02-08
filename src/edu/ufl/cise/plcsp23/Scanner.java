package edu.ufl.cise.plcsp23;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.ufl.cise.plcsp23.IToken.Kind;


class Scanner implements IScanner {
  private final String input;
  private final char[] inputChars;
  private int pos;
  private char ch;
  private int column;
  private int line;
  
  private static final Map<String, Kind> reservedWords;
  static {
	  
	    reservedWords = new HashMap<>();
	    reservedWords.put("res_image", Kind.RES_image);
	    reservedWords.put("res_pixel", Kind.RES_pixel);
	    reservedWords.put("res_int", Kind.RES_int);
	    reservedWords.put("res_string", Kind.RES_string);
	    reservedWords.put("res_void", Kind.RES_void);
	    reservedWords.put("res_nil", Kind.RES_nil);
	    reservedWords.put("res_load", Kind.RES_load);
	    reservedWords.put("res_display", Kind.RES_display);
	    reservedWords.put("res_write", Kind.RES_write);
	    reservedWords.put("res_x", Kind.RES_x);
	    reservedWords.put("res_y", Kind.RES_y);
	    reservedWords.put("res_a", Kind.RES_a);
	    reservedWords.put("res_r", Kind.RES_r);
	    reservedWords.put("res_X", Kind.RES_X);
	    reservedWords.put("res_Y", Kind.RES_Y);
	    reservedWords.put("res_Z", Kind.RES_Z);
	    reservedWords.put("res_x_cart", Kind.RES_x_cart);
	    reservedWords.put("res_y_cart", Kind.RES_y_cart);
	    reservedWords.put("res_a_polar", Kind.RES_a_polar);
	    reservedWords.put("res_r_polar", Kind.RES_r_polar);
	    reservedWords.put("res_rand", Kind.RES_rand);
	    reservedWords.put("res_sin", Kind.RES_sin);
	    reservedWords.put("res_cos", Kind.RES_cos);
	    reservedWords.put("res_atan", Kind.RES_atan);
	    reservedWords.put("res_if", Kind.RES_if);
	    reservedWords.put("res_while", Kind.RES_while);   
	    
	  }
  
  
  
  private enum State {START, AMP, LINE, IN_IDENT, HAVE_ASTE,  
	   IN_NUMB_LIT, IN_STR, HAVE_EQ, HAVE_LROW, HAVE_RROW, IN_COMMENT}
  
  //Constructor refrenced from 1/23 Slides
  public Scanner(String input) {
	  this.input = input;
	  inputChars = Arrays.copyOf(input.toCharArray(),input.length()+1);
	  pos = -1;
	  ch = inputChars[0];
	  column = 0;
	  line = 0;
  }



  @Override
  public IToken next() throws LexicalException {
	  IToken nextToken = scanTokens();
	  return nextToken;
  }
  
  private void nextChar() {
	  ++pos;
	  ch = inputChars[pos];
  }
  // Utility functions referenced from 1/23 Lecture Slides
  private boolean isDigit(int ch) {
	   return '0' <= ch && ch <= '9';
  }
  private boolean isLetter(int ch) {
	return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
  }
  private boolean isIdentStart(int ch) {
	 return isLetter(ch) || (ch == '$') || (ch == '_');
  }
  private void error(String message) throws LexicalException{
	 throw new LexicalException("Error at pos " + pos + ": " + message); 
  }

  
  private IToken scanTokens() throws LexicalException {
	  State state = State.START;
	  int tokenStart = -1;
	  while (true) {
		 nextChar();
		 switch (state) {
		 	case START -> {
		 		tokenStart = pos;
	 			++column;
		 		switch(ch) {
			 		case 0 -> { //end of input
			 		    return new Token(Kind.EOF, tokenStart, 0, inputChars, line, column);
			 		}
		 			case ' ', '\t', '\r', '\f' -> {nextChar();}
		 			case '\n' -> {
		 				++line;
		 				column = 0;
		 				nextChar();
		 			}
		 			case '.' -> {
		 				nextChar();
			 		    return new Token(Kind.DOT, tokenStart, 1, inputChars, line, column);
		 			}
		 			case '?' -> {
		 				nextChar();
		 				return new Token(Kind.EOF, tokenStart, 1, inputChars, line, column);
		 			}
		 			case '+' -> {
		 				nextChar();
		 				return new Token(Kind.PLUS, tokenStart, 1, inputChars, line, column);
		 			}
		 			case '(' -> {
		 				nextChar();
		 				return new Token(Kind.LPAREN, tokenStart, 1, inputChars, line, column);
		 			}

		 			case ')' -> {
		 				nextChar();
		 				return new Token(Kind.RPAREN, tokenStart, 1, inputChars, line, column);
		 			}
		 			case '[' -> {
		 				nextChar();
		 				return new Token(Kind.LSQUARE, tokenStart, 1, inputChars, line, column);
		 			}
			    	case ']' -> {
		 				nextChar();
			    		return new Token(Kind.RSQUARE, tokenStart, 1, inputChars, line, column);
		 			}
			    	case '/' -> {
		 				nextChar();
			    		return new Token(Kind.DIV, tokenStart, 1, inputChars, line, column);
		 			}
			    	case '%' -> {
		 				nextChar();
			    		return new Token(Kind.MOD, tokenStart, 1, inputChars, line, column);
		 			}
			    	case ':' -> {
		 				nextChar();
			    		return new Token(Kind.COLON, tokenStart, 1, inputChars, line, column);
		 			}
			    	case ',' -> {
		 				nextChar();
			    		return new Token(Kind.COMMA, tokenStart, 1, inputChars, line, column);
		 			}
		 			case '{' -> {
		 				nextChar();
		 				return new Token(Kind.LCURLY, tokenStart, 1, inputChars, line, column);
		 			}
		 			case '}' -> {
		 				nextChar();
		 				return new Token(Kind.RCURLY, tokenStart, 1, inputChars, line, column);
		 			}
		 			case '!' -> {
		 				nextChar();
		 				return new Token(Kind.BANG, tokenStart, 1, inputChars, line, column);
		 			}
		 			case '-' -> {
		 				nextChar();
		 				return new Token(Kind.MINUS, tokenStart, 1, inputChars, line, column);
		 			}
		 			case '*' -> {
		 				nextChar();
			    		state = State.HAVE_ASTE;
			    	}
		 			case '&' -> {
		 				nextChar();
			    		state = State.AMP;
			    	}
		 			case '|' -> {
		 				nextChar();
			    		state = State.LINE;
			    	}
			    	case '>' -> {
		 				nextChar();
			    		state = State.HAVE_RROW;
			    	}
			    	case '<' -> {
		 				nextChar();
			    		state = State.HAVE_LROW;
			    	}
		 			case '"' -> {
		 				nextChar();
		 				state = State.IN_STR;
		 			}
			    	case '=' -> {
		 				nextChar();
			    		state= State.HAVE_EQ;
			    	}

			    	case '~' -> {
		 				nextChar();
			    		state = State.IN_COMMENT;
			    	}
			    	case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
		 				nextChar();
			    		state = State.IN_NUMB_LIT;
			    	}

			    	case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			    		 'a', 'b', 'c', 'd', 'e', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			    		 '_', '$' -> {
			    			 state = State.IN_IDENT;
			    	}	 
			    	default -> {error("Invalid Character");}
		 		}
		 		}
		 
		 	
		 	
		 	case HAVE_EQ -> {
		 		switch(ch) {
		 			case '=' -> {
		 				nextChar();
		 				return new Token(Kind.EQ, tokenStart, 2, inputChars, line, column);
		 			}
		 			default -> {
		 				return new Token(Kind.ASSIGN, tokenStart, 1, inputChars, line, column);
		 			}

		 		}
		 	}

		 	case HAVE_LROW -> {
		 		switch(ch) {
		 			case '-' -> {
		 				nextChar();
		 				if (ch == '>') {
		 					return new Token(Kind.EXCHANGE, tokenStart, 3, inputChars, line, column);
		 				}
		 				error ("Invalid LROW token");

		 			}
		 			case '=' -> {
	 					return new Token(Kind.LE, tokenStart, 2, inputChars, line, column);
		 			}
		 			default -> {
		 				return new Token(Kind.LT, tokenStart, 1, inputChars, line, column);

		 			}

		 		}
		 	}
		 	
		 	case HAVE_RROW -> {
		 		switch(ch) {
		 			case '=' -> {
	 					return new Token(Kind.GE, tokenStart, 2, inputChars, line, column);
		 			}
		 			default -> {
		 				return new Token(Kind.GT, tokenStart, 1, inputChars, line, column);

		 			}

		 		}
		 	}
		 	case AMP -> {
		 		switch(ch) {
		 			case '&' -> {
		 				nextChar();
		 				return new Token(Kind.AND, tokenStart, 2, inputChars, line, column);

		 			}
		 			default -> {
		 				return new Token(Kind.BITAND, tokenStart, 1, inputChars, line, column);

		 			}

		 		}
		 	}

		 	case LINE -> {
		 		switch(ch) {
		 			case '|' -> {
		 				nextChar();
		 				return new Token(Kind.OR, tokenStart, 2, inputChars, line, column);

		 			}
		 			default -> {
		 				return new Token(Kind.BITOR, tokenStart, 1, inputChars, line, column);

		 			}

		 		}
		 	}


		 	case HAVE_ASTE -> {
		 		switch(ch) {
		 			case '*' -> {
		 				nextChar();
		 				return new Token(Kind.EXP, tokenStart, 2, inputChars, line, column);

		 			}
		 			default -> {
		 				return new Token(Kind.TIMES, tokenStart, 1, inputChars, line, column);

		 			}

		 		}
		 	}
		 	case IN_NUMB_LIT -> {
		 		if (isDigit(ch)) {
		 			nextChar();
		 		}
		 		else {
		 			int length = pos-tokenStart;
		 			return new NumLitToken(Kind.NUM_LIT, tokenStart, length, inputChars, line, column);
		 		}
		 	}

		 	case IN_IDENT -> {
		 		if (isIdentStart(ch) || isDigit(ch)) {
		 			nextChar();
		 		}
		 		else {
		 			int length = pos-tokenStart;
		 			String text = input.substring(tokenStart, tokenStart + length);
	 	            Kind kind = reservedWords.get(text);
	 	            if (kind == null){kind = Kind.IDENT;}
	 	            return new Token(kind, tokenStart, length, inputChars, line, column);
		 		}
		 	}
		 	case IN_STR -> {
			 	switch(ch) {
				 		case '"' -> {
				 			int length = pos-tokenStart;
				 			String text = input.substring(tokenStart, tokenStart + length);
			 	            Kind kind = reservedWords.get(text);
			 	            if (kind != null) {error("Reserved words in string literal");}
				 			return new StringLitToken(Kind.NUM_LIT, tokenStart, length, inputChars, line, column);
			 			}
				 		default -> {
			 				nextChar();
			 		
			 		}
		 		}
		 	}
		 	case IN_COMMENT -> {
		 		if (ch == '\n') {
		 			state = State.START;
		 		}
		 		else {
		 			nextChar();
		 		}
		 	}
		 }
	  }
  }
};