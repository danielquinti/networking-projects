package TCP;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a TCP echo server.
 */
public class TCPServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: ServerTCP <port number>");
            System.exit(-1);
        }
        ServerSocket sServer = null;
        try {
            // Create the server socket
            sServer = new ServerSocket((int) Integer.parseInt(argv[0]));

// Set socket timeout to 300 secs
            sServer.setSoTimeout(300000);
            while (true) {
                // Wait for connections                
                Socket sClient = sServer.accept();

                // Create a ServerThread object, passing the new socket as parameter
                ServerThread ts = new ServerThread(sClient);
                // Start the thread execution by calling the start() method
                ts.start();
            }
        } catch (SocketTimeoutException e) {
            System.err.println("No connection requests in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
//Closet he server socket
            try {
                sServer.close();
            } catch (IOException ex) {
                Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
