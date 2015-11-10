package com.projectvalis.util.rabin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.rabinfingerprint.fingerprint.RabinFingerprintLong;
import org.rabinfingerprint.polynomial.Polynomial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projectvalis.util.ByteManipulation;

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

	/**
	 * normally the index of the push table is computed as a function of the
	 * data being read. for now we're reading the table in a linear fashion to
	 * make it easy to test smoosh.
	 * 
	 * @TODO: change the pushTable lookup indexes to still be randomized, but
	 *        randomized via a deterministic algorithm. this way we can figure
	 *        out what the XOR values were for each byte.
	 */
	@Override
	public void pushBytes(final byte[] bytes) {
		int countI = 0;

//		LOGGER.info("PUSH TABLE IS: \n");
//		for (int i = 0; i < pushTable.length; i++) {
//			LOGGER.info(String.format("%X", i) + " "
//					+ String.format("%X", pushTable[i]));
//		}

		for (byte b : bytes) {
//			LOGGER.info("FINGERPRINT WAS: " + String.format("%X", fingerprint));
//
//			LOGGER.info("inbound byte is: " + String.format("%X", (b & 0xFF)));

			int j = (int) ((fingerprint >> shift) & 0x1FF);

//			LOGGER.info("pushTable index and value are: "
//					+ String.format("%X", j) + " "
//					+ String.format("%X", pushTable[j]));
//
//			LOGGER.info("fingerprint pre-XOR, post shift/append is: "
//					+ String.format("%X", ((fingerprint << 8) | (b & 0xFF))));

			fingerprint = ((fingerprint << 8) | (b & 0xFF)) ^ pushTable[j];

//			LOGGER.info("FINGERPRINT IS NOW: "
//					+ String.format("%X", fingerprint) + "\n");

			countI = (countI < this.pushTable.length - 1) ? (countI += 1) : (0);
		}
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
				ByteManipulation.appendByteToHead(xorIndex, xordFingerprintL);

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
						(xordFingerprintL << 8)| (tailByteL & 0xFF);
				
				returnAL.add(validCandidateL);
			}

		}

		return returnAL;
	}	

	
	
	/**
	 * method assumes you've followed the suggested method for choosing a
	 * polynomial degree (53, 47, 31, 15, ...). 
	 * 
	 * @see RabinFingerprintLong for details.
	 * 
	 * @return byte array representation of fingerprint long. if the fingerprint
	 * is 99AABBCCDDEEFF, the the returned array will be 
	 * {99, AA, BB, CC, DD, EE, FF}
	 */
	public byte[] getFingerprintAsByteArray() {
		int lengthI = (this.degree + 1) / 8;
		byte[] returnARR = new byte[lengthI];
		
		String fingerprintHexS = getFingerprint().toHexString();
		
		if (fingerprintHexS.length() % 2 > 0) {
			fingerprintHexS = "0" + fingerprintHexS;
		}
		
		int toI = lengthI * 2;
		int countI = 0;
		
		for (int i = 0; i < toI; i+=2) {
			String byteHexS = fingerprintHexS.substring(i, i + 1);
			returnARR[countI] = Byte.parseByte(byteHexS, 16);
			countI += 1;
		}
		
		return returnARR;
	}
	
	
	
	/**
	 * smooshes a block of sixteen bytes. 
	 * 
	 * @param bytesIn - a sixteen byte block of bytes
	 * 
	 * 
	 * @return a fifteen byte array in the following form:
	 * 
	 * [0-6]: the first seven bytes in their original state
	 * 
	 * [7]: byte nine, which was at the head of the fingerprint when byte
	 * sixteen was pushed, foring it to be shoved out the front of the chain.
	 * 
	 * [8-14]: the fingerprint of the sixteen byte block
	 * 
	 * TODO: experiment with returning 15 1/2 bytes instead of fifteen to 
	 * provide some room for extra metadata as needed.
	 */
	public byte[] compress16(byte[] bytesIn) {
		int returnArrIndexI = 0;
		byte[] returnARR = new byte[15];
		
		for (int i = 0; i < 16; i ++) {
			byte b = bytesIn[i];
			int headByteI = (int) ((fingerprint >> shift) & 0x1FF);	
			
			fingerprint = 
					((fingerprint << 8) | (b & 0xFF)) ^ pushTable[headByteI];
			
			// if we're in the first seven of the list throw them into the
			// return array
			if (i < 7) {
				returnARR[returnArrIndexI]  = b;
				returnArrIndexI++;
			}
			// else if we're on the sixteenth byte, grab the xord byte nine
			// (aka the head of the fingerprint when byte sixteen was pushed)
			// and eject.
			else if (i == 15) {
				returnARR[returnArrIndexI] = (byte) (headByteI & 0x0FF);
				returnArrIndexI++;
			}
			
		}

		for (byte b: getFingerprintAsByteArray()) {
			returnARR[returnArrIndexI] = b;
			returnArrIndexI++;
		}
		
		
		return returnARR;
	}
	
	
	
}








