
Narrative:
In order to evaluate the assumptions made by the smoosh algorithm
As a guy that wants to see if this actually will work
I want to determine whether or not it's possible to unroll a rabin fingerprint
				


Scenario: Given a proposed pushTable index value, ensure that the result of the
XOR operation between it and a fingerprint contains the index value in the first
n bits.
Given an array of 8 bytes
When the byte array is fingerprinted
And the fingerprinted array is xor'd against every entry in the push table
Then the correct eight bits is appended to the head of the fingerprint such that
the first [n] bits in the result are equal to the push table index used to 
generate the result.


Scenario: Create a data structure containing all possible solutions
Given an array of 8 bytes
When the byte array is fingerprinted
Then a data structure is created containing all possible sets of input.



Scenario: Fingerprint eight bytes then retrive the original eight bytes
Given an array of 8 bytes
When the byte array is fingerprinted
Then the original 8 bytes is retrieved from the fingerprint.