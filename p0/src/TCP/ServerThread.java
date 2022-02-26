package TCP;

import java.net.*;
import java.io.*;

/**
 * Thread attending a connection of the TCP echo server.
 */
public class ServerThread extends Thread {

    Socket socket;

    public ServerThread(Socket s) {
        // Store the socket, so it can be used in the run() method
        this.socket = s;
    }

    public void run() {
        BufferedReader br = null;
        PrintWriter pw = null;
        try {
            // Set the input stream
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Set the output stream
            pw = new PrintWriter(socket.getOutputStream(), true);
            // Read message from client
            String message = br.readLine();
            System.out.println("SERVER: Received " + message);
            // Write message to client
            pw.println(message);
            System.out.println("SERVER: Sending " + message);

            // Close the streams
            br.close();
            pw.close();
        } catch (SocketTimeoutException e) {
            System.err.println("No message received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
// Close the socket
            try {
                // Cerramos el socket
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
