package edu.dasizeman.jftp;

import java.util.HashMap;
import java.util.HashSet;

public class Parser {
	public static  HashMap<String,String> Parse(String[] input, String[] flags) {
		if (input.length < 2 || flags.length < 1) {
			return null;
		}
		HashSet<String> requiredFlags = new HashSet<String>();
		HashMap<String,String> results = new HashMap<String,String>();
		for (String str : flags) {
			requiredFlags.add(str);
		}
		
		boolean shouldBeFlag = true;
		String lastFlag = "";
		
		for (String token : input) {
			if (shouldBeFlag) {
				// Look for a required flag
				if (!requiredFlags.contains(token)) {
					return null;
				}
				lastFlag = token;
			}
			else {
				// Add the flag data to our results
				results.put(lastFlag, token);
				requiredFlags.remove(lastFlag);
			}
			
			shouldBeFlag = !shouldBeFlag;
		}
		
		// If our required set of flags is not empty at this point, parsing failed
		return (requiredFlags.isEmpty())?results:null;
			
	}
	
}
