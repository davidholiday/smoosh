package com.projectvalis.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.rabinfingerprint.fingerprint.RabinFingerprintLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projectvalis.util.rabin.RabinFingerprintLong_SmooshMod;

public class ByteManipulation {
	
	static Logger LOGGER = LoggerFactory.getLogger(ByteManipulation.class);
	
	/**
	 * simple utility plucked from the interwebs to convert a byte array to a
	 * hex string (yes I am that lazy)
	 * 
	 * yoinked from: http://www.rgagnon.com/javadetails/java-0596.html
	 * 
	 * 
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static String getHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	
	/**
	 * removes the tail byte of a fingerprint by shifting right 8bits
	 * @param xordFingerprint
	 * @return
	 */
	public static long removeTailByte(long xordFingerprint) {
		return xordFingerprint >> 8;
	}

	
	
	/**
	 * returns only the tail byte of a fingerprint
	 * @param xordFingerprint
	 * @return
	 */
	public static byte getTailByte(long xordFingerprint) {
		return (byte)(0x00000000000000FF & xordFingerprint);
	}
	
	

	/**
	 * returns the tail byte of a given 
	 * @param value
	 * @param newTailByte
	 * @return
	 */
	public static long replaceTailByte(long value, byte newTailByte) {
	    return (value & 0xFFFFFFFFFFFFFF00L) | (newTailByte & 0xFF);
	}
	
	
	
	/**
	 * 
	 * @param fingerprint
	 * @return
	 */
	public static byte[] getRemainingFingerprintBytes(long fingerprint) {
	    return BigInteger.valueOf(fingerprint).toByteArray();
	}	
	
	

	/**
	 * here because the other one doesn't play nice with appended 00's 
	 * @param newByte
	 * @param xordFingerprint
	 * @param fingerprinterShift
	 * @return
	 */
	public static String appendByteToHeadString(
	        int newByte, String fingerprintBinary, int fingerprinterShift, int fingerprintBitSizeI) {

	    // head is the bits that are indexed @ > fingerprinterShift - which means they were
	    // part of the value that generated the index which selected the
	    // member of the push table against which this fingerprint was
	    // xor'd
	    int xordFingerprintHeadLengthI = Math.abs(fingerprintBinary.length() - fingerprinterShift);
//LOGGER.info("Long.toBinaryString(xordFingerprint).length() is: " + fingerprintBinary.length());
//LOGGER.info("fingerprinter shift is: " + fingerprinterShift);
//LOGGER.info("fingerprint head length is: " + xordFingerprintHeadLengthI);
	    // figure out the current value of the existing bits in head, and what
	    // the delta is between what's already there and what needs to be there.
	    // then setup to update the head value accordingly.
	   
        //int xordFingerprintHeadI = (int) (xordFingerprint >> fingerprinterShift);
        String headBinaryS = fingerprintBinary.substring(0,  xordFingerprintHeadLengthI);
        while(headBinaryS.length() < 8) { headBinaryS = '0' + headBinaryS; }
        int xordFingerprintHeadI = Integer.parseInt(headBinaryS, 2);

//LOGGER.info("fingerprint head is: " + xordFingerprintHeadI);
	    int diffIndexAndXordHeadI = newByte - xordFingerprintHeadI;
	    xordFingerprintHeadI += diffIndexAndXordHeadI;

	    

	    String headlessXordFingerprintS = fingerprintBinary.substring(xordFingerprintHeadLengthI);

	    String appendedXordFingerprintS = headBinaryS
	            + headlessXordFingerprintS;

	    long appendedXordFingerprintL = Long.parseLong(appendedXordFingerprintS, 2);
	    String appendedFingerprintHexS = Long.toHexString(appendedXordFingerprintL);
	    
        int bufferByteSizeI = (fingerprintBitSizeI % 8 == 0) ? 
                (fingerprintBitSizeI / 8) : ((fingerprintBitSizeI / 8) + 1);
                
        while (appendedFingerprintHexS.length() < bufferByteSizeI * 2) {
            appendedFingerprintHexS = '0' + appendedFingerprintHexS; 
        }

        return appendedFingerprintHexS;
	}
	
	
	/**
	 * shifts the fingerprint 45 bits to the right, then ensures that what
	 * remains is updated to equal a given byte. 
	 * 
	 * FIXME get rid of all this string binary processing
	 * 
	 * @param newByte
	 * @param xordFingerprint
	 * @return
	 * 
	 * @deprecated this diesn't work well when you try to append a '0' to the fingerprint head
	 */
	public static long appendByteToHead(int newByte, long xordFingerprint, int fingerprinterShift) {

		// head is the bits that are indexed @ > 45 - which means they were
		// part of the value that generated the index which selected the
		// member of the push table against which this fingerprint was
		// xor'd
		int xordFingerprintHeadLengthI = Math.abs(Long.toBinaryString(
				xordFingerprint).length() - fingerprinterShift);

		// figure out the current value of the existing bits in head, and what
		// the delta is between what's already there and what needs to be there.
		// then setup to update the head value accordingly.
		int xordFingerprintHeadI = (int) (xordFingerprint >> fingerprinterShift);
		int diffIndexAndXordHeadI = newByte - xordFingerprintHeadI;
		xordFingerprintHeadI += diffIndexAndXordHeadI;

		String headBinaryS = 
				Integer.toBinaryString((xordFingerprintHeadI & 0xFF));
		
		while(headBinaryS.length() < 8) { headBinaryS = '0' + headBinaryS; }
		
		String headlessXordFingerprintS = Long.toBinaryString(xordFingerprint)
				.substring(xordFingerprintHeadLengthI);

		String appendedXordFingerprintS = headBinaryS
				+ headlessXordFingerprintS;

		return Long.parseLong(appendedXordFingerprintS, 2);

	}
	

	/**
	 * converts a long to a byte array. if the value
	 * is 99AABBCCDDEEFF, the the returned array will be 
	 * {99, AA, BB, CC, DD, EE, FF}
	 * 
	 * @param value: the long to be converted
	 * 
	 * @param fingerprint: if you're trying to array-a-fy a fingerprint, then
	 * you know it's only seven bytes, not eight. setting this to true will 
	 * cause the head byte (which should be 00x) to be dropped.
	 * 
	 * @return
	 * 
	 * @deprecated assumes fingerprint is degree 53
	 */
	public static byte[] getLongAsByteArray(long value, boolean fingerprint) {
		byte[] longAsByteArr =  ByteBuffer 
									.allocate(Long.SIZE / Byte.SIZE)
									.putLong(value)
									.array();
		
		if (fingerprint) { 
			return Arrays.copyOfRange(longAsByteArr, 1, 8); 
		}
		else {
			return longAsByteArr;
		}
	}
	
	
	/**
	 * 
	 * @param fingerprint
	 * @param fingerprintDegree
	 * @return
	 */
	public static byte[] getFingerprintAsByteArray(long fingerprint, int fingerprintDegree) {
//LOGGER.info("fingerprint in is::: " + String.format("%X", fingerprint));
//LOGGER.info("fingerprint in degree is: " + fingerprintDegree);
	    
        byte[] longAsByteArr =  ByteBuffer 
                .allocate(Long.SIZE / Byte.SIZE)
                .putLong(fingerprint)
                .array();	    
	    
        int numFingerprintBytesI = 
                (fingerprintDegree % 8 == 0) ? (fingerprintDegree / 8) : ((fingerprintDegree / 8) + 1);
//LOGGER.info("numFingerprintBytesI is: " + numFingerprintBytesI);          
        int startByteIndexI = 8 - numFingerprintBytesI;
//LOGGER.info("startByteIndexI is: " + startByteIndexI);
//LOGGER.info("longAsByteArr is: " + ByteManipulation.getByteArrayAsHexString(longAsByteArr));
//LOGGER.info("^^^^^^^^^^^^^^^");
        return Arrays.copyOfRange(longAsByteArr, startByteIndexI, 8);    
	}
	
	
	
	/**
	 * FIXME not in love with the idea of using strings
	 * 
	 * @param fingerprint
	 * @return
	 */
	public static long getFingerprintAsLong(byte[] fingerprint) {
	    String fingerprintHexString = getHexString(fingerprint);
	    return Long.parseLong(fingerprintHexString, 16);	    
	}
	
	
	
	
	/**
	 * converts a 7 byte fingerprint byte array to a long
	 * 
	 * @param byteARR
	 * @return
	 */
	public static long getSevenByteArrayAsLong(byte[] byteARR) {

		Assert.assertTrue("input array isn't seven bytes in length!",
				byteARR.length == 7);
		
		long returnLong = 0;
		
		for (int i = 0; i < 7; i ++) {
			if (i > 0) { returnLong <<= 8; }
			returnLong |= ((long) byteARR[i] & 0xFF);	
		}
		
		return returnLong;
	}
	
	
	/**
	 * pretty self explanatory -- takes a byte array and returns a string
	 * representing the contents of the array as a hex string
	 * 
	 * @param byteARR
	 */
	public static String getByteArrayAsHexString(byte[] byteARR) {
		StringBuffer stringBuffer = new StringBuffer();
		
		for (byte b : byteARR) {
			stringBuffer.append(String.format("%02X ", b));
		}
		
		return stringBuffer.toString();
	}
	
	
}





