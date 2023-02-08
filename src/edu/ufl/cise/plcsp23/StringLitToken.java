
package edu.ufl.cise.plcsp23;

public class StringLitToken extends Token implements IStringLitToken{

	public StringLitToken(Kind kind, int pos, int length, char[] source, int line, int column) {
		super(kind, pos, length, source, line, column);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return source.toString();
	}

}
