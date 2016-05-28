
package com.holitek.smoosh.bootstrap;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.holitek.smoosh.util.BlockUtils;
import com.holitek.smoosh.util.ResourceFileUtil;


public class Bootstrap {
	
	public static Logger LOGGER = 
			LoggerFactory.getLogger(Bootstrap.class);
	
	public static void main(String[] args) {
		
		BlockUtils blockUtils = new BlockUtils();
		

		File file = new File("1mbRandom");
		FileInputStream fileInputStream = null;
		FileOutputStream fileOutputStream = null;	
		byte[] readBufferARR = new byte[BlockUtils.BLOCK_SIZE_IN_BYTES];
		
		try {
			fileInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			fileOutputStream = new FileOutputStream("1mbRandom.smoosh");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		BufferedInputStream bufferedInputStream = new BufferedInputStream(
				fileInputStream, BlockUtils.BLOCK_SIZE_IN_BYTES);
				
		try {
			
			while (bufferedInputStream.available() > 0) {
				int readBytesI = bufferedInputStream.read(readBufferARR);
							
				if (readBytesI == BlockUtils.BLOCK_SIZE_IN_BYTES) {
	
// quick test to see what how well the output of biginteger.tobytearray matches input
//					byte[] smooshedBlockARR = new BigInteger(readBufferARR).toByteArray();
					
					byte[] smooshedBlockARR = 
							blockUtils.smooshBlock(readBufferARR);
					
					fileOutputStream.write(smooshedBlockARR);
				}
				else {
					fileOutputStream.write(readBufferARR);
				}
				
			}
				
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			fileInputStream.close();
			bufferedInputStream.close();
			fileOutputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
for (int i = 0; i < blockUtils.numBitsFlippedCountARR.length; i ++) {
	LOGGER.info("count for: " + i + " ones is: " + blockUtils.numBitsFlippedCountARR[i]);
}

//LOGGER.info("*** ambiguous HO bit flips count is: " + blockUtils.ambiguousHO_BitFlipI);		

	


//// 
//// try to unsmoosh
////	
//
//try {
//	fileInputStream = new FileInputStream(new File("randy200kish.smoosh"));
//} catch (FileNotFoundException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//
//
//try {
//	fileOutputStream = new FileOutputStream("randy200kish.smoosh.UnSmooshed");
//} catch (FileNotFoundException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//} 
//
//
//bufferedInputStream = new BufferedInputStream(
//		fileInputStream, BlockUtils.BLOCK_SIZE_IN_BYTES);
//
//try {
//	
//	while (bufferedInputStream.available() > 0) {
//		int readBytesI = bufferedInputStream.read(readBufferARR);
//		
//		
//		if (readBytesI == BlockUtils.BLOCK_SIZE_IN_BYTES) {
//			
//			byte[] unSmooshedBlockARR = 
//					blockUtils.unsmooshBlock(readBufferARR);
//	
//			fileOutputStream.write(unSmooshedBlockARR);
//		}
//		else {
//			fileOutputStream.write(readBufferARR);
//		}
//		
//	}
//		
//	
//} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//
//try {
//	fileInputStream.close();
//	bufferedInputStream.close();
//	fileOutputStream.close();
//} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//
//
//
//



















		
	}
	
}
