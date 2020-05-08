
package app;

import java.io.IOException;
import java.net.SocketException;

import app.exceptions.UnknownPacketTypeException;
import app.packets.Packet;

/**
 * This class is used to listen to client requests and start a connection for file transfer when
 * a request is received
 * @author SYSC3303 Project Team 12
 */
public class Server extends Thread {

	private Socket socket;           // Datagram socket used to recieve client requests
	private String workingDirectory; // Directory used for file I/O
	
	/**
	 * Flag which says this server is in the process of shutting down.
	 */
	private boolean isShuttingDown;
	
	/**
	 * @throws SocketException - Thrown if an problem occurs while starting the server
	 */
	public Server() throws SocketException {
		Settings.verbose = false;
		socket = new Socket(Definitions.SERVER_LISTEN_PORT);	// Create socket to receive requests
	}
	
	// Setter and Getter methods
	public void setVerbose (boolean verbose) {
		Settings.verbose = verbose;
	}
	
	public boolean isVerbose () {
		return Settings.verbose;
	}
	
	public void setWorkingDirectory (String value) {
		this.workingDirectory = value;
	}
	
	public String getWorkingDirectory () {
		return workingDirectory;
	}
	
	@Override
	public void run () {
		int connectionNumber = 0;
		GUI.printMessage(Definitions.SERVER_START_MESSAGE);
		
		boolean printListening = true;
		
		// Take requests until we're told to shut down
		while (!isShuttingDown) {
			// Receive request packet
			try {
				Packet receivePacket = socket.receive(3000, printListening); // Only listen for 3 seconds at a time
				printListening = false; // Never print out that it's listening ever again
				// If the socket timed out, just restart listening
				if (receivePacket == null) {
					continue;
				}
				
				// Start a thread to handle a new connection with a client
				Connection connection = new Connection(receivePacket, workingDirectory);
				Thread connectionThread = new Thread(connection, "Server Connection" + (++connectionNumber));
				connectionThread.start();
				printListening = true; // Show that we're listening after we've started this thread
			} catch (IOException e) {
				GUI.printError(Definitions.RECEIVE_DATAGRAM_ERROR);
			} catch (UnknownPacketTypeException e) {
				GUI.printError(Definitions.WRONG_PACKET_TYPE_ERROR);
			}
		}
	}
	
	/**
	 * This method is used to terminate server execution, as it will throw an exception
	 * when the server is blocking to receive a packet
	 * @throws IOException
	 */
	public void close () {
		this.isShuttingDown = true;
		// Somehow close the socket here once the final open connection is finished
	}
	
	/**
	 * String representation of the Server class
	 */
	public String toString () {
		String serverInfo = new String("Server: \nDisplay Mode: ");
		if (Settings.verbose) {
			serverInfo += "verbose";
		} else {
			serverInfo += "quiet";
		}
		serverInfo += "\nI/O Directory: " + workingDirectory;
		
		return serverInfo;
	}
	
}
