package com.projectvalis.reverse_rabin;

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

import com.projectvalis.util.ByteManipulation;
import com.projectvalis.util.TestHelper;
import com.projectvalis.util.rabin.RabinFingerprintLong_SmooshMod;

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

	
	private byte[] fingerprintHeadsARR;
	
	
	
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
//generatedByteARR[15] = generatedByteARR[6];
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
	

	
	@When("the byte array is fingerprinted with heads saved")
	public void fingerprintByteArraySaveHeads() {
	    fingerprintHeadsARR = fingerprinter.pushAndSaveHeads(generatedByteARR);
	    //fingerprintHeadsARR = fingerprinter.pushAndXorByteSixAndSaveHeads(generatedByteARR);
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

		for (int i = 0; i < /*polynomialAL.size();*/ 256; i++) {
			long xordFingerprintL = polynomialAL.get(i) ^ fingerprintL;
			
			xordFingerprintL = 
					ByteManipulation.removeTailByte(xordFingerprintL);
			
			xordFingerprintAL.add(xordFingerprintL);
			
			long appendedXordFingerprintL = 
				ByteManipulation.appendByteToHead(i, xordFingerprintAL.get(i), fingerprinter.getShiftVal());
			
			appendedXordFingerprintAL.add(appendedXordFingerprintL);
		}

	}

	
	
	
	@Then("the original 8 bytes is retrieved from the fingerprint.")
	public void retrieveBytes() {
		int matchCountI = 0;
		boolean matchFoundB = false;	
		int actualMatchIndexI = -1;
		int actualMatchCountI = 0;
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
//				LOGGER.info("original and answer candidate byte arrays are: \n"
//						+ Arrays.toString(generatedByteARR) + " \n"
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
						actualMatchCountI += 1;
					}
				}

			}

		}

		LOGGER.info("match count is: " + matchCountI);
		LOGGER.info("actual match count is: " + actualMatchCountI);
		LOGGER.info("actual match index is: " + actualMatchIndexI);

		Assert.assertTrue(
				"NO MATCHING BYTE ARRAY FOUND FOR GIVEN FINGERPRINT!",
				matchFoundB);

	}



	@Then("it is possible to suss out what the original bytes were")
	public void backThatAssUp() {
	    long fingerprintLocalL = fingerprinter.getFingerprintLong();
	    long fingerprintOriginalL = fingerprintLocalL;
	    LOGGER.info("fingerprint is: " + String.format("%X", fingerprintLocalL));
	    LOGGER.info("original bytes was: " + ByteManipulation.getByteArrayAsHexString(generatedByteARR));
	    LOGGER.info("FINGERPRINT heads is: " + Arrays.toString(fingerprintHeadsARR));
	    
        for (int i = fingerprintHeadsARR.length - 1; i > 5; i --) {  
        //for (int i = 5; i < fingerprintHeadsARR.length; i ++) {   
	        int pushTableIndex = fingerprintHeadsARR[i] & 0xFF;
	        long xorValL = fingerprinter.getPushTable()[pushTableIndex];
	        
	        //LOGGER.info("pushTable index and value are: "
	        //            + String.format("%X", pushTableIndex) + " "
	        //            + String.format("%X", xorValL));
	        
	        fingerprintLocalL = xorValL ^ fingerprintLocalL;
	        
	        LOGGER.info("fingerprint with -pre-tail-snip is: " + String.format("%X", fingerprintLocalL));
	        
	        fingerprintLocalL = ByteManipulation.removeTailByte(fingerprintLocalL);
            LOGGER.info("FINGERPRINT IS NOW: " + String.format("%X", fingerprintLocalL));
	    }
	    

	    
	    
        LOGGER.info("*** NOW FLIPPING BIT IN THE INPUT DATA ***");
        LOGGER.info("generated byte array was: " + ByteManipulation.getByteArrayAsHexString(generatedByteARR));
        
        // is HObit 1?     
        int index = 0;
        int fu = generatedByteARR[index] & 0xFF;
        LOGGER.info("xor at push index: " + generatedByteARR[index] + "is: " + String.format("%X", fingerprinter.getPushTable()[fu]));
        if ( ( (  generatedByteARR[index] >>> 7 ) & 1 ) ==1 ) {
            generatedByteARR[index] = (byte) (generatedByteARR[index] & 0x00/*0xEF*/);
        } else {
            generatedByteARR[index] = (byte) (generatedByteARR[index] | 0xFF/*0x80*/);
        }
        fu = generatedByteARR[index] & 0xFF;
        LOGGER.info("xor at push index: " + generatedByteARR[index] + "is now: " + String.format("%X", fingerprinter.getPushTable()[fu]));
        
        
        
        LOGGER.info("generated byte array is:  " + ByteManipulation.getByteArrayAsHexString(generatedByteARR));

        LOGGER.info("*** updating fingerprint heads list and reseting fingerprint to original***");
        fingerprinter.reset();
        //fingerprintByteArraySaveHeads();
        fingerprintHeadsARR = fingerprinter.pushAndSaveHeads(generatedByteARR);
        fingerprintLocalL = fingerprintOriginalL;
        
        LOGGER.info("fingerprint heads is now:  " + Arrays.toString(fingerprintHeadsARR));
        LOGGER.info("FINGERPRINT IS: " + String.format("%X", fingerprintLocalL));
        for (int i = fingerprintHeadsARR.length - 1; i > 5; i --) {  
        //for (int i = 5; i < fingerprintHeadsARR.length; i ++) {
             int pushTableIndex = fingerprintHeadsARR[i];
             long xorValL = fingerprinter.getPushTable()[pushTableIndex & 0xFF];
                
             //LOGGER.info("pushTable index and value are: "
             //            + String.format("%X", pushTableIndex) + " "
             //            + String.format("%X", xorValL));
                
             fingerprintLocalL = xorValL ^ fingerprintLocalL;
                
             LOGGER.info("fingerprint with -pre-tail-snip is: " + String.format("%X", fingerprintLocalL));
                
             fingerprintLocalL = ByteManipulation.removeTailByte(fingerprintLocalL);
             LOGGER.info("FINGERPRINT IS NOW: " + String.format("%X", fingerprintLocalL));
         }	    
	    
	    
	    
	}




}
