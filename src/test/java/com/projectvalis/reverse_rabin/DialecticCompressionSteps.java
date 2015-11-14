package com.projectvalis.reverse_rabin;

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
		
		
		// ensure first seven bytes are retained in smooshblock
		for (int i = 0; i < 7; i ++) {
			Assert.assertTrue(
				"error detected in smooshblock retention of first seven bytes!", 
					generatedByteARR[i] == smooshedByteBlockARR[i]);
		}
		
		// ensure the last seven bytes contain the fingerprint for all 16
		for (int i = 8; i < 15; i ++) {
			Assert.assertTrue(
				"error detected in smooshblock retention of fingerprint[1-16]!", 
						fingerprinted16ARR[i-8] == smooshedByteBlockARR[i]);
		}

		
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
			
		LOGGER.info("fingerprinted fifteen is: ");
		ByteManipulation.printByteArray(fingerprintedFifteenARR);
		
		LOGGER.info("smoosh block is: " );
		ByteManipulation.printByteArray(smooshedByteBlockARR);
		
		LOGGER.info("fingerprint of all sixteen is ");
		ByteManipulation.printByteArray(fingerprinted16ARR);
		
		LOGGER.info("original 16 are: " );
		ByteManipulation.printByteArray(generatedByteARR);
		
		LOGGER.info("rolled back smoosh are is: ");
		ByteManipulation.printByteArray(rolledBackSmooshBlockARR);
		
		
		
		Assert.assertTrue("error detected in rollback of byte 16!",
				generatedByteARR[15] == rolledBackSmooshBlockARR[7]);
		
//		for (int i = 0; i < 6; i ++) {
//				
//			Assert.assertTrue(
//				"error detected in smooshblock retention of fingerprint[1-14]!", 
//					fingerprintedFifteenARR[i] == smooshedByteBlockARR[i + 8]);
//		}
		
		
		
		
	}
	
	
}











