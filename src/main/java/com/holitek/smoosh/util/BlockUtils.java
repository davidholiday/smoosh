package com.holitek.smoosh.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;


public class BlockUtils {
	public static final int BLOCK_SIZE_IN_BYTES = 4;
	
	public int ambiguousHO_BitFlipI = 0;
	public int[] numBitsFlippedCountARR = new int[32];
	
	public BigInteger floorBI = new BigInteger("0");
	public BigInteger ceilingBI = new BigInteger("0");
	
	public static Logger LOGGER = 
			LoggerFactory.getLogger(BlockUtils.class);
	
	
	public byte[] smooshBlock(byte[] rawBlockARR) {
		
//		Assert.assertEquals(
//				"input block array should be eight elements!",  
//				8, 
//				rawBlockARR.length);
				
//		byte[] lowerHalfOfBlockARR = new byte[4];
//		
//		for (int i = 0; i < 4; i ++) {
//			lowerHalfOfBlockARR[i] = rawBlockARR[i + 4];
//		}
		
		BigInteger blockInBI = new BigInteger(1, rawBlockARR);
		BigInteger blockOutBI = blockInBI;
		
		numBitsFlippedCountARR[blockOutBI.bitCount()]++;


		
//		if (ThreadLocalRandom.current().nextInt(0, 100) == 6) {
//			blockOutBI = blockOutBI.shiftLeft(8);
//			blockOutBI = blockOutBI.shiftRight(8);
//		}
		
		
		
//		BigInteger blockInLowerHalfBI = new BigInteger(1, lowerHalfOfBlockARR);
//		
//		BigInteger blockOutBI = 
//				subtractAndCollectMetrics(blockInBI, blockInLowerHalfBI);	
//		
//		BigInteger originalHighOrderFourBytesBI = blockInBI.shiftRight(32);
//		BigInteger currentHighOrderFourBytesBI = blockOutBI.shiftRight(32);
//		
//		BigInteger bitsFlippedBI = 
//				originalHighOrderFourBytesBI.xor(currentHighOrderFourBytesBI);
//
//		
//		LOGGER.info(originalHighOrderFourBytesBI.toString(16));
//		LOGGER.info(currentHighOrderFourBytesBI.toString(16));
//		LOGGER.info(bitsFlippedBI.toString(16));
//		
//		blockOutBI = blockOutBI.or(bitsFlippedBI);	
		byte[] returnARR = bigIntegerToLongByteArray(blockOutBI);
		return returnARR;
			
		
	}
	
	
	public byte[] unsmooshBlock(byte[] rawBlockARR) {
		
		Assert.assertEquals(
				"input "
				+ "block array should be eight elements!",  
				8, 
				rawBlockARR.length);
		
		byte[] lowerHalfOfBlockARR = new byte[4];
		
		for (int i = 0; i < 4; i ++) {
			lowerHalfOfBlockARR[i] = rawBlockARR[i + 4];
		}
		
		BigInteger blockInBI = new BigInteger(1, rawBlockARR);
		BigInteger blockInLowerHalfBI = new BigInteger(1, lowerHalfOfBlockARR);
		BigInteger smooshedHighOrderFourBytesBI = blockInBI.shiftRight(32);
		
        BigInteger originalHighOrderFourBytesBI = 
        		smooshedHighOrderFourBytesBI.xor(blockInLowerHalfBI);
        
        originalHighOrderFourBytesBI = 
        		originalHighOrderFourBytesBI.shiftLeft(32);
     
        smooshedHighOrderFourBytesBI = 
        		smooshedHighOrderFourBytesBI.shiftLeft(32);
        
        BigInteger originalSmooshedHighOrderFourDeltaBI = 
        		smooshedHighOrderFourBytesBI.subtract(
        				originalHighOrderFourBytesBI);
        
LOGGER.info(smooshedHighOrderFourBytesBI.toString(16));
LOGGER.info(originalHighOrderFourBytesBI.toString(16));

        BigInteger blockOutBI = 
        		originalHighOrderFourBytesBI.subtract(
        				originalSmooshedHighOrderFourDeltaBI);
		
		LOGGER.info(blockInBI.toString(16));
		LOGGER.info(originalHighOrderFourBytesBI.toString(16));
		LOGGER.info(originalSmooshedHighOrderFourDeltaBI.toString(16));
		LOGGER.info(blockOutBI.toString(16));
		LOGGER.info("=-=-=-=-=-=-=-=");
		
		byte[] returnARR = bigIntegerToLongByteArray(blockOutBI);
		return returnARR;
		
	}
	
	
	
	
	public void clearMetricsVars() {
		ambiguousHO_BitFlipI = 0;
		for (int i = 0; i < numBitsFlippedCountARR.length; i ++) {
			numBitsFlippedCountARR[i] = 0;
		}
	}
	
	
	
	public BigInteger subtractAndCollectMetrics(
			BigInteger blockInBI, BigInteger whatToSubtractBI) {
		
		BigInteger blockInShiftedBI = blockInBI.shiftRight(32);
		String blockInBitString = blockInShiftedBI.toString(2); 
		while (blockInBitString.length() < 32) { 
			blockInBitString = "0" + blockInBitString; 
		}


		BigInteger returnBI = blockInBI.subtract(whatToSubtractBI);
		String returnBitString = returnBI.shiftRight(32).toString(2);		
		while (returnBitString.length() < 32) {
			returnBitString = "0" + returnBitString;
		}
	
		int numBitsFlippedI = 0;

		for (int i = 0; i < 32; i ++) {
			
			String blockInBitS = blockInBitString.substring(i, i + 1);
			String returnBitS = returnBitString.substring(i, i + 1);
					
			
			if (!blockInBitS.contentEquals(returnBitS)) {
				numBitsFlippedI++;
				
				// hopefully this doesn't happen that often :-) 
				if (i == 0) {
					ambiguousHO_BitFlipI++;
				}
				
			}
			
		}
		
		numBitsFlippedCountARR[numBitsFlippedI]++;
		numBitsFlippedI = 0;
		return returnBI;
	}
	
	
	
	/**
	 * takes a BigInteger, assumes it represents a long, and converts it to
	 * a byte[8] w/o the sign bit BigInteger.toByteArray tacks onto the head.
	 * 
	 * @param bigIntegerIn
	 * @return
	 */
	public byte[] bigIntegerToLongByteArray(BigInteger bigIntegerIn) {
		long blockOutLong = bigIntegerIn.longValue();
		
		byte[] returnARR = ByteBuffer.allocate(Long.SIZE / Byte.SIZE)
				                     .putLong(blockOutLong)
				                     .array();
		
		Assert.assertEquals(
				"output block array should be eight elements!",  
				8, 
				returnARR.length);
		
		return returnARR;
	}
	
	
}
