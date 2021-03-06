package com.projectvalis.reverse_rabin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.steps.Steps;
import org.junit.Assert;
import org.rabinfingerprint.polynomial.Polynomial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projectvalis.util.ByteManipulation;
import com.projectvalis.util.TestHelper;
import com.projectvalis.util.rabin.RabinFingerprintLong_SmooshMod;



/**
 * BDD step file for the dialectic rabin compression experiment
 * 
 * @author snerd
 *
 */
public class DialecticCompressionSteps extends Steps {
	
	static Logger LOGGER = LoggerFactory.getLogger(
			DialecticCompressionSteps.class);
	
	private byte[] generatedByteARR;
	private byte[] smooshedByteBlockARR;
	private byte[] fingerprinted16ARR;

	private Polynomial rabinPolynomial;
	private RabinFingerprintLong_SmooshMod fingerprinter;

	
	
	@BeforeScenario
	public void setup() {
		rabinPolynomial = Polynomial.createIrreducible(53);
		fingerprinter = new RabinFingerprintLong_SmooshMod(rabinPolynomial);

		LOGGER.info("GENERATED POLYNOMIAL IS: " +
				rabinPolynomial.toHexString());
	}
	
	
	
	@Given("an array of $numBytesI bytes:")
	public void createByteArray(@Named("numBytesI") int numBytesI) {
		generatedByteARR = TestHelper.createByteArray(numBytesI);
	}

	
	
	@When("the byte array is fingerprinted:")
	public void fingerPrintByteArray() {
		fingerprinter.pushBytes(generatedByteARR);
		
		fingerprinted16ARR = 
				ByteManipulation.getLongAsByteArray(
						fingerprinter.getFingerprintLong(), true);
	}
	
	
	
	@When("the first seven bytes and the head fingerprint byte (fingerprinted "
			+ "byte nine) is retained after byte 16 is pushed")
	public void smooshBlock() {	
		// compute smoosh block for all sixteen bytes
		fingerprinter.reset();
		smooshedByteBlockARR = fingerprinter.compress16(generatedByteARR);
		
		LOGGER.info("smoosh block is: " + 
				ByteManipulation.getByteArrayAsHexString(smooshedByteBlockARR));
		
		
		// grab what the xor'd byte eight value should be
		byte byteEightNibbleExpected = (byte) (generatedByteARR[7] & 0xF0);
		
		LOGGER.trace("xordByteEightNibbleExpected is: " 
				+ String.format("%02X", byteEightNibbleExpected));
		
		// grab what the xor'd byte nine value should be
		fingerprinter.reset();
		fingerprinter.pushBytes(Arrays.copyOf(generatedByteARR, 15));
		long fingerprint15 = fingerprinter.getFingerprintLong();	
		byte xordByteNineExpected = (byte)(fingerprint15 >> 45);
		
		LOGGER.trace("xordByteNineExpected is: " 
				+ String.format("%02X", xordByteNineExpected));
		
		// ensure first seven bytes are retained in smooshblock
		for (int i = 0; i < 7; i ++) {
			Assert.assertTrue(
				"error detected in smooshblock retention of first seven bytes!", 
					generatedByteARR[i] == smooshedByteBlockARR[i]);
		}
		
		// ensure we've got the xor'd byte nine in the smoosh block
		Assert.assertTrue(
				"error detected in smooshblock retention of xord byte nine!", 
					smooshedByteBlockARR[7] == xordByteNineExpected);
		
		// ensure the last seven bytes contain the fingerprint for all 16
		for (int i = 8; i < 15; i ++) {
			Assert.assertTrue(
				"error detected in smooshblock retention of fingerprint[1-16]!", 
						fingerprinted16ARR[i-8] == smooshedByteBlockARR[i]);
		}
		

		// ensure we've stored the high order nibble from byte eight
		Assert.assertTrue(
				"error detected in smooshblock retention of nibble eight!", 
					smooshedByteBlockARR[15] == byteEightNibbleExpected);

		
	}

	
	
	@Then("the retained fingerprint byte can be used to retrieve byte 16 and "
			+ "the fingerprint representing bytes 1-15.")
	public void getByte16AndPreviousFingerprint() {
		
		// compute fingerprint for first fifteen bytes and check
		fingerprinter.reset();
		fingerprinter.pushBytes(Arrays.copyOfRange(generatedByteARR, 0, 15));
		
		byte[] fingerprintedFifteenARR = 
				ByteManipulation.getLongAsByteArray(
						fingerprinter.getFingerprintLong(), true);
		
		byte[] rolledBackSmooshBlockARR = 
				fingerprinter.rollBack16(smooshedByteBlockARR);
			
		LOGGER.info("fingerprinted fifteen is: " + 
				ByteManipulation.getByteArrayAsHexString(
						fingerprintedFifteenARR));
		
		LOGGER.info("smoosh block is: "  + 
				ByteManipulation.getByteArrayAsHexString(
						smooshedByteBlockARR));
		
		LOGGER.info("fingerprint of all sixteen is "  + 
				ByteManipulation.getByteArrayAsHexString(
						fingerprinted16ARR));
		
		LOGGER.info("original 16 are: " + 
				ByteManipulation.getByteArrayAsHexString(
						generatedByteARR));
		
		LOGGER.info("rolled back smoosh is: " + 
				ByteManipulation.getByteArrayAsHexString(
						rolledBackSmooshBlockARR));		
		
		// check to ensure byte 16 was successfully retrieved
		Assert.assertTrue("error detected in rollback of byte 16!",
				generatedByteARR[15] == rolledBackSmooshBlockARR[7]);
		
		// check to ensure the fingerprint for bytes [1-15] was accurately 
		// computed 
		for (int i = 0; i < 6; i ++) {		
			Assert.assertTrue(
				"error detected in smooshblock retention of fingerprint[1-15]!", 
					fingerprintedFifteenARR[i] == rolledBackSmooshBlockARR[i]);
		}
		
	
	}

	
	
	
	@Then("the given bytes 1-7 can be used to undo all the xors against "
			+ "bytes 9-15 except for the one xor that is indexed by yet "
			+ "unknown byte 8")
	public void undoXorChainMostly() {
		byte[] firstSevenARR = Arrays.copyOf(smooshedByteBlockARR, 7);
		
		int[] xorValueChainIndexesARR = 
				fingerprinter.getXorChainIndexes(
						firstSevenARR, smooshedByteBlockARR[15]);
		
		long[] expectedXorValueChainARR = 
				TestHelper.pushBytesReturnXorValues(
						generatedByteARR, 
						fingerprinter.getShiftVal(), 
						fingerprinter.getPushTable());
		
		for (int i = 0; i < xorValueChainIndexesARR.length; i ++) {			
			long xorValL = 
					fingerprinter.getPushTable()[xorValueChainIndexesARR[i]];
		
			Assert.assertTrue("error detected in computed xor value list!",
					xorValL == expectedXorValueChainARR[i + 6]);
		}
		
		// what the xor value was when byte 15 was pushed - the processed
		// byte 8
		long xorEightValL = expectedXorValueChainARR[14];
		
		byte[] mostlyUnXordNineToFifteenARR = 
			fingerprinter.mostlyUnXorNineToFifteen(smooshedByteBlockARR);
		
		byte[] unXordNineToFifteenARR = fingerprinter.applyXorEight(
							xorEightValL, mostlyUnXordNineToFifteenARR);
		
		for (int i = 0; i < 7; i ++) {

			Assert.assertTrue("error detected in computed "
					+ "mostlyUnXordNineToFifteen byte array!", 
						unXordNineToFifteenARR[i] == generatedByteARR[i + 8]);
		}
		
		
	}
	
	
	/**
	 * TODO: why does fingerprintFourteen have '00' appended to the head 
	 * instead of the correct value for processed byte 8 (which is at index 1
	 * instead of 0)?
	 */
	@Then("all 256 possible values for bytes 8-15 can be inserted between "
			+ "known byte(s) 1-7 and 16.")
	public void computeAllPossibleSolutions() {
		
		byte[] firstSevenARR = Arrays.copyOf(smooshedByteBlockARR, 7);
		
		byte[] rolledBackSmooshBlockARR = 
				fingerprinter.rollBack16(smooshedByteBlockARR);
		
		byte[] fingerprintedFifteenARR = 
				Arrays.copyOf(rolledBackSmooshBlockARR, 7);
Assert.assertTrue(7 == fingerprintedFifteenARR.length);				
		long fingerprintedFifteenL = 
			ByteManipulation.getSevenByteArrayAsLong(fingerprintedFifteenARR);
		
		int[] xorChainIndexesARR = 
				fingerprinter.getXorChainIndexes(
						firstSevenARR, smooshedByteBlockARR[15]);
		
		byte[] mostlyUnXordNineToFifteenARR = 
				fingerprinter.mostlyUnXorNineToFifteen(smooshedByteBlockARR);
		
		List<byte[]> candidateNineToFifteenAL = new ArrayList<byte[]>();
		
		for (int i = 0; i < 256; i ++) {
			long xorEightValL = fingerprinter.getPushTable()[i];
			
			byte[] answerCandidateARR = 
					fingerprinter.applyXorEight(
							xorEightValL, mostlyUnXordNineToFifteenARR);
			
			candidateNineToFifteenAL.add(answerCandidateARR);
		}
		
		
		// compute all possible answers and figure out which one is correct.
		// there can be only one...
		int matchCountI = 0;
		int matchIndexI = -1;
		
		for (int h = 0; h < candidateNineToFifteenAL.size(); h ++) {
			byte[] candidateARR = candidateNineToFifteenAL.get(h);
			boolean matchFoundB = true;
			
			for (int i = 0; i < candidateARR.length; i ++) {
				
				if (candidateARR[i] != generatedByteARR[i + 8]) {
					matchFoundB = false;
				}
				
			}
			
			if (matchFoundB) { 
				matchCountI++; 
				matchIndexI = h;
			}
			
		}
		
		LOGGER.info("********************* matchCountI is: " + matchCountI);
		LOGGER.info("********************* matchIndexI is: " + matchIndexI);
		Assert.assertTrue(
				"match count was something other than one!", matchCountI == 1);
		
		
		
		
		// now that we know what the value of byte eight was when it was at the
		// head of the fingerprint, we can roll it back to its original state.
		long eightAppendedToFingerprintFifteenL = 
				ByteManipulation.appendByteToHead(
						(byte)matchIndexI, 
						fingerprintedFifteenL,
						fingerprinter.getShiftVal());
		
		long fingerprintFourteenL = 
			ByteManipulation.removeTailByte(eightAppendedToFingerprintFifteenL);
		
		byte[] fingerprintFourteenARR = 
				ByteManipulation.getLongAsByteArray(
						fingerprintFourteenL, true);
		
LOGGER.info("fingerprintFourteen is: " + 
			ByteManipulation.getByteArrayAsHexString(fingerprintFourteenARR));

		byte processedByteEight = fingerprintFourteenARR[1];
		
LOGGER.info("processed byte eight is: " + 
		String.format("%02X", processedByteEight));
		
		byte originalByteEight = 
				fingerprinter.applyXorChain(
						1, xorChainIndexesARR, processedByteEight);
		
LOGGER.info(String.format("%02X", originalByteEight) + " " + 
		String.format("%02X", generatedByteARR[7]) + " " 
			+ String.format("%02X", smooshedByteBlockARR[15]));

		Assert.assertTrue("error detected in computing original byte eight!", 
				originalByteEight == generatedByteARR[7]);
		
		
		
		// now put it all together and check
		byte[] reversedSmooshBlockARR = new byte[16];
		
		byte[] originalNineToFifteen = 
				candidateNineToFifteenAL.get(matchIndexI);
		
		for (int i = 0; i < 7; i ++) {
			reversedSmooshBlockARR[i] = smooshedByteBlockARR[i];
			reversedSmooshBlockARR[i + 8] = originalNineToFifteen[i];			
		}
		
		reversedSmooshBlockARR[7] = originalByteEight;
		reversedSmooshBlockARR[15] = rolledBackSmooshBlockARR[7];
		
LOGGER.info(ByteManipulation.getByteArrayAsHexString(reversedSmooshBlockARR));
LOGGER.info(ByteManipulation.getByteArrayAsHexString(generatedByteARR));
		
		for (int i = 0; i < 16; i ++) {
			Assert.assertTrue("error detected in reversed smoosh block!", 
					reversedSmooshBlockARR[i] == generatedByteARR[i]);
		}
		
		
		
	}
	
	
	
}
















