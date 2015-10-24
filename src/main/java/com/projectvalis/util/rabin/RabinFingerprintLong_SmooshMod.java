package com.projectvalis.util.rabin;

import org.rabinfingerprint.fingerprint.RabinFingerprintLong;
import org.rabinfingerprint.polynomial.Polynomial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		LOGGER.info("PUSH TABLE IS: \n");
		for (int i = 0; i < pushTable.length; i++) {
			LOGGER.info(String.format("%X", i) + " "
					+ String.format("%X", pushTable[i]));
		}

		for (byte b : bytes) {
			LOGGER.info("FINGERPRINT WAS: " + String.format("%X", fingerprint));

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

			countI = (countI < this.pushTable.length - 1) ? (countI += 1) : (0);
		}
	}

	/**
	 * gives access to the contents of the current push table
	 * 
	 * @param index
	 * @return
	 */
	public long getPushTableByteAt(int index) {
		return pushTable[index];
	}

}
