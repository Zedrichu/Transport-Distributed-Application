# STOP-and-WAIT and Selective Repeat protocol

# Lossy Channel
To test the protocol with the provided proxy, remember to change the
sender port in *StartClient.java* to match with the sender port of the proxy
(i.e. 9875).
# Maximum possible timeout duration
Assuming set value `timeoutDuration` to 10 seconds. Then, according to the
implementation shown, the maximum possible wait between transmitting
a packet and timing out is 20 seconds. This can only occur when:
    
1. Transmitted a packet at time `0`.
2. Received an unrelated (old) ACK at time `10`, just before the timeout.
3. Received no other ACK until the socket times out naturally `10` seconds afterwards at time `20`.