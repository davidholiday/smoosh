package com.projectvalis.util.rabin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
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

		for (byte b : bytes) {
			
			LOGGER.info("FINGERPRINT WAS: " + 
					String.format("%X", fingerprint));

			LOGGER.info("inbound byte is: " + String.format("%X", (b & 0xFF)));

			int j = (int) ((fingerprint >> shift) & 0x1FF);

			LOGGER.info("pushTable index and value are: "
					+ String.format("%X", j) + " "
					+ String.format("%X", pushTable[j]));

			LOGGER.info("fingerprint pre-XOR, post shift/append is: "
					+ String.format("%X", ((fingerprint << 8) | (b & 0xFF))));

			fingerprint = ((fingerprint << 8) | (b & 0xFF)) ^ pushTable[j];

			LOGGER.info("FINGERPRINT IS NOW: "
					+ String.format("%X", fingerprint) + "\n");
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
	 * XOR values that were applied to the fingerprint
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
				ByteManipulation.appendByteToHead(xordByteNine, fingerprint15L);
	
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
	 * @param xorChainARR the values used to xor the fingerprint for bytes 1-14
	 * 
	 * @param processedByte a single fingerprinted byte
	 * 
	 * @return a mostly unprocessed byte (6/7 XORs are handled by this method)
	 */
	public byte applyXorChain(int startIndexI, 
							  int[] xorChainARR, 
							  byte processedByte) {
		
		int countI = 8 - startIndexI;
		int positionI = 7;
		
		for (int i = positionI; i < countI; i--) {
			
			byte[] xorValBytesARR = 
				ByteManipulation.getLongAsByteArray(
						xorChainARR[startIndexI], false);
			
			processedByte = (byte) (processedByte ^ xorValBytesARR[i]);
			startIndexI++;
		}
		
		return processedByte;
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

















