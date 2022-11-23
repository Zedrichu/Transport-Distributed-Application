package start;

import reliabletransfer.ReliableServer;

public class StartServer {
	public static void main(String[] args) {
        int port = 9876; // --> Port at which the server listens from proxy (server port)

        ReliableServer server = new ReliableServer(port);
        server.listen();
	}
}
