package org.kawane.filebox.json;

public interface JSON {
	 final int END_DOCUMENT = -1;
	 final int START = 0;
	 final int START_DOCUMENT = 1;
	 final int START_OBJECT = 2;
	 final int END_OBJECT = 3;
	 final int MEMBER = 4;
	 final int VALUE = 5;
	 final int START_ARRAY = 6;
	 final int END_ARRAY = 7;
	 final int STRING_TYPE = 1;
	 final int NUMBER_TYPE = 2;
	 final int BOOLEAN_TYPE = 3;
	 final int NULL_TYPE = 4;
	 final int UNKNOWN_TYPE = 5;
	// KEYWORD
	 final String TRUE = "true";
	 final String FALSE = "false";
	 final String NULL = "null";
}