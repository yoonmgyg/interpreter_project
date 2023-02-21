package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Parser implements IParser
{
    IScanner Scanner;
    IToken nextToken;
    IToken currentToken;
    IToken primaryToken;
    IToken beginToken;
    IToken.Kind globalkind;    
    Expr globalExpression = null;
    boolean thruCondtional = false;
    boolean primaryParenthesis = false;
    int parserPos = 0;
    final String input;


    public Parser(String inputParser) {
        this.input = inputParser;
    }
    
    @Override
    public AST parse() throws PLCException,SyntaxException{
       String parserInput = new String(input);
       if(input == "")
           throw new SyntaxException("No Program");
       
       else {
               parserInput = input.substring(parserPos,input.length());
               Scanner = CompilerComponentFactory.makeScanner(parserInput);
               beginToken = Scanner.next();
               globalExpression = expr();
           return globalExpression;
       }
    }
    
    public IToken.Kind kind() {
    	return beginToken.getKind();    	
    }
       
    public IToken consume() throws SyntaxException, LexicalException {
    	//while (input.substring(currentPos, (currentPos+1)) == " ") {
    	char[] whitespace = Arrays.copyOf(input.toCharArray(), input.length() + 1);
        while (whitespace[parserPos] == ' ') 
            parserPos++;      
        if (beginToken.getSourceLocation() != null && beginToken.getKind() != IToken.Kind.EOF) 
            parserPos += ((Token) beginToken).getLength();        
        else 
            parserPos++;        
        Scanner = CompilerComponentFactory.makeScanner(input.substring(parserPos,input.length()));
        IToken token;
        token = Scanner.next();
        return token;
    }
    
    public Expr expr() throws SyntaxException, LexicalException {
    	Expr returner;
        if (kind() != IToken.Kind.RES_if) 
        	returner = orExpr();
        
        else
        	returner =  conditionalExpr();
        
        return returner;
    }
    
    public Expr primaryExpr() throws SyntaxException, LexicalException {
            if (nextToken == null){
                primaryToken = beginToken;
            }
            else
            {
                primaryToken = nextToken;
                if(thruCondtional != true && primaryParenthesis != true) {
                    nextToken = consume();
                }
            }
            if (primaryToken.getKind() == IToken.Kind.NUM_LIT) {
                if(nextToken == null) {
                    nextToken = consume();
                }
                if(nextToken.getKind() == IToken.Kind.EOF || primaryParenthesis == true)
                    return new NumLitExpr(primaryToken);
                else
                    expr();
            }
            
            else if (primaryToken.getKind() == IToken.Kind.IDENT)
                return new IdentExpr(primaryToken);         
            
            else if (primaryToken.getKind() == IToken.Kind.STRING_LIT) 
                return new StringLitExpr(primaryToken);
            
            else if (primaryToken.getKind() == IToken.Kind.RES_Z) 
                return new ZExpr(primaryToken); 
            
            else if (primaryToken.getKind() == IToken.Kind.RES_rand)            
                return new RandomExpr(primaryToken);
            
            else if (primaryToken.getKind() == IToken.Kind.LPAREN || primaryToken.getKind() == IToken.Kind.RPAREN) {
            	primaryParenthesis = true;
            	
            if(primaryToken.getKind() == IToken.Kind.RPAREN)
            		return globalExpression;   
            
            else {
               nextToken = consume();
               beginToken = nextToken;
               globalExpression = expr();
            }
        }
        else            
        	throw new SyntaxException("Unable to parse given expression");            
        return globalExpression;
    }
    // Unary needs primary
    public Expr unaryExpr() throws SyntaxException, LexicalException {
        globalkind = beginToken.getKind();
        if (kind() == IToken.Kind.BANG || kind() == IToken.Kind.MINUS|| kind() == IToken.Kind.RES_sin || kind() == IToken.Kind.RES_cos|| kind() == IToken.Kind.RES_atan) {
            currentToken = beginToken;
            nextToken =  consume();
            globalkind = nextToken.getKind();
            globalExpression = primaryExpr();
            return new UnaryExpr(currentToken,currentToken.getKind(),globalExpression);
        }
        else
            globalExpression =  primaryExpr();
        return globalExpression;
    }

    
    public Expr additiveExpr() throws SyntaxException, LexicalException {
        globalkind = beginToken.getKind();
        Expr addReturn = multiplicativeExpr();
        while (kind() == IToken.Kind.PLUS || globalkind == IToken.Kind.MINUS) // The second kind here throws an error when replaced with kind() function
            consume();
        return addReturn;
    }
    
    public Expr andExpr() throws SyntaxException, LexicalException {
        Expr andReturn = comparisonExpr();
        while (kind() == IToken.Kind.AND || kind() == IToken.Kind.BITAND) 
            consume();
        return andReturn;
    }
    
    public Expr comparisonExpr() throws SyntaxException, LexicalException {
        Expr comparisonReturn = powerExpr();
        while (kind() == IToken.Kind.GT || kind() == IToken.Kind.GE || kind() == IToken.Kind.LT || kind() == IToken.Kind.LE) 
            consume();
        return comparisonReturn;
    }   

    public Expr orExpr() throws SyntaxException, LexicalException {
        Expr orReturn = andExpr();
        while (kind() == IToken.Kind.OR || kind() == IToken.Kind.BITOR) 
            consume();
        return orReturn;
    }

    public Expr multiplicativeExpr() throws SyntaxException, LexicalException {
        Expr multiplyReturn = unaryExpr();
        while (kind() == IToken.Kind.TIMES || kind() == IToken.Kind.DIV || kind() == IToken.Kind.MOD) 
            consume();
        return multiplyReturn;
    }
    
    public Expr powerExpr() throws SyntaxException, LexicalException {
        Expr powerReturn = additiveExpr();
        while(kind() == IToken.Kind.EXP)
            consume();
        return powerReturn;
    }
    
    public Expr conditionalExpr() throws LexicalException, SyntaxException {
    	Expr T;
    	Expr F;
        thruCondtional = true;
        currentToken = beginToken;
        if (kind() == IToken.Kind.RES_if){
            nextToken =  consume();
            Expr conditionalReturn = primaryExpr();
            while (nextToken.getKind() != IToken.Kind.QUESTION){
                 beginToken = nextToken;
                 nextToken = consume();
            }
            nextToken = consume();
            T = primaryExpr();
            while (nextToken.getKind() != IToken.Kind.QUESTION) {
                beginToken = nextToken;
                nextToken = consume();
            }
            nextToken = consume();
            F = primaryExpr();
            return new ConditionalExpr(currentToken, conditionalReturn, T, F);
        }
        else
           throw new SyntaxException("Error");
    }


}
