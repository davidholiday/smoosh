
package com.holitek.smoosh.bootstrap;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.holitek.smoosh.util.ResourceFileUtil;

public class Bootstrap {
	
	public static Logger LOGGER = 
			LoggerFactory.getLogger(Bootstrap.class);
	
	public static void main(String[] args) {
		
		ResourceFileUtil.buildResourceFiles();
		
		List<Integer> elementListOne = 
				ResourceFileUtil.deserializeElementList(2);
		
		LOGGER.info("deserialized list one size is: " + elementListOne.size());
		LOGGER.info("list contents are: " );
		
		for (int i : elementListOne) {
			
			String elementAsBinaryS = 
				String.format("%8s", Integer.toBinaryString(i))
				      .replace(' ', '0');
					
			LOGGER.info(i + " " + elementAsBinaryS);
		}
		
		
	}
	
}
