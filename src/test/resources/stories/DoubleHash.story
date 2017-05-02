

Scenario: check new xor fingerprint method
Given an array of 10 bytes
When the array is fingerprinted
Then the fingerprint xord in both ways will yield the same result


Scenario: check head list resolution
Given an array of 9 bytes
When the array is fingerprinted with heads saved
And all the possible heads are computed from the fingerprint
Then one of the correct set of heads is in the computed head list


Scenario: check reverse fingerprint with head list
Given an array of 9 bytes
When the array is fingerprinted with heads saved
Then the fingerprint heads are pushed back through the fingerprint


					 
Scenario: test to see if hash collisions are also collions for the same data, inverted
Given an array of 15 bytes
When both the array and its inverse are fingerprinted
And all collisions are calculated for both fingerprints
Then there will be only one member of both sets of results that resolve to both hashes


