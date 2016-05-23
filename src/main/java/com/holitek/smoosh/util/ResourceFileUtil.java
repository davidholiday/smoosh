package com.holitek.smoosh.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import junit.framework.Assert;

/**
 *  methods to create and read the 31 resource files containing the c(32/k) 
 *  data. there are 32bits worth of entries, so the files can get quite large.
 * @author snerd
 *
 */
public class ResourceFileUtil {

	public static Logger LOGGER = 
			LoggerFactory.getLogger(ResourceFileUtil.class);
	
	// every answer for c(31|k) where arr[x] = c(31|x+1)
	// this arrays is 30 elements long because we can assume the answer to
	// c(31/0)
	private static final int[] thirtyOneChooseK_ARR = { 31, 
			                                            495,
			                                            4995,
			                                            31465,
			                                            169911,
			                                            736281,
			                                            2629575,
			                                            7888725,
			                                            20160075,
			                                            44352165,
			                                            84672315,
			                                            141120525,
			                                            206253075,
			                                            265182525,
			                                            300540195,
			                                            300540195,
			                                            265182525,
			                                            206253075,
			                                            141120525,
			                                            84672315,
			                                            44352165,
			                                            20160075,
			                                            7888725,
			                                            2629575,
			                                            736281,
			                                            169911,
			                                            31465,
			                                            4995,
			                                            495,
			                                            31 };
	
	
	// peak index value for every element table. computed by
	// arr[n] = *sigma* n=0->n: thirtyOneChooseK_ARR[n]
	//
	// note that this array is one element shorter than its counterpart! 
	private static final int[] thirtyOneChooseK_FloorValueARR = { 30, 
			                                                      525,
			                                                      5520,
			                                                      31990,
			                                                      175431,
			                                                      911712,
			                                                      3541287,
			                                                      11430012,
			                                                      31590087,
			                                                      75942252,
			                                                      160614567,
			                                                      301732092,
			                                                      507988167,
			                                                      773170692,
			                                                      1073710887,
			                                                      1374251082,
			                                                      1639433607,
			                                                      1845686682,
			                                                      1986807207,
			                                                      2071479522,
			                                                      2115831687,
			                                                      2135991762,
			                                                      2143880487,
			                                                      2146510062,
			                                                      2147246343,
			                                                      2147416254,
			                                                      2147447719,
			                                                      2147452714,
			                                                      2147453209 };	

	
	/**
	 * creates the resource files
	 * @return
	 */
	public static boolean buildResourceFiles() {
		boolean successB = true;
		int n = 8;
				
		for (int k = 1; k < 5; k ++) {			
LOGGER.info("k is: " + k);				
            List<Integer> answerL = buildSortedAnswerList(n, k);
LOGGER.info("answerL size is: " + answerL.size());
            boolean serializeSuccessB = serializeElementList(k, answerL);
            successB = (!serializeSuccessB) ? (false) : (successB);

		}
		return successB;
	}
	
	
	
	/**
	 * private helper method that enumerates all the possible answers to
	 * c(n|k)
	 * 
	 * @param n
	 * @param k
	 * @return
	 */
	private static List<Integer> buildSortedAnswerList(int n, int k) {
		int rangeCeilingI = (int)Math.pow(2, n);
		
        List<Integer> listOfMatches = 
                IntStream.range(0, rangeCeilingI)
                         .parallel()
                         .filter(x -> checkOnesCount(
                        		 Integer.toBinaryString(x), k))
                         .boxed()
                         .collect(Collectors.toList());

        Collections.sort(listOfMatches);
        return listOfMatches;
	}
	
	
	
	/**
	 * takes an element index and a table index and combines them into a single
	 * integer representation. there are thirty-one tables, each containing one
	 * possible answer to c(n|k). as such, the tables are sized thusly:
	 * 
	 * 31/1 --> 31
     * 31/2 --> 495
     * 31/3 --> 4995
     * 31/4 --> 31465
     * 31/5 --> 169911
     * 31/6 --> 736281
     * 31/7 --> 2629575
     * 31/8 --> 7888725
     * 31/9 --> 20160075
     * 31/10 --> 44352165
     * 31/11 --> 84672315
     * 31/12 --> 141120525
     * 31/13 --> 206253075
     * 31/14 --> 265182525
     *
     * 31/15 --> 300540195
     * 31/16 --> 300540195
     *
     * 31/17 --> 265182525
     * 31/18 --> 206253075
     * 31/19 --> 141120525
     * 31/20 --> 84672315
     * 31/21 --> 44352165
     * 31/22 --> 20160075
     * 31/23 --> 7888725
     * 31/24 --> 2629575
     * 31/25 --> 736281
     * 31/26 --> 169911
     * 31/27 --> 31465
     * 31/28 --> 4995
     * 31/29 --> 495
     * 31/30 --> 31
     * 
     * the table a particular index resides in is denoted by the range of the
     * combined index value. Table 1's index range is 0-29. Table 2's range is
     * 31-494. Table 3's is 495-4994, etc etc etc. 
	 * 
	 * 
	 * @param elementIndexI
	 * @param tableIndexI
	 * @return
	 */
	public static int createCombinedElementIndex(
			int elementIndexI, int tableIndexI) {
		
		Assert.assertTrue("tableIndex shouldn't be larger than 29!", 
				tableIndexI < 30);
		
		int returnI = elementIndexI;
		
		for (int i = 0; i < tableIndexI; i ++) {
			returnI += thirtyOneChooseK_ARR[i];
		}
		
		return returnI;
	}
	
	
	/**
	 * takes a combined element index and splits it into a 
	 * tuple -- > (element_index, table_index)
	 * 
	 * @param combinedElementIndex
	 * @return
	 */
	public static Pair<Integer, Integer> splitCombinedElementIndex(
			int combinedElementIndex) {
				
		Pair<Integer, Integer> returnPair = null;
		
		int elementIndexFloorValue_Index = 
				thirtyOneChooseK_FloorValueARR.length;
	
		int returnElementIndexI = -1;
		int returnTableIndexI = -1;
		
		// check to see if the element is in the last table
		//
		int highestTableElementFloorI = 
				thirtyOneChooseK_FloorValueARR[elementIndexFloorValue_Index];
						
		if (combinedElementIndex > highestTableElementFloorI) {
			returnElementIndexI = 
					combinedElementIndex - highestTableElementFloorI;
			
			returnTableIndexI = thirtyOneChooseK_ARR.length;
			
			returnPair = 
				new Pair<Integer, Integer>
			        (returnElementIndexI, returnTableIndexI);		
		}
		
		// if necessary, run through the remaining options
		//
		while ((elementIndexFloorValue_Index > 0) && (returnPair == null)) {
			elementIndexFloorValue_Index --;
			
			int tempIndexI = 
			  combinedElementIndex -
			    thirtyOneChooseK_FloorValueARR[elementIndexFloorValue_Index];
			
			// if true we've found the floor value
			if (tempIndexI > 0) {
				returnElementIndexI = tempIndexI;
				returnTableIndexI = elementIndexFloorValue_Index + 1;
				
				returnPair = 
						new Pair<Integer, Integer>
					        (returnElementIndexI, returnTableIndexI);	
			}
					
		}
		
		Assert.assertTrue("returnPair shouldn't be null!", returnPair != null);
		
		return returnPair;
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
	

	
	/**
	 * takes an 8-bit binary string, pads it with zeros if necessary, and 
	 * returns a list containing the indexes where the 1's are located. 
	 * 
	 * @param binaryString
	 * @return
	 */
	private static List<Integer> binaryStringToOnesList(String binaryString) {
		String formattedBinaryString = 
				String.format("%8s", binaryString).replace(' ', '0');
		
		List<Integer> returnL = 
				IntStream.range(0, 8)
		                 .filter(i -> formattedBinaryString.charAt(i) == '1')
		                 .boxed().collect(Collectors.toList());
		
		return returnL;
	}
	
	
	
	/**
	 * utility method to create the element list files needed for decompression
	 * 
	 * @param listID
	 * @param listToSerialize
	 * @return
	 */
	private static boolean serializeElementList(
			int listID, List<Integer> listToSerialize) {
		
		boolean successB = true;
		
		String fileNameS = 
				"./src/main/resources/elementList_" + listID + ".dat";
		
	    try {
	        FileOutputStream fileOutStream = new FileOutputStream(fileNameS);
	       
	        ObjectOutputStream objectOutputStream = 
	    		   new ObjectOutputStream(fileOutStream);
	       
	        objectOutputStream.writeObject(listToSerialize);
	        objectOutputStream.close();
	        fileOutStream.close();
	    } catch(IOException e) {
	        LOGGER.error("couldn't serialize element list!", e);
	        successB = false;
	    }
	   
		return successB;
	}
	
	
	/**
	 * utility method to read in the files needed for decompression, as needed.
	 * @param listID
	 * @return
	 */
	public static List<Integer> deserializeElementList (int listID) {
		
		List<Integer> returnL = null;
		
		String fileNameS = 
				"./src/main/resources/elementList_" + listID + ".dat";
		
	    try {
	        FileInputStream fileInputStream = 
	        		new FileInputStream(fileNameS);
	        
	        ObjectInputStream objectInputStream = 
	        		new ObjectInputStream(fileInputStream);
	        
	        returnL = (List<Integer>) objectInputStream.readObject();
	        objectInputStream.close();
	        fileInputStream.close();
	    } catch(IOException e) {
	    	LOGGER.error("couldn't de-serialize element list!", e);
	    } catch(ClassNotFoundException e1) {
	    	LOGGER.error("class not found error!", e1);
	    }
	    
	    return returnL;
	}
	
	
}





