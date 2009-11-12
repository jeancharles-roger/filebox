package org.kawane.filebox.search;

/**
 * Boyer moore
 * @author <a href="maito:laurent.legoff@geensys.com">Laurent Le Goff </a>
 *
 */
public class Search {
	static private boolean debug = true;

	static public int search(byte[] text, byte[] pattern) {
		int[] last = computeLast(pattern);
		int[] suffix = computeSuffix(pattern);
		int[] match = computeMatch(pattern, suffix);
		// Searching
		return search(text, pattern, last, match);

	}

	static private int search(byte[] text, byte[] pattern, int[] last, int[] match) {
		int textCursor = pattern.length - 1;
		int patternCursor = pattern.length - 1;
		while (textCursor < text.length) {
			debug(text, text.length, textCursor);
			if (pattern[patternCursor] == text[textCursor]) {
				if (patternCursor == 0) {
					return textCursor;
				}
				patternCursor--;
				textCursor--;
			} else {
				textCursor += pattern.length - patternCursor - 1 + Math.max(patternCursor - last[getIndex(text[textCursor])], match[patternCursor]);
				patternCursor = pattern.length - 1;
			}
		}
		return -1;
	}

	static public void debug(byte[] text, int length, int textCursor) {
		if (debug) {
			System.err.println(new String(text, 0, length).replace("\n", " ").replace("\r", " "));
			for (int i = 0; i < textCursor; i++) {
				System.err.print(" ");
			}
			System.err.println("^");
		}
	}

	static public int[] computeLast(byte[] pattern) {
		int[] last = new int[256];
		for (int i = 0; i < last.length; i++) {
			last[i] = -1;
		}
		for (int i = pattern.length - 1; i >= 0; i--) {
			int index = getIndex(pattern[i]);
			if (last[index] < 0) {
				last[index] = i;
			}
		}
		return last;
	}

	static public int getIndex(byte b) {
		return Byte.MAX_VALUE + b;
	}

	/**
	  * Computes the values of suffix, which is an auxiliary array, 
	  * backwards version of the KMP failure function.
	  * 
	  * suffix[i] = the smallest j > i s.t. p[j..m-1] is a prefix of p[i..m-1],
	  * if there is no such j, suffix[i] = m, i.e. 

	  * p[suffix[i]..m-1] is the longest prefix of p[i..m-1], if suffix[i] < m.
	  */
	static public int[] computeSuffix(byte[] pattern) {
		int[] suffix = new int[pattern.length];
		suffix[suffix.length - 1] = suffix.length;
		int j = suffix.length - 1;
		for (int i = suffix.length - 2; i >= 0; i--) {
			while (j < suffix.length - 1 && pattern[j] != pattern[i]) {
				j = suffix[j + 1] - 1;
			}
			if (pattern[j] == pattern[i]) {
				j--;
			}
			suffix[i] = j + 1;
		}
		return suffix;
	}

	static public int[] computeMatch(byte[] pattern, int[] suffix) {
		int[] match = new int[suffix.length];
		/* Phase 1 */
		for (int j = 0; j < match.length; j++) {
			match[j] = match.length;
		} //O(m) 

		/* Phase 2 */
		//Uses an auxiliary array, backwards version of the KMP failure function.
		//suffix[i] = the smallest j > i s.t. p[j..m-1] is a prefix of p[i..m-1],
		//if there is no such j, suffix[i] = m

		//Compute the smallest shift s, such that 0 < s <= j and
		//p[j-s]!=p[j] and p[j-s+1..m-s-1] is suffix of p[j+1..m-1] or j == m-1}, 
		//                                                         if such s exists,
		for (int i = 0; i < match.length - 1; i++) {
			int j = suffix[i + 1] - 1; // suffix[i+1] <= suffix[i] + 1
			if (suffix[i] > j) { // therefore pattern[i] != pattern[j]
				match[j] = j - i;
			} else {// j == suffix[i]
				match[j] = Math.min(j - i + match[i], match[j]);
			}
		}

		/* Phase 3 */
		//Uses the suffix array to compute each shift s such that
		//p[0..m-s-1] is a suffix of p[j+1..m-1] with j < s < m
		//and stores the minimum of this shift and the previously computed one.
		if (suffix[0] < pattern.length) {
			for (int j = suffix[0] - 1; j >= 0; j--) {
				if (suffix[0] < match[j]) {
					match[j] = suffix[0];
				}
			}
			int j = suffix[0];
			for (int k = suffix[j]; k < pattern.length; k = suffix[k]) {
				while (j < k) {
					if (match[j] > k)
						match[j] = k;
					j++;
				}
			}
		}
		return match;
	}

	public static void main(String[] args) {
		String text = "hello tata titi tutu toto!";
		int result = text.lastIndexOf("toto");
		System.out.println(result);
		result = Search.search(text.getBytes(), "toto".getBytes());
		System.out.println(result);
	}

}
