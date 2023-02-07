package edu.ufl.cise.plcsp23;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.ufl.cise.plcsp23.IToken.Kind;


class Lexer implements IScanner {
  private final String chars;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int pos = -1;
  private int columns = 0;
  private int lines = 0;
  private boolean end = false;
  
  private static final Map<String, Kind> labels;
  static {
	  
	    labels = new HashMap<>();
	    labels.put("ident", Kind.IDENT);
	    labels.put("num_lit", Kind.NUM_LIT);
	    labels.put("string_lit", Kind.STRING_LIT);
	    labels.put("res_image", Kind.RES_image);
	    labels.put("res_pixel", Kind.RES_pixel);
	    labels.put("res_int", Kind.RES_int);
	    labels.put("res_string", Kind.RES_string);
	    labels.put("res_void", Kind.RES_void);
	    labels.put("res_nil", Kind.RES_nil);
	    labels.put("res_load", Kind.RES_load);
	    labels.put("res_display", Kind.RES_display);
	    labels.put("res_write", Kind.RES_write);
	    labels.put("res_x", Kind.RES_x);
	    labels.put("res_y", Kind.RES_y);
	    labels.put("res_a", Kind.RES_a);
	    labels.put("res_r", Kind.RES_r);
	    labels.put("res_X", Kind.RES_X);
	    labels.put("res_Y", Kind.RES_Y);
	    labels.put("res_Z", Kind.RES_Z);
	    labels.put("res_x_cart", Kind.RES_x_cart);
	    labels.put("res_y_cart", Kind.RES_y_cart);
	    labels.put("res_a_polar", Kind.RES_a_polar);
	    labels.put("res_r_polar", Kind.RES_r_polar);
	    labels.put("res_rand", Kind.RES_rand);
	    labels.put("res_sin", Kind.RES_sin);
	    labels.put("res_cos", Kind.RES_cos);
	    labels.put("res_atan", Kind.RES_atan);
	    labels.put("res_if", Kind.RES_if);
	    labels.put("res_while", Kind.RES_while);   
	    
	  }
  
  
  
  private enum State {START, AMP, LINE, IN_IDENT, HAVE_ZERO, HAVE_DOT, HAVE_ASTE,  
	   IN_NUM, IN_STR, HAVE_EQ, HAVE_LROW, HAVE_RROW, HAVE_EX, IN_COND, END_COND}
  


  Lexer(String chars) {
    this.chars = chars;
    
  }


	public List<Token> getTokens() throws LexicalException {
		  while (!end) {
			  scanTokens();
		  }
		  return tokens;
	}

  // Referenced from Crafting Interpreters 4.4
  private boolean isAtEnd() {
	  return pos >= chars.length() - 1;
  }

  
  private void addToken(Kind kind) {
    tokens.add(new Token());
  }

  @Override
  public IToken next() throws LexicalException {
	  int i = 0;
	  while (tokens.isEmpty()) {
		  tokens.get(i);
		  i++;
		  scanTokens();
	  }
	  return tokens.remove(0);
  }


  
  private void scanTokens() throws LexicalException {
	  State state = State.START;
	  while (true) {
		 if (isAtEnd()) {
			  pos++;
			  addToken(Kind.EOF);
			  end = true;
			  return;
		 }
		 
		 char ch = chars.charAt(++pos);
		 ++columns;
		 switch (state) {
		 	case START -> {
		 		start = pos;
		 		switch(ch) {
		 			case ' ', '\t', '\r' -> {
		 			}
		 			case '\n' -> {
		 				lines++;
		 				columns = 0;
		 				
		 			}
		 			case '.' -> {
		 				addToken(Kind.DOT);
		 				return;
		 			}
		 			case '?' -> {
		 				addToken(Kind.QUESTION);
		 				return;
		 			}
		 			
		 			case '+' -> {
		 				addToken(Kind.PLUS);
		 				return;
		 			}
		 			case '(' -> {
		 				addToken(Kind.LPAREN);
		 				return;
		 			}

		 			case ')' -> {
		 				addToken(Kind.RPAREN);
		 				return;
		 			}
		 			case '[' -> {
		 				addToken(Kind.LSQUARE);
		 				return;
		 			}
			    	case ']' -> {
		 				addToken(Kind.RSQUARE);
		 				return;
		 			}
			    	case '/' -> {
		 				addToken(Kind.DIV);
		 				return;
		 			}
			    	case '%' -> {
		 				addToken(Kind.MOD);
		 				return;
		 			}
			    	case ':' -> {
		 				addToken(Kind.COLON);
		 				return;
		 			}
			    	case ',' -> {
		 				addToken(Kind.COMMA);
		 				return;
		 			}
		 			case '{' -> {
		 				addToken(Kind.LCURLY);
		 				return;
		 			}
		 			case '}' -> {
		 				addToken(Kind.RCURLY);
		 				return;
		 			}
		 			case '!' -> {
		 				addToken(Kind.BANG);
		 				return;
		 			}
		 			case '-' -> {
		 				addToken(Kind.MINUS);
		 				return;
		 			}
		 			case '*' -> {
			    		state = State.HAVE_ASTE;
			    	}
		 			case '&' -> {
			    		state = State.AMP;
			    	}
		 			case '|' -> {
			    		state = State.LINE;
			    	}

			    	case '>' -> {
			    		state = State.HAVE_RROW;
			    	}
			    	case '<' -> {
			    		state = State.HAVE_LROW;
			    	}
		 			case '"' -> {
		 				state = State.IN_STR;
		 			}
			    	case '=' -> {
			    		state= State.HAVE_EQ;
			    	}
			    	case '0' -> {
			    		state = State.HAVE_ZERO;
			    	}
			    	case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
			    		state = State.IN_NUM;
			    	}

			    	case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			    		 'a', 'b', 'c', 'd', 'e', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			    		 '$', '_' -> {
			    			 state = State.IN_IDENT;
			    	}	 
			    	default -> throw new LexicalException("invalid character");
		 		}
		 	}
		 	
		 	case HAVE_EQ -> {
		 		switch(ch) {
		 			case '=' -> {
		 				pos++;
		 				addToken(Kind.EQ);
		 				pos--;
		 				return;
		 			}
		 			default -> {
		 				addToken(Kind.ASSIGN);
		 				--pos;
		 				--columns;
		 				return;
		 			}

		 		}
		 	}
		 	case AMP -> {
		 		switch(ch) {
		 			case '&' -> {
		 				pos++;
		 				addToken(Kind.AND);
		 				pos--;
		 				return;
		 			}
		 			default -> {
		 				addToken(Kind.BITAND);
		 				--pos;
		 				--columns;
		 				return;
		 			}

		 		}
		 	}
		 	case LINE -> {
		 		switch(ch) {
		 			case '|' -> {
		 				pos++;
		 				addToken(Kind.OR);
		 				pos--;
		 				return;
		 			}
		 			default -> {
		 				addToken(Kind.BITOR);
		 				--pos;
		 				--columns;
		 				return;
		 			}

		 		}
		 	}
		 	case HAVE_ASTE -> {
		 		switch(ch) {
		 			case '*' -> {
		 				pos++;
		 				addToken(Kind.EXP);
		 				pos--;
		 				return;
		 			}
		 			default -> {
		 				addToken(Kind.TIMES);
		 				--pos;
		 				--columns;
		 				return;
		 			}

		 		}
		 	}

		 	case IN_STR -> {
		 		switch(ch) {
		 			case '"' -> {
		 				++pos;
		 				addToken(Kind.STRING_LIT);
		 				--pos;
		 				return;
		 			}
		 			case 0 -> {
                		throw new LexicalException("String is not complete");
		 			}
		 		}
		 	}
		 	case IN_IDENT -> {
		 		switch(ch) {
			 		case '\n' -> {
			 			String text = chars.substring(start, pos);
				 	    Kind kind = labels.get(text);
				 	    if (kind == null) kind = Kind.IDENT;
				 	    addToken(kind);
			 			lines++;
			 			columns = 0;
				 	    return;
			 		}
			 		
			 		case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		    		 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		    		 '$', '_', '0', '1', '2', '3', '4', '5', '6', '7', '9' -> {
		    			 state = State.IN_IDENT;
		    		 }	
		 			
			 		default -> {
			 			String text = chars.substring(start, pos);
				 	    Kind kind = labels.get(text);
				 	    if (kind == null) kind = Kind.IDENT;
				 	    addToken(kind);
				 	    --pos;
				 	    --columns;
				 	    return;
			 		}
		 		}
		 		
		 	}
		 	case HAVE_ZERO -> {
			 	switch(ch) {
			 		case '.' -> {
			 			state = State.HAVE_DOT;
			 		}
			 		default -> {
			 			addToken(Kind.NUM_LIT);
				 	    --pos;
				 	    --columns;
				 	    return;
			 		}
		 		}
		 		
		 	}


		 	case HAVE_LROW -> {
		 		switch(ch) {
			 		case('-') -> {
			 			addToken(Kind.EXCHANGE);
			 			return;
			 		}
			 		case('=') -> {
			 			addToken(Kind.LE);
			 			return;
			 		}
			 		
		 			default -> {
		 				addToken(Kind.LT);
				 	    --pos;
				 	    --columns;
		 				return;
		 			}
		 		}
		 	}
		 	case HAVE_RROW -> {
		 		switch(ch) {
			 		case('=') -> {
			 			addToken(Kind.GE);
			 			return;
			 		}
		 			
			 		default -> {
			 			addToken(Kind.GT);
				 	    --pos;
				 	    --columns;
			 			return;
			 		}
			 		
		 		}
		 	}

		 }
	  }
  }
};