
package edu.ufl.cise.plcsp23;

public class NumLitToken extends Token implements INumLitToken {

	public NumLitToken(Kind kind, int pos, int length, char[] source, int line, int column) {
		super(kind, pos, length, source, line, column);
	}

	public int getValue() {
		return Integer.parseInt(String.valueOf(source));
	}

}
