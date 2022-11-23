package reliabletransfer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This class represents represents a data packet of the TP6 reliable transfer protocol.
 */
public class MyDataPacket {
	static public int MAX_SIZE = 1024;

	private int seqNum;				// sequence number
	private byte[] data;			// byte array containing the data of the packet

	/**
	 * Creates a data packet with a given sequence number and data payload
	 */
	public MyDataPacket(int seqNum, byte[] data, int length) {
		this.seqNum = seqNum;
		this.data = new byte[length];
		System.arraycopy(data, 0, this.data, 0, length);
	}

	/**
	 * Creates a data packet from a byte array representation (sent/received over the network).
	 */
	public MyDataPacket(byte[] content, int length) throws IOException {
		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(content));
		
		// Get the sequence number contained in the 4 first bytes
		this.seqNum = ds.readInt();

		// Get the data
		this.data = new byte[length-4];
		ds.read(data, 0, length-4);
	}

	/**
	 * Returns the sequence number of the data packet.
	 */
	public int getSeqNum() {
		return seqNum;
	}

	/**
	 * Returns the payload of the data packet.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Returns the byte array representation (sent/received over the network) of the data packet.
	 */
	public byte[] toByteArray() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream(4 + data.length);
		DataOutputStream ds = new DataOutputStream(bs);
		try {
			// Send sequence number
			ds.writeInt(seqNum);

			// Send data
			ds.write(data, 0, data.length);

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
		return "[SeqNum:" + seqNum + ", Size:" + data.length + "]";
	}
}
