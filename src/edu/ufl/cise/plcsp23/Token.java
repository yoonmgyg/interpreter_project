package edu.ufl.cise.plcsp23;

public class Token implements IToken {
	final Kind kind;
	final int pos;
	final int length;
	final char[] source;
	final SourceLocation location;
	
	public Token(Kind kind, int pos, int length, char[] source, int line, int column) {
		  super();
		  this.kind = kind;
		  this.pos = pos;
		  this.length = length;
		  this.source = source;
		  this.location = new SourceLocation(line, column);
		}
		
	public SourceLocation getSourceLocation() {
		// TODO Auto-generated method stub
		return location;
	}

	@Override
	public Kind getKind() {
		return kind;
	}
	
	public int getLength() {
		return length;
	}
	@Override
	public String getTokenString() {
		// TODO Auto-generated method stub
		return String.valueOf(source);
	}

}
