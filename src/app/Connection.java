package app;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.SocketException;

import app.exceptions.DiskFullException;
import app.exceptions.UnknownPacketTypeException;
import app.packets.AcknowledgementPacket;
import app.packets.DataPacket;
import app.packets.Packet;
import app.packets.ReadRequestPacket;
import app.packets.WriteRequestPacket;

/**
 * This class is used to handle file transfer operations for the server class.
 * 
 * @author SYSC3303 Project Team 12
 */
public class Connection implements Runnable {

	/**
	 * The request packet that spawned this connection.
	 */
	private Packet request;

	/**
	 * Socket used to transfer packets.
	 */
	private Socket socket;

	/**
	 * Directory used for file I/O.
	 */
	private String workingDirectory;

	/**
	 * Stores the IP/port of the first received packet
	 */
	private InetAddress receivedAddress = null;
	private int receivedPort = 0;

	/**
	 * @param request - client request packet	
	 * @param workingDirectory - directory used for file I/O
	 */
	public Connection (Packet request, String workingDirectory) {
		this.request = request;
		this.workingDirectory = workingDirectory;
		try {
			socket = new Socket();
			receivedAddress = request.getAddress();
			receivedPort = request.getPort();
		} catch (SocketException e) {
			GUI.printError(Definitions.CREATE_SOCKET_ERROR);
			System.exit(1);
		}
		// set a last action time
		// lastActionTime = currentTime();
	}

	/**
	 * This method is used to handle the flow of operations to send a file to the
	 * client
	 * @param parsedRequest - Object with the data received in the client request
	 * @throws IOException 
	 */
	private void handleReadRequest (ReadRequestPacket parsedRequest) throws IOException {
		byte[][] fileData = null;

		// Get information from the request
		int port = parsedRequest.getPort();
		InetAddress address = parsedRequest.getAddress();
		String mode = parsedRequest.getMode();
		String filename = parsedRequest.getFilename();

		// Retrieve file bytes from specified working directory
		try {
			fileData = FileParser.readFileBytes(mode, workingDirectory + File.separator + filename);
		} catch (FileNotFoundException fe) {
			GUI.printError(Definitions.FILE_NOT_FOUND_ERROR);
			socket.sendError(1, Definitions.FILE_NOT_FOUND_ERROR, port, address);
			return;
		} catch (IOException e) {
			GUI.printError(Definitions.UNKNOWN_ERROR);
			socket.close();
			return;
		} catch (SecurityException e) {
			GUI.printError(Definitions.ACCESS_VIOLATION_ERROR);
			socket.sendError(2, Definitions.ACCESS_VIOLATION_ERROR, port, address);
			return;
		}

		// Loop until the file transfer operation is complete
		for (int i = 0; i < fileData.length; i++) {
			// Prepare data packet for transfer
			DataPacket dataPacket = new DataPacket((i + 1), fileData[i], port, address);

			// Send data packet and receive acknowledgement
			try {
				AcknowledgementPacket ackPacket;
				// keep re-sending the data until the port is correct.
				// Keep re-sending the data packet until we get the right ACK
				// packet back
				do {
					ackPacket = (AcknowledgementPacket) socket.sendAndReceive(dataPacket, AcknowledgementPacket.class);
					if (ackPacket == null) {
						return;
					}
					if (ackPacket.getBlock() > i+1) {
						// Reply with an error 4 and then just terminate this connection
						GUI.printError(Definitions.ACK_PACKET_OUT_OF_ORDER);
						socket.sendError(4, Definitions.ACK_PACKET_OUT_OF_ORDER, port, address);
						return;
					}
				} while (!(ackPacket.getBlock() < i-1) && ackPacket.getBlock() != (i + 1) && !isRightAddressPort(ackPacket));

			} catch (UnknownPacketTypeException e) {
				GUI.printError(Definitions.WRONG_PACKET_TYPE_ERROR);
			} catch (IOException e) {
				GUI.printError(Definitions.UNKNOWN_ERROR);
			}
			// } else if (acknowledgementPacket instanceof ErrorPacket) { //
			// Check if packet received is an error packet
			// GUI.printMessage("Error Packet received: " + ((ErrorPacket)
			// acknowledgementPacket).getErrorMessage()); // Print error message
			// sendReceiveSocket.close();
			// System.exit(1);
			// } else {
			// GUI.printError(Definitions.WRONG_PACKET_TYPE_ERROR);
			// sendReceiveSocket.close();
			// System.exit(1);
			// }
		}
		GUI.printMessage(Definitions.TRANSFER_FINISHED);
		socket.close(); // Close socket after file transfer is completed
	}

	/**
	 * This method is used to handle the flow of operations to receive a file from the
	 * client
	 * @param parsedRequest - Object with the data received in the client request
	 */
	private void handleWriteRequest(WriteRequestPacket parsedRequest) {
		// Get some information from the request
		int port = parsedRequest.getPort();
		InetAddress address = parsedRequest.getAddress();
		String mode = parsedRequest.getMode();
		String filename = parsedRequest.getFilename();

		// Verify that a file with that name doesn't already exist
		if (new File(workingDirectory + File.separator + parsedRequest.getFilename()).exists()) {
			// Send an error packet back and return from this mess
			socket.sendError(6, Definitions.FILE_ALREADY_EXISTS, port, address);
		}

		// Prepare acknowledgement packet with block number 0 to tell the client
		// the write request was accepted
		AcknowledgementPacket ackPacket = new AcknowledgementPacket(0, port, address);

		int expectedBlock;
		
		// Send connection acknowledgment packet
		try {
			DataPacket dataPacket;
			do {
				dataPacket = (DataPacket) socket.sendAndReceive(ackPacket, DataPacket.class);
				if (dataPacket == null) {
					return;
				}
				if (dataPacket.getBlock() > 1) {
					// Reply with an error 4 and then just terminate this connection
					GUI.printError(Definitions.DATA_PACKET_OUT_OF_ORDER);
					socket.sendError(4, Definitions.DATA_PACKET_OUT_OF_ORDER, port, address);
					return;
				}
				
				expectedBlock = dataPacket.getBlock() + 1;
				
			} while (isRightAddressPort(dataPacket));
			// Get the data and write it to the disk
			byte[] data = dataPacket.getData();
			writeDataToDisk(data, mode, filename);

			// Loop until all file data has been received
			while (true) {
				// Get the proper port and address of the client from the data
				// we received
				port = dataPacket.getPort();
				address = dataPacket.getAddress();

				// Create an acknowledgement packet to reply with
				ackPacket = new AcknowledgementPacket(dataPacket.getBlock(), port, address);
				
				// If we received less than 512 bytes of data, that's the last
				// one and we can stop
				if (data.length < 512) {
					// Send an ack without expecting anything back
					socket.send(ackPacket);
					break;
				} else {
					// Send an ack but expect a data in return
					dataPacket = (DataPacket) socket.sendAndReceive(ackPacket, DataPacket.class);
					if (dataPacket == null) {
						return;
					}
					if (dataPacket.getBlock() > expectedBlock) {
						// Reply with an error 4 and then just terminate this connection
						GUI.printError(Definitions.DATA_PACKET_OUT_OF_ORDER);
						socket.sendError(4, Definitions.DATA_PACKET_OUT_OF_ORDER, port, address);
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
			GUI.printMessage(Definitions.TRANSFER_FINISHED);
			socket.close();

		} catch (SecurityException e) {
			GUI.printError(Definitions.ACCESS_VIOLATION_ERROR);
			socket.sendError(2, Definitions.ACCESS_VIOLATION_ERROR, port, address);
			return;
			// System.exit(1);
		} catch (UnknownPacketTypeException e) {
			GUI.printError(Definitions.WRONG_PACKET_TYPE_ERROR);
			socket.close();
			return;
			// System.exit(1);
		} catch (FileNotFoundException fe) {
			GUI.printError(Definitions.FILE_NOT_FOUND_ERROR);
			socket.close();
			return;
		} catch (IOException e) {
			GUI.printError(Definitions.UNKNOWN_ERROR);
			socket.close();
			return;
			// System.exit(1);
		}
	}

	void writeDataToDisk(byte[] data, String mode, String filename) {
		// Write data to a file
		try {
			FileParser.writeFileBytes(mode, data, workingDirectory + File.separator + filename);
		} catch (FileNotFoundException fe) {
			GUI.printError(Definitions.FILE_NOT_FOUND_ERROR);
			socket.close();
			return;
		} catch (IOException e) {
			GUI.printError(Definitions.UNKNOWN_ERROR);
			socket.close();
			return;
		} catch (DiskFullException dfe) { // Handle Disk Full or Exceeded Allocation Error
			GUI.printError(Definitions.DISK_FULL_ERROR);
			socket.sendError(3, Definitions.DISK_FULL_ERROR, receivedPort, receivedAddress);
			return;
		}
	}

	@Override
	public void run() {
		if (request instanceof ReadRequestPacket) {
			try {
				handleReadRequest((ReadRequestPacket) request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Start read operation
		} else if (request instanceof WriteRequestPacket) {
			handleWriteRequest((WriteRequestPacket) request); // Start write
																// operation
		} else {
			GUI.printError(Definitions.WRONG_PACKET_TYPE_ERROR);
			socket.close();
			System.exit(1);
		}
	}

	public boolean isRightAddressPort(Packet packet) throws IOException {
		if (packet.getAddress().equals(receivedAddress) && packet.getPort() == receivedPort) {
			return false;
		} else {
			socket.ignoreAndCarryOnWithYourLife(packet);
			return true;
		}

	}

}
