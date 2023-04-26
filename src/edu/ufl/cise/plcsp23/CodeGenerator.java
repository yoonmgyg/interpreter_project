package edu.ufl.cise.plcsp23;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.ast.*;

public class CodeGenerator implements ASTVisitor {
	HashSet<String> imports = new HashSet<String>(); 
	HashMap<String, Type> varTypes = new HashMap<String, Type>();
	String packageName;
	boolean convertToInt = false;
	
	public CodeGenerator(String name) {
		packageName = name;
	}
	
	private String TypeToString(Type type) {
		if (type == Type.STRING) {
			return "String";
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
		varTypes.put("return", programType);
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
		declaration.getNameDef().visit(this, sb);
		Expr decInit = declaration.getInitializer();	
		if (declaration.getNameDef().getType() == Type.INT) {
			convertToInt = true;
		}
			
		if (decInit != null) {
			sb.append("=");
			decInit.visit(this, sb);
			/*
			if (declaration.getNameDef().getType() == Type.STRING && decInit.getType()== Type.INT) {
				sb.append(" == ");
				NumLitExpr decInitNo = (NumLitExpr) declaration.getInitializer();
				sb.append(decInitNo.getValue());
				
			}*/
		}		
		convertToInt = false;
		return sb;
	}

	public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
		StringBuilder sb = (StringBuilder) arg;
		sb.append(TypeToString(nameDef.getType()) + " ");
		nameDef.getIdent().visit(this, sb);
		varTypes.put(nameDef.getIdent().getName(), nameDef.getType());
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
		if (binOp == Kind.EXP) {
			sb.append("Math.pow(");
			binaryExpr.getLeft().visit(this, sb);
			sb.append(",");
			binaryExpr.getRight().visit(this, sb);
		}
		else if (binOp == Kind.LT || binOp == Kind.GT || binOp == Kind.LE || binOp == Kind.GE || binOp == Kind.EQ || binOp == Kind.OR || binOp ==Kind.AND) {

			sb.append("(");
			if (convertToInt) {
				sb.append("(");
			}
			binaryExpr.getLeft().visit(this, sb);
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
			binaryExpr.getRight().visit(this, sb);
			sb.append(")");
			if (convertToInt) {
				sb.append("? 1 : 0)");
			}
			convertToInt = false;
		}
		else {
			sb.append("(");
			binaryExpr.getLeft().visit(this, sb);
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
			binaryExpr.getRight().visit(this, sb);
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
		statementAssign.getLv().visit(this, sb);
		sb.append(" = ");
		statementAssign.getE().visit(this, sb);

		/*
		else if (statementAssign.getLv().getIdent().getDef().getType() == Type.INT && statementAssign.getE().getType() == Type.STRING) {
			sb.append(" == ");
			sb.append(statementAssign.getE().get)
		}
		*/
		return sb;
	}


	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
		// TODO Auto-generated method stub
		return null;
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
		sb.append("ConsoleIO.write(");
		statementWrite.getE().visit(this, sb);
		sb.append(")");
		return sb;
	}

}
