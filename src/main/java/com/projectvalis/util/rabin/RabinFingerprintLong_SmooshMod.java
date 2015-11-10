package com.projectvalis.util.rabin;

import java.util.ArrayList;
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

	
	
	
	
}








