package lossychannel;
import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class LossyProxy {
    
    private int packetDelay = 200;
    private float lossRatio = 0.0f;
    
    volatile private DatagramSocket senderSocket;
    volatile private DatagramSocket receiverSocket;
    volatile private int receiverPort;
    volatile private InetAddress receiverHost;
    volatile private int senderPort;
    volatile private InetAddress senderHost;
    
    private LossyProxy mutex;
    
    private Random rnd;  
    private Timer tmr;
    
    private StartLossyChannel container;
    
    private SenderToReceiver StoR;
    private ReceiverToSender RtoS;
    
    public LossyProxy(StartLossyChannel container, int portSenderLocal, String hostReceiver, int portReceiver) throws SocketException, UnknownHostException {
        this.senderSocket = new DatagramSocket(portSenderLocal);
        this.receiverSocket = new DatagramSocket();
        this.senderPort = -1;
        this.senderHost = null;
        this.receiverHost = InetAddress.getByName(hostReceiver);
        this.receiverPort = portReceiver;
        
        this.rnd = new Random();  
        this.tmr = new Timer(true); 
        this.container = container;
        
        this.mutex = this;
        
        this.StoR = new SenderToReceiver();
        this.RtoS = new ReceiverToSender();
        
        (new Thread(StoR)).start();
        (new Thread(RtoS)).start();
    }


    public void changeReceiverHost(String hostReceiver) throws UnknownHostException {
        synchronized (mutex) {
            InetAddress hostO = InetAddress.getByName(hostReceiver);
            this.receiverHost = hostO;
        }
    }
    
    public void changeReceiverPort(int portReceiver) {
        synchronized (mutex) { 
            this.receiverPort = portReceiver;
        }
    }
    
    
    public void changeSender(int portSender) throws SocketException  {
        synchronized (mutex) {
            this.senderSocket.close();
            this.receiverSocket.close();
            this.senderSocket = new DatagramSocket(portSender);
            this.receiverSocket = new DatagramSocket();
            container.log("Sender port set to " + senderSocket.getLocalPort());
        }
    }
    
    
    private class DelayPacket extends TimerTask {
        DatagramPacket datagram;
        DatagramSocket socket;

        public DelayPacket(DatagramPacket dp, DatagramSocket ds) {
            this.datagram = dp;
            this.socket = ds;
        }

        public void run() {
            try {
                if (!socket.isClosed())
                    socket.send(datagram);
            } catch (IOException e) {
                e.printStackTrace();
            }           
        }
    }
    
    private class SenderToReceiver implements Runnable {
        public void run() {
            DatagramPacket dpOut;

            while (true) {
                try {
                	container.log("SenderToReceiver: Waiting for sender");
                	
                    DatagramPacket dpIn = new DatagramPacket(new byte[1024], 1024);
                    senderSocket.receive(dpIn);
                    
                    synchronized (mutex) {
                        container.log("Sender socket received datagram");
                        if (senderPort != dpIn.getPort() || !senderHost.equals(dpIn.getAddress())) {
                            container.log("New sender from port " + dpIn.getPort());
                            senderPort = dpIn.getPort();
                            senderHost = dpIn.getAddress();                     
                        }
                        if (rnd.nextFloat() > lossRatio) {
                            dpOut = new DatagramPacket(dpIn.getData(), dpIn.getLength(), receiverHost, receiverPort);
                            if (packetDelay > 0) {
                                int delay = packetDelay;
                                container.log("Sender socket (" + senderSocket.getLocalPort() + ") is delaying by " + delay);
                                tmr.schedule(new DelayPacket(dpOut, receiverSocket), delay);
                            }
                            else {
                                receiverSocket.send(dpOut);
                                container.log("Sender socket (" + senderSocket.getLocalPort() + ") is forwarding with no delay");
                            }
                        } else { //drop
                            container.log("Sender socket (" + senderSocket.getLocalPort() + ") is dropping");
                        }
                    }

                }
                catch (IOException e) {
                    try {
                        e.printStackTrace();
                        container.log(e.toString());
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                }
            }           
        }
    }
    
    private class ReceiverToSender implements Runnable {
        public void run() {
            DatagramPacket dpOut;

            while (true) {
                try {
                	container.log("ReceiverToSender: Waiting for receiver");
                	
                    DatagramPacket dpIn = new DatagramPacket(new byte[1024], 1024);
                    receiverSocket.receive(dpIn);

                    synchronized (mutex) {
                        container.log("Receiver socket received datagram");
                        
                        if (senderHost == null) {
                            container.log("Sender address unknown, receiver socket forced to drop.");
                        }
                        else if (rnd.nextFloat() > lossRatio) {
                            dpOut = new DatagramPacket(dpIn.getData(), dpIn.getLength(), senderHost, senderPort);
                            if (packetDelay > 0) {
                                int delay = packetDelay;
                                tmr.schedule(new DelayPacket(dpOut, senderSocket), delay);
                            }
                            else {
                                container.log("Receiver socket (" + receiverSocket.getLocalPort() + ") is forwarding with no delay");
                            }
                        } else { //drop
                            container.log("Receiver socket (" + receiverSocket.getLocalPort() + ") is dropping"); System.out.flush();
                        }
                    }

                }
                catch (IOException e) {
                    try {
                        e.printStackTrace();
                        container.log(e.toString());
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                    
                }
            }           
        }
    }

    public int getPacketDelay() {
        synchronized (mutex) {
            return packetDelay;
        }
    }

    public void setPacketDelay(int packetDelay) {
        synchronized (mutex) {
            this.packetDelay = packetDelay;
        }
    }

    public float getLossRatio() {
        synchronized (mutex) {
            return lossRatio;
        }
    }

    public void setLossRatio(float lossRatio) {
        synchronized (mutex) {
            this.lossRatio = lossRatio;
        }
    }   
 
}
