package start;

import reliabletransfer.ReliableClient;

public class StartClient {

    public static void main(String[] args) {
        String address = "localhost";
        int port = 9875; // --> Port at which we send packets (proxy port)
        int timeout = 1000;     // 1 second timeout

        ReliableClient client = new ReliableClient(address, port, timeout);
        client.SendFile();
    }
}
