package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.IToken.Kind;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.List;
import java.util.ArrayList;


public class Parser implements IParser
{
	// Variables from Crafting Interpreters 6.2.1
    private final List<IToken> tokenList = new ArrayList();
    private int current = 0;
    private final String input;
    private IToken currentToken;
    private List<NameDef> naDefList = new ArrayList<NameDef>(); 
    
    Parser(String input) {
    	this.input = input;
    }
    
    public AST parse() throws PLCException,SyntaxException{
    	IScanner Scanner = new Scanner(input);
    	IToken tokenInput;
    	do {
    		tokenInput = Scanner.next();
    		tokenList.add(tokenInput);
    	} while (tokenInput.getKind() != Kind.EOF);
    	currentToken = tokenList.get(0);
    	return expr();
    }

    // Helper functions referenced from Crafting Interpreters 6.2.1
	private IToken peek() {
		return tokenList.get(current);
	}
	
	private IToken consume() {
		if (!isAtEnd()) {
			current++;
			currentToken = peek();
		}
		return previous();
	}
	
	private IToken next() {
		if (!isAtEnd()) {
			return tokenList.get(current+1);
		}
		else {
			return peek();
		}
	}
	
	private IToken previous() {
		if (current!= 0) {
			return tokenList.get(current - 1);
		}
		else {
			return peek();
		}
	}
	
	private boolean isAtEnd() {
		return peek().getKind() == Kind.EOF;
	}
	
	protected boolean isKind(Kind...kinds)  {
		for (Kind k: kinds) {
			if (k == currentToken.getKind()) {
				return true;
			}
		}
		return false;
	}
	protected Kind match(Kind...kinds) throws SyntaxException{
		for (Kind k: kinds) {
			if (k == currentToken.getKind()) {
				return consume().getKind();
			}
		}
		throw new SyntaxException("Mismatching kind: " + currentToken.getKind().toString());
	}
	public Dimension dimension() throws PLCException {
       Dimension dim = null;
       IToken temp = null;
       IToken nextToken = next();
       IToken firstToken = currentToken;
       Expr e1 = null;
       Expr e2 = null;
       if(nextToken.getKind() == IToken.Kind.LSQUARE) {
    	   temp = firstToken;
           firstToken = nextToken;
           nextToken = consume();
           e1 = expr();
           while (nextToken.getKind() != IToken.Kind.RSQUARE)
        	   e2 = expr();
           	   //consume();
           dim = new Dimension(temp,e1,e2);
       }
       return dim;
    }
	

	   public NameDef nameDef() throws PLCException {
	        Dimension dimension = null;
	        IToken nextToken = next();
	        IToken firstToken = currentToken;
	        if(nextToken.getKind() != IToken.Kind.RES_void && nextToken.getKind() != IToken.Kind.RES_image &&
	        		nextToken.getKind() != IToken.Kind.RES_int && nextToken.getKind() != IToken.Kind.RES_pixel &&
	        		nextToken.getKind() != IToken.Kind.RES_string && firstToken.getKind() != IToken.Kind.RES_while &&
	        		firstToken.getKind() != IToken.Kind.RES_string )
	            throw  new SyntaxException("Invalid Syntax wrong type");
	        Type typeval = Type.getType(nextToken);
	        firstToken = nextToken;
	        nextToken = consume();
	        if(nextToken.getKind() != IToken.Kind.IDENT)
	        {
	            dimension = dimension();
	            firstToken = nextToken;
	            nextToken = consume();
	        }
	        return new NameDef(nextToken, typeval, dimension, new Ident(nextToken));
	    }
	public Declaration decl() throws PLCException {
		IToken firstToken = currentToken;
		Expr temp = null;
		NameDef nameDef = nameDef();	
		if (isKind(Kind.ASSIGN)) {
			match(Kind.ASSIGN);
			temp = expr();
		}
		return new Declaration(firstToken, nameDef, temp);
	}
	
	public Block block() throws PLCException {
        IToken nextToken = next();
        List<Declaration> declarationList = new ArrayList<>();
        List<Statement> statementList = new ArrayList<>();
        IToken firstToken = nextToken;
        nextToken = consume();
        while(nextToken.getKind() != IToken.Kind.RCURLY)
        {
            if(nextToken.getKind() != IToken.Kind.RES_write && nextToken.getKind() != IToken.Kind.RES_while)
            {
                if(nextToken.getKind() == IToken.Kind.DOT) {
                    firstToken = nextToken;
                    nextToken = consume();
                    continue;
                }
                else if(firstToken.getKind() != IToken.Kind.RES_while)
                    declarationList.add(decl());
            }
            else {
                   firstToken = nextToken;
                   nextToken = consume();
                   statementList.add(statement());
            }
        }
        return new Block(currentToken,declarationList,statementList);
    }
	
	public Statement statement() throws PLCException {
		IToken firstToken = currentToken;
		Statement state = null;
		if (isKind(Kind.RES_write)) {
			Expr ex = expr();
			return new WriteStatement(firstToken, ex);
		}
		
		else if (isKind(Kind.RES_while)) {
            Expr ex = expr();
            Block progBlock = block();
            return new WhileStatement(firstToken,ex,progBlock);
		}	
		return state;
		
	}
	public PixelSelector pixelSelector() throws PLCException {
		if (currentToken.getKind() == Kind.LSQUARE) {			
			Expr x = expr();
			//Need to consume
			Expr y = expr();
			return new PixelSelector(currentToken, x, y);
		}
		return null;		
	}
	


	private Expr expr() throws SyntaxException{
		Expr e = conditional();
		if (e == null) {
			e = or();
		}
		return e;
	}
	
	private Expr conditional() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr e = null;
		if (isKind(Kind.RES_if)) {
			match(Kind.RES_if);
			Expr condition = expr();
			match(Kind.QUESTION);
			Expr trueCase = expr();
			match(Kind.QUESTION);
			Expr falseCase = expr();
			e = new ConditionalExpr(firstToken, condition, trueCase, falseCase);
		}
		return e;
	}
	
	private Expr or() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr e = and();
		while (isKind(Kind.OR, Kind.BITOR)) {
			Kind op = match(Kind.OR, Kind.BITOR);
			Expr rhs = and();
			e = new BinaryExpr(firstToken, e, op, rhs);
		}
		return e;
	}
	
	private Expr and() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr e = comparison();
		while (isKind(Kind.AND, Kind.BITAND)) {
			Kind op = match(Kind.AND, Kind.BITAND);
			Expr rhs = comparison();
			e = new BinaryExpr(firstToken, e, op, rhs);
		}
		return e;
	}
	
	private Expr comparison() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr e = power();
		while (isKind(Kind.LT, Kind.LE, Kind.GT, Kind.GE, Kind.EQ)) {
			Kind op = match(Kind.LT, Kind.LE, Kind.GT, Kind.GE, Kind.EQ);
			Expr rhs = comparison();
			e = new BinaryExpr(firstToken, e, op, rhs);
		}
		return e;
	}
	private Expr power() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr e = add();
		if (isKind(Kind.EXP)) {
			Kind op = match(Kind.EXP);
			Expr rhs = power();
			e = new BinaryExpr(firstToken, e, op, rhs);
		}
		return e;
	}
	private Expr add() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr e = multiply();
		while (isKind(Kind.PLUS, Kind.MINUS)) {
			Kind op = match(Kind.PLUS, Kind.MINUS);
			Expr rhs = multiply();
			e = new BinaryExpr(firstToken, e, op, rhs);
		}
		return e;
	}
	private Expr multiply() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr e = unary();
		while (isKind(Kind.TIMES, Kind.DIV, Kind.MOD)) {
			Kind op = match(Kind.TIMES, Kind.DIV, Kind.MOD);
			Expr rhs = unary();
			e = new BinaryExpr(firstToken, e, op, rhs);
		}
		return e;
	}
	private Expr unary() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr e = null;
		if (isKind(Kind.BANG, Kind.RES_sin, Kind.RES_cos, Kind.RES_atan, Kind.MINUS)) {
			Kind op = match(Kind.BANG, Kind.RES_sin, Kind.RES_cos, Kind.RES_atan, Kind.MINUS);
			Expr rhs = unary();
			e = new UnaryExpr(firstToken, op, rhs);
		}
		else {
			e = primary();
		}
		return e;
	}
	private Expr primary() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr e = null;
		switch(currentToken.getKind()) {
			case STRING_LIT -> {
				match(Kind.STRING_LIT);
				e = new StringLitExpr(firstToken);
			}
			case NUM_LIT -> {
				match(Kind.NUM_LIT);
				e = new NumLitExpr(firstToken);
			}
			case IDENT, RES_if -> {
				match(Kind.IDENT);
				e = new IdentExpr(firstToken);
			}

			case RES_Z -> {
				match(Kind.RES_Z);
				e = new ZExpr(firstToken);
			}
			case RES_rand -> {
				match(Kind.RES_rand);
				e = new RandomExpr(firstToken);
			}
			case LPAREN -> {
				match(Kind.LPAREN);
				e = expr();
				match(Kind.RPAREN);
			}
			default -> {
				throw new SyntaxException("Invalid Token: " + currentToken.getKind());
			}
		}
		return e;
	}
}
