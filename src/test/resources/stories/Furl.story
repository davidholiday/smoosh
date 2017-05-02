
					 
Scenario: Check to ensure you can set the low order byte of the push table to match the element index
Given a push table updated for furl
Then the low order byte of each element matches that element's index value


Scenario: Check to ensure you can recover the correct head bit list 
Given an array of 16 bytes
And a push table updated for furl
When the byte array is fingerprinted with heads saved
Then you can recover the correct head list