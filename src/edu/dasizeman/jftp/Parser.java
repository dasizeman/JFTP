package edu.dasizeman.jftp;

import java.util.HashSet;

public class Parser {
	public static  ParseMap Parse(String[] input, String[] flags) {
		if (input.length < 2 || flags.length < 1) {
			return null;
		}
		HashSet<String> requiredFlags = new HashSet<String>();
		ParseMap results = new ParseMap();
		for (String str : flags) {
			requiredFlags.add(str);
		}
		
		boolean shouldBeFlag = true;
		String lastFlag = "";
		
		int tokenIdx = 0;
		while (tokenIdx < input.length) {
			if (shouldBeFlag) {
				// Look for a required flag
				if (!requiredFlags.contains(input[tokenIdx])) {
					return null;
				}
				lastFlag = input[tokenIdx];
				// Handle the case where this is an empty flag at the end of the args
				if (tokenIdx == input.length-1) {
					requiredFlags.remove(lastFlag);
					results.put(lastFlag, "");
				}
			}
			else {
				// If this token is another flag, then the last flag had no argument,
				// which is ok
				if (input[tokenIdx].startsWith("-")) {
					results.put(lastFlag, "");
					tokenIdx--;
				} else {
					// Add the flag data to our results
					results.put(lastFlag, input[tokenIdx]);
				}
				requiredFlags.remove(lastFlag);
			}
			
			shouldBeFlag = !shouldBeFlag;
			tokenIdx++;
		}
		
		// If our required set of flags is not empty at this point, parsing failed
		return (requiredFlags.isEmpty())?results:null;
			
	}
	
}
