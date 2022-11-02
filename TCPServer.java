import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.*;
import java.io.*;

public class TCPServer {
    private static Map<String, Integer> getOccurences(String message) {
        Map<String, Integer> occurences = new TreeMap<String, Integer>();
        String delimiter_regexp = "[^a-zA-Z]+";
        Scanner fileScan = new Scanner(message).useDelimiter(delimiter_regexp);
        
        while (fileScan.hasNext()){
            String word = fileScan.next();
            word = word.toLowerCase();
            
            Integer oldCount = occurences.get(word);
            if (oldCount == null) {
                oldCount = 0;
            }
            occurences.put(word, oldCount + 1);
        }
        fileScan.close();
        return occurences;
    }

    public static void handleConnection(Socket connectionSocket){
        DataInputStream inFromClient = null;
        DataOutputStream outToClient = null;

        try {
            connectionSocket.setSoTimeout(50000);

            // Open the input-output streams
            inFromClient = new DataInputStream(connectionSocket.getInputStream());
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            // Controls when loop should terminate
            boolean repeatFlag = true;

            do {
                // Read a new file HEADER
                String head = new String(inFromClient.readNBytes(8));
                if (!head.equals("NEWFILE|")) continue;

                // Read the length of the file
                int length = inFromClient.readInt();
                System.out.println("The file has length: "+length+" bytes");

                if (length == 0) {
                    // Terminate connection
                    repeatFlag = false;
                } else {
                    // Read the file contents into message
                    byte[] bytearray = inFromClient.readNBytes(length);
                    String message = new String(bytearray);
                    System.out.println(message);

                    // Call the response handler
                    send_response(outToClient, message);
                }
            } while (repeatFlag);

        } catch (IOException ioex) {
            System.out.println("Failed to handle connection: "+ioex.getMessage());
        } finally {
            try {
                inFromClient.close();
                outToClient.close();
                connectionSocket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Nothing to handle!");
    }

    private static void send_response(DataOutputStream outToClient, String message) throws IOException {
        // Perform word-occurrence stats
        Map<String, Integer> occurrences = getOccurences(message);

        // Send the number of words to be sent
        int num_values = occurrences.size();
        outToClient.writeInt(num_values);

        for (String key: occurrences.keySet()) {
            String word = key.toString();
            int times = occurrences.get(key);

            // Send the length of the word first
            outToClient.writeInt(word.length());

            // Then, send the actual word
            outToClient.writeBytes(word);

            // Finally, send the number of times the word appears
            outToClient.writeInt(times);

            // Break when already sent 5 words
            if (num_values-- == 0) break;
        }
    }

    public static void main(String args[]) {
        ServerSocket welcomeSocket = null;
        Socket connectionSocket = null;

        try {
            // Create a socket listening to port 6789
            welcomeSocket = new ServerSocket(6789);

            while (true) {
                // Get a new connection
                connectionSocket = welcomeSocket.accept();
                System.out.println("Connected to "+connectionSocket.getRemoteSocketAddress());

                // Start a new thread for the accepted connection
                RequestHandler handler = new RequestHandler(connectionSocket);
                handler.run();
                if (handler.done) break;
            }

        } catch (IOException exp) {
            System.out.println("Failed to open welcomeSocket: "+exp.getMessage());
        } finally {
            try {
                welcomeSocket.close();
                connectionSocket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}

class RequestHandler implements Runnable {
    Socket socket;
    boolean done;

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        TCPServer.handleConnection(socket);
        done = true;
    }
}

