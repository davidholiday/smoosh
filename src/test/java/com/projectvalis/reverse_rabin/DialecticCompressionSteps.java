package com.projectvalis.reverse_rabin;

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
	}
	
	
	@When("the first seven bytes and the head fingerprint byte (fingerprinted "
			+ "byte nine) is retained after byte 16 is pushed")
	public void smooshBlock() {
		smooshedByteBlockARR = fingerprinter.compress16(generatedByteARR);
	}

	
	@Then("the retained fingerprint byte can be used to retrieve byte 16 and "
			+ "the fingerprint representing bytes 1-15.")
	public void getByte16AndPreviousFingerprint() {
		// create method that takes smoosh block and returns a byte array with
		// [0] being byte 16 in original state
		// [1] - [7] being the fingerprint for bytes [1-15]
		
		
		Assert.fail("look here, fool!");
	}
	
	
}











