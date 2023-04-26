package edu.ufl.cise.plcsp23;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.runtime.PixelOps;

public class CodeGenerator implements ASTVisitor {
	HashSet<String> imports = new HashSet<String>(); 
	String packageName;
	boolean convertToInt = false;
	
	public CodeGenerator(String name) {
		packageName = name;
	}
	
	private String TypeToString(Type type) {
		if (type == Type.STRING) {
			return "String";
		}
		else if (type == Type.PIXEL) {
			return "int";
			
		}
		else if (type == Type.IMAGE) {
			imports.add("java.awt.image.BufferedImage");
			return "BufferedImage";
		}
		else {
			return type.toString().toLowerCase();
		}
}
	
	public Object visitProgram(Program program, Object arg) throws PLCException {
		// class
		StringBuilder sb = new StringBuilder();
		if (packageName != "") {
			sb.append("package " + packageName + ";\n");
		}

		
		sb.append("public class "); 
		program.getIdent().visit(this, sb) ;
		sb.append(" {\n");

		// method
		Type programType = program.getType();
		sb.append("public static " + TypeToString(programType) + " apply(");
		
		// parameters
		List<NameDef> progParams = program.getParamList();
		Iterator<NameDef> iterator = progParams.iterator();
		while (iterator.hasNext()) {
			NameDef param = iterator.next();
			param.visit(this, sb);

	        if (iterator.hasNext()) {
	            sb.append(",");
	        }
		}
		
		// block
		sb.append(") {\n");
		program.getBlock().visit(this, sb);
		sb.append("}\n}");
		
		for (String imps : imports) {
			int packageCharacters = 0;
			if (packageName != "") {
				packageCharacters = 10 + packageName.length();
			}
			sb.insert(packageCharacters, "import " + imps + ";\n");
		}
		
		return sb.toString();
	}
	
	public Object visitBlock(Block block, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		
		// declaration list
		List<Declaration> decList = block.getDecList();
		for (Declaration declaration : decList) {
			declaration.visit(this, sb);
			sb.append(";\n");
		}
		
		// statement list
		List<Statement> statementList = block.getStatementList();
		for (Statement statement : statementList) {
			statement.visit(this, sb);
			sb.append(";\n");
		}
		
		return sb;
	}
	public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		NameDef decND = declaration.getNameDef();
		Expr decInit = declaration.getInitializer();				
		decND.visit(this, arg);		
		if (decND.getType() == Type.PIXEL) {
			imports.add("edu.ufl.cise.plcsp23.runtime.PixelOps");
			if (decInit != null) {
				sb.append(" = ");
				decInit.visit(this,arg);
			}
		}
		else if (decND.getType() == Type.IMAGE) {	
			// initializer
			if (decInit != null) {
				// null dimension
				Type initType = decInit.getType();
				if (decND.getDimension() == null) {
					switch(initType) {
						case STRING -> {
							sb.append(" = ");
							imports.add("edu.ufl.cise.plcsp23.runtime.FileURLIO");
							sb.append("FileURLIO.readImage(");
							decInit.visit(this, arg);
							sb.append(")");
						}
						case IMAGE -> {
							sb.append(" = ");
							imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
							sb.append("ImageOps.cloneImage(");
							decInit.visit(this, arg);
							sb.append(")");
						}
						case PIXEL -> {
							sb.append(" = ");
							imports.add("edu.ufl.cise.plcsp23.runtime.PixelOps");
							sb.append("PixelOps.pack(");
							decInit.visit(this, arg);
							sb.append(")");						}
						default -> {
							throw new PLCException("Invalid initializer type " + decInit.getType() + " with null dimension");
						}
					}
				}
				else if (decND.getDimension() != null){
					switch(initType) {
						case STRING -> {
							sb.append(" = ");
							imports.add("edu.ufl.cise.plcsp23.runtime.FileURLIO");
							sb.append("FileURLIO.readImage(");
							decInit.visit(this, arg);
							sb.append(",");	
							decND.getDimension().visit(this, arg);
							sb.append(")");
						}
						case IMAGE -> {
							sb.append(" = ");
							imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
							sb.append("ImageOps.copyAndResize(");
							decInit.visit(this, arg);
							sb.append(",");
							decND.getDimension().visit(this, arg);
							sb.append(")");						}
						case PIXEL -> {
							imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
							sb.append(" = ImageOps.makeImage(");
							decND.getDimension().getWidth().visit(this,arg);
							sb.append(",");
							decND.getDimension().getHeight().visit(this, arg);
							sb.append(");\n");
							sb.append("ImageOps.setAllPixels(");
							sb.append(decND.getIdent().getName() + ",");
							decInit.visit(this, arg);
							sb.append(")");
						}
						default -> {
							throw new PLCException("Invalid initializer type " + decInit.getType() + " with dimension");
						}
					}
				}
				// dimension
			}
			else if (decND.getDimension() != null) {
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				sb.append(" = ImageOps.makeImage(");
				decND.getDimension().getWidth().visit(this,arg);
				sb.append(",");
				decND.getDimension().getHeight().visit(this, arg);
				sb.append(")");
			} 
		}
		
		else {
			if (decND.getType() == Type.INT) {
				convertToInt = true;
			}
			if (decInit != null) {
				sb.append("=");
				decInit.visit(this, sb);
			}		
			convertToInt = false;
		}
		return sb;
	}

	public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		sb.append(TypeToString(nameDef.getType()) + " ");
		nameDef.getIdent().visit(this, sb);
		return sb;

	}
	
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		sb.append("(");
		Expr guardExpr = conditionalExpr.getGuard();
		if (guardExpr.getType() == Type.STRING) {
			sb.append("\"");
			guardExpr.visit(this, sb);
			sb.append("\"");
		}
		else if (guardExpr.getClass().getName() == "edu.ufl.cise.plcsp23.ast.IdentExpr") {
			sb.append("(");
			guardExpr.visit(this, sb);
			sb.append(" == 1)");
		}
		else {
			guardExpr.visit(this, sb);
		}
		sb.append(" ? ");
		
		Expr trueCase = conditionalExpr.getTrueCase(); 
		if (trueCase.getType() == Type.STRING) {
			sb.append("\"");
			trueCase.visit(this, sb);
			sb.append("\"");
		}		
		else {
			trueCase.visit(this, sb);
		}
		sb.append(" : ");

		Expr falseCase = conditionalExpr.getFalseCase(); 
		if (falseCase.getType() == Type.STRING) {
			sb.append("\"");
			falseCase.visit(this, sb);
			sb.append("\"");
		}		
		else {
			falseCase.visit(this, sb);
		}
		
		sb.append(")");
		return sb;
	}

	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;   
		Kind binOp = binaryExpr.getOp();
		Expr leftExpr = binaryExpr.getLeft();
		Expr rightExpr = binaryExpr.getRight();
		if (leftExpr.getType() == Type.IMAGE && rightExpr.getType() == Type.IMAGE) {
			if (binOp == Kind.PLUS || binOp == Kind.MINUS || binOp == Kind.TIMES || binOp == Kind.DIV || binOp == Kind.MOD) {		
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				sb.append("ImageOps.binaryImageImageOp(");
				switch (binOp) {
				case PLUS -> {
					sb.append("ImageOps.OP.PLUS");
				}
				case MINUS -> {
					sb.append("ImageOps.OP.MINUS");
				}
				case TIMES -> {
					sb.append("ImageOps.OP.TIMES");
				}
				case DIV -> {
					sb.append("ImageOps.OP.DIV");
				}

				case MOD -> {
					sb.append("ImageOps.OP.MOD");
				}
			}
			sb.append(",");
				leftExpr.visit(this, arg);
				sb.append(",");
				rightExpr.visit(this, arg);
				sb.append(")");

			}
			else {
				throw new PLCException("Invalid image operator");
			}
		}
		else if (leftExpr.getType() == Type.IMAGE && rightExpr.getType() == Type.INT) {
			if (binOp == Kind.PLUS || binOp == Kind.MINUS || binOp == Kind.TIMES || binOp == Kind.DIV || binOp == Kind.MOD) {		
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				sb.append("ImageOps.binaryImageScalarOp(");
				switch (binOp) {
					case PLUS -> {
						sb.append("ImageOps.OP.PLUS");
					}
					case MINUS -> {
						sb.append("ImageOps.OP.MINUS");
					}
					case TIMES -> {
						sb.append("ImageOps.OP.TIMES");
					}
					case DIV -> {
						sb.append("ImageOps.OP.DIV");
					}

					case MOD -> {
						sb.append("ImageOps.OP.MOD");
					}
				}
				sb.append(",");
				leftExpr.visit(this, arg);
				sb.append(",");
				rightExpr.visit(this, arg);
				sb.append(")");

			}
			else {
				throw new PLCException("Invalid image operator");
			}
		}
		else if (leftExpr.getType() == Type.PIXEL && rightExpr.getType() == Type.PIXEL) {
			if (binOp == Kind.PLUS || binOp == Kind.MINUS || binOp == Kind.TIMES || binOp == Kind.DIV || binOp == Kind.MOD) {		
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				sb.append("ImageOps.binaryPackedPixelPixelOp(");
				switch (binOp) {
				case PLUS -> {
					sb.append("ImageOps.OP.PLUS");
				}
				case MINUS -> {
					sb.append("ImageOps.OP.MINUS");
				}
				case TIMES -> {
					sb.append("ImageOps.OP.TIMES");
				}
				case DIV -> {
					sb.append("ImageOps.OP.DIV");
				}

				case MOD -> {
					sb.append("ImageOps.OP.MOD");
				}
			}
			sb.append(",");
				leftExpr.visit(this, arg);
				sb.append(",");
				rightExpr.visit(this, arg);
				sb.append(")");

			}
			else {
				throw new PLCException("Invalid image operator");
			}
		}
		else if (leftExpr.getType() == Type.PIXEL && rightExpr.getType() == Type.INT) {
			if (binOp == Kind.PLUS || binOp == Kind.MINUS || binOp == Kind.TIMES || binOp == Kind.DIV || binOp == Kind.MOD) {		
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				sb.append("ImageOps.binaryPackedPixelIntOp(");
				switch (binOp) {
				case PLUS -> {
					sb.append("ImageOps.OP.PLUS");
				}
				case MINUS -> {
					sb.append("ImageOps.OP.MINUS");
				}
				case TIMES -> {
					sb.append("ImageOps.OP.TIMES");
				}
				case DIV -> {
					sb.append("ImageOps.OP.DIV");
				}

				case MOD -> {
					sb.append("ImageOps.OP.MOD");
				}
			}
			sb.append(",");
				leftExpr.visit(this, arg);
				sb.append(",");
				rightExpr.visit(this, arg);
				sb.append(")");

			}
			else {
				throw new PLCException("Invalid image operator");
			}
		}
		else if (binOp == Kind.EXP) {
			sb.append("Math.pow(");
			leftExpr.visit(this, sb);
			sb.append(",");
			rightExpr.visit(this, sb);
		}
		else if (binOp == Kind.LT || binOp == Kind.GT || binOp == Kind.LE || binOp == Kind.GE || binOp == Kind.EQ || binOp == Kind.OR || binOp ==Kind.AND) {

			sb.append("(");
			if (convertToInt) {
				sb.append("(");
			}
			leftExpr.visit(this, sb);
			switch (binOp) {
				case LT -> sb.append("<");
				case GT -> sb.append(">");
				case LE -> sb.append("<=");
				case GE -> sb.append(">=");
				case EQ -> sb.append("==");
				case OR -> sb.append("||");
				case AND -> sb.append("&");
			}
			sb.append(" ");
			rightExpr.visit(this, sb);
			sb.append(")");
			if (convertToInt) {
				sb.append("? 1 : 0)");
			}
			convertToInt = false;
		}
		else {
			sb.append("(");
			leftExpr.visit(this, sb);
			sb.append(" ");
			switch (binOp) {
				case PLUS -> sb.append("+");
				case MINUS -> sb.append("-");
				case TIMES -> sb.append("*");
				case DIV -> sb.append("/");
				case MOD -> sb.append("%");
				case BITOR -> sb.append("|");
				case BITAND -> sb.append("&&");
			}
			sb.append(" "); 
			rightExpr.visit(this, sb);
			sb.append(")");
		}			
		return sb;
	}


	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;       
		sb.append(stringLitExpr.getValue());
		return sb;
	}

	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		sb.append(identExpr.getName());
		return sb;	
	}

	public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		sb.append(zExpr.getValue());
		return sb;
	}

	public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		sb.append(Math.floor(Math.random() * 256));
		return sb;
	}
	
	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		LValue leftVal = statementAssign.getLv();
		Expr rightVal = statementAssign.getE();
		System.out.println(leftVal.getType());

		if (leftVal.getType() == Type.PIXEL) {	
			leftVal.visit(this, arg);
			sb.append(" = ");
			sb.append("PixelOps.pack(");
			rightVal.visit(this, arg);
			sb.append(")");
		}
		
		else if (leftVal.getType() == Type.IMAGE) {
			if (leftVal.getColor() == null  && leftVal.getPixelSelector() == null) {
				if (rightVal.getType() == Type.STRING) {
					imports.add("edu.ufl.cise.plcsp23.runtime.FileURLIO");
					imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
					sb.append("ImageOps.copyInto(FileURLIO.readImage(\"");
					rightVal.visit(this, arg);
					sb.append(") ," + leftVal.getIdent().getName() + ")");
					
				}
				else if (rightVal.getType() == Type.IMAGE) {
					imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
					sb.append("ImageOps.copyInto(");
					rightVal.visit(this, arg);
					sb.append(" ," + leftVal.getIdent().getName() + ")");
					
				}
				else if (rightVal.getType() == Type.PIXEL) {
					imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
					sb.append("ImageOps.setAllPixels(");
					sb.append(leftVal.getIdent().getName() + ",");
					rightVal.visit(this, arg);
					sb.append(")");
					
				}
				else {
					throw new PLCException("Invalid assignment type to Image");
				}
			}
			else if (leftVal.getColor() == null  && leftVal.getPixelSelector() != null) {
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				sb.append("for (int y = 0; y !=" + leftVal.getIdent().getName() + ".getHeight(); y++}){\n"
						+ "for (int x = 0; x !=" + leftVal.getIdent().getName() + ".getWidth(); x++){\n"
						+ "ImageOps.setRGB(" + leftVal.getIdent().getName() + ",x,y," 
						+ "ImageOps.getRGB(");
				rightVal.visit(this, arg);
				sb.append(")); }}");
						
			}
			else if (leftVal.getColor() != null  && leftVal.getPixelSelector() != null) {
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				imports.add("edu.ufl.cise.plcsp23.runtime.PixelOps");
				sb.append("for (int y = 0; y !=" + leftVal.getIdent().getName() + ".getHeight(); y++}){\n"
						+ "for (int x = 0; x !=" + leftVal.getIdent().getName() + ".getWidth(); x++){\n"
						+ "ImageOps.setRGB(" + leftVal.getIdent().getName() + ",x,y," 
						+ "PixelOps.set");
				switch (leftVal.getColor()) {
					case red -> {
						sb.append("Red");
					}
					case grn -> {
						sb.append("Grn");
					}
					case blu -> {
						sb.append("Blu");
					}
				}
				sb.append("(");
				rightVal.visit(this, arg);
				sb.append(")); }}");
						
			}
			else {
				throw new PLCException("Invalid type for image assignment");
			}
			

		}
		else {		
			leftVal.visit(this, arg);
			sb.append(" = ");
			rightVal.visit(this, sb);
		}
		return sb;
	}


	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		dimension.getWidth().visit(this, arg);
		sb.append(",");
		dimension.getHeight().visit(this, arg);
		return sb;
	}

	@Override
	public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		imports.add("edu.ufl.cise.plcsp23.runtime.PixelOps");
		sb.append("PixelOps.pack(");
		expandedPixelExpr.getRedExpr().visit(this, arg);
		sb.append(",");
		expandedPixelExpr.getGrnExpr().visit(this, arg);
		sb.append(",");
		expandedPixelExpr.getBluExpr().visit(this, arg);
		sb.append(")");
		return sb;
	
	}

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		sb.append(ident.getName());
		return sb;	
	}

	@Override
	public Object visitLValue(LValue lValue, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		lValue.getIdent().visit(this, sb);
		return sb;
	}

	@Override
	public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		sb.append(numLitExpr.getValue());
		return sb;	
	}

	@Override
	public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		pixelSelector.getX().visit(this, arg);
		sb.append(",");
		pixelSelector.getY().visit(this, arg);
		return sb;
	}

	@Override
	public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		return sb;
	}


	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		sb.append("return ");
		returnStatement.getE().visit(this, sb);
		return sb;
	}


	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		switch (unaryExpr.getOp()) {
		case BANG -> {
			sb.append("((");
			unaryExpr.getE().visit(this, arg);
			sb.append(") == 0 ? 1 : 0)");
		}
		case MINUS -> {
			sb.append("-");
			sb.append("(");
			unaryExpr.getE().visit(this, arg);
			sb.append(")");
		}
		}
		return sb;
	}

	@Override
	public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		System.out.println(unaryExprPostfix.getPrimary().getType());
		if (unaryExprPostfix.getPrimary().getType() == Type.IMAGE) {
			if (unaryExprPostfix.getPixel() != null && unaryExprPostfix.getColor() == null) {
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				sb.append("ImageOps.getRGB(");
				unaryExprPostfix.getPrimary().visit(this, arg);
				sb.append(",");
				unaryExprPostfix.getPixel().visit(this, arg);
				sb.append(")");
			}
			else if (unaryExprPostfix.getPixel()!= null && unaryExprPostfix.getColor() != null){
				imports.add("edu.ufl.cise.plcsp23.runtime.PixelOps");
				sb.append("PixelOps.");
				sb.append(unaryExprPostfix.getColor().toString());
				sb.append("(");
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				sb.append("ImageOps.getRGB(");
				unaryExprPostfix.getPrimary().visit(this, arg);
				sb.append(",");
				unaryExprPostfix.getPixel().visit(this, arg);
				sb.append("))");
			}
			else if (unaryExprPostfix.getPixel()== null && unaryExprPostfix.getColor() != null){
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				sb.append("ImageOps.extract");
				switch (unaryExprPostfix.getColor()) {
					case red -> {
						sb.append("Red");
					}
					case grn -> {
						sb.append("Grn");
					}
					case blu -> {
						sb.append("Blu");
					}
				}
				sb.append("(");
				unaryExprPostfix.getPrimary().visit(this, arg);
				sb.append(")");
			}
			else if (unaryExprPostfix.getPixel() != null && unaryExprPostfix.getColor() == null){
				imports.add("edu.ufl.cise.plcsp23.runtime.ImageOps");
				sb.append("ImageOps.extract");
				switch (unaryExprPostfix.getColor()) {
					case red -> {
						sb.append("Red");
					}
					case grn -> {
						sb.append("Grn");
					}
					case blu -> {
						sb.append("Blu");
					}
				}
				sb.append("(");
				unaryExprPostfix.getPrimary().visit(this, arg);
				sb.append("))");
			}
			else {
				throw new PLCException("Invalid unaryExprPostfix");
			}
		}
		else if (unaryExprPostfix.getPrimary().getType() == Type.PIXEL && unaryExprPostfix.getColor() != null) {
			imports.add("edu.ufl.cise.plcsp23.runtime.PixelOps");
			sb.append("PixelOps.");
			sb.append(unaryExprPostfix.getColor().toString());
			sb.append("(");
			unaryExprPostfix.getPrimary().visit(this, arg);
			sb.append(")");
		}
		
		else {
			throw new PLCException ("Invalid unary postfix primary type " + unaryExprPostfix.getPrimary().getType());
		}
		return sb;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		sb.append("while (");
		whileStatement.getGuard().visit(this, sb);
		sb.append(") {\n");
		whileStatement.getBlock().visit(this, sb);
		sb.append("}");
		return sb;
	}

	@Override
	public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		imports.add("edu.ufl.cise.plcsp23.runtime.ConsoleIO");
		if (statementWrite.getE().getType() == Type.PIXEL) {							
			imports.add("edu.ufl.cise.plcsp23.runtime.PixelOps");
			sb.append("ConsoleIO.writePixel(");
			statementWrite.getE().visit(this, sb);
			sb.append(")");
		}
		else {
			sb.append("ConsoleIO.write(");
			statementWrite.getE().visit(this, sb);
			sb.append(")");
		}
		return sb;
	}

}
