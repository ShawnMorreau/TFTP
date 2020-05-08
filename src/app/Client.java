package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import app.exceptions.DiskFullException;
import app.exceptions.UnknownPacketTypeException;
import app.packets.AcknowledgementPacket;
import app.packets.DataPacket;
import app.packets.Packet;
import app.packets.ReadRequestPacket;
import app.packets.WriteRequestPacket;
//save me
/**
 * This is the model for the client application.
 * 
 * @author SYSC3303 Project Team 12
 */
public class Client {

	/**
	 * The various sockets and packets that are required for this program.
	 */
	private Socket socket;
	
	/**
	 * The directory that the bulk of the operations will take place in.
	 */
	private String workingDirectory;

	/**
	 * Stores the IP/port of the first received packet
	 */
	private InetAddress receivedAddress = null;
	private int receivedPort = 0;
	
	/**
	 * Stores the IP of the host to connect to
	 */
	private InetAddress ipAddress = null;
	
	/**
	 * Returns a new instance of the Client application model.
	 * 
	 * @param testing Whether or not the program should be started in test mode
	 * @param verbose Whether or not the program should be started in verbose mode
	 * @constructor
	 */
	public Client(boolean testing, boolean verbose) {
		Settings.testing = testing;
		Settings.verbose = verbose;
		
		try {
			this.socket = new Socket();
		} catch (SocketException e) {
			System.exit(1);
		}
	}

	/**
	 * The main loop of the program in the context of sending read requests.
	 * 
	 * @param filename The relative path and name of the file to read from
	 */
	public void sendReadRequest(String filename) {
		// If we're trying to read a file we already have, just quit.
		if (new File(workingDirectory + File.separator + filename).exists()) {
			GUI.printMessage("We already have that file.");
			return;
		}
		
		try {
			// Determine the port and address to send to
			int port;
			if (Settings.testing) { port = Definitions.ERROR_SIMULATOR_LISTEN_PORT; receivedPort = Definitions.ERROR_SIMULATOR_LISTEN_PORT;}
			else { port = Definitions.SERVER_LISTEN_PORT;}
			// Create a new read request packet
			String mode = "netascii";
			ReadRequestPacket readRequestPacket = new ReadRequestPacket(filename, mode, port, this.ipAddress);
			
			// Send the read request packet and obtain a response
			DataPacket dataPacket = (DataPacket) socket.sendAndReceive(readRequestPacket, DataPacket.class);
			
			// If there was an error, exit the application
			if (dataPacket == null) {
				GUI.printError(Definitions.RECEIVE_DATAGRAM_ERROR);
				return;
			}
			// Make sure it's the block number we were expecting
			if (dataPacket.getBlock() > 1) {
				// Reply with an error 4 and then just terminate this connection
				GUI.printError(Definitions.DATA_PACKET_OUT_OF_ORDER);
				socket.sendError(4, Definitions.DATA_PACKET_OUT_OF_ORDER, port, this.ipAddress);
				return;
			}
			int expectedBlock = dataPacket.getBlock() + 1;
			
			// first packet received
			receivedAddress = dataPacket.getAddress();
			receivedPort = dataPacket.getPort();
			if (! Settings.testing) { port = dataPacket.getPort(); }
			if(Settings.verbose) System.out.println("Setting first received address to " + receivedAddress + " and Port to " + receivedPort);
			// The data from the data packet must be written to disk
			byte[] data = dataPacket.getData();
			writeDataToDisk(data, mode, filename);
			
			while (true) {
				// Create an acknowledgement packet to reply with
				AcknowledgementPacket ackPacket = new AcknowledgementPacket(dataPacket.getBlock(), port, receivedAddress);
				
				// If we received less than 512 bytes of data, that's the last one and we can stop
				if (data.length < 512) {
					// Send an ack without expecting anything back
					socket.send(ackPacket);
					return;
				} else {
					do {
						// Send an ack but expect a data in return
						dataPacket = (DataPacket) socket.sendAndReceive(ackPacket, DataPacket.class);
					} while (isRightAddressPort(dataPacket));
					// Make sure we received the data for the right block number
					if (dataPacket.getBlock() > expectedBlock) {
						// Reply with an error 4 and then just terminate this connection
						GUI.printError(Definitions.DATA_PACKET_OUT_OF_ORDER);
						socket.sendError(4, Definitions.DATA_PACKET_OUT_OF_ORDER, port, this.ipAddress);
						return;
					}

					// Get the needed information from the packet
					if (dataPacket.getBlock() == expectedBlock) {
						expectedBlock += 1;
						data = dataPacket.getData();
						writeDataToDisk(data, mode, filename);
					}
				}
			}
		}
		catch (IOException e) { System.exit(1); }
		catch (UnknownPacketTypeException e) { System.exit(1); }
	}

	private void writeDataToDisk(byte[] data, String mode, String filename) {
		try {
			FileParser.writeFileBytes(mode, data, workingDirectory + File.separator + filename);
		} catch(DiskFullException dfe){	// Handle Disk Full or Exceeded Allocation error
			this.socket.sendError(3, Definitions.DISK_FULL_ERROR, receivedPort, receivedAddress);
			GUI.printError(Definitions.DISK_FULL_ERROR);
			return;
		} catch (SecurityException se) {
			System.out.println("immma lil bitch that's being thrown");
			GUI.printError(Definitions.ACCESS_VIOLATION_ERROR);
			return;
		} catch (FileNotFoundException e) {
			GUI.printError(Definitions.FILE_NOT_FOUND_ERROR);
			return;
		} catch (IOException e) {
			GUI.printError(Definitions.IO_ERROR);
				return;
		}

	}
	
	/**
	 * The main loop of the program in the context of sending write requests.
     * 
	 * @param filename The relative path and name of the file to write to
	 */
	public void sendWriteRequest(String filename) {
		int port = 0;
		try {
			// Determine the port and address to send to
			AcknowledgementPacket ackPacket = null;
			if (Settings.testing) { port = Definitions.ERROR_SIMULATOR_LISTEN_PORT; }
			else { port = Definitions.SERVER_LISTEN_PORT; }

			// Create a new write request packet
			String mode = "netascii";
			WriteRequestPacket writeRequestPacket = new WriteRequestPacket(filename, mode, port, ipAddress);
			do{
				// Send the packet and receive an acknowledgement
				ackPacket = (AcknowledgementPacket) socket.sendAndReceive(writeRequestPacket, AcknowledgementPacket.class);
				if (ackPacket == null) {
					GUI.printError(Definitions.RECEIVE_DATAGRAM_ERROR);
					return;
				}
				receivedPort = ackPacket.getPort();
				receivedAddress = ackPacket.getAddress();
				if (ackPacket.getBlock() > 0) {
					// Reply with an error 4 and then just terminate this connection
					GUI.printError(Definitions.ACK_PACKET_OUT_OF_ORDER);
					socket.sendError(4, Definitions.ACK_PACKET_OUT_OF_ORDER, port, ipAddress);
					return;
				}
			} while(isRightAddressPort(ackPacket));
//			if (ackPacket instanceof ErrorPacket) {
//				if (verbose) {
//					GUI.printPacketInfo(ackPacket, true);
//				}
//				return;
//			} else if (! (ackPacket instanceof AcknowledgementPacket)) {
//				return;
//			}
			
			// Keep track of the block we're on
			int blockCounter = 1;

			// Read the file from the disk
			byte[][] fileData = FileParser.readFileBytes(mode, workingDirectory + File.separator + filename);
			
			while (blockCounter <= fileData.length) {
				// Get the proper port and address of the connection from the data we received
				if (Settings.testing) { port = Definitions.ERROR_SIMULATOR_LISTEN_PORT; }
				else { port = ackPacket.getPort(); }
				InetAddress address = ackPacket.getAddress();
				
				// Create the data packet to send
				DataPacket dataPacket = new DataPacket(blockCounter, fileData[blockCounter - 1], port, address);
				
				// Send a block of data and expect an acknowledgement in return
				do {
					ackPacket = (AcknowledgementPacket) socket.sendAndReceive(dataPacket, AcknowledgementPacket.class);
					if (ackPacket.getBlock() > blockCounter) {
						// Reply with an error 4 and then just terminate this connection
						GUI.printError(Definitions.ACK_PACKET_OUT_OF_ORDER);
						socket.sendError(4, Definitions.ACK_PACKET_OUT_OF_ORDER, port, address);
						return;
					}
				} while (ackPacket.getBlock() != blockCounter);
				
//				if(received instanceof ErrorPacket){
//					if(verbose) {
//						GUI.printMessage("Error Packet received: " + ((ErrorPacket) received).getErrorMessage());	// Print message
//					}
//					sendReceiveSocket.close();
//					System.exit(1);
//				}
				
				// Get ready to send the next packet
				++blockCounter;
			};
		}
		catch (FileNotFoundException e) {socket.sendError(1, Definitions.FILE_NOT_FOUND_ERROR, port, ipAddress);}
		catch (SecurityException e) {System.out.println("bruh");}
		catch (IOException e) { System.exit(1); }
		catch (UnknownPacketTypeException e) { System.exit(1); }
	}
	
	/**
	 * @return Whether the program is in testing mode or not
	 */
	public boolean isTesting() {
		return Settings.testing;
	}
	
	/**
	 * @return Whether the program is in verbose mode or not
	 */
	public boolean isVerbose() {
		return Settings.verbose;
	}
	
	/**
	 * Sets the test mode of the program.
	 * 
	 * @param value The value to set the test mode to
	 */
	public void setTesting(boolean value) {
		Settings.testing = value;
	}
	
	/**
	 * Sets the verbose mode of the program.
	 * 
	 * @param value The value to set the verbose mode to
	 */
	public void setVerbose(boolean value) {
		Settings.verbose = value;
	}
	
	/**
	 * Sets the working directory of the program.
	 * 
	 * @param value The value to set the working directory to.
	 */
	public void setWorkingDirectory(String value) {
		this.workingDirectory = value;
	}
	
	/**
	 * Closes the program's sockets cleanly so it can be exited.
	 */
	public void close() {
		this.socket.close();
	}
	public boolean isRightAddressPort(Packet packet) throws IOException{
		try{
			if(packet.getAddress().equals(receivedAddress) && packet.getPort() == receivedPort){
			return false;
		}
		else{
			socket.ignoreAndCarryOnWithYourLife(packet);
			return true;
		}
		}catch(NullPointerException e){};
		return true;
	}

	public void setServerIP(InetAddress ip) {
		this.ipAddress = ip;
	}
}