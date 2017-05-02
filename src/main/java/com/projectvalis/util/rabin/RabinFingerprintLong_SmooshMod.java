package com.projectvalis.util.rabin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.rabinfingerprint.fingerprint.RabinFingerprintLong;
import org.rabinfingerprint.polynomial.Polynomial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projectvalis.util.ByteManipulation;
import com.projectvalis.util.TestHelper;


import javax.xml.bind.DatatypeConverter;



/**
 * extends the stock fingerprinting logic to make it easier to hook it into
 * smoosh
 * 
 * @author snerd
 *
 */
public class RabinFingerprintLong_SmooshMod extends RabinFingerprintLong {

	static Logger LOGGER = LoggerFactory
			.getLogger(RabinFingerprintLong_SmooshMod.class);

	public RabinFingerprintLong_SmooshMod(Polynomial poly) {
		super(poly);
	}

	
	private byte[][] pushTableArray;
	

	
	
	/**
	 * 
	 */
	public void populatePushTableArray() {
	
	    int numBytesI = (this.degree % 8 == 0) ? (this.degree / 8) : ((this.degree / 8) + 1);
	    int pushTableSizeI = this.getPushTable().length;    
	    pushTableArray = new byte[pushTableSizeI][numBytesI];
	    
	    for(int i = 0; i < pushTableSizeI; i ++) {
	        long pushTableValueL = this.getPushTable()[i];
	        String pushTableValueS = Long.toHexString(pushTableValueL);
	        
	        // *2 because we are adding nibbles to the head, not bytes
	        while(pushTableValueS.length() < numBytesI * 2) { pushTableValueS = "0" + pushTableValueS; }
	        if (pushTableValueS.length() % 2 != 0) { pushTableValueS = "0" + pushTableValueS; }
	        
	        byte[] pushTableValueArray = hexStringToByteArray(pushTableValueS);
	        pushTableArray[i] = pushTableValueArray;
	    }
	    
	}
	
	
	public byte getDegree() {
	    return (byte)this.degree;
	}
	
	
	/**
	 * 
	 * @param pushTable
	 * @param fingerprintArray
	 * @return
	 */
	public byte[] xorFingerprintAgainstPushTableElement(int pushTableElementIndex, byte[] fingerprintArray) {
	    byte[] pushTableElementArray = pushTableArray[pushTableElementIndex];
	    byte[] xordFingerprintArray = new byte[pushTableElementArray.length];
//LOGGER.info("fingerprint array is: " + ByteManipulation.getByteArrayAsHexString(fingerprintArray));	   
//LOGGER.info("pushtable element long is: " + this.getPushTable()[pushTableElementIndex]);
	    if (fingerprintArray.length < pushTableElementArray.length) {
	        byte[] tempArray = new byte[pushTableElementArray.length];
	        int sizeDifferenceI = pushTableElementArray.length - fingerprintArray.length;
	        
	        // because we know fingerprint can't be greater than 53 bits, we know it can't be more than 8bytes
	        for (int i = 0; i < fingerprintArray.length; i ++) {
	            tempArray[i + sizeDifferenceI] = fingerprintArray[i];
	        }
	        	        
	        fingerprintArray = tempArray;
	    }
	    // FIXME this is a kludge...
	    else if (pushTableElementArray.length < fingerprintArray.length) {
	        fingerprintArray = Arrays.copyOfRange(fingerprintArray, 1, 8);
	    }
//LOGGER.info("fingerprint array is: " + ByteManipulation.getByteArrayAsHexString(fingerprintArray));
//LOGGER.info("push table element is: " + ByteManipulation.getByteArrayAsHexString(pushTableElementArray));
	    for (int i = 0; i < fingerprintArray.length; i ++) {        
	        xordFingerprintArray[i] = (byte) (pushTableElementArray[i] ^ fingerprintArray[i]);
	    }

//LOGGER.info("returning: " + ByteManipulation.getByteArrayAsHexString(xordFingerprintArray));	    
	    return xordFingerprintArray;
	}
	
	
	
	/**
	 * appends a new head byte to a fingerprint array. returns the updated fingerprint array and the removed tailbyte
	 * 
	 * FIXME why did you make everything bytes? 
	 * 
	 * @param fingerprintArray
	 * @param newHeadByte
	 * @param fingerprintSizeInBits
	 * @param fingerprintShift
	 * @return Pair<updated fingerprint array, removed tail byte>
	 */
	public Pair<byte[], Byte> appendHeadByte(byte[] fingerprintArray, 
	                                         byte newHeadByte, 
	                                         byte fingerprintSizeInBits,
	                                         byte fingerprintShift) {
	    
//	    byte fingerprintSizeInBytesB = 
//	            (byte) ((fingerprintSizeInBits % 8 == 0) ? 
//                ((byte)fingerprintSizeInBits / 8) : ((byte)(fingerprintSizeInBits / 8) + 1));
	    	    
	    // figure out how to split the head byte 
        byte newHeadByteTailSize = (byte)(Math.abs((fingerprintSizeInBits % 8) - 8)); 	   
	    byte newHeadByteHeadSize = (byte)(8 - newHeadByteTailSize);
LOGGER.info("***** " + Math.abs((fingerprintSizeInBits % 8) - 8));
LOGGER.info("newHeadByteTailSize is: " + String.format("%X", newHeadByteTailSize));
LOGGER.info("newHeadByteHeadSize is: " + String.format("%X", newHeadByteHeadSize));
	    
	    // the "head" of the new byte is the low-order bit set of the byte appended to the fingerprint
	    // the "tail" of the new byte is the high-order bit set of the byte currently at the head of the fingerprint
	    byte currentHeadByte = fingerprintArray[0];
LOGGER.info("currentHeadByte is: " + String.format("%X", currentHeadByte));	    
	    // figure out how many empty bits are at the beginning of the current head byte and append that many bits from
	    // the tail of the new head byte
	    byte updatedCurrentHeadByte = (byte) ((0xFF & currentHeadByte) | (newHeadByte << newHeadByteHeadSize));
LOGGER.info("updatedCurrentHeadByte is: " + String.format("%X", updatedCurrentHeadByte));	    
	    // shift right unsigned to the bits we already appended to current head byte
	    byte newFingerprintHeadByte = (byte) (newHeadByte >>> newHeadByteTailSize);
LOGGER.info("newFingerprintHeadByte is: " + String.format("%X", newFingerprintHeadByte));   	    
	    
	    // create new fingerprint array
	    byte[] newFingerprintArray = new byte[fingerprintArray.length];
	    newFingerprintArray[0] = newFingerprintHeadByte;
	    newFingerprintArray[1] = updatedCurrentHeadByte;
	    
	    for (int i = 2; i < fingerprintArray.length; i ++) {
	        newFingerprintArray[i] = fingerprintArray[i - 1];
	    }
	 
	    
	    Byte tailByte = fingerprintArray[fingerprintArray.length -1];
//LOGGER.info("returning: " + ByteManipulation.getByteArrayAsHexString(newFingerprintArray));  
//LOGGER.info("+++++++++++++++");
	    
	    
    LOGGER.info("fingerprintArray is: " + ByteManipulation.getByteArrayAsHexString(fingerprintArray));      
	LOGGER.info("newHeadByte is: " + String.format("%X", newHeadByte));
	LOGGER.info("newFingerprintHeadByte is: " + String.format("%X", newFingerprintHeadByte));
	LOGGER.info("returning: " + ByteManipulation.getByteArrayAsHexString(newFingerprintArray)); 
	LOGGER.info("******");      
	        
int fingerprintLength = BigInteger.valueOf(ByteManipulation.getFingerprintAsLong(newFingerprintArray)).bitLength();
if (fingerprintLength > 53) {
    LOGGER.info("fingerprintLength should be < 54bits but is: " + fingerprintLength + " bits!");
    System.exit(8);
}
	
	    return Pair.of(newFingerprintArray, tailByte);
	}
	
	
	

	
	
	
	
	/**
	 * 
	 */
	@Override
	public void pushBytes(final byte[] bytes) {

		for (byte b : bytes) {
			
			LOGGER.trace("FINGERPRINT WAS: " + 
					String.format("%X", fingerprint));

			LOGGER.trace("inbound byte is: " + String.format("%X", (b & 0xFF)));

			int j = (int) ((fingerprint >> shift) & 0x1FF);

			LOGGER.trace("pushTable index and value are: "
					+ String.format("%X", j) + " "
					+ String.format("%X", pushTable[j]));

			LOGGER.trace("fingerprint pre-XOR, post shift/append is: "
					+ String.format("%X", ((fingerprint << 8) | (b & 0xFF))));
//LOGGER.info("fingerprint size in bits before: " + BigInteger.valueOf(fingerprint).bitLength());
			fingerprint = ((fingerprint << 8) | (b & 0xFF)) ^ pushTable[j];
//LOGGER.info("push entry size in bits : " + BigInteger.valueOf(pushTable[j]).bitLength());	
//LOGGER.info("((fingerprint << 8) | (b & 0xFF)) in bits is: " + BigInteger.valueOf(((fingerprint << 8) | (b & 0xFF))).bitLength());
//LOGGER.info("fingerprint size in bits after: " + BigInteger.valueOf(fingerprint).bitLength());
//LOGGER.info("********* ");
	
/**
 * 
 * ??????
 * 
14:37:29.137 [pool-2-thread-1] INFO  c.p.u.r.RabinFingerprintLong_SmooshMod - fingerprint size in bits before: 53
14:37:29.137 [pool-2-thread-1] INFO  c.p.u.r.RabinFingerprintLong_SmooshMod - push entry size in bits : 61
14:37:29.137 [pool-2-thread-1] INFO  c.p.u.r.RabinFingerprintLong_SmooshMod - ((fingerprint << 8) | (b & 0xFF)) in bits is: 55
14:37:29.137 [pool-2-thread-1] INFO  c.p.u.r.RabinFingerprintLong_SmooshMod - fingerprint size in bits after: 47
14:37:29.137 [pool-2-thread-1] INFO  c.p.u.r.RabinFingerprintLong_SmooshMod - ********* 

???
 * 
 */
			LOGGER.trace("FINGERPRINT IS NOW: "
					+ String.format("%X", fingerprint) + "\n");
		}
		
	}

	
	
    /**
     * 
     * @param bytes
     * @return
     */
    public int[] pushAndXorByteSixAndSaveHeads(final byte[] bytes) {
        int dataSize = bytes.length;
        int[] heads = new int[dataSize];

        for (int i = 0; i < dataSize; i++) {
            byte b = bytes[i]; 

            LOGGER.info("FINGERPRINT WAS: " + 
                    String.format("%X", fingerprint));

            LOGGER.info("inbound byte is: " + String.format("%X", (b & 0xFF)));
            LOGGER.info("shift is:::" + shift);
            
            int j = (int) ((fingerprint >> shift) & 0x1FF);
            
            heads[i] = j;
            if (i == 5) {
                j = (int)(bytes[7] & 0xFF);
            }
            
            
            
            LOGGER.info("pushTable index and value are: "
                    + String.format("%X", j) + " "
                    + String.format("%X", pushTable[j]));

            LOGGER.info("fingerprint pre-XOR, post shift/append is: "
                    + String.format("%X", ((fingerprint << 8) | (b & 0xFF))));

            fingerprint = ((fingerprint << 8) | (b & 0xFF)) ^ pushTable[j];

            LOGGER.info("FINGERPRINT IS NOW: "
                    + String.format("%X", fingerprint) + "\n");
        }

        return heads;
    }	
	
	
	
	/**
	 * 
	 * @param bytes
	 * @return
	 */
	public byte[] pushAndSaveHeads(final byte[] bytes) {
	    int dataSize = bytes.length;
	    byte[] heads = new byte[dataSize];

	    for (int i = 0; i < dataSize; i++) {
	        byte b = bytes[i]; 

	        LOGGER.trace("FINGERPRINT WAS: " + 
	                String.format("%X", fingerprint));

	        LOGGER.trace("inbound byte is: " + String.format("%X", (b & 0xFF)));

	        int j = (int) ((fingerprint >> shift) & 0x1FF);
	        heads[i] = (byte)j;

	        LOGGER.trace("pushTable index and value are: "
	                + String.format("%X", j) + " "
	                + String.format("%X", pushTable[j]));

	        LOGGER.trace("fingerprint pre-XOR, post shift/append is: "
	                + String.format("%X", ((fingerprint << 8) | (b & 0xFF))));

	        fingerprint = ((fingerprint << 8) | (b & 0xFF)) ^ pushTable[j];

	        LOGGER.trace("FINGERPRINT IS NOW: "
	                + String.format("%X", fingerprint) + "\n");
	    }

	    return heads;
	}
	
	
	
	

	/**
	 * returns the push table array as a List<Long>
	 * 
	 * @return
	 */
	public List<Long> getPushTableAsList() {
		List<Long> polynomialL = new ArrayList<Long>();	
		for (long l : pushTable) { polynomialL.add(l); }
		return polynomialL;
	}
		
	
	
	
	/**
	 * 
	 * *note that this limits you to a head list of no more than 8 + head byte!*
	 * 
	 * FIXME not in love with the string stuff 
	 * 
	 * @param numBitsPushed
	 * @param numHeadBits
	 * @return
	 */
	public List<List<Byte>> getAllPossibleHeadsTwo(int numBitsPushed, int numHeadBits) {
	    List<List<Byte>> returnAL = new ArrayList<>();
	    
	    // range for first head byte
	    int endNumHeadI = (int)Math.pow(2, (numHeadBits));
	    
	    // how many head bytes in the body (subsequent to the first) 
	    int headListBodySizeI = (numBitsPushed - numHeadBits) / 8;
	    
	    // range for all subsequent head bytes
	    long endNumMainL =  (long)Math.pow(2, (numBitsPushed - numHeadBits));
	        
	    // just in case someone pushes a very small number of bits through the fingerprint
	    if (headListBodySizeI == 0) {
	        
            // for every possible first head byte...
            for (int i = 0; i < endNumHeadI; i ++) {
                List<Byte> candidateAL = new ArrayList<>();
                candidateAL.add( (byte)i );
                returnAL.add(candidateAL);
            }   
            
	    }
	    else {
	        
	        // for every possible first head byte...
	        for (int i = 0; i < endNumHeadI; i ++) {
	    
	            // create all possible combinations of body bytes within given range
	            for(long l = 0; l < endNumMainL; l ++) {
	                
	                List<Byte> candidateAL = new ArrayList<>();
	                candidateAL.add( (byte)i );
	                
	                String longAsHex = Long.toHexString(l);
	                
	                while(longAsHex.length() < headListBodySizeI * 2) { 
	                    longAsHex = "0" + longAsHex; 
	                }
	                
	                byte[] longAsByteArray = hexStringToByteArray(longAsHex);
	                for (byte b : longAsByteArray) { candidateAL.add(b); }

	                Collections.reverse(candidateAL);
	                returnAL.add(candidateAL);
	                
	            }           

	        }
	        
	    }
	    	    
	    return returnAL;
	}
	
	
	
	
	
	/**
	 * FIXME this is fart code ...
	 * 
	 * @param numBitsI
	 * @return
	 */
//	public List<List<Byte>> getAllPossibleHeads(int numBitsI, int headSizeInBitsI) {
//	    
//	    List<List<Byte>> returnAL = new ArrayList<>();
//	    long endNumL = (long)Math.pow(2, numBitsI);
//
//
//	    for (long dong = 0; dong < endNumL; dong ++) {
//	        List<Byte> candidateAL = new ArrayList<>();
//	        
//	        int numCandidateBitsI = Math.abs(Long.numberOfLeadingZeros(dong) - 64);
////LOGGER.info("numHeadBitsI and numCandidateBitsI are: " + headSizeInBitsI  + " " + numCandidateBitsI);
//	        int headBitsShiftValueI = (numCandidateBitsI > headSizeInBitsI) ? 
//	                (Math.abs(headSizeInBitsI - numCandidateBitsI)) : (0);
//	                
//	        int bodyBytesShiftValueI = (numCandidateBitsI > headSizeInBitsI) ? 
//	                (Long.numberOfLeadingZeros(dong) + headSizeInBitsI) : (0);
////LOGGER.info("bodyBytesShiftValueI is: " + bodyBytesShiftValueI);
////LOGGER.info("buffer head shift size is: " + headBitsShiftValueI);	        
////LOGGER.info("dong in the raw is: " + String.format("%X", dong)); 
//	        // grab the head byte
//	        byte firstHeadByteB = (byte)(dong >>> headBitsShiftValueI);
//	        candidateAL.add(firstHeadByteB);
////LOGGER.info("dong head is: " + String.format("%X", firstHeadByteB));        
//	        // now grab the remainder
//	        // shift out the head and return the body to the other end of the long
//	        long dongBody = (dong << bodyBytesShiftValueI) >>> bodyBytesShiftValueI;
////LOGGER.info("dong remaining body is: " + String.format("%X", dongBody));
//
//            // this pigfucker appends bits to the head - don't use --v
//	        //byte[] remainingHeadsARR = BigInteger.valueOf(dongBody).toByteArray();
//
//            // this is shite --v
//            String dongString = Long.toHexString(dongBody);
//            if (dongString.length() % 2 != 0) { dongString = "0" + dongString; }
//            byte[] remainingHeadsARR = hexStringToByteArray(dongString);
////LOGGER.info("remaining heads arr is: " + ByteManipulation.getByteArrayAsHexString(remainingHeadsARR)); 
//	        for (byte b : remainingHeadsARR) {
//	            candidateAL.add(b);
//	        }
//
////LOGGER.info("candidate list size is: " + candidateAL.size());
//	        returnAL.add(candidateAL);
////LOGGER.info("*** *** *** ***");	  	        
//	    }
//  
////LOGGER.info("returning with a list size of: " + returnAL.size());    
//	    return returnAL;
//	    
//	}
	

	
	
	/**
	 * ty so
	 * http://stackoverflow.com/a/140861/2234770
	 * @param s
	 * @return
	 */
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	
	
	
	
	
	/**
	 * assumes what's being passed is eight bytes fingerprinted. what's 
	 * returned is, given a polynomial and an XOR table index, what the 
	 * previous state of the fingerprint was (ie - when the first seven / eight
	 * bytes were pushed).
	 * 
	 * returns 
	 * 
	 * @param polynomial
	 * @param fingerprint
	 * @param xorIndex
	 * @return
	 * 		LIST{rolled back fingerprint, tail byte appended to rolled back
	 * fingerprint prior to XOR (the eighth byte pushed)}
	 */
	public List<Long> rollbackFingerprintFirst(long polynomial,
			long fingerprint, int xorIndex) {

		long xordFingerprintL = polynomial ^ fingerprint;
		long tailByteL = ByteManipulation.getTailByte(xordFingerprintL);
		xordFingerprintL = ByteManipulation.removeTailByte(xordFingerprintL);

		long appendedXordFingerprintL = 
				ByteManipulation.appendByteToHead(xorIndex, xordFingerprintL, this.shift);

		List<Long> returnAL = new ArrayList<Long>();
		returnAL.add(appendedXordFingerprintL);
		returnAL.add(tailByteL);
		returnAL.add((long) xorIndex);
		return returnAL;
	}

	
	/**
	 * assumes what's being passed is the result of a fingerprinted stream of
	 * eight bytes being rolled back by rollbackFingerprintFirst. because only
	 * eight bytes were pushed, what this method tries to figure out is what
	 * the fingerprint looked like prior to the seventh byte being pushed. given
	 * that only three bits could've been used to determine the XOR index value,
	 * what's returned is a set of candidates where, after XOR against all 
	 * possible XOR values with index values representable in three bits, the
	 * first three bits are equal to the index of the XOR value used.
	 * 
	 * @param resultFromFirstRollbackAL
	 * @return
	 */
	public List<Long> rollbackFingerprintSecond(
			List<Long> resultFromFirstRollbackAL) {

		long fingerprintL = resultFromFirstRollbackAL.get(0);
		long tailByteL = resultFromFirstRollbackAL.get(1);
		List<Long> returnAL = new ArrayList<Long>();

		for (int i = 0; i < 8; i++) {
			List<Long> polynomialAL = getPushTableAsList();
			long polynomialL = polynomialAL.get(i);
			long xordFingerprintL = polynomialL ^ fingerprintL;

			if (i == (int) (xordFingerprintL >> 53)) {
				
				long validCandidateL = 
						(xordFingerprintL << 8) | (tailByteL & 0xFF);
				
				returnAL.add(validCandidateL);
			}

		}

		return returnAL;
	}	


	
	
	
	/**
	 * smooshes a block of sixteen bytes. 
	 * 
	 * @param bytesIn - a sixteen byte block of bytes
	 * 
	 * 
	 * @return a fifteen 1/2 byte array in the following form:
	 * 
	 * [0-6]: the first seven bytes in their original state
	 * 
	 * [7]: byte nine, which was at the head of the fingerprint when byte
	 * sixteen was pushed, forcing it to be shoved out the front of the chain.
	 * 
	 * [8-14]: the fingerprint of the sixteen byte block
	 * 
	 * [15]: nibble containing the high order bits of byte eight
	 * 
	 * TODO: experiment with returning 15 1/2 bytes instead of fifteen to 
	 * provide some room for extra metadata as needed. currently an assert is
	 * in place ensuring the value of the xorIndex <= 8 bits. if that assert
	 * ever pops, then we know we have to include at least the head bit of 
	 * the xord byte 8 with what's returned so later we can ensure we've got the
	 * correct XOR table index when we process byte 7.
	 * 
	 * TODO: refactor to make less wonky -- the logic got weird when you 
	 * tacked the xor'd byte eight nibble to the tail of the return list
	 */
	public byte[] compress16(byte[] bytesIn) {
		int returnArrIndexI = 0;
		byte[] returnARR = new byte[16];
		long fingerprintLocalL = 0;
		
		for (int i = 0; i < 16; i ++) {
			byte b = bytesIn[i];
			int headByteI = (int) ((fingerprintLocalL >> shift) & 0x1FF);	
			Assert.assertTrue("nine bit headbyte detected!", headByteI < 256);		
			
			fingerprintLocalL = 
				((fingerprintLocalL << 8) | (b & 0xFF)) ^ pushTable[headByteI];
			
			// if we're in the first seven of the list throw them into the
			// return array
			if (i < 7) {
				returnARR[returnArrIndexI]  = b;
				returnArrIndexI++;
			}
			// else if we're on the fifteenth byte, gran the xord byte eight
			// (aka the head of the fingerprint when byte fifteen was pushed)
			// and store the high order nibble. This so we can compute the 
			// unXor chain later
			else if (i == 7) {
				returnARR[15] = (byte) (b & 0x0F0);
			}
			// else if we're on the sixteenth byte, grab the xord byte nine
			// (aka the head of the fingerprint when byte sixteen was pushed)
			// and eject.
			else if (i == 15) {
				returnARR[returnArrIndexI] = (byte) (headByteI & 0x0FF);
				returnArrIndexI++;
			}
			
		}

		byte[] fingerprintByteARR = 
				ByteManipulation.getLongAsByteArray(fingerprintLocalL, true);
		
		for (byte b : fingerprintByteARR) {
			returnARR[returnArrIndexI] = b;
			returnArrIndexI++;
		}
				
		return returnARR;
	}
	
	
	

	
	/**
	 * takes an array containing the first seven bytes given as part of a 
	 * smoosh block and returns an array containing all eight of the derived
	 * XOR values that were applied to the fingerprint
	 * 
	 * @param firstSevenByteARR
	 * @return
	 */
	public long[] getXorChain(
			byte[] firstSevenByteARR, byte xordByteEightNibble) {
		
		long[] returnARR = new long[8];
		long fingerprintLocalL = 0;	
		
		for (int i = 0; i < 14; i ++) {
			int headByteI = (int) ((fingerprintLocalL >> shift) & 0x1FF);
			LOGGER.trace("headbyte is: " + String.format("%02x", headByteI));
			byte appendedByte = 0x00;
			
			if (i < 7) {
				appendedByte = firstSevenByteARR[i];
			}
			else if (i == 7) {
				appendedByte = xordByteEightNibble;
			}
			
			LOGGER.trace("fingerprint was: " 
					+ Long.toHexString(fingerprintLocalL));	

			fingerprintLocalL = 
				((fingerprintLocalL << 8) | (appendedByte & 0xFF)) 
					^ pushTable[headByteI];	
			
			LOGGER.trace("fingerprint is: " 
					+ Long.toHexString(fingerprintLocalL));		
			
			// if we've just pushed byte seventh or greater, then we've been
			// xoring stuff and we need to track those values.
			if (i > 5) { 
				returnARR[i - 6] = pushTable[headByteI]; 
				
				LOGGER.info("computed xor chain index and value is: " + 
						String.format("%02X", headByteI) + " " + 
							String.format("%02X", pushTable[headByteI]));	
			}
			
			LOGGER.trace("******");			
		}	
		
		return returnARR;	
	}
	
	
	
	
	
	/**
	 * takes an array containing the first seven bytes given as part of a 
	 * smoosh block and returns an array containing all eight of the derived
	 * XOR table index values that were applied to the fingerprint
	 * 
	 * @param firstSevenByteARR
	 * @return
	 */
	public int[] getXorChainIndexes(
			byte[] firstSevenByteARR, byte xordByteEightNibble) {
		
		int[] returnARR = new int[8];
		long fingerprintLocalL = 0;	
		
		for (int i = 0; i < 14; i ++) {
			int headByteI = (int) ((fingerprintLocalL >> shift) & 0x1FF);
			LOGGER.trace("headbyte is: " + String.format("%02x", headByteI));
			byte appendedByte = 0x00;
			
			if (i < 7) {
				appendedByte = firstSevenByteARR[i];
			}
			else if (i == 7) {
				appendedByte = xordByteEightNibble;
			}
			
			LOGGER.trace("fingerprint was: " 
					+ Long.toHexString(fingerprintLocalL));	

			fingerprintLocalL = 
				((fingerprintLocalL << 8) | (appendedByte & 0xFF)) 
					^ pushTable[headByteI];	
			
			LOGGER.trace("fingerprint is: " 
					+ Long.toHexString(fingerprintLocalL));		
			
			// if we've just pushed byte seventh or greater, then we've been
			// xoring stuff and we need to track those values.
			if (i > 5) { 
				returnARR[i - 6] = headByteI; 
				
				LOGGER.info("computed xor chain index and value is: " + 
						String.format("%02X", headByteI) + " " + 
							String.format("%02X", pushTable[headByteI]));	
			}
			
			LOGGER.trace("******");			
		}	
		
		return returnARR;	
	}
	
	
	
	
	
	/**
	 * takes a smoosh block and rolls back the fingerprint for bytes [1-16] 
	 * to the fingerprint for bytes [1-15] and grabs the original state of byte
	 * 16.
	 * 
	 * @param smooshBlock
	 * @return byte[] = {fingerprint for bytes [1-15], original byte 16}
	 */
	public byte[] rollBack16(byte[] smooshBlock) {
		byte[] returnARR = new byte[8];
		byte xordByteNine = smooshBlock[7];
		long xorValL = pushTable[(int)xordByteNine & 0xFF];
		
		LOGGER.trace("xordByteNine and xor val are: " 
				+ Long.toHexString(xordByteNine) + " " 
					+ Long.toHexString(xorValL));

		long fingerprint16L = 
				ByteManipulation.getSevenByteArrayAsLong(
						Arrays.copyOfRange(smooshBlock, 8, 15)); 
		
		// check to make sure the conversion from byte array to long happened
		// correctly
		String fingerprint16HexS = 
				ByteManipulation.getHexString(
						Arrays.copyOfRange(smooshBlock, 8, 15));
		
		long fromHexStringL = Long.parseLong(fingerprint16HexS, 16);
		
		LOGGER.trace("fingerprint16 from array v conversion from array: " + 
				fingerprint16HexS + " " + fromHexStringL);
		
		Assert.assertTrue("fingerprint conversion from byte array failure!",
				fromHexStringL == fingerprint16L);

		
		// do the rollback
		fingerprint16L = xorValL ^ fingerprint16L;
		byte byte16 = (byte) (fingerprint16L & 0x000000000000FF);
		
		LOGGER.info("byte16 in hex is: " + String.format("%02X", byte16));
		
		LOGGER.info("fingerprint16 after xor is: " 
				+ Long.toHexString(fingerprint16L));

		long fingerprint15L = fingerprint16L >> 8;
				
		fingerprint15L = 
				ByteManipulation.appendByteToHead(xordByteNine, fingerprint15L, this.shift);
	
		LOGGER.info("computed fingerprint15L is: " + 
				String.format("%X", fingerprint15L));
		
		byte[] fingerprint15ARR = 
				ByteManipulation.getLongAsByteArray(fingerprint15L, true);
				
		for (int i = 0; i < fingerprint15ARR.length; i ++) { 
			returnARR[i] = fingerprint15ARR[i]; 
		}
		
		returnARR[7] = byte16;
		return returnARR;
	}	
	
	

	
	/**
	 * applies the correct chain of xor operations against a given fingerprinted
	 * byte. ensures the position in the fingerprint of byte [processedByte]
	 * is taken into account when unrolling the chain of xor operations.
	 * 
	 * DON'T FORGET xorChainARR[0] REPRESENTS THE XOR VALUE USED WHEN
	 * BYTE SEVEN WAS PUSHED! For your probable use case - you're going to want
	 * to start at xorChainARR[2] given that it was at the head of the 
	 * fingerprint when byte 9 was pushed.
	 * 
	 * @param startIndexI the index of the xorChainARR at which to start. eg: 
	 * 'which byte was shifted off the head of the fingerprint when 
	 * value [processedByte] was appended to the tail?
	 * 
	 * @param xorChainARR the indexes to the values used used to xor 
	 * the fingerprint for bytes 1-14
	 * 
	 * @param processedByte a single fingerprinted byte
	 * 
	 * @return a mostly unprocessed byte (6/7 XORs are handled by this method)
	 */
	public byte applyXorChain(int startIndexI, 
							  int[] xorIndexChainARR, 
							  byte processedByte) {
		
		int countI = 8 - startIndexI;
		int positionI = 6;

		for (int i = 0; i < countI; i++) {
			
			int xorIndexValueI = xorIndexChainARR[startIndexI];
			long xorValL = pushTable[xorIndexValueI];
			
			byte[] xorValBytesARR = 
				ByteManipulation.getLongAsByteArray(xorValL, true);
			
LOGGER.info(String.format("%02X", processedByte) + " " + positionI);
for (byte b : xorValBytesARR) {
	System.out.print((b & 0xFF) + " | ");
}
System.out.print("\n");

			processedByte = (byte) (processedByte ^ xorValBytesARR[positionI]);
			
			startIndexI++;
			positionI--;
		}
LOGGER.info("returning:: " + String.format("%02X", processedByte));		
		return processedByte;
	}
	
	
	
	/**
	 * unrolls all but the last xor operation performed against bytes 
	 * nine through fifteen
	 * 
	 * @param smooshedByteBlockARR
	 * @return
	 */
	public byte[] mostlyUnXorNineToFifteen(byte[] smooshedByteBlockARR) {
		byte[] firstSevenARR = Arrays.copyOf(smooshedByteBlockARR, 7);
		
		int[] xorValueChainIndexesARR = 
				getXorChainIndexes(firstSevenARR, smooshedByteBlockARR[15]);

		byte[] rolledBackSmooshBlockARR = rollBack16(smooshedByteBlockARR);	
			
		byte[] mostlyUnXordNineToFifteenARR = new byte[7];
		
		for (int i = 0; i < 7; i ++) {
			
			mostlyUnXordNineToFifteenARR[i] = applyXorChain(
				(i + 2), xorValueChainIndexesARR, rolledBackSmooshBlockARR[i]);			
			
		}
		
		return mostlyUnXordNineToFifteenARR;
	}
	
	
	
	/**
	 * applies the last xor operation (xor'd byte eight) to a mostly unxord
	 * chain representing bytes nine through fifteen. 
	 * 
	 * @param xorEightVal
	 * @param mostlyUnXordNineToFifteenARR
	 * @return
	 */
	public byte[] applyXorEight(
			long xorEightVal, byte[] mostlyUnXordNineToFifteenARR) {
		byte[] returnARR = new byte[7];
		
		byte[] xorEightValARR = 
				ByteManipulation.getLongAsByteArray(xorEightVal, true);
		
		for (int i = 0; i < 7; i ++) {
			returnARR[i] = 
					(byte)(mostlyUnXordNineToFifteenARR[i] ^ xorEightValARR[i]);
		}
		
		
		return returnARR;
	}
	
	
	
	
	/**
	 * return the current shift right value (as in - the number of bytes right
	 * the current fingerprint will be shifted to compute the xor pushtable 
	 * index).
	 * 
	 * @return
	 */
	public int getShiftVal() {
		return this.shift;
	}
	
	
	
	
	/**
	 * return the current xor value table
	 * 
	 * @return
	 */
	public long[] getPushTable() {
		return this.pushTable;
	}
	
	
}

















