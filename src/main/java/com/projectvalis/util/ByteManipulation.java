package com.projectvalis.util;

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
	public static long getTailByte(long xordFingerprint) {
		return 0x00000000000000FF & xordFingerprint;
	}
	
	
	
	/**
	 * shifts the fingerprint 45 bits to the right, then ensures that what
	 * remains is updated to equal a given byte. 
	 * 
	 * @param newByte
	 * @param xordFingerprint
	 * @return
	 */
	public static long appendByteToHead(int newByte, long xordFingerprint) {

		// head is the bits that are indexed @ > 45 - which means they were
		// part of the value that generated the index which selected the
		// member of the push table against which this fingerprint was
		// xor'd
		int xordFingerprintHeadLengthI = Math.abs(Long.toBinaryString(
				xordFingerprint).length() - 45);

		// figure out the current value of the existing bits in head, and what
		// the delta is between what's already there and what needs to be there.
		// then setup to update the head value accordingly.
		int xordFingerprintHeadI = (int) (xordFingerprint >> 45);
		int diffIndexAndXordHeadI = newByte - xordFingerprintHeadI;
		xordFingerprintHeadI += diffIndexAndXordHeadI;

		String headBinaryS = Integer.toBinaryString(xordFingerprintHeadI);

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
	 * pretty self explanatory -- takes a byte array and prints the contents
	 * to the log
	 * 
	 * @param byteARR
	 */
	public static void printByteArray(byte[] byteARR) {
		StringBuffer stringBuffer = new StringBuffer();
		
		for (byte b : byteARR) {
			stringBuffer.append(b + " ");
		}
		
		LOGGER.info("byte array is: " + stringBuffer.toString());
	}
	
	
}





