package com.projectvalis.reverse_rabin;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
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


public class DoubleHashSteps extends Steps {

    static Logger LOGGER = LoggerFactory.getLogger(DoubleHashSteps.class);
    
    private byte[] generatedByteARR;
    private byte[] invertedGeneratedByteARR;
    
    private byte[] generatedByteArrayHeads;
    private List<List<Byte>> allPossibleHeadsL;
    
    private List<Long> allXorPossibilitiesAL;
    private List<Long> inverted_allXorPossibilitiesAL;


    
    private Polynomial rabinPolynomial;
    private RabinFingerprintLong_SmooshMod fingerprinter;
    private long fingerprintL;
    private long inverseFingerprintL;
    
    @BeforeScenario
    public void setup() {
        rabinPolynomial = Polynomial.createIrreducible(53); //53
        fingerprinter = new RabinFingerprintLong_SmooshMod(rabinPolynomial);

        LOGGER.info("GENERATED POLYNOMIAL IS: " +
                rabinPolynomial.toHexString());
        
        //fingerprinter.reducePushTableToDegree();
        fingerprinter.populatePushTableArray();
    }
    
    
    @Given("an array of $numBytesI bytes")
    public void createByteArray(@Named("numBytesI") int numBytesI) {
        
        //sloppy...
        if (numBytesI == 15) { //4
            rabinPolynomial = Polynomial.createIrreducible(15); //53
            fingerprinter = new RabinFingerprintLong_SmooshMod(rabinPolynomial);

            LOGGER.info("GENERATED POLYNOMIAL IS: " +
                    rabinPolynomial.toHexString());
            
            //fingerprinter.reducePushTableToDegree();
            fingerprinter.populatePushTableArray();            
        }
        
        generatedByteARR = TestHelper.createByteArray(numBytesI);
        LOGGER.info("generatedByteARR is: " + ByteManipulation.getByteArrayAsHexString(generatedByteARR));
        
        invertedGeneratedByteARR = ArrayUtils.clone(generatedByteARR);
        ArrayUtils.reverse(invertedGeneratedByteARR);
        LOGGER.info("inverted generatedByteARR is: " + ByteManipulation.getByteArrayAsHexString(invertedGeneratedByteARR));
    }

    
    
    @When("both the array and its inverse are fingerprinted")
    public void fingerprintArrays() {
        fingerprinter.pushBytes(invertedGeneratedByteARR);
        inverseFingerprintL = fingerprinter.getFingerprintLong();
        
        fingerprinter.reset();
        
        fingerprinter.pushBytes(generatedByteARR);
        fingerprintL = fingerprinter.getFingerprintLong();
    }

    
    @When("all collisions are calculated for both fingerprints")
    public void getBothFingerprintCollisions() {
//        allXorPossibilitiesAL = new ArrayList<Long>();
//        generateAllSets(allXorPossibilitiesAL, fingerprintL);
//        
//        inverted_allXorPossibilitiesAL = new ArrayList<Long>();
//        generateAllSets(inverted_allXorPossibilitiesAL, inverseFingerprintL);       
    }
    
    
    
    @Then("there will be only one member of both sets of results that resolve to both hashes")
    public void checkBothLists() {
//        LOGGER.info("** CHECKING CANDIDATE LISTS **");
//        retrieveBytes(allXorPossibilitiesAL, fingerprintL, generatedByteARR, false);
//        retrieveBytes(inverted_allXorPossibilitiesAL, inverseFingerprintL, invertedGeneratedByteARR, false);
//        
//        LOGGER.info("** CROSS CHECKING LISTS AND FINGERPRINTS **");
//        retrieveBytes(allXorPossibilitiesAL, inverseFingerprintL, invertedGeneratedByteARR, true);
//        retrieveBytes(inverted_allXorPossibilitiesAL, fingerprintL, generatedByteARR, true);
        
//        LOGGER.info("** CHECKING EXPERIMENTAL UNROLL CODE **");
//        List<List<Byte>> candidatesAL = generateAllSetsFour(fingerprintL, 17, 15); 
//        List<List<Byte>> inverseCandidatesAL = generateAllSetsFour(inverseFingerprintL, 17, 15);
       
//        LOGGER.info("** checking candidate lists");
//        retrieveBytesTwo(candidatesAL, fingerprintL, generatedByteARR, false);
//        retrieveBytesTwo(inverseCandidatesAL, inverseFingerprintL, invertedGeneratedByteARR, false);
        
//       LOGGER.info("** cross checking candidate lists");
//        retrieveBytesTwo(candidatesAL, inverseFingerprintL, invertedGeneratedByteARR, true);
//        retrieveBytesTwo(inverseCandidatesAL, fingerprintL, generatedByteARR, true);       
        
        LOGGER.info("** checking to see what happens when the first two of 16 bytes are unknown...");
        byte[] temp_generatedByteARR = new byte[generatedByteARR.length];
        for (int a = 0; a < generatedByteARR.length; a ++) {
            temp_generatedByteARR[a] = generatedByteARR[a];
        }
        
        for (int i = 0; i < 256; i ++) {
            temp_generatedByteARR[generatedByteARR.length - 1] = (byte)i;
            
            for(int k = 0; k < 256; k ++) {
                temp_generatedByteARR[generatedByteARR.length - 2] = (byte)k;
                fingerprinter.reset();
                fingerprinter.pushBytes(temp_generatedByteARR);
                
                if (fingerprinter.getFingerprintLong() == fingerprintL) {
                    LOGGER.info("match found!");
                    LOGGER.info("fingerprint is: " + String.format("%X", fingerprintL));
                    LOGGER.info("generatedBytes was: " + ByteManipulation.getByteArrayAsHexString(generatedByteARR));
                    LOGGER.info("matching set was:   " + ByteManipulation.getByteArrayAsHexString(temp_generatedByteARR));
                }
                
            }
            
        }
        
        

        
               
    }
    
    
    @When("the array is fingerprinted")
    public void fingerprintArray() {
        fingerprinter.pushBytes(generatedByteARR);
    }
    
    
    @Then("the fingerprint xord in both ways will yield the same result")
    public void checkXOR() {
        int randyI = ThreadLocalRandom.current().nextInt(0, 255);
        long fingerprintXordOldWayL = fingerprinter.getFingerprintLong() ^ fingerprinter.getPushTable()[randyI];
              
        byte[] fingerprintArray = 
                ByteManipulation.getFingerprintAsByteArray(fingerprinter.getFingerprintLong(), fingerprinter.getDegree());

        byte[] fingerprintXordNewWayARR = 
                fingerprinter.xorFingerprintAgainstPushTableElement(randyI, fingerprintArray);
        
        long fingerprintXordNewWayL = ByteManipulation.getFingerprintAsLong(fingerprintXordNewWayARR);
   
        Assert.assertEquals(
                "xord fingerprints should match and they don't!", fingerprintXordOldWayL, fingerprintXordNewWayL);
    }
    
    
    @When("the array is fingerprinted with heads saved")
    public void fingerprintWithHeads() {
        generatedByteArrayHeads = fingerprinter.pushAndSaveHeads(generatedByteARR);
    }
    
    
    @When("all the possible heads are computed from the fingerprint")
    public void getAllPossibleHeads() {
        allPossibleHeadsL = fingerprinter.getAllPossibleHeadsTwo(19, 3);
    }
    
    
    /**
     * don't forget to change the size of 'actualHeadL' if you change the number of generated bytes
     * 
     */
    @Then("one of the correct set of heads is in the computed head list")
    public void checkHeadList() {
        List<Byte> actualHeadL = new ArrayList<Byte>();
        for (Byte b : generatedByteArrayHeads) { actualHeadL.add(b); }
        
        // push with heads will give you all the leading zeros as well, hence the sublist
        actualHeadL = actualHeadL.subList(actualHeadL.size() - 3, actualHeadL.size());
        
        // we're reversing the lsit because the new get heads methods does that before it returns its results, thus
        // enabling the caller to more easily iterate through the list and apply the heads to the fingerprint
        // LIFO style
        Collections.reverse(actualHeadL);
        
        int matchCountI = 0;
        for (List<Byte> headL : allPossibleHeadsL) {            
            LOGGER.trace("head candidate is   : " + headL);
            LOGGER.trace("actual head list is :" + actualHeadL);
            LOGGER.trace("******");

            if (actualHeadL.equals(headL)) {
                matchCountI += 1;
            }
        }
        LOGGER.info(">> match count is; " + matchCountI);
        Assert.assertTrue("there should be at least one match in the computed head list!! ", matchCountI == 1);
    }
    
    
    
    @Then("the fingerprint heads are pushed back through the fingerprint")
    public void pushItPushItRealGood() {
        LOGGER.info("actual heads are: " + ByteManipulation.getByteArrayAsHexString(generatedByteArrayHeads));
        
        long fingerprintL = fingerprinter.getFingerprintLong();
        List<Byte> actualBytesAL = new ArrayList<>();
        for (Byte b : generatedByteARR) { actualBytesAL.add(b); }
        
        int matchCountI = 0;
        List<List<Byte>> candidatesAL = generateAllSetsFour(fingerprintL, 19, 53);
        LOGGER.trace("candidatesAL size is: " + candidatesAL.size());
        
        for (List<Byte> candidateAL : candidatesAL) {
            
            if (candidateAL.get(0) == actualBytesAL.get(0)) {
                LOGGER.trace("candidate is: " + candidateAL);
                LOGGER.trace("actual is   : " + actualBytesAL);
                LOGGER.trace("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");               
            }

            if (candidateAL.equals(actualBytesAL)) {
                matchCountI += 1;
            }
        }
        LOGGER.info("match count is; " + matchCountI);
        Assert.assertTrue("there should be one match in the computed bytes list!! ", matchCountI == 1);
        
    }
    
    
    
    
    /**
     * 
     */
    public void generateAllSets(List<Long> xorPossibilities, long fingerprintL) {
        List<Long> polynomialAL = fingerprinter.getPushTableAsList();

        for (int i = 0; i < polynomialAL.size(); i++) {
            long polynomial_L = polynomialAL.get(i);

            List<Long> rolledBackFingerprintAL = 
                fingerprinter.rollbackFingerprintFirst(
                        polynomial_L, fingerprintL, i);

            // long tailByteL = rolledBackFingerprintAL.get(1);

            
            xorPossibilities.addAll(
                    fingerprinter.rollbackFingerprintSecond(
                            rolledBackFingerprintAL));
        }

    }
    
    
    
    
    /**
     * 
     * @param fingerprintL
     * @param numBitsI
     * @param fingerprintBitSizeI
     * @return
     */
    public List<List<Byte>> generateAllSetsFour(long fingerprintL, int numBitsI, int fingerprintBitSizeI) {
        
        List<List<Byte>> returnAL = new ArrayList<>();
                
        int bufferHeadSizeI = Math.abs((fingerprintBitSizeI % 8) - 8);        
        List<List<Byte>> headsL = fingerprinter.getAllPossibleHeadsTwo(numBitsI, bufferHeadSizeI);
        
        for (List<Byte> headL : headsL) { 
            
            List<Byte> candidateAL = new ArrayList<>();
            long fingerprintLocalL = fingerprintL;
            for (int i = 0; i < headL.size(); i ++) {    
                int pushTableIndex = headL.get(i) & 0xFF;
                long xorValL = fingerprinter.getPushTable()[pushTableIndex];

                LOGGER.trace("pushTable index and value are: "
                            + String.format("%X", pushTableIndex) + " "
                            + String.format("%X", xorValL));

                fingerprintLocalL = xorValL ^ fingerprintLocalL;

                LOGGER.trace("fingerprint with -pre-tail-snip is: " + String.format("%X", fingerprintLocalL));

                byte tailByte = ByteManipulation.getTailByte(fingerprintLocalL);
                LOGGER.trace("tailByte is: " + String.format("%02X", tailByte));
                candidateAL.add(tailByte);
                
                fingerprintLocalL = ByteManipulation.removeTailByte(fingerprintLocalL);
                LOGGER.trace("FINGERPRINT IS NOW: " + String.format("%X", fingerprintLocalL));         
                LOGGER.trace("***** ");
            }
            
            // reversing to keep things in big-endian order
            Collections.reverse(candidateAL);
            LOGGER.trace("big-endian-order candidateAL: " + candidateAL);
            
            // now adding the un-xor'd remainder still in the fingerprint buffer
            byte[] fingerprintTempArray = 
                    ByteManipulation.getFingerprintAsByteArray(fingerprintLocalL, fingerprintBitSizeI);
            
            for (int i = fingerprintTempArray.length - 1; i > -1; i --) {
                byte b = fingerprintTempArray[i];
                
                // depending on the size of the fingerprint buffer, there might (probably) be an empty byte
                // at the head of the fingerprint array at this point. 
                if ((i == 0) && (b == 0)) { continue; }
                candidateAL.add(0, b);
            }
            
            LOGGER.trace("adding candidateAL: " + candidateAL);
            returnAL.add(candidateAL);
        }
       
        return returnAL;
    }
    
    
    
    
    /**
     * 
     * @param fingerprintL
     * @param numBitsI
     * @param fingerprintBitSizeI
     * @return
     */
    public List<List<Byte>> generateAllSetsThree(long fingerprintL, int numBitsI, int fingerprintBitSizeI) {
        
        List<List<Byte>> returnAL = new ArrayList<>();
        
        int bufferByteSizeI = (fingerprintBitSizeI % 8 == 0) ? 
                (fingerprintBitSizeI / 8) : ((fingerprintBitSizeI / 8) + 1);
                
        int bufferHeadSizeI = Math.abs((fingerprintBitSizeI % 8) - 8);
     
       
        List<List<Byte>> headsL = fingerprinter.getAllPossibleHeadsTwo(numBitsI, bufferHeadSizeI);
        
        byte fingerprintSizeInBits = fingerprinter.getDegree();
        byte fingerprintShift = (byte)fingerprinter.getShiftVal();
        
        long fingerprintTempL = fingerprintL;

byte[] tmpArr1 = null;
long pushTableValue = 0;        
int pushTableIndexSave = 0;       
        for (List<Byte> headL : headsL) {           
//LOGGER.info("headL is: " + headL);                              
            byte[] fingerprintTempArray = 
                    ByteManipulation.getFingerprintAsByteArray(fingerprintTempL, fingerprintSizeInBits);      
//LOGGER.info("fingerprint is: " + String.format("%X", fingerprintL));            
            

            // reversing to put the list in big endian order
            //Collections.reverse(headL);          
            List<Byte> candidateAL = new ArrayList<Byte>();

            // first push through the heads         
            //for (byte headByte : headL) {
            for (int i = 0; i < headL.size(); i ++) {
                byte headByte = headL.get(i);
                int pushTableIndexI = headByte & 0xFF;
pushTableIndexSave = pushTableIndexI;
//LOGGER.info("headbyte is: " + String.format("%X", headByte));
tmpArr1 = fingerprintTempArray;
pushTableValue = fingerprinter.getPushTable()[pushTableIndexI];
//LOGGER.info("(1)fingerprintTempArray is: " + ByteManipulation.getByteArrayAsHexString(fingerprintTempArray));
                byte[] xordFingerprintArray = 
                        fingerprinter.xorFingerprintAgainstPushTableElement(pushTableIndexI, fingerprintTempArray);
LOGGER.info("generated bytes is: " + ByteManipulation.getByteArrayAsHexString(generatedByteARR));             
//LOGGER.info("(*)xordFingerprintArray is: " + ByteManipulation.getByteArrayAsHexString(xordFingerprintArray)); 
                Pair<byte[], Byte> updatedFingerprintPair = fingerprinter.appendHeadByte(xordFingerprintArray, 
                                                                                         headByte, 
                                                                                         fingerprintSizeInBits, 
                                                                                         fingerprintShift);
                
                candidateAL.add(updatedFingerprintPair.getRight());
                fingerprintTempArray = updatedFingerprintPair.getLeft(); 
//LOGGER.info("(2)fingerprintTempArray is: " + ByteManipulation.getByteArrayAsHexString(fingerprintTempArray));
            }
            
            // now add the remaining elements in the fingerprint
//LOGGER.info("fingerprintTempArray is: " + ByteManipulation.getByteArrayAsHexString(fingerprintTempArray));
//LOGGER.info("candidateAL is:          " + candidateAL);

//            // this is necessary to preserve LIFO ordering
//            Collections.reverse(candidateAL);
//boolean printMe = false;
//if ((candidateAL.get(0) == generatedByteARR[8]) &
//        (candidateAL.get(1) == generatedByteARR[7]) & 
//            (candidateAL.get(2) == generatedByteARR[6])) { 
//       
//    LOGGER.info("candidateAL is: ");
//    for (byte b: candidateAL) {
//        System.out.print(String.format("%X", b) + " " );
//    }
//    LOGGER.info("** now reversing candidate list... ");
//    // this is necessary to preserve LIFO ordering
//    Collections.reverse(candidateAL);
//    LOGGER.info("fingerprint at start of loop was: " + ByteManipulation.getByteArrayAsHexString(tmpArr1));
//    LOGGER.info("pushTable value was: " + Long.toHexString(pushTableValue));
//    LOGGER.info("pushTable index was: " + Integer.toHexString(pushTableIndexSave));
//    printMe = true;
//}
            // start from 
            for (int i = fingerprintTempArray.length - 1; i > -1; i --) {
                byte b = fingerprintTempArray[i];
                
                // depending on the size of the fingerprint buffer, there might (probably) be an empty byte
                // at the head of the fingerprint array at this point. 
                if ((i == 0) && (b == 0)) { continue; }
                candidateAL.add(0, b);
            }
//if (printMe) {
//    LOGGER.info("fingerprintTempArray is: " + ByteManipulation.getByteArrayAsHexString(fingerprintTempArray));
//    LOGGER.info("candidateAL is: ");
//    for (byte b: candidateAL) {
//        System.out.print(String.format("%X", b) + " " );
//    }
//    System.out.println("");    
//    
//}
//LOGGER.info("candidateAL is:          " + candidateAL);
//LOGGER.info("**********");         
//            Collections.reverse(candidateAL);

            returnAL.add(candidateAL);
        }
        
        return returnAL;
    }
    
    
    
    
    /**
     * FIXME this too is shitty code
     * 
     * @param fingerprintL
     * @param numBitsI -- number of bits that have been pushed all the way through the fingerprint buffer
     * @return
     */
    public List<List<Byte>> generateAllSetsTwo( long fingerprintL, int numBitsI, int fingerprintBitSizeI) {
        List<List<Byte>> returnAL = new ArrayList<>();   
        
        int bufferByteSizeI = (fingerprintBitSizeI % 8 == 0) ? 
                (fingerprintBitSizeI / 8) : ((fingerprintBitSizeI / 8) + 1);
                
        int bufferHeadSizeI = Math.abs((fingerprintBitSizeI % 8) - 8);
        List<List<Byte>> headsL = fingerprinter.getAllPossibleHeadsTwo(numBitsI, bufferHeadSizeI);
        
        long fingerprintTempL = fingerprintL;
        for (List<Byte> headL : headsL) {
            List<Byte> candidateAL = new ArrayList<Byte>();
            
            // first push through the heads           
            for (byte headByte : headL) {
                int pushTableIndexI = headByte & 0xFF;
                long xorValueL = fingerprinter.getPushTable()[pushTableIndexI];
//LOGGER.info("headbyte is: " + String.format("%02X", headByte));
//LOGGER.info("fingerprint was   : " + String.format("%X", fingerprintTempL));               
                fingerprintTempL = fingerprintTempL ^ xorValueL;
//LOGGER.info("fingerprint is xor: " + String.format("%X", fingerprintTempL)); 
                

                

                // if we fall into this, either what we're feeding in is 00 or we did so previously
                int fingerprintBitCountI = Long.numberOfLeadingZeros(fingerprintTempL);
                if ((headByte == 0) || (fingerprintBitCountI > bufferByteSizeI - 1)) {               
                    // append the head the annoying way for now -- this should deal with the issue of 
                    // adding one or more 00's to the head of the fingerprint
                    String fingerprintBinaryS = Long.toBinaryString(fingerprintTempL);
                    while(fingerprintBinaryS.length() < (bufferByteSizeI * 8) - 1) {
                        fingerprintBinaryS = "0" + fingerprintBinaryS;
                    }
                    
                    String fingerprintTempHexS = 
                            ByteManipulation.appendByteToHeadString(
                                    headByte, fingerprintBinaryS, fingerprinter.getShiftVal(), fingerprintBitSizeI);
//LOGGER.info("fingerprint is now: " + fingerprintTempHexS.toUpperCase());                
                    fingerprintTempL = Long.parseLong(fingerprintTempHexS, 16); 
                }
                else {
                    // this doesn't work for heads of zero!! 
                    fingerprintTempL = ByteManipulation.appendByteToHead(
                                headByte, fingerprintTempL, fingerprinter.getShiftVal());                    
                }
    
                
                // you HAVE to remove the tail byte after you append the head
                byte tailByteB = ByteManipulation.getTailByte(fingerprintTempL);
                fingerprintTempL = fingerprintTempL >>> 8;
                
                
//LOGGER.info("tailByteB is: " + String.format("%02X", tailByteB));
//LOGGER.info("*******************");
                candidateAL.add(tailByteB);
            }
//LOGGER.info("answer candidate list size is: " + candidateAL.size());             
            // now get what remains in the buffer
            // this check --v is because we're, at this point, dealing with a fingerprint that might be 
            // in a state where the HO-Byte isn't representative of pushed data. this because if the 
            // fingerprint buffer size in bits isn't a factor of 8, and given that we've already pushed through
            // all the bytes that were pushed out the other end of the buffer, then the only data left in the
            // buffer is that which hasn't passed beyond the buffers edge. in other words, we can snip off
            // the HO-Byte because there's no data there/
            String fingerprintTempHexS = String.format("%X", fingerprintTempL);
            while(fingerprintTempHexS.length() < bufferByteSizeI * 2) {
                fingerprintTempHexS = '0' + fingerprintTempHexS;
            }
            if (fingerprintBitSizeI % 8 != 0) {
                fingerprintTempHexS = fingerprintTempHexS.substring(2);
            }
            
//LOGGER.info("fingerprint is now---> " + fingerprintTempHexS);             
            
            byte[] remainingBytes = fingerprinter.hexStringToByteArray(fingerprintTempHexS);
//LOGGER.info("remaining bytes arr is: " + ByteManipulation.getByteArrayAsHexString(remainingBytes));
            
            
            for (int i = 0; i < remainingBytes.length; i ++) { 
                candidateAL.add(remainingBytes[i]);
            }
//LOGGER.info("answer candidate list size is now: " + candidateAL.size());  
//if (candidateAL.size() != 8) { System.exit(8); }
//LOGGER.info("******");
            returnAL.add(candidateAL);
        }
  
        return returnAL;
    }    
    
    
    
    
    
    
    /**
     * 
     */
    public void retrieveBytes(List<Long> allXorPossibilitiesAL, long fingerprintL, byte[] byteARR, boolean checkInverse) {
        int matchCountI = 0;
        boolean matchFoundB = false;    
        int actualMatchIndexI = -1;
        int actualMatchCountI = 0;
        LOGGER.debug("fingerprintL was: " + String.format("%X", fingerprintL));
        
        //generateAllSets();

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
            
            if (checkInverse) { ArrayUtils.reverse(answerCandidateByteARR); }
            
            fingerprinter.pushBytes(answerCandidateByteARR);
            
            LOGGER.trace("current and original fingerprint are: "
                    + fingerprinter.getFingerprintLong() + " " + fingerprintL);
            
            if (fingerprinter.getFingerprintLong() == fingerprintL) {
                matchCountI++;

//              LOGGER.info("************************************************");
//              fingerprinter.reset();
//              fingerprinter.pushBytes(byteARR);
//              LOGGER.info(String.format("%X", fingerprinter.getFingerprintLong()) + " ");
//
//              fingerprinter.reset();
//              fingerprinter.pushBytes(answerCandidateByteARR);
//              LOGGER.info(String.format("%X", fingerprinter.getFingerprintLong()) + "\n");
//
//              LOGGER.info("original and answer candidate byte arrays are: \n"
//                      + ByteManipulation.getByteArrayAsHexString(byteARR) + " \n"
//                      + ByteManipulation.getByteArrayAsHexString(answerCandidateByteARR));
//              LOGGER.info("************************************************");

                if (!matchFoundB) {
                    boolean tempMatchB = true;

                    for (int k = 0; k < answerCandidateByteARR.length; k++) {
                        if (answerCandidateByteARR[k] != byteARR[k]) {
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
    
    
    
    /**
     * 
     */
    public void retrieveBytesTwo(
            List<List<Byte>> candidatesAL, long fingerprintL, byte[] byteARR, boolean checkInverse) {
        
        int matchCountI = 0;
        boolean matchFoundB = false;    
        int actualMatchIndexI = -1;
        int actualMatchCountI = 0;
        LOGGER.debug("fingerprintL was: " + String.format("%X", fingerprintL));
        
        for (int i = 0; i < candidatesAL.size(); i ++) {
            fingerprinter.reset();
            
            List<Byte> candidateAL = candidatesAL.get(i);
            byte[] answerCandidateByteARR = new byte[candidateAL.size()];
            for (int z = 0; z < candidateAL.size(); z ++) { answerCandidateByteARR[z] = candidateAL.get(z); }
            
            if (checkInverse) { ArrayUtils.reverse(answerCandidateByteARR); }
            
            // for 2/3 swap experiment --v
//            if (checkInverse) {
//                byte tempB = answerCandidateByteARR[2];
//                answerCandidateByteARR[2] = answerCandidateByteARR[1];
//                answerCandidateByteARR[1] = tempB;
//            }
            
            fingerprinter.pushBytes(answerCandidateByteARR);
        
            
            
      
//LOGGER.info("answer candidate is: " + ByteManipulation.getByteArrayAsHexString(answerCandidateByteARR));          
//LOGGER.info("original bytes is  : " + ByteManipulation.getByteArrayAsHexString(byteARR));    
//
//LOGGER.info("current fingerprint is : " + String.format("%X", fingerprinter.getFingerprintLong()));
//LOGGER.info("original fingerprint is: " + String.format("%X", fingerprintL));           
//LOGGER.info("************************************************");
            
            if (fingerprinter.getFingerprintLong() == fingerprintL) {
                matchCountI++;
//
//              LOGGER.info("************************************************");
//              fingerprinter.reset();
//              fingerprinter.pushBytes(byteARR);
//              LOGGER.info(String.format("%X", fingerprinter.getFingerprintLong()) + " ");
//
//              fingerprinter.reset();
//              fingerprinter.pushBytes(answerCandidateByteARR);
//              LOGGER.info(String.format("%X", fingerprinter.getFingerprintLong()) + "\n");
//
//              LOGGER.info("original and answer candidate byte arrays are: \n"
//                      + ByteManipulation.getByteArrayAsHexString(byteARR) + " \n"
//                      + ByteManipulation.getByteArrayAsHexString(answerCandidateByteARR));
//              LOGGER.info("************************************************");

                if (!matchFoundB) {
                    boolean tempMatchB = true;

                    for (int k = 0; k < answerCandidateByteARR.length; k++) {
                        if (answerCandidateByteARR[k] != byteARR[k]) {
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
    
    
    

    
    
    
    
    
}










