package reliabletransfer;

import java.io.*;
import java.net.*;

/**
 * This class implements a receiver of the TP6 reliable transfer protocol
 */
public class ReliableServer {

	private DatagramSocket socket;
	private int nextPacketToDeliver;

	public ReliableServer(int port) {
		try {
			// Open the UDP socket.
			this.socket = new DatagramSocket(port);
			System.out.println("UDP Socket Opened <||");
			this.nextPacketToDeliver = 0;
		} catch (SocketException e) {
			e.printStackTrace();
		}


    }
    public void listen() {
		try {
			while (true) {
                // Allocate enough space for the biggest possible packet
				DatagramPacket datagramPacket = new DatagramPacket(new byte[MyDataPacket.MAX_SIZE], MyDataPacket.MAX_SIZE);

                // Receive the new packet
				this.socket.receive(datagramPacket);
				MyDataPacket udpPacket = new MyDataPacket(datagramPacket.getData(),
						datagramPacket.getLength());

				// If we expect this packet, send data to upper layer.
                // In this case, we simply print the bytes
				if (udpPacket.getSeqNum() == nextPacketToDeliver) {
					System.out.println(new String(udpPacket.getData()));
					System.out.flush();
					nextPacketToDeliver++;
				}

				// ACK packet if it is the one we have been expecting.
                // Otherwise, do nothing (we drop the packet silently)
				if (udpPacket.getSeqNum() < nextPacketToDeliver) {
					MyAckPacket ackPacket = new MyAckPacket(udpPacket.getSeqNum());
					byte[] ackBuffer = ackPacket.toByteArray();
					DatagramPacket ackDatagram = new DatagramPacket(ackBuffer, ackBuffer.length,
							datagramPacket.getAddress(), datagramPacket.getPort());
					// Send acknowledgement back to client
					this.socket.send(ackDatagram);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
