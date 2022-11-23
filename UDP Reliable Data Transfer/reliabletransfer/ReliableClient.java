package reliabletransfer;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class implements a sender of the TP6 reliable transfer protocol
 */
public class ReliableClient {

	private InetAddress IPAddress;
	private int port;
	private DatagramSocket socket;

    private Reader r;
	private int nextToAck;			// Next packet to be acked
	private int nextSeqNum;			// Sequence number of the next packet

	private int timeoutDuration;	// Time [ms] before the packet times out
    public static long lastTransmission;
    private FileInputStream fis;

	/**
	 * Creates a sender socket for communication with a receiver at a given IP address and port.
	 * @throws SocketException, IOException
	 */
	public ReliableClient(String address, int port, int timeout)   {
        try {
        	// Open the UDP socket.
            socket = new DatagramSocket();
			this.port = port;
			this.IPAddress = InetAddress.getByName(address);
			System.out.println("UDP Socket Opened ||>");

            // Set the timeout duration for the socket
			this.timeoutDuration = timeout;
			this.socket.setSoTimeout(this.timeoutDuration);

            // Initialize packet buffer and other state variables.
        	lastTransmission = 0;
			nextSeqNum = 0;
			nextToAck = 0;

            // Initialize the filestream we will be using to read data from
            this.fis = new FileInputStream("MonaLisa.txt");
			System.out.println("Stream initialized with file ... ");
        } catch (IOException e) {
			e.printStackTrace();
		}
	}

    // This is a utility function to help us read a random amount of data
    private static int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

    public void SendFile() {
        boolean successFlag = true;
        int length;

        try {
            // Read a random number of data
            int bufferSize = getRandomNumberInRange(10, 25);
            byte[] data = new byte[bufferSize];

            // Until we finish sending the file...
            while ((length = fis.read(data)) != -1) {
                // Create a UDP packet
				MyDataPacket udppack = GetNewPacket(data, length);

                do {
                    // Send it
					lastTransmission = System.currentTimeMillis();
                	SendPacket(udppack);

                    // And wait for an ACK
                	successFlag = WaitForAck();
                } while (!successFlag);
            }
        } catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
	 * Here we create the new packet. We also make sure to increment the sequence number
	 */
    private MyDataPacket GetNewPacket(byte[] data, int length) {
        MyDataPacket packet = new MyDataPacket(nextSeqNum, data, length);
		nextSeqNum++;

        return packet;
    }

	/**
	 * Encapsulates the given data packet into a UDP datagram and sends it over the network.
	 */
	private void SendPacket(MyDataPacket packet) {
		// Send a datagram with the data packet.
		byte[] content = packet.toByteArray();
		DatagramPacket datagramPacket = new DatagramPacket(content, content.length, this.IPAddress, this.port);
		
		try {
			// send UDP packet
			this.socket.send(datagramPacket);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Class responsible for handing incoming ack packets
     * Returns whether we received the ACK in time or not
	 */
    private boolean WaitForAck() {
        try {
            while (true) {
            	// We wait until we receive the right ACK
                DatagramPacket dp = new DatagramPacket(new byte[MyAckPacket.MAX_SIZE],
                                                       MyAckPacket.MAX_SIZE);
				this.socket.receive(dp);
                MyAckPacket ackPacket = new MyAckPacket(dp.getData());
				long currentTime = System.currentTimeMillis();

                // If this is the right ACK, we return "Success"
                if (ackPacket.getSeqNum() == nextToAck) {
					System.out.println("ACK packet received!");
					nextToAck++;
					return true;
				}

                // If an ACK wakes us up, check the time again
                if (currentTime - lastTransmission > timeoutDuration) {
					System.out.println("Timeout occurred :.:.:");
					return false;
				}
			}
        } catch (SocketTimeoutException e) {
            // If we timed out waiting for an ACK we return "Failure"
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
