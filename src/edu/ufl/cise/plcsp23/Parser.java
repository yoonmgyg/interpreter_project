package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.IToken.Kind;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.List;
import java.util.ArrayList;


public class Parser implements IParser
{
	// Variables from Crafting Interpreters 6.2.1
    private final List<IToken> tokenList = new ArrayList<>();
    private int current = 0;
    private final String input;
    private IToken currentToken;
    
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
    	return program();
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
				System.out.println(currentToken.getKind());
				return consume().getKind();
			}
		}
		throw new SyntaxException("Mismatching kind: " + currentToken.getKind().toString());
	}
	
	public Program program() throws PLCException {
		IToken firstToken=currentToken;
		Type progType = type();
		Ident progIdent = new Ident(currentToken);
		match(Kind.IDENT);
		match(Kind.LPAREN);
		List<NameDef> progParamList = paramList();
		System.out.println(progParamList.toString());
		match(Kind.RPAREN);
		Block progBlock = block();
		return new Program(firstToken, progType, progIdent, progParamList, progBlock);
	}
	
	
	public Type type() throws SyntaxException {
		IToken firstToken = currentToken;
		Type type = null;
		switch (firstToken.getKind()) {
			case RES_image -> {
				type = Type.IMAGE;
				match(Kind.RES_image);
			}
			case RES_pixel -> {
				type = Type.PIXEL;
				match(Kind.RES_pixel);
			}
			case RES_int -> {
				type = Type.INT;
				match(Kind.RES_int);
			}
			case RES_string -> {
				type = Type.STRING;
				match(Kind.RES_string);
			}
			case RES_void -> {
				type = Type.VOID;
				match(Kind.RES_void);
			}
			default -> throw new SyntaxException("Invalid type: " + currentToken.getKind());
		}
		return type;
		
	}
	public Dimension dimension() throws PLCException {
       Dimension dim = null;
       IToken firstToken = currentToken;
	   if (isKind(Kind.LSQUARE)) {
	       match(Kind.LSQUARE);
	       Expr dimOne = expr();
	       match(Kind.COMMA);
	       Expr dimTwo = expr();
	       match(Kind.RSQUARE);
	       dim = new Dimension(firstToken, dimOne, dimTwo);
       }
       return dim;
    }
	

    public NameDef nameDef() throws PLCException {
        IToken firstToken = currentToken;
        Type nameDefType = null;
        Dimension dim = null;
        Ident nameIdent = null;
	    try {
	        nameDefType = type();
	        dim = dimension();
	        nameIdent = new Ident(currentToken);
	        match(Kind.IDENT);
        } catch (PLCException e) {
        	return null;
        }
        return new NameDef(firstToken, nameDefType, dim, nameIdent);
    }    
    public List<NameDef> paramList() throws PLCException {
		List<NameDef> paramList = new ArrayList<>();
        IToken firstToken = currentToken;
        NameDef paramNameDef = nameDef();
        if (paramNameDef != null) {
        	paramList.add(paramNameDef);
        }
        while (isKind(Kind.COMMA)) {
        	match(Kind.COMMA);
        	paramNameDef = nameDef();
        	paramList.add(paramNameDef);
        }
        return paramList;
    }
    
	public Declaration decl() throws PLCException {
		IToken firstToken = currentToken;
		Expr e = null;
		NameDef nameDef = nameDef();	
		if (isKind(Kind.ASSIGN)) {
			match(Kind.ASSIGN);
			e = expr();
		}
		if (nameDef == null && e == null) {
			return null;
		}
		return new Declaration(firstToken, nameDef, e);
	}
	
	public Block block() throws PLCException {
        IToken firstToken = currentToken;
        match(Kind.LCURLY);
        List<Declaration> declarationList = decList();
        List<Statement> statementList = stateList();
        match(Kind.RCURLY);
        return new Block(firstToken,declarationList,statementList);
    }
	public List<Declaration> decList() throws PLCException {
		List<Declaration> declarationList = new ArrayList<>();
		Declaration dec = decl();
		while (dec != null) {
			match(Kind.DOT);
			declarationList.add(dec);
			dec = decl();
		}
		return declarationList;
	}
	
	public List<Statement> stateList() throws PLCException {
		List<Statement> statementList = new ArrayList<>();
		Statement statem = statement();
		while (statem != null) {
			match(Kind.DOT);
			statementList.add(statem);
			statem = statement();
		}
		return statementList;
	}
	public LValue lValue() throws PLCException {
		IToken firstToken = currentToken;
		try {
			Ident lValueIdent = new Ident(currentToken);
			match(Kind.IDENT);
			PixelSelector lValuePix = pixel();
			ColorChannel lValueChann = channel();
			return new LValue(firstToken, lValueIdent, lValuePix, lValueChann);
		} catch (PLCException e) {
			return null;
		}
	}
	public Statement statement() throws PLCException {
		IToken firstToken = currentToken;
		Expr stateExpr = null;
		Block stateBlock = null;

		LValue statementLValue = lValue();
		if (statementLValue == null) {
			switch (firstToken.getKind()) {
				case RES_write -> {
					match(Kind.RES_write);
					stateExpr = expr();
					return new WriteStatement(firstToken, stateExpr);
				}
				case RES_while -> {
					match(Kind.RES_while);
					stateExpr = expr();
					stateBlock = block();
					return new WhileStatement(firstToken, stateExpr, stateBlock);
				}
			}
		}
		else if (isKind(Kind.ASSIGN)){
			match(Kind.ASSIGN);
			stateExpr = expr();
			return new AssignmentStatement(firstToken, statementLValue, stateExpr);
		}
		return null;
		
	}
	public ColorChannel channel() throws SyntaxException {
		IToken firstToken = currentToken;
		ColorChannel chann = null;
		if (isKind(Kind.COLON)) {
			match(Kind.COLON);
			switch(currentToken.getKind()) {
				case RES_red -> {
					match(Kind.RES_red);
					chann = ColorChannel.red;
				}
				case RES_blu -> {
					match(Kind.RES_blu);
					chann = ColorChannel.blu;
					
				}
				case RES_grn -> {
					match(Kind.RES_grn);
					chann = ColorChannel.grn;
				}
				default -> throw new SyntaxException("Invalid color channel: " + currentToken.getKind());
			}
		}
		return chann;
	}
	public PixelSelector pixel() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr pixelX = null;
		Expr pixelY = null;
		try {
			match(Kind.LSQUARE);
			pixelX = expr();
			match(Kind.COMMA);
			pixelY = expr(); 
			match(Kind.RSQUARE);
			
		} catch (PLCException e) {
			return null;
		}
		return new PixelSelector(firstToken, pixelX, pixelY);
	}
	public ExpandedPixelExpr exPixel() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr pixelX = null;
		Expr pixelY = null;
		Expr pixelZ = null;
		try {
			match(Kind.LSQUARE);
			pixelX = expr();
			match(Kind.COMMA);
			pixelY = expr();
			match(Kind.COMMA);
			pixelZ = expr();
			match(Kind.RSQUARE);
		} catch (PLCException e) {
			return null;
		}
		return new ExpandedPixelExpr(firstToken, pixelX, pixelY, pixelZ);
	}

	public PixelFuncExpr pixelFuncExpr() throws SyntaxException {
		IToken firstToken = currentToken;
		Kind func = match(Kind.RES_x_cart, Kind.RES_y_cart, Kind.RES_a_polar, Kind.RES_r_polar);
		PixelSelector pixelSelector = pixel();
		return new PixelFuncExpr(firstToken, func, pixelSelector);
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
			e = unaryExprPostfix();
		}
		return e;
	}
	private Expr unaryExprPostfix() throws SyntaxException {
		IToken firstToken = currentToken;
		Expr e = primary();
		PixelSelector postfixPixel = pixel();
		ColorChannel postfixChannel = channel();
		if (postfixPixel != null || postfixChannel != null) {
			return new UnaryExprPostfix(firstToken, e, postfixPixel, postfixChannel);
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
			case LSQUARE -> {
				e = exPixel();
			}
			case RES_x_cart, RES_y_cart, RES_a_polar, RES_r_polar -> {
				e = pixelFuncExpr();
			}
			case RES_x, RES_y, RES_a, RES_r -> {
				match(Kind.RES_x, Kind.RES_y, Kind.RES_a, Kind.RES_r);
				e = new PredeclaredVarExpr(firstToken);
			}
			default -> {
				throw new SyntaxException("Invalid Token: " + currentToken.getKind());
			}
		}
		return e;
	}
}
