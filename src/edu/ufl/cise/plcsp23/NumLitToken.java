
package edu.ufl.cise.plcsp23;

public class NumLitToken extends Token implements INumLitToken{

	public NumLitToken(Kind kind, int pos, int length, char[] source, int line, int column) {
		super(kind, pos, length, source, line, column);
	}

	public int getValue() {
		// TODO Auto-generated method stub
		return 0;
	}

}
