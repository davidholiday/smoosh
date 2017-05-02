package com.projectvalis.reverse_rabin;

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

public class FurlSteps extends Steps {
    static Logger LOGGER = LoggerFactory.getLogger(FurlSteps.class);
    
    private byte[] generatedByteARR;
    private byte[] fingerprintHeadsARR;

    private Polynomial rabinPolynomial;
    private RabinFingerprintLong_SmooshMod fingerprinter;
    
    @BeforeScenario
    public void setup() {
        rabinPolynomial = Polynomial.createIrreducible(53);
        fingerprinter = new RabinFingerprintLong_SmooshMod(rabinPolynomial);

        LOGGER.info("GENERATED POLYNOMIAL IS: " +
                rabinPolynomial.toHexString());
    }
    
    
    @Given("a push table updated for furl")
    public void updatePushTable() {
        
        for (int i = 0; i < 256; i ++) {
            long oldPoly = fingerprinter.getPushTable()[i];
            long newPoly = ByteManipulation.replaceTailByte(oldPoly, (byte)i);
            LOGGER.trace("oldPoly is: " + String.format("%X", oldPoly));
            LOGGER.trace("newPoly is: " + String.format("%X", newPoly) + "\n");
            fingerprinter.getPushTable()[i] = newPoly;
        }
        
    }

    
    @Then("the low order byte of each element matches that element's index value")
    public void checkUpdatedPushTableIndexes() {
        
        for (int i = 0; i < 256; i++) {
            long poly = fingerprinter.getPushTable()[i];
            byte lowOrderByteActual = (byte) ByteManipulation.getTailByte(poly);
            LOGGER.trace("lowOrderByteActual and expected-value are: " + lowOrderByteActual + " " + (byte)i);
            Assert.assertTrue("low order byte doesn't match element index!", lowOrderByteActual == (byte)i);
        }
        
    }
    
    
    
    @Given("an array of $numBytesI bytes")
    public void createByteArray(@Named("numBytesI") int numBytesI) {
        generatedByteARR = TestHelper.createByteArray(numBytesI);
    }

    
    @When("the byte array is fingerprinted with heads saved")
    public void fingerprintByteArraySaveHeads() {
        fingerprintHeadsARR = fingerprinter.pushAndSaveHeads(generatedByteARR);
    }
    
    
    
    
    /**
     * 
     * for retain-bytes-list --> heads, don't forget that the lists aren't quite in sync. the first head
     * in the list will be the first three bits of byte 7, then the trailing 5 bits of byte 7 with the leading
     * three bits of byte 8 - then the trailing five bits of b8 with the leading three bits from b9, etc etc.
     * 
     * 
     * 
     * 
     * 
     * 
     */
    @Then("you can recover the correct head list")
    public void checkRecoveredHeadsList() {
          
        long fingerprintLocalL = fingerprinter.getFingerprintLong();
        LOGGER.info("fingerprint is: " + String.format("%X", fingerprintLocalL));
        
        
        // replay all the heads except the first one - that's the one that'll be in play
        //
        byte[] computedBytesARR = new byte[generatedByteARR.length];
        byte[] computedHeadBytesARR = new byte[generatedByteARR.length];
        for (int i = generatedByteARR.length - 1; i > 6; i --) {
            
            byte currentTailByte = ByteManipulation.getTailByte(fingerprintLocalL);
            byte currentHeadByte = (byte)(currentTailByte ^ generatedByteARR[i]);        
            computedHeadBytesARR[i] = currentHeadByte;
            
            LOGGER.info("head byte was derived from value: " + String.format("%X", generatedByteARR[i]));
            LOGGER.info("computed head byte is: " + String.format("%X", currentHeadByte));
            
            int pushTableIndexI = (currentHeadByte & 0xFF);
            fingerprintLocalL = fingerprintLocalL ^ fingerprinter.getPushTable()[pushTableIndexI];
            computedBytesARR[i] = ByteManipulation.getTailByte(fingerprintLocalL);
            fingerprintLocalL = ByteManipulation.removeTailByte(fingerprintLocalL);
            ByteManipulation.appendByteToHead(currentHeadByte, fingerprintLocalL, fingerprinter.getShiftVal());
            
            LOGGER.info("fingerprint is now: " + String.format("%X", fingerprintLocalL));
        }
        LOGGER.info("");
        LOGGER.info("computed heads are : " + ByteManipulation.getByteArrayAsHexString(computedHeadBytesARR));
        LOGGER.info("actual heads are   : " + ByteManipulation.getByteArrayAsHexString(fingerprintHeadsARR));
        LOGGER.info("");
        LOGGER.info("computed bytes are : " + ByteManipulation.getByteArrayAsHexString(computedBytesARR));
        LOGGER.info("generated bytes are: " + ByteManipulation.getByteArrayAsHexString(generatedByteARR));
        LOGGER.info("");
        LOGGER.info("*** NOW UNFURLING WITH A MODIFIED (WRONG) FIRST HEAD BIT **");
        
        byte wrongFirstHeadI = (byte) (fingerprintHeadsARR[6] + 1);
        computedHeadBytesARR[6] = wrongFirstHeadI;
        
        LOGGER.info("head byte was derived from value: " + String.format("%X", generatedByteARR[6]));
        LOGGER.info("computed head byte is: " + String.format("%X", wrongFirstHeadI));

        int pushTableIndexI = (wrongFirstHeadI & 0xFF);
        fingerprintLocalL = fingerprintLocalL ^ fingerprinter.getPushTable()[pushTableIndexI];
        computedBytesARR[6] = ByteManipulation.getTailByte(fingerprintLocalL);
        fingerprintLocalL = ByteManipulation.removeTailByte(fingerprintLocalL);
        ByteManipulation.appendByteToHead(wrongFirstHeadI, fingerprintLocalL, fingerprinter.getShiftVal());

        LOGGER.info("fingerprint is now: " + String.format("%X", fingerprintLocalL));

        LOGGER.info("");
        LOGGER.info("*** UPDATING COMPUTED BYTES ARR WITH KNOWN-INCORRECT BYTE DATA AND GENERATED NEW HEADS**");
        for (int k = 5; k > -1; k --) {
            computedBytesARR[k] = ByteManipulation.getTailByte(fingerprintLocalL);
            fingerprintLocalL = fingerprintLocalL >> 8;
        }

        LOGGER.info("computed bytes are : " + ByteManipulation.getByteArrayAsHexString(computedBytesARR));
        fingerprinter.reset();
        byte[] shouldBeWrongHeadsARR = fingerprinter.pushAndSaveHeads(computedBytesARR);
        
        
        LOGGER.info("");
        LOGGER.info("right heads are: " + ByteManipulation.getByteArrayAsHexString(fingerprintHeadsARR));
        LOGGER.info("wrong heads are: " + ByteManipulation.getByteArrayAsHexString(shouldBeWrongHeadsARR));
        
        LOGGER.info("");
        LOGGER.info("computed bytes are : " + ByteManipulation.getByteArrayAsHexString(computedBytesARR));
        LOGGER.info("generated bytes are: " + ByteManipulation.getByteArrayAsHexString(generatedByteARR));
        LOGGER.info("");
        
        
        /*
        byte[][] allHeadsARR = new byte[8][];
        for (int i = 0; i < 8; i ++) {
            long fingerprintLocalTempL = fingerprintLocalL;

            int wrongFirstHeadI = i;
            byte currentTailByte = ByteManipulation.getTailByte(fingerprintLocalTempL);
            byte currentHeadByte = (byte)(currentTailByte ^ wrongFirstHeadI);        
            computedHeadBytesARR[6] = currentHeadByte;

            LOGGER.info("head byte was derived from value: " + String.format("%X", generatedByteARR[6]));
            LOGGER.info("computed head byte is: " + String.format("%X", currentHeadByte));

            int pushTableIndexI = (currentHeadByte & 0xFF);
            fingerprintLocalTempL = fingerprintLocalTempL ^ fingerprinter.getPushTable()[pushTableIndexI];
            computedBytesARR[6] = ByteManipulation.getTailByte(fingerprintLocalTempL);
            fingerprintLocalTempL = ByteManipulation.removeTailByte(fingerprintLocalTempL);
            ByteManipulation.appendByteToHead(currentHeadByte, fingerprintLocalTempL);

            LOGGER.info("fingerprint is now: " + String.format("%X", fingerprintLocalTempL));

            LOGGER.info("");
            LOGGER.info("*** UPDATING COMPUTED BYTES ARR WITH KNOWN-INCORRECT BYTE DATA AND GENERATED NEW HEADS**");
            for (int k = 5; k > -1; k --) {
                computedBytesARR[k] = ByteManipulation.getTailByte(fingerprintLocalTempL);
                fingerprintLocalTempL = fingerprintLocalTempL >> 8;
            }

            LOGGER.info("computed bytes are : " + ByteManipulation.getByteArrayAsHexString(computedBytesARR));
            fingerprinter.reset();
            allHeadsARR[i] = fingerprinter.pushAndSaveHeads(computedBytesARR);      
        }

        
        LOGGER.info("");
        LOGGER.info("right heads are: " + ByteManipulation.getByteArrayAsHexString(fingerprintHeadsARR));
        LOGGER.info("all heads are: ");
        for (int i = 0; i < allHeadsARR.length; i ++) {
            LOGGER.info("              : " + ByteManipulation.getByteArrayAsHexString(allHeadsARR[i]));
        }
        
        LOGGER.info("");
        LOGGER.info("computed bytes are : " + ByteManipulation.getByteArrayAsHexString(computedBytesARR));
        LOGGER.info("generated bytes are: " + ByteManipulation.getByteArrayAsHexString(generatedByteARR));
        LOGGER.info("");
        */
        
        
    }
    
    
    
}












