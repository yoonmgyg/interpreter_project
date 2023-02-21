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
	    reservedWords.put("image", Kind.RES_image);
	    reservedWords.put("pixel", Kind.RES_pixel);
	    reservedWords.put("int", Kind.RES_int);
	    reservedWords.put("string", Kind.RES_string);
	    reservedWords.put("void", Kind.RES_void);
	    reservedWords.put("nil", Kind.RES_nil);
	    reservedWords.put("load", Kind.RES_load);
	    reservedWords.put("display", Kind.RES_display);
	    reservedWords.put("write", Kind.RES_write);
	    reservedWords.put("x", Kind.RES_x);
	    reservedWords.put("y", Kind.RES_y);
	    reservedWords.put("a", Kind.RES_a);
	    reservedWords.put("r", Kind.RES_r);
	    reservedWords.put("X", Kind.RES_X);
	    reservedWords.put("Y", Kind.RES_Y);
	    reservedWords.put("Z", Kind.RES_Z);
	    reservedWords.put("x_cart", Kind.RES_x_cart);
	    reservedWords.put("y_cart", Kind.RES_y_cart);
	    reservedWords.put("a_polar", Kind.RES_a_polar);
	    reservedWords.put("r_polar", Kind.RES_r_polar);
	    reservedWords.put("rand", Kind.RES_rand);
	    reservedWords.put("sin", Kind.RES_sin);
	    reservedWords.put("cos", Kind.RES_cos);
	    reservedWords.put("atan", Kind.RES_atan);
	    reservedWords.put("if", Kind.RES_if);
	    reservedWords.put("while", Kind.RES_while);   
	    
	  }
  
  
  
  private enum State {START, AMP, LINE, IN_IDENT, HAVE_ASTE,  
	   IN_NUMB_LIT, IN_STR, HAVE_EQ, HAVE_LROW, HAVE_RROW, IN_COMMENT}
  
  //Constructor referenced from 1/23 Slides
  public Scanner(String input) {
	  this.input = input;
	  inputChars = Arrays.copyOf(input.toCharArray(),input.length()+1);
	  pos = 0;
	  ch = inputChars[0];
	  column = 1;
	  line = 1;
  }



  @Override
  public IToken next() throws LexicalException {
	  IToken nextToken = scanTokens();
	  //System.out.println(nextToken.getKind().name());
	  //System.out.println(nextToken.getTokenString() + "\n");
	  return nextToken;
  }
  
  private void nextChar() {
	  ++pos;
	  ++column;
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
	  int tokenLine = -1;
	  int tokenColumn = -1;
	  while (true) {
		 //System.out.println("State:" + state.toString());
		 //System.out.println("Character:" + ch);
		 switch (state) {
		 	case START -> {
		 		tokenStart = pos;
		 		tokenLine = line;
		 		tokenColumn = column;
		 		switch(ch) {
			 		case 0 -> { //end of input
			 		    return new Token(Kind.EOF, tokenStart, 0, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
			 		}
		 			case ' ', '\t', '\r', '\f' -> {nextChar();}
		 			case '\n' -> {
		 				++line;
		 				column = 0;
		 				nextChar();
		 			}
		 			case '.' -> {
		 				nextChar();
			 		    return new Token(Kind.DOT, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			case '?' -> {
		 				nextChar();
		 				return new Token(Kind.QUESTION, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			case '+' -> {
		 				nextChar();
		 				return new Token(Kind.PLUS, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			case '(' -> {
		 				nextChar();
		 				return new Token(Kind.LPAREN, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}

		 			case ')' -> {
		 				nextChar();
		 				return new Token(Kind.RPAREN, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			case '[' -> {
		 				nextChar();
		 				return new Token(Kind.LSQUARE, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
			    	case ']' -> {
		 				nextChar();
			    		return new Token(Kind.RSQUARE, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
			    	case '/' -> {
		 				nextChar();
			    		return new Token(Kind.DIV, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
			    	case '%' -> {
		 				nextChar();
			    		return new Token(Kind.MOD, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
			    	case ':' -> {
		 				nextChar();
			    		return new Token(Kind.COLON, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
			    	case ',' -> {
		 				nextChar();
			    		return new Token(Kind.COMMA, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			case '{' -> {
		 				nextChar();
		 				return new Token(Kind.LCURLY, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			case '}' -> {
		 				nextChar();
		 				return new Token(Kind.RCURLY, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			case '!' -> {
		 				nextChar();
		 				return new Token(Kind.BANG, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			case '-' -> {
		 				nextChar();
		 				return new Token(Kind.MINUS, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
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
			    	case '0' -> {
			    	   nextChar();
			    	   return new NumLitToken(Kind.NUM_LIT, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
			    	}
			    	case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
		 				nextChar();
			    		state = State.IN_NUMB_LIT;
			    	}

			    	case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			    		 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
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
		 				return new Token(Kind.EQ, tokenStart, 2, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			default -> {
		 				return new Token(Kind.ASSIGN, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}

		 		}
		 	}

		 	case HAVE_LROW -> {
		 		switch(ch) {
		 			case '-' -> {
		 				nextChar();
		 				if (ch == '>') {
			 				nextChar();
		 					return new Token(Kind.EXCHANGE, tokenStart, 3, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 				}
		 				error ("Invalid LROW token");

		 			}
		 			case '=' -> {
		 				nextChar();
	 					return new Token(Kind.LE, tokenStart, 2, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			default -> {
		 				return new Token(Kind.LT, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);

		 			}

		 		}
		 	}
		 	
		 	case HAVE_RROW -> {
		 		switch(ch) {
		 			case '=' -> 
		 			{
		 				nextChar();
	 					return new Token(Kind.GE, tokenStart, 2, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 			}
		 			default -> {
		 				return new Token(Kind.GT, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);

		 			}

		 		}
		 	}
		 	case AMP -> {
		 		switch(ch) {
		 			case '&' -> {
		 				nextChar();
		 				return new Token(Kind.AND, tokenStart, 2, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);

		 			}
		 			default -> {
		 				return new Token(Kind.BITAND, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);

		 			}

		 		}
		 	}

		 	case LINE -> {
		 		switch(ch) {
		 			case '|' -> {
		 				nextChar();
		 				return new Token(Kind.OR, tokenStart, 2, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);

		 			}
		 			default -> {
		 				return new Token(Kind.BITOR, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);

		 			}

		 		}
		 	}


		 	case HAVE_ASTE -> {
		 		switch(ch) {
		 			case '*' -> {
		 				nextChar();
		 				return new Token(Kind.EXP, tokenStart, 2, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);

		 			}
		 			default -> {
		 				return new Token(Kind.TIMES, tokenStart, 1, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);

		 			}

		 		}
		 	}
		 	case IN_NUMB_LIT -> {
                if (isDigit(ch)) {
                    nextChar();
                }
                else {
                    int length = pos;
                    String conv = String.valueOf(Arrays.copyOfRange(inputChars, tokenStart, pos));
                    try
                   {
                        Integer.parseInt(conv);
                   }
                   catch(NumberFormatException e)
                   {
                       throw new LexicalException("Int out of bounds");
                   }
                    return new NumLitToken(Kind.NUM_LIT, tokenStart, length, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
                }
            }

		 	case IN_IDENT -> {
		 		if (isIdentStart(ch) || isDigit(ch)) {
		 			nextChar();
		 		}
		 		else {
		 			int length = pos-tokenStart;
		 			String text = input.substring(tokenStart, pos);
		 			//System.out.println
	 	            Kind kind = reservedWords.get(text);
	 	            if (kind == null)
	 	            	kind = Kind.IDENT;
	 	            //else
	 	            //	System.out.println(text);	 	            	
	 	            return new Token(kind, tokenStart, length, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
		 		}
		 	}
		 	case IN_STR -> {
			 	switch(ch) {
			 			case 0-> {
			 				error("Unterminated String");
			 			}
				 		case '"' -> {
				 			nextChar();
				 			int length = pos-tokenStart;
				 			String text = input.substring(tokenStart, pos);
			 	            Kind kind = reservedWords.get(text);
			 	            if (kind != null) {error("Reserved words in string literal");}
			 	            System.out.println(Arrays.copyOfRange(inputChars, tokenStart, pos));
				 			return new StringLitToken(Kind.STRING_LIT, tokenStart, length, Arrays.copyOfRange(inputChars, tokenStart, pos), tokenLine, tokenColumn);
			 			}
				 		default -> {
			 				nextChar();
			 				//System.out.println(ch);
			 		
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