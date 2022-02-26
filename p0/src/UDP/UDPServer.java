package UDP;

import java.net.*;

/**
 * Implements an UDP Echo Server.
 */
public class UDPServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: ServerUDP <port_number>");
            System.exit(-1);
        }
        DatagramSocket serverSocket = null;
        try {
            // Create a server socket
            serverSocket = new DatagramSocket((int) Integer.parseInt(argv[0]));

            // Set max. timeout to 300 secs
            serverSocket.setSoTimeout(300000);
// Prepare datagram for reception
            while (true) {
                byte[] receivedData = new byte[1024];
                DatagramPacket receivedDatagramPacket = new DatagramPacket(receivedData,
                        receivedData.length);
// Receive the message
                serverSocket.receive(receivedDatagramPacket);
                System.out.println("SERVIDOR: Received "
                        + new String(receivedDatagramPacket.getData(), 0, receivedDatagramPacket.getLength())
                        + " de " + receivedDatagramPacket.getAddress().toString() + ":"
                        + receivedDatagramPacket.getPort());
                // Prepare datagram to send response
                DatagramPacket sentDatagramPacket = new DatagramPacket(receivedDatagramPacket.getData(),
                        receivedDatagramPacket.getLength(), receivedDatagramPacket.getAddress(), receivedDatagramPacket.getPort());
                // Send response
                serverSocket.send(sentDatagramPacket);
                System.out.println("SERVER: Sending "
                        + new String(sentDatagramPacket.getData()) + " a "
                        + sentDatagramPacket.getAddress().toString() + ":"
                        + sentDatagramPacket.getPort());
            }

        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
// Close socket to release connection
                serverSocket.close();
            }
    }
}
