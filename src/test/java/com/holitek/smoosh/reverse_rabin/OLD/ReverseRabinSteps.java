package com.holitek.smoosh.reverse_rabin.OLD;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.steps.Steps;
import org.rabinfingerprint.polynomial.Polynomial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.holitek.smoosh.OLD.ByteManipulation;
import com.holitek.smoosh.OLD.RabinFingerprintLong_SmooshMod;
import com.projectvalis.util.TestHelper;

/**
 * BDD step file for the 'can we reverse a rabin hash' experiment
 * 
 * @author snerd
 *
 */
public class ReverseRabinSteps extends Steps {
	static Logger LOGGER = LoggerFactory.getLogger(ReverseRabinSteps.class);

	private byte[] generatedByteARR;

	private Polynomial rabinPolynomial;
	private RabinFingerprintLong_SmooshMod fingerprinter;

	private List<Long> xordFingerprintAL;
	private List<Long> appendedXordFingerprintAL;
	private List<Long> allXorPossibilitiesAL;

	
	
	@BeforeScenario
	public void setup() {
		rabinPolynomial = Polynomial.createIrreducible(53);
		fingerprinter = new RabinFingerprintLong_SmooshMod(rabinPolynomial);

		LOGGER.info("GENERATED POLYNOMIAL IS: " +
				rabinPolynomial.toHexString());
	}
	
	

	@Given("an array of $numBytesI bytes")
	public void createByteArray(@Named("numBytesI") int numBytesI) {
		generatedByteARR = TestHelper.createByteArray(numBytesI);
	}

	
	
	@Then("a data structure is created containing all possible sets of input.")
	public void generateAllSets() {
		allXorPossibilitiesAL = new ArrayList<Long>();
		long fingerprintL = fingerprinter.getFingerprintLong();
		List<Long> polynomialAL = fingerprinter.getPushTableAsList();

		for (int i = 0; i < polynomialAL.size(); i++) {
			long polynomial_L = polynomialAL.get(i);

			List<Long> rolledBackFingerprintAL = 
				fingerprinter.rollbackFingerprintFirst(
						polynomial_L, fingerprintL, i);

			// long tailByteL = rolledBackFingerprintAL.get(1);

			allXorPossibilitiesAL.addAll(
					fingerprinter.rollbackFingerprintSecond(
							rolledBackFingerprintAL));
		}

	}

	
	
	@When("the byte array is fingerprinted")
	public void fingerPrintByteArray() {
		fingerprinter.pushBytes(generatedByteARR);
	}
	
	
	

	@Then("the correct eight bits is appended to the head of the fingerprint "
			+ "such that\n the first [n] bits in the result are equal to the "
			+ "push table index used to \n generate the result.")
	public void checkAppendedBits() {

		for (int i = 0; i < appendedXordFingerprintAL.size(); i++) {

			Long currentFingerprintL = appendedXordFingerprintAL.get(i);

			int currentFingerprintHeadI = 
					(int) ((currentFingerprintL >> 45) & 0x1FF);

			Assert.assertTrue("BITS AT HEAD OF APPENDED LIST NOT CORRECT! "
					+ "EXPECTED: " + i + " GOT: " + currentFingerprintHeadI,
					i == currentFingerprintHeadI);

		}

	}

	
	
	@When("the fingerprinted array is xor'd against every entry "
			+ "in the push table")
	public void xorFingerprintAll() {
		long fingerprintL = fingerprinter.getFingerprintLong();
		List<Long> polynomialAL = fingerprinter.getPushTableAsList();
		xordFingerprintAL = new ArrayList<Long>();
		appendedXordFingerprintAL = new ArrayList<Long>();

		for (int i = 0; i < polynomialAL.size(); i++) {
			long xordFingerprintL = polynomialAL.get(i) ^ fingerprintL;
			
			xordFingerprintL = 
					ByteManipulation.removeTailByte(xordFingerprintL);
			
			xordFingerprintAL.add(xordFingerprintL);
			
			long appendedXordFingerprintL = 
				ByteManipulation.appendByteToHead(i, xordFingerprintAL.get(i));
			
			appendedXordFingerprintAL.add(appendedXordFingerprintL);
		}

	}

	
	
	
	@Then("the original 8 bytes is retrieved from the fingerprint.")
	public void retrieveBytes() {
		int matchCountI = 0;
		boolean matchFoundB = false;	
		int actualMatchIndexI = -1;
		long fingerprintL = fingerprinter.getFingerprintLong();
		LOGGER.debug("fingerprintL was: " + fingerprintL);
		
		generateAllSets();

		for (int i = 0; i < allXorPossibilitiesAL.size(); i++) {
			fingerprinter.reset();
			long answerCandidateL = allXorPossibilitiesAL.get(i);

			LOGGER.trace("answerCandidateL is: " + answerCandidateL);

			byte[] answerCandidateByteARR = ByteBuffer
					.allocate(Long.SIZE / Byte.SIZE).putLong(answerCandidateL)
					.array();

			LOGGER.trace("answerCandidateL is in hex: "
					+ ByteManipulation.getHexString(
							answerCandidateByteARR).toUpperCase());
			
			fingerprinter.pushBytes(answerCandidateByteARR);
			
			LOGGER.trace("current and original fingerprint are: "
					+ fingerprinter.getFingerprintLong() + " " + fingerprintL);
			
			if (fingerprinter.getFingerprintLong() == fingerprintL) {
				matchCountI++;

//				LOGGER.info("************************************************");
//				fingerprinter.reset();
//				fingerprinter.pushBytes(generatedByteARR);
//				LOGGER.info(fingerprinter.getFingerprintLong() + " ");
//
//				fingerprinter.reset();
//				fingerprinter.pushBytes(answerCandidateByteARR);
//				LOGGER.info(fingerprinter.getFingerprintLong() + "\n");
//
//				LOGGER.info("original and answer candidate byte arrays are: "
//						+ Arrays.toString(generatedByteARR) + " "
//						+ Arrays.toString(answerCandidateByteARR));
//				LOGGER.info("************************************************");

				if (!matchFoundB) {
					boolean tempMatchB = true;

					for (int k = 0; k < answerCandidateByteARR.length; k++) {
						if (answerCandidateByteARR[k] != generatedByteARR[k]) {
							tempMatchB = false;
							break;
						}
					}

					matchFoundB = tempMatchB;
					if (matchFoundB) {
						actualMatchIndexI = i;
					}
				}

			}

		}

		LOGGER.info("match count is: " + matchCountI);
		LOGGER.info("actual match index is: " + actualMatchIndexI);

		Assert.assertTrue(
				"NO MATCHING BYTE ARRAY FOUND FOR GIVEN FINGERPRINT!",
				matchFoundB);

	}



	




}
