package com.holitek.smoosh.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 *  methods to create and read the 31 resource files containing the c(32/k) 
 *  data. there are 32bits worth of entries, so the files can get quite large.
 * @author snerd
 *
 */
public class ResourceFileUtil {

	/**
	 * creates the resource files
	 * @return
	 */
	public static boolean buildResourceFiles() {
		boolean successB = false;
		
		Map<Integer, Integer[]> tableElementMap = 
				new HashMap<Integer, Integer[]>();
				
		int rangeCeilingI = (int)Math.pow(31, 2);		
				
		for (int i = 1; i < 32; i ++) {
			
			for (int k = 0; k < rangeCeilingI; k ++) {
				String valueAsBinaryS = Integer.toBinaryString(0);
				
				if (checkOnesCount(valueAsBinaryS, i)) {
					tableElementMap.put(key, value)
				}
				
				
			}
			
// serialize da file
			
		}
		
		
		
		return successB;
	}
	
	
	
	
	public static int elementIndexToTableElementIndex(int elementIndexI) {
		int returnI = -1;
		
		
		return returnI;
	}
	
	
	
	public static int TableElementIndexToElementIndex(int tableElementIndexI) {
		int returnI = -1;
		
		
		return returnI;
	}
	
	

	/**
	 * counts the number of '1's in a binary string. credit to SO for this
	 * solution: http://stackoverflow.com/a/8910767
	 * 
	 * @param binaryString
	 * @return
	 */
	private static boolean checkOnesCount(
			String binaryString, int requiredOnesCount) {
		
		int actualOnesCountI =
				binaryString.length() - binaryString.replace("1", "").length();
		
		boolean returnB = 
				(requiredOnesCount == actualOnesCountI) ? (true) : (false);
				
		return returnB;
	}
	
	
}


