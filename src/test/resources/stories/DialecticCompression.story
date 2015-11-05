Narrative:
In order to evaluate the assumptions made by v2 of the smoosh algorithm
As a guy who wants to see if this will actually work
I want to see whether or not it's possible to derive the eighth and fifteenth 
bytes of a sixteen byte chain given the first seven, the fingerprint of all 
sixteen, and the byte that was pooped out the front when the final byte was 
shoved in the back.
		
		
		
Scenario: get byte 16 & fingerprint representing bytes 1-15
Given an array of 16 bytes
When the array is fingerprinted
And the first seven bytes and the head fingerprint byte (fingerprinted byte 
nine) is retained after byte 16 is pushed
Then the retained fingerprint byte can be used to retrieve byte 16 and the 
fingerprint representing bytes 1-15.


Scenario: un-xor bytes 9-14 using given bytes 1-7
Given an array of 16 bytes
When the array is fingerprinted
And the first seven bytes and the head fingerprint byte (fingerprinted byte 
nine) is retained after byte 16 is pushed
Then the given bytes 1-7 can be used to undo all the xors against bytes 9-14
except for the one that is indexed by yet unknown byte 8


Scenario: compute what the values for bytes 9-15 must be given all possible 
values for byte 8 
Given an array of 16 bytes
When the array is fingerprinted
And the first seven bytes and the head fingerprint byte (fingerprinted byte 
nine) is retained after byte 16 is pushed
And every possible value for byte 8 is used to enumerate through the xor table 
to compute all 256 possible values for bytes 8-15
Then all 256 possible values for bytes 8-15 can be inserted between known 
byte(s) 1-7 and 16


Scenario: recreate original 16 byte chain from candidate list
Given an array of 16 bytes
When the array is fingerprinted
And the first seven bytes and the head fingerprint byte (fingerprinted byte 
nine) is retained after byte 16 is pushed
And all 256 possible values for bytes 8-15 have been computed and fingerprinted
Then the original 16 bytes is among the set of results
And the size of the set of results is less than 17


