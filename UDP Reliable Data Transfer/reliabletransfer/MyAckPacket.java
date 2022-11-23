package reliabletransfer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This class represents an ack packet of the TP6 reliable transfer protocol.
 */
public class MyAckPacket {
	private int seqNum;	// packet sequence number ack-ed by this ACK
    static public int MAX_SIZE = 4;
    
	/**
	 * Creates an ack packet with a given sequence number.
	 */
	public MyAckPacket(int seqNum) {
		this.seqNum = seqNum;
	}

	/**
	 * Creates an ack packet from a byte array representation (sent/received over the network).
	 */
	public MyAckPacket(byte[] content) {
		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(content));
		
		// Obtain the sequence number from the 4 first bytes
		try {
			seqNum = ds.readInt();
		} catch (IOException e) {
			seqNum = -1;
		}
	}

	/**
	 * Returns the sequence number of the ack packet.
	 */
	public int getSeqNum() {
		return seqNum;
	}

	/**
	 * Returns the byte array representation (sent/received over the network) of the ack packet.
	 */
	public byte[] toByteArray() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		DataOutputStream ds = new DataOutputStream(bs);
		
		try {
			// Send sequence number
			ds.writeInt(seqNum);
		} catch (IOException e) {
			// Should not happen
			e.printStackTrace();
		}
		
		return bs.toByteArray();
	}

	/**
	 * Get a string representation of the object.
	 */
	public String toString() {
		return "[Ack, SeqNum: " + seqNum + "]";
	}
}
