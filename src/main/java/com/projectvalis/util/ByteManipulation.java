package com.projectvalis.util;

import java.nio.ByteBuffer;

import org.rabinfingerprint.fingerprint.RabinFingerprintLong;

public class ByteManipulation {
	
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
	 * @param listIndex
	 * @param xordFingerprint
	 * @return
	 */
	public static long appendByteToHead(int listIndex, long xordFingerprint) {

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
		int diffIndexAndXordHeadI = listIndex - xordFingerprintHeadI;
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
	 * @param value
	 * @return
	 */
	public static byte[] getLongAsByteArray(long value) {
		return ByteBuffer
				.allocate(Long.SIZE / Byte.SIZE)
				.putLong(value)
				.array();
	}
	
	
	/**
	 * converts a byte array to a long
	 * 
	 * @param byteARR
	 * @return
	 */
	public static long getByteArrayAsLong(byte[] byteARR) {
		return ByteBuffer
				.allocate(Long.SIZE / Byte.SIZE)
				.put(byteARR)
				.getLong();
	}
	
	
}





