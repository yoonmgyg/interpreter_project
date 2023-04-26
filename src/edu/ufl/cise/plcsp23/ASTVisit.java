package edu.ufl.cise.plcsp23;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import edu.ufl.cise.plcsp23.ast.*;

public class ASTVisit implements ASTVisitor{
    
    int scope = 0;
    int tracker;
    Symbol_Table table = new Symbol_Table();
    boolean checker;
    
        
    public boolean typeCompCheck(Type left, Type right) {
    	
        if (left == Type.PIXEL && ((right == Type.PIXEL) || (right == Type.INT))) 
                return true;
        else if (left == Type.INT && ((right == Type.INT) || (right == Type.PIXEL))) 
                return true;
            
        else if (left == Type.IMAGE && ((right == Type.IMAGE) || (right == Type.PIXEL) || (right == Type.STRING))) 
                return true;
            
        else if (left == Type.STRING && ((right == Type.STRING) || (right == Type.IMAGE) || (right == Type.PIXEL) || (right == Type.INT)))           
                return true;            
        
        else
        	return false;
    }
    
    public static class Symbol_Table {
    	int inscope = 0;
    	boolean outscope = false;
        HashMap <String, ArrayList<scopeND>> table = new HashMap <String, ArrayList<scopeND>>();
        int i;
        
        public void editST(String name, NameDef nameDef, int scope) throws TypeCheckException {
            if (table.containsKey(name) && !outscope) {
            	while (i < table.get(name).size()) {               
                    if (table.get(name).get(i).scope == scope && !outscope) 
                        throw new TypeCheckException("Duplicate in scope");                    
                	}
                	table.get(name).add(new scopeND(scope, nameDef));
                	inscope++;
                	i++;
            }
            
            else {
            	inscope++;
                ArrayList<scopeND> temp = new ArrayList<scopeND>();
                temp.add(new scopeND(scope, nameDef));
                inscope--;
                table.put(name, temp);
            }
            
        }
        
        class scopeND {       	
            NameDef nameDef;
            int scope;
            boolean defined;
            
            scopeND(int scope, NameDef nameDef) {
                this.scope = scope;
                this.nameDef = nameDef;
                this.defined = false;
            }
        }
        

        
        public NameDef returnND(String name) throws TypeCheckException {
            if (table.containsKey(name)) {
            	if (table.get(name).size() != 0) {
                return table.get(name).get(0).nameDef;
            	}
            	else {
            		throw new TypeCheckException("Variable not declared in scope");
            	}
                }
            return null;
        }
        
        public void whileST (int scope) {
        	i = 0;
            for (String key : table.keySet())
            	while (i < table.get(key).size()) {
                    if (table.get(key).get(i).scope == scope) 
                        table.get(key).remove(i);
                    i++;
            	}
        }
        
        public boolean findWithND(NameDef Namedef, int scope) {
        	i = 0;      
            if (table.containsKey(Namedef.getIdent().getName())) {            	
            	while (i < table.get(Namedef.getIdent().getName()).size()) {                
                    if (table.get(Namedef.getIdent().getName()).get(i).scope == scope) {
                        return true;
                    }
                    i++;
                }
            }
            return false;
        }
        


        public boolean findWithName(String name, int scope) {
        	i = 0;
            if (table.containsKey(name)) {
            	while (i < table.get(name).size()) {                
                    if (table.get(name).get(i).scope == scope) 
                        return true;                   
                    i++;
                }
            }
            return false;
        }
        

    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
    	Expr vASType;
    	vASType = statementAssign.getE();
    	Type vASType2;
    	vASType2 = (Type) vASType.visit(this, arg);
    	LValue vASLValue;
    	vASLValue = statementAssign.getLv();        
        Type vASLValueType;
        vASLValueType = (Type) vASLValue.visit(this, arg);        
        if((vASLValueType == Type.IMAGE && vASType2 != Type.IMAGE && vASType2 != Type.PIXEL && vASType2 != Type.STRING) ||
        		(vASLValueType == Type.PIXEL && vASType2 != Type.PIXEL && vASType2 != Type.INT)||
        		(vASLValueType == Type.INT && vASType2 != Type.INT && vASType2 != Type.PIXEL) ||
        		(vASLValueType == Type.STRING && vASType2 != Type.STRING && vASType2 != Type.INT && vASType2 != Type.PIXEL && vASType2 != Type.IMAGE)){
                throw new TypeCheckException("Mismatch");
        }
        else
        	return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException{
        Type resultType;
        resultType = null;        
        Type binLeft;
        binLeft = (Type) binaryExpr.getLeft().visit(this, arg);
        Type binRight;
        binRight = (Type) binaryExpr.getRight().visit(this, arg);
        IToken.Kind switchOp;
        switchOp = binaryExpr.getOp();
        
        if (resultType == null)
        	checker = true;

        switch(switchOp){
        case LT, GT, LE, GE -> {
        	
            if(binLeft == Type.INT && binRight == Type.INT && checker)
                resultType = Type.INT;
            
             else 
            	throw new TypeCheckException("Error");   
            
        }
        case EQ -> {
        	
            if((binLeft == Type.INT && binRight == Type.INT) ||
            	(binLeft == Type.PIXEL && binRight == Type.PIXEL) || 
            	(binLeft == Type.IMAGE && binRight == Type.IMAGE) || 
            	(binLeft == Type.STRING && binRight == Type.STRING))
            	
                resultType = Type.INT;   
            
             else 
            	throw new TypeCheckException("Error");                
        }
            case BITOR, BITAND -> {
            	
                if (binLeft == Type.PIXEL && binRight == Type.PIXEL && checker)
                    resultType = Type.PIXEL;
                
                 else 
                	throw new TypeCheckException("Error");  
                
            }
            case OR, AND -> {
            	
                if(binLeft == Type.INT && binRight == Type.INT && checker)
                    resultType = Type.INT;
                
                 else 
                	throw new TypeCheckException("Error"); 
                
            }

            case MINUS -> {
            	
                if(binLeft == Type.INT && binRight == Type.INT && checker)
                    resultType = Type.INT;
                
                 else if(binLeft == Type.PIXEL && binRight == Type.PIXEL && checker)
                    resultType = Type.PIXEL;
                
                else if(binLeft == Type.IMAGE && binRight == Type.IMAGE && checker)
                    resultType = Type.IMAGE;
                
                  else
                	throw new TypeCheckException("Error"); 
              
            }
            case TIMES, DIV, MOD -> {
                if (binLeft == Type.INT && binRight == Type.INT && checker)
                    resultType = Type.INT;
                
                 else if ((binLeft == Type.PIXEL && binRight == Type.PIXEL && checker)||
                		 (binLeft == Type.PIXEL && binRight == Type.INT && checker))
                    resultType = Type.PIXEL;
                
                 else if ((binLeft == Type.IMAGE && binRight == Type.IMAGE && checker) || 
                		 (binLeft == Type.IMAGE && binRight == Type.INT && checker))
                    resultType = Type.IMAGE;
                
                 else 
                	throw new TypeCheckException("Error");                
            }
            
            case EXP -> {
                 if(binLeft == Type.INT && binRight == Type.INT && checker)
                    resultType = Type.INT;
                 
                 else if(binLeft == Type.PIXEL && binRight == Type.INT && checker)
                    resultType = Type.PIXEL;
                 
                 else 
                	throw new TypeCheckException("Error");    
                 
            }
            case PLUS -> {
                if(binLeft == Type.INT && binRight == Type.INT && checker){
                    resultType = Type.INT;
                } else if(binLeft == Type.PIXEL && binRight == Type.PIXEL && checker){
                    resultType = Type.PIXEL;
                } else if(binLeft == Type.IMAGE && binRight == Type.IMAGE && checker){
                    resultType = Type.IMAGE;
                } else if(binLeft == Type.STRING && binRight == Type.STRING && checker){
                    resultType = Type.STRING;
                } else {
                	throw new TypeCheckException("Error");                }
            }

            default -> {
                throw new TypeCheckException("Error");
            }
        }
        binaryExpr.setType(resultType);
        return resultType;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException{
    	int count = 0;
    	int listSize = 0;
    	listSize = block.getDecList().size();
    	while (count < listSize) {
            block.getDecList().get(count).visit(this, arg);
            count++;
    	}
    	count = 0;
    	listSize = block.getStatementList().size();
    	while (count < listSize) {
            block.getStatementList().get(count).visit(this, arg);
            count++;
    	}
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException{
    	
        if((Type) conditionalExpr.getGuard().visit(this, arg) != Type.INT) 
            throw new TypeCheckException("Not Int");
        
        if((Type) conditionalExpr.getTrueCase().visit(this, arg) != (Type) conditionalExpr.getFalseCase().visit(this, arg))
            throw new TypeCheckException("True != False");
        
        conditionalExpr.setType((Type) conditionalExpr.getTrueCase().visit(this, arg));
        return conditionalExpr.getType();
        
    }

    @Override
   public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException{
        NameDef vdNameDef;
        vdNameDef = declaration.getNameDef();
        Expr vdExpr;;
        vdExpr = declaration.getInitializer();
        Type visitor;
        checker = false;
        // System.out.println("declaration " + vdNameDef.getIdent());
        if(vdNameDef.getType() == Type.IMAGE && (vdExpr == null && vdNameDef.getDimension() == null))
                throw new TypeCheckException("var without initializer/dimension");
        
        if(table.findWithName(vdNameDef.getIdent().getName(), scope) && !checker)
            throw new TypeCheckException("varDec Error");
        
        else if(table.findWithND(vdNameDef, scope) && !checker)
            throw new TypeCheckException("nameDef error");
                    
        else if (!checker){
        	// System.out.println("variable declared");
        	if (vdExpr != null) {
        		visitor = (Type) vdExpr.visit(this, arg);
        	}
            table.editST(vdNameDef.getIdent().getName(), vdNameDef, scope);
        }
        
        return null;
    }

    @Override
   public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
    	Type visitor1 , visitor2;
        visitor1 = (Type) dimension.getWidth().visit(this, arg);
        visitor2 = (Type) dimension.getHeight().visit(this, arg);
        if(visitor1 != Type.INT || visitor2 != Type.INT)
            throw new TypeCheckException("Width/Height must be INT");
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException{
        expandedPixelExpr.setType(Type.PIXEL);
        if(		(Type) expandedPixelExpr.getBluExpr().visit(this, arg) != Type.INT || 
        		(Type) expandedPixelExpr.getGrnExpr().visit(this, arg) != Type.INT ||
        		(Type) expandedPixelExpr.getRedExpr().visit(this, arg) != Type.INT){
            throw new TypeCheckException("Not of Type INT");
        }
        return (Type) expandedPixelExpr.getType();
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException{
        String name;
        name = ident.getName();
        ident.visit(this, arg);
        checker = true;
        if(table.findWithName(name, scope) && checker){
            throw new TypeCheckException("Undefined Variable");
        }
        return null;
    }
    

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException{
        NameDef vieNameDef = null;
        Type returnerType;
        if (vieNameDef == null)
        	checker = false;
        vieNameDef = table.returnND(identExpr.getName());   
        if(table.returnND(identExpr.getName()) == null && !checker){
        	System.out.println(Arrays.asList(table));
            throw new TypeCheckException("Variable " + identExpr.getName() + " is not defined");
        }
        returnerType = vieNameDef.getType();
        return returnerType;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException{
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException{
    	
        if (nameDef.getType() == Type.VOID || (table.findWithName(nameDef.getIdent().getName(), scope)) || 
        		(nameDef.getDimension() != null && nameDef.getType() != Type.IMAGE)){
            throw new TypeCheckException("Error");
        }
        
        else if (nameDef.getDimension() != null) 
            nameDef.getDimension().visit(this, arg); 
           
        table.editST(nameDef.getIdent().getName(), nameDef, scope);
        
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException{
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException{
        pixelFuncExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException{
    	
        if((Type) pixelSelector.getX().visit(this, arg) != Type.INT)
            throw new TypeCheckException("X Value not Int");
        
        else if((Type) pixelSelector.getY().visit(this, arg) != Type.INT)
            throw new TypeCheckException("Y Value not Int");
        
        return Type.PIXEL;
    }

    @Override
    public  Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException{
        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }
    
    Type returnerType;
    
    
    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException{
    	checker = true;
        returnerType =  program.getType();
        
        for(int i = 0; i < program.getParamList().size(); i++) {
            program.getParamList().get(i).visit(this, arg);
            tracker++;
        }

        if (checker)
        	program.getBlock().visit(this, arg);

        return program.getType();
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException{
        randomExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg)throws PLCException {
    	
        if((Type) returnStatement.getE().visit(this, arg) != returnerType)
            throw new TypeCheckException("Program-Return match error");
        else
        	return (Type) returnStatement.getE().visit(this, arg);
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException{
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
    	
        switch(unaryExpr.getOp()){
        
        case MINUS, RES_cos, RES_sin, RES_atan -> {
            if((Type) unaryExpr.getE().visit(this, arg) != Type.INT)
            	throw new TypeCheckException("Error");
            
        } 
        
            case BANG -> {
            	
                if((Type) unaryExpr.getE().visit(this, arg) != Type.INT && (Type) unaryExpr.getE().visit(this, arg) != Type.PIXEL)
                	throw new TypeCheckException("Error");
                
            }
              
            default -> {
                throw new TypeCheckException("Switch Error");
            }
        }

        return (Type) unaryExpr.getE().visit(this, arg);
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        if((Type) unaryExprPostfix.getPixel().visit(this, arg) == null && unaryExprPostfix.getColor() == null){
            throw new TypeCheckException("Cannot create unary expression postfix without pixel or color");
        }

        if ((Type) unaryExprPostfix.getPrimary().visit(this, arg) == Type.PIXEL) {
        	
            if((Type) unaryExprPostfix.getPixel().visit(this, arg) == null)
                return Type.INT;
            else 
                throw new TypeCheckException("UEXPFError");
            
        } 
        
        else if((Type) unaryExprPostfix.getPrimary().visit(this, arg) == Type.IMAGE) {
        	
            if((Type) unaryExprPostfix.getPixel().visit(this, arg) == null && unaryExprPostfix.getColor() != null)
                return Type.IMAGE;
             else if((Type) unaryExprPostfix.getPixel().visit(this, arg) != null && unaryExprPostfix.getColor() == null)
                return Type.PIXEL;
             else 
                return Type.INT;
            
        } 
        
        else 
            throw new TypeCheckException("UEXPFError");

    }

    @Override
    
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
    	boolean inscope;
       Type guardType = (Type) whileStatement.getGuard().visit(this, arg);
       inscope = false;
       int i;
       i = scope;
        if (guardType != Type.INT && !inscope)
            throw new TypeCheckException("Error"); 
        
        i++;
        inscope = true;
        if (inscope) {
        	scope = i;
        	whileStatement.getBlock().visit(this, arg);
        	table.whileST(i);
        	scope--;
        }
        return null;

    }

    @Override

    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        return (Type) statementWrite.getE().visit(this, arg);
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        zExpr.setType(Type.INT);
        return zExpr.getType();
    }

}
