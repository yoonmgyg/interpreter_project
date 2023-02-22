package edu.ufl.cise.plcsp23;

public class StringLitToken extends Token implements IStringLitToken{

	public StringLitToken(Kind kind, int pos, int length, char[] source, int line, int column) {
		super(kind, pos, length, source, line, column);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		String text = String.valueOf(source);
		text = text.substring(1, text.length()-1);

		String stringValue = "";
		boolean backslash=false;
		for (char ch : text.toCharArray()) {
			if (ch == '\\') {
				backslash=true;
			}
			else if (backslash) {
				switch (ch) {
					case 't' -> {
						stringValue += '\t';
					}
					case 'n' -> {
						stringValue += '\n';
					}
					case 'b' -> {
						stringValue += '\b';
					}
					case 'r' -> {
						stringValue += '\r';
					}
					case '"' -> {
						stringValue += '"';
					}
				}
				backslash = false;
			}
			else {
				stringValue += ch;
			}
		}
		return stringValue;
		
	}

}
