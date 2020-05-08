
package app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Scanner;

import app.packets.AcknowledgementPacket;
import app.packets.DataPacket;
import app.packets.ErrorPacket;
import app.packets.Packet;
import app.Definitions;
import app.exceptions.UnknownPacketTypeException;

/**
 * This class represents the error simulator that sits in between the client and
 * the server.
 * 
 * @author SYSC3303 Project Team 12
 */
public class ErrorSimulator {

	/**
	 * The packets and sockets that we'll need.
	 */
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	private DatagramPacket duplicateReceivePacket;
	private DatagramSocket sendReceiveSocket;
	private DatagramSocket receiveSocket;

	private Socket socket = null;

	/**
	 * Booleans that store whose ACK will be the final one.
	 * 
	 * For example: when the client sends the last data packet in a write
	 * transaction, denoted by having less than 512 bytes, the variable
	 * serverShouldEndTransaction is set to true, and the next ACK that's
	 * received by the server ends the transaction. Both these flags are then
	 * reset and the port numbers are reset again, so as to be ready for the
	 * next transaction.
	 */
	private boolean clientShouldEndTransaction = false;
	private boolean serverShouldEndTransaction = false;
	
	/**
	 * The port with which we'll communicate with the server
	 */
	private int connectionPort = 0;
	private int clientPort = 0;
	
	/**
	 * The altered packet information
	 */
	private int subMode;
	private int mainMode;
	private int packetNumber;
	private int delayTime;
	private int packetType = 0;
	private boolean opCodeAltering = false;
	private boolean modeAltering = false;
	private boolean removeFileNameZero = false;
	private boolean removeTerminatingZero = false;
	private boolean sizeAltering = false;
	private boolean tidAltering = false;
	private boolean errorCodeAltering;
	
	/**
	 * The input scanner
	 */
	private static Scanner input = new Scanner(System.in);
	
	/**
	 * The main menu chooser
	 */
	public void mainMenu() throws IOException{
		mainMode=chooseMainMode();
		switch (mainMode) {
		case 1:
			break;
		case 2:
			packetErrorsMenu();
			break;
		case 3:
			transferErrorsMenu();
			break;
		case 4:
			tidErrorsMenu();
			break;
		case 5:
			shawnMode();
			break;
		default:
			break;
		}
	}
	
	private int shawnsErrorMenu() {
		//should send  a high data and receive an error packet 5 -> invalid opcode?
		System.out.println("1) Send a data packet higher than expected");
		//should send a low data and receive an ack which is ignored.
		System.out.println("2) Send a data packet lower than expected");
		//should send an ack packet that is higher than expected. if this is the case, just fault;
		System.out.println("3) Send an ack packet higher than expected");
		//should send an ack packet that is lower than expected and just be ignored. 
		System.out.println("4) Send an ack packet lower than expected");
		input = new Scanner(System.in);
		return input.nextInt();
	}
	private void shawnMode() throws IOException{
		int shawnMode = shawnsErrorMenu();
		switch (shawnMode) {
		case 1:
			sendHighData();
			break;
		case 2:
			sendLowData();
			break;
		case 3:
			sendHighAck();
			break;
		case 4:
			sendLowAck();
			break;
		default:
			chooseMainMode();
		}
	}

	/**
	 * Main menu mode input
	 */
	public int chooseMainMode(){
		System.out.println("Choose the mode of the Error Simulator:");
		System.out.println("1) Normal Mode");
		System.out.println("2) Packet Parsing Errors");
		System.out.println("3) Packet Transfer Errors");
		System.out.println("4) TID Errors");
		System.out.println("5) Additional Errors Menu");
		while (true) {
			System.out.print("> ");
			input = new Scanner(System.in);
			String number = input.nextLine();
			try {
				int n = Integer.parseInt(number);
				if (n <= 5 && n > 0) {
					return n;
				}
			} catch (NumberFormatException e) {}
			System.out.println("Invalid option selected");
		}
		//input = new Scanner(System.in);
		
		//return input.nextInt();
		
	}
	/**
	 * Packet parsing errors menu
	 * @throws IOException 
	 */
	public void packetErrorsMenu() throws IOException{
		choosePacketType(false);
		choosePacketNumber();
		switch (packetType){
		case 1: rrqAndWrqMenu();
				break;
		case 2: rrqAndWrqMenu();
				break;
		case 3: dataPacketMenu();
				break;
		case 4: ackPacketMenu();
				break;
		case 5: errorPacketMenu();
		default: break;
		
		}
		
	}
	/**
	 * The method that controls what type of error to produce
	 */
	public void transferErrorsMenu() {
		subMode = chooseMode();
		switch (subMode) {
		case 1:
			packetLossMenu();
			break;
		case 2:
			packetDelayMenu();
			break;
		case 3:
			packetDupMenu();
			break;
		default:
			break;

		}
	}
	
	public void tidErrorsMenu(){
		choosePacketType(true);
		choosePacketNumber();
		System.out.println("The packet will be sent from an unknown host");
		tidAltering=true;
		packetErrorsMode();
		
		}
	
	
	
	/**
	 * Choosing the mode of the error simulator (packet transfer errors)
	 */
	public int chooseMode() {			
		
		System.out.println("Choose the mode of the Error Simulator:");
		System.out.println("1) Packet Loss mode");
		System.out.println("2) Packet Delay mode");
		System.out.println("3) Packet Duplication mode");
		while (true) {
			System.out.print("> ");
			input = new Scanner(System.in);
			String number = input.nextLine();
			try {
				int n = Integer.parseInt(number);
				if (n <= 3 && n > 0) {
					return n;
				}
			} catch (NumberFormatException e) {}
			System.out.println("Invalid option selected");
		}
		//input = new Scanner(System.in);
		
		
		//return input.nextInt();
	}
	
	/**
	 * The menu for packet loss
	 */
	public void packetLossMenu() {
		choosePacketType(false);
		choosePacketNumber();
	}
	/**
	 * the packet delay menu
	 */
	public void packetDelayMenu() {
		choosePacketType(false);
		choosePacketNumber();
		System.out.println("Please write the delay you wish to have (in ms): ");
		input = new Scanner(System.in);
		delayTime=input.nextInt();
	}
	/**
	 * the packet duplicate sub menu
	 */
	public void packetDupMenu() {
		choosePacketType(false);
		choosePacketNumber();
	}
	/**
	 * Choosing the packet type
	 */
	public void choosePacketType(boolean partialMenu) {
		System.out.println("Choose the packet type:");
		 if (!partialMenu) {
		System.out.println("1) RRQ");
		System.out.println("2) WRQ");
		System.out.println("3) DATA");
		System.out.println("4) ACK");
		System.out.println("5) ERROR");
		while (true) {
			System.out.print("> ");
			input = new Scanner(System.in);
			String number = input.nextLine();
			try {
				int n = Integer.parseInt(number);
				if (n <= 5 && n > 0) {
					packetType=n; 
					break;
				}
			} catch (NumberFormatException e) {}
			System.out.println("Invalid option selected");
		}
		 }
		 else {
		    System.out.println("1) DATA");
			System.out.println("2) ACK");
			System.out.println("3) ERROR");
			while (true) {
				System.out.print("> ");
				input = new Scanner(System.in);
				String number = input.nextLine();
				try {
					int n = Integer.parseInt(number);
					if (n <= 3 && n > 0) {
						packetType=n+2;
						break;
					}
				} catch (NumberFormatException e) {}
				System.out.println("Invalid option selected");
			
			}
		 }
		//input = new Scanner(System.in);
		//packetType= packetType+ input.nextInt();
	}
	
	
	
	
	/**
	 * Choosing the packet number
	 */
	public void choosePacketNumber() {
		if (packetType<3) {
			packetNumber=0;}
		else if (packetType==5) {
			packetNumber=0;}
		else {
			
			while (true) {
				System.out.println("Please write the packet number: ");
				input = new Scanner(System.in);
				packetNumber=input.nextInt();
				if (packetNumber <0 || packetNumber>99) {
					System.out.println("Invalid packet number");
					continue;
				}
				else break;
				
			}
			//input = new Scanner(System.in);
			//packetNumber=input.nextInt();
			
		}
	}
	/**
	 * the RRQ and WRQ menu
	 * @return
	 */
	public void rrqAndWrqMenu(){
		System.out.println("Please choose what you want to do with the packet");
		System.out.println("1) Invalid Opcode");
		System.out.println("2) Invalid mode");
		System.out.println("3) Remove 0 after Filename");
		System.out.println("4) Remove terminating zero");
		//System.out.println("5) TID errors");
		input = new Scanner(System.in);
		int rrqAndWrqMode = input.nextInt();
		switch (rrqAndWrqMode){
		case 1: opCodeAltering=true;
				break;
		case 2: modeAltering=true;
				break;
		case 3: removeFileNameZero = true; 
				break;
		case 4: removeTerminatingZero=true;
				break;
		//case 5: tidAltering=true;
		//break;
		default: break;
		}
	}
	/**
	 * Data Packet Menu
	 */
	public void dataPacketMenu() {
		System.out.println("Please choose what you want to do with the packet");
		System.out.println("1) Invalid Opcode");
		System.out.println("2) Invalid Size");
		//System.out.println("3) TID errors");
		input = new Scanner(System.in);
		int dataPacketMode = input.nextInt();
		switch (dataPacketMode){
		case 1: opCodeAltering=true;
				break;
		case 2: sizeAltering=true;
				break;
		//case 3: tidAltering=true;
		//break;
		default: break;
		}
	}
	/**
	 * The Ack packet menu
	 */
	public void ackPacketMenu() {
		System.out.println("Please choose what you want to do with the packet");
		System.out.println("1) Invalid Opcode");
		//System.out.println("2) TID errors");
		input = new Scanner(System.in);
		int dataPacketMode = input.nextInt();
		switch (dataPacketMode){
		case 1: opCodeAltering=true;
				break;
		//case 3: tidAltering=true;
		//break;
		///case 2: sizeAltering=true;
				//break;
		default: break;
		}
	}
		/**
		 * the error packet menu
		 */
		public void errorPacketMenu() {
			System.out.println("Please choose what you want to do with the packet");
			System.out.println("1) Invalid Opcode");
			System.out.println("2) Invalid error code");
			//System.out.println("2) TID errors");
			input = new Scanner(System.in);
			int dataPacketMode = input.nextInt();
			switch (dataPacketMode){
			case 1: opCodeAltering=true;
					break;
			case 2: errorCodeAltering = true;
					break;
			//case 3: tidAltering=true;
			//break;
			///case 2: sizeAltering=true;
					//break;
			default: break;
			}
		
	}
	/**
	 * The packet transfer Errors mode
	 */
	public void transferErrorsMode(){
		switch (subMode) {
		case 1: packetLossExecution();
				break;
		case 2: packetDelayExecution();
				break;
		case 3: packetDuplicationExecution();
				break;
		default: break;
		}
	}
	/**
	 * Packet finder method
	 */
	public boolean packetFinder(DatagramPacket packet) {
		// & ((!opCodeAltering) & (!removeFileNameZero) & (!modeAltering) & (!removeTerminatingZero) & (!sizeAltering))
		if (packet.getData()[1]==packetType) {
			 if (packetType<=2 ||packetType==5) {						
				return true;
			 } else {
				 int blockNumber = packet.getData()[2]*10 + packet.getData()[3];
				 return (blockNumber == packetNumber);			
					 
			 }
		} else return false;
		
	}
	
	/**
	 * packet errors execution
	 */
	public DatagramPacket packetErrorsExecution(byte[] arr, int stepper, DatagramPacket packet, DatagramPacket duplicateReceivePacket){
		 arr = packet.getData();
		    
			if(opCodeAltering){ arr[1]=6;  }//CHANGE OPCODE
			else if (errorCodeAltering) arr[3]='x';
			else if (removeFileNameZero) {// Go until the first 0 separator
				for (stepper = 2; arr[stepper] != 0; ++stepper) {}
				arr[stepper]='x';
				}
			else if (modeAltering) {for (stepper = 2; arr[stepper] != 0; ++stepper) {}
				arr[stepper+1]='x';} //f wit mode
			
			else if (removeTerminatingZero) {
				for (stepper = 2; arr[stepper] != 0; ++stepper){}
				for (stepper = stepper+1; arr[stepper] != 0; ++stepper){}//reach the end zero
				
				arr[stepper]='x';
				}//remove terminating0 and replace it with an x
			else if (sizeAltering) {
				int port = packet.getPort();
				byte filler = 'x';
				InetAddress address = packet.getAddress(); 
				byte[] arrBig = new byte[600];
				Arrays.fill(arrBig, filler);
				for (int i=0; i< packet.getLength(); i++) {
					arrBig[i]=arr[i];
				}
				duplicateReceivePacket.setData(arrBig);
				duplicateReceivePacket.setLength(arrBig.length);
				//System.out.println(duplicateReceivePacket.getLength()+" is the size of the new pacekt, and "+ arrBig.length+"is the size of Arrbig.");
				
				duplicateReceivePacket.setPort(port);
				duplicateReceivePacket.setAddress(address);
				return duplicateReceivePacket;
				
			}
			packet.setData(arr);
			return packet;
		
	}
	
	
/**
 * The packet errors execution 	
 */
public void packetErrorsMode(){
	try {
		receiveSocket = new DatagramSocket(Definitions.ERROR_SIMULATOR_LISTEN_PORT);
		sendReceiveSocket = new DatagramSocket();
		DatagramSocket theUnknownSocket = new DatagramSocket();
		sendPacket = new DatagramPacket(new byte[550], 550);
		receivePacket = new DatagramPacket(new byte[550], 550);
		duplicateReceivePacket= new DatagramPacket(new byte[600], 600);
		boolean packetFound=false;
		boolean doneOnce=false;
		byte[] arr=new byte[550];
		int stepper=2;
	
		while (true) {
			// Wait for a packet from the client
			GUI.printListening(receiveSocket.getLocalPort());
			receiveSocket.receive(receivePacket);
			GUI.printPacketInfo(Packet.parsePacket(receivePacket), true);
			packetFound=packetFinder(receivePacket);
			if (packetFound&& !doneOnce &&!tidAltering) { receivePacket= packetErrorsExecution(arr, stepper, receivePacket, duplicateReceivePacket); 
			doneOnce=true; }
			
			//if (!opCodeAltering) GUI.printPacketInfo(Packet.parsePacket(receivePacket), true);
			//else System.out.println("Received packet");
			if (isFinalDataPacket(receivePacket)) serverShouldEndTransaction = true;
			
			// Keep track of the client's port
			clientPort = receivePacket.getPort();
							
			// Re-route it to the server and send it
			if (connectionPort > 0) {				 
				
				receivePacket.setPort(connectionPort);
			} else {
				receivePacket.setPort(Definitions.SERVER_LISTEN_PORT);
			}
			if(tidAltering&&packetFound) {
				theUnknownSocket.send(receivePacket);
				tidAltering=false;
			} else sendReceiveSocket.send(receivePacket);			
			
			GUI.printPacketInfo(Packet.parsePacket(receivePacket), false);
			//if (!opCodeAltering) GUI.printPacketInfo(Packet.parsePacket(receivePacket), false);
			//else System.out.println("Sending packet to server");
			
			if (isAck(receivePacket) && clientShouldEndTransaction) {
				// Reset the port numbers
				clientPort = 0;
				connectionPort = 0;
				
				// Reset the transaction details
				clientShouldEndTransaction = false;
				serverShouldEndTransaction = false;
				
				continue;
			}
			// Wait for a packet from the server
			GUI.printListening(sendReceiveSocket.getLocalPort());
			sendReceiveSocket.receive(sendPacket);
			GUI.printPacketInfo(Packet.parsePacket(sendPacket), true);

			packetFound=packetFinder(sendPacket);
			if (packetFound&&!doneOnce &&!tidAltering) { sendPacket=packetErrorsExecution(arr, stepper, sendPacket, duplicateReceivePacket);
			doneOnce=true; 
			}
			//if (!opCodeAltering) GUI.printPacketInfo(Packet.parsePacket(sendPacket), true);
			//else System.out.println("Received packet");
			
		
			if (isFinalDataPacket(sendPacket)) clientShouldEndTransaction = true;
			
			// Store the port number of the server's connection
			connectionPort = sendPacket.getPort();

			// Re-route it to the client and send it
			sendPacket.setPort(clientPort);
			if(packetFound&&tidAltering) {
				theUnknownSocket.send(sendPacket);	
				tidAltering=false;
			} else {
				sendReceiveSocket.send(sendPacket);		
			}
   		 GUI.printPacketInfo(Packet.parsePacket(sendPacket), false);
			
			//if (!opCodeAltering) GUI.printPacketInfo(Packet.parsePacket(sendPacket), false);
			//else System.out.println("Sending packet to client");
	
			if (serverShouldEndTransaction) {
				// Reset the port numbers
				clientPort = 0;
				connectionPort = 0;
				
				// Reset the transaction details
				clientShouldEndTransaction = false;
				serverShouldEndTransaction = false;
			}
		}
	} catch (IOException e) {
		System.exit(1);
	} catch (Exception e) {		
		System.exit(1);
	}
	
	
}
	
	
	/**
	 * Packet Duplication
	 */
	public void packetDuplicationExecution() { //TODO Monster Method
		
		try {
			receiveSocket = new DatagramSocket(Definitions.ERROR_SIMULATOR_LISTEN_PORT);
			sendReceiveSocket = new DatagramSocket();
			sendPacket = new DatagramPacket(new byte[550], 550);
			receivePacket = new DatagramPacket(new byte[550], 550);
			boolean packetFound=false;
		
			while (true) {
				// Wait for a packet from the client
				GUI.printListening(receiveSocket.getLocalPort());
				receiveSocket.receive(receivePacket);
				GUI.printPacketInfo(Packet.parsePacket(receivePacket), true);
				packetFound= packetFinder(receivePacket);
				System.out.println(packetFound);
				if (isFinalDataPacket(receivePacket)) serverShouldEndTransaction = true;
				
				// Keep track of the client's port
				clientPort = receivePacket.getPort();
								
				// Re-route it to the server and send it
				if (connectionPort > 0) {
					receivePacket.setPort(connectionPort);
				} else {
					receivePacket.setPort(Definitions.SERVER_LISTEN_PORT);
				}
				if (packetFound){
						sendReceiveSocket.send(receivePacket);
						GUI.printPacketInfo(Packet.parsePacket(receivePacket), false);
						GUI.printListening(sendReceiveSocket.getLocalPort());
						sendReceiveSocket.receive(sendPacket);
						
						GUI.printPacketInfo(Packet.parsePacket(sendPacket), true);
						System.out.println("Duplicatinnnnngggg");
						sendReceiveSocket.send(receivePacket);
						GUI.printPacketInfo(Packet.parsePacket(receivePacket), false);
						if (isAck(receivePacket) && clientShouldEndTransaction) {
							// Reset the port numbers
							clientPort = 0;
							connectionPort = 0;
							
							// Reset the transaction details
							clientShouldEndTransaction = false;
							serverShouldEndTransaction = false;
							
							continue;
						}
					
					packetFound=false;					
				} else { //do it normally
					
					sendReceiveSocket.send(receivePacket);
					GUI.printPacketInfo(Packet.parsePacket(receivePacket), false);
					if (isAck(receivePacket) && clientShouldEndTransaction) {
						// Reset the port numbers
						clientPort = 0;
						connectionPort = 0;
						
						// Reset the transaction details
						clientShouldEndTransaction = false;
						serverShouldEndTransaction = false;
						
						continue;
					}
					

					// Wait for a packet from the server
					GUI.printListening(sendReceiveSocket.getLocalPort());
					sendReceiveSocket.receive(sendPacket);
					GUI.printPacketInfo(Packet.parsePacket(sendPacket), true);
				}
				
							
				packetFound=packetFinder(sendPacket);
								
				if (isFinalDataPacket(sendPacket)) clientShouldEndTransaction = true;
				
				// Store the port number of the server's connection
				connectionPort = sendPacket.getPort();

				// Re-route it to the client and send it
				sendPacket.setPort(clientPort);
				if (packetFound){
					System.out.println("gonna send twice");
					for (int i=0;i<2;i++){
						sendReceiveSocket.send(sendPacket);
						GUI.printPacketInfo(Packet.parsePacket(sendPacket), false);
						packetFound=false;
						
					}
				} else {
					sendReceiveSocket.send(sendPacket);
					GUI.printPacketInfo(Packet.parsePacket(sendPacket), false);
				}
				
				
				if (serverShouldEndTransaction) {
					// Reset the port numbers
					clientPort = 0;
					connectionPort = 0;
					
					// Reset the transaction details
					clientShouldEndTransaction = false;
					serverShouldEndTransaction = false;
				}
				}
			
		} catch (IOException e) {
			System.exit(1);
		} catch (Exception e) {
			System.exit(1);
		}
		
			
	}

	/**
	 * 
	 */
	public void packetDelayExecution() {
		//TODO: Re-use code to simplify
		try {
			receiveSocket = new DatagramSocket(Definitions.ERROR_SIMULATOR_LISTEN_PORT);
			sendReceiveSocket = new DatagramSocket();
			GUI.printSocketOpened(sendReceiveSocket.getLocalPort());
			sendPacket = new DatagramPacket(new byte[550], 550);
			receivePacket = new DatagramPacket(new byte[550], 550);
			boolean delayed=false;
		
			while (true) {
				// Wait for a packet from the client
				GUI.printListening(receiveSocket.getLocalPort());
				receiveSocket.receive(receivePacket);
				GUI.printPacketInfo(Packet.parsePacket(receivePacket), true);
				if (isFinalDataPacket(receivePacket)) serverShouldEndTransaction = true;
				//the packet checker
				if (receivePacket.getData()[1]==packetType&&(!delayed)) {
					 if (packetType<=2||packetType==5) {
						 //do nothing
						 System.out.println("Delaying a RRQ/WRQ/ERROR Packet");
						 Thread.sleep(delayTime);
						 delayed=true;
						 
					 }
					 else {
						 int blockNumber = receivePacket.getData()[2]*10 + receivePacket.getData()[3];
						 if (blockNumber == packetNumber) {
							 
							 System.out.println("Delaying packet number: "+ blockNumber);
							 Thread.sleep(delayTime);
							 delayed=true;
							 
						 }
					 }
				}
				
				// Keep track of the client's port
				clientPort = receivePacket.getPort();
				
				// Re-route it to the server and send it
				if (connectionPort > 0) {
					receivePacket.setPort(connectionPort);
				} else {
					receivePacket.setPort(Definitions.SERVER_LISTEN_PORT);
				}
				sendReceiveSocket.send(receivePacket);
				GUI.printPacketInfo(Packet.parsePacket(receivePacket), false);
				
				if (isAck(receivePacket) && clientShouldEndTransaction) {
					// Reset the port numbers
					clientPort = 0;
					connectionPort = 0;
					
					// Reset the transaction details
					clientShouldEndTransaction = false;
					serverShouldEndTransaction = false;
					
					continue;
				}
				
				// Wait for a packet from the server
				GUI.printListening(sendReceiveSocket.getLocalPort());
				sendReceiveSocket.receive(sendPacket);
				//TODO have this in a separate function
				if (sendPacket.getData()[1]==packetType&&(!delayed)) {
					 if (packetType<=2||packetType==5) {
						 //do nothing
						 System.out.println("Delaying a RRQ/WRQ/ERROR Packet");
						 Thread.sleep(delayTime);
						 delayed=true;
						 
					 }
					 else {
						 int blockNumber = sendPacket.getData()[2]*10 + sendPacket.getData()[3];
						 if (blockNumber == packetNumber) {
							 
							 System.out.println("Delaying packet number: "+ blockNumber);
							 Thread.sleep(delayTime);
							 delayed=true;
						 }
					 }
				}
				GUI.printPacketInfo(Packet.parsePacket(sendPacket), true);
				if (isFinalDataPacket(sendPacket)) clientShouldEndTransaction = true;
				
				// Store the port number of the server's connection
				connectionPort = sendPacket.getPort();

				// Re-route it to the client and send it
				sendPacket.setPort(clientPort);
				sendReceiveSocket.send(sendPacket);
				GUI.printPacketInfo(Packet.parsePacket(sendPacket), false);
				
				if (serverShouldEndTransaction) {
					// Reset the port numbers
					clientPort = 0;
					connectionPort = 0;
					
					// Reset the transaction details
					clientShouldEndTransaction = false;
					serverShouldEndTransaction = false;
				}
			}
		} catch (IOException e) {
			System.exit(1);
		} catch (Exception e) {
			System.exit(1);
		}
		
	}
	
	/**
	 * PacketLossExecution
	 */
	public void packetLossExecution() {
		try {
			receiveSocket = new DatagramSocket(Definitions.ERROR_SIMULATOR_LISTEN_PORT);
			sendReceiveSocket = new DatagramSocket();
			sendPacket = new DatagramPacket(new byte[550], 550);
			receivePacket = new DatagramPacket(new byte[550], 550);
			boolean packetFound=false;
			boolean doneOnce=false;
			while (true) {
				// Wait for a packet from the client
				GUI.printListening(receiveSocket.getLocalPort());
				receiveSocket.receive(receivePacket);
				GUI.printPacketInfo(Packet.parsePacket(receivePacket), true);
				if (isFinalDataPacket(receivePacket)) serverShouldEndTransaction = true;
				if (packetFinder(receivePacket)&&!doneOnce) {
					doneOnce=true;
					continue;					
				}
				// Keep track of the client's port
				clientPort = receivePacket.getPort();
								
				// Re-route it to the server and send it
				if (connectionPort > 0) {
					receivePacket.setPort(connectionPort);
				} else {
					receivePacket.setPort(Definitions.SERVER_LISTEN_PORT);
				}
				sendReceiveSocket.send(receivePacket);
				GUI.printPacketInfo(Packet.parsePacket(receivePacket), false);
				
				if (isAck(receivePacket) && clientShouldEndTransaction) {
					// Reset the port numbers
					clientPort = 0;
					connectionPort = 0;
					
					// Reset the transaction details
					clientShouldEndTransaction = false;
					serverShouldEndTransaction = false;
					
					continue;
				}
				

				// Wait for a packet from the server
				GUI.printListening(sendReceiveSocket.getLocalPort());
				sendReceiveSocket.receive(sendPacket);
				GUI.printPacketInfo(Packet.parsePacket(sendPacket), true);
				if(packetFinder(sendPacket)&&!doneOnce){
					System.out.println("Lost the packet, now listening again");
					GUI.printListening(sendReceiveSocket.getLocalPort());
					sendReceiveSocket.receive(sendPacket);
					GUI.printPacketInfo(Packet.parsePacket(sendPacket), true);
					doneOnce=true;
					
					}
				
				if (isFinalDataPacket(sendPacket)) clientShouldEndTransaction = true;
				
				// Store the port number of the server's connection
				connectionPort = sendPacket.getPort();
				
				// Re-route it to the client and send it
				sendPacket.setPort(clientPort);
				sendReceiveSocket.send(sendPacket);
				GUI.printPacketInfo(Packet.parsePacket(sendPacket), false);
				
				if (serverShouldEndTransaction) {
					// Reset the port numbers
					clientPort = 0;
					connectionPort = 0;
					
					// Reset the transaction details
					clientShouldEndTransaction = false;
					serverShouldEndTransaction = false;
				}
			}
		} catch (IOException e) {
			System.exit(1);
		} catch (Exception e) {
			System.exit(1);
		}
		
		
	}
	
	
	
	
	/**
	 * The normal execution of the forward packet method
	 */
	public void normalExecution() {
		try {
			receiveSocket = new DatagramSocket(Definitions.ERROR_SIMULATOR_LISTEN_PORT);
			sendReceiveSocket = new DatagramSocket();
			sendPacket = new DatagramPacket(new byte[550], 550);
			receivePacket = new DatagramPacket(new byte[550], 550);
		
			while (true) {
				// Wait for a packet from the client
				GUI.printListening(receiveSocket.getLocalPort());
				receiveSocket.receive(receivePacket);
				GUI.printPacketInfo(Packet.parsePacket(receivePacket), true);
				if (isFinalDataPacket(receivePacket)) serverShouldEndTransaction = true;
				
				// Keep track of the client's port
				clientPort = receivePacket.getPort();
								
				// Re-route it to the server and send it
				if (connectionPort > 0) {
					receivePacket.setPort(connectionPort);
				} else {
					receivePacket.setPort(Definitions.SERVER_LISTEN_PORT);
				}
				sendReceiveSocket.send(receivePacket);
				GUI.printPacketInfo(Packet.parsePacket(receivePacket), false);
				
				if (isAck(receivePacket) && clientShouldEndTransaction) {
					// Reset the port numbers
					clientPort = 0;
					connectionPort = 0;
					
					// Reset the transaction details
					clientShouldEndTransaction = false;
					serverShouldEndTransaction = false;
					
					continue;
				}
				

				// Wait for a packet from the server
				GUI.printListening(sendReceiveSocket.getLocalPort());
				sendReceiveSocket.receive(sendPacket);
				GUI.printPacketInfo(Packet.parsePacket(sendPacket), true);
				
				if (isFinalDataPacket(sendPacket)) clientShouldEndTransaction = true;
				
				// Store the port number of the server's connection
				connectionPort = sendPacket.getPort();

				// Re-route it to the client and send it
				sendPacket.setPort(clientPort);
				sendReceiveSocket.send(sendPacket);
				GUI.printPacketInfo(Packet.parsePacket(sendPacket), false);
				
				if (serverShouldEndTransaction) {
					// Reset the port numbers
					clientPort = 0;
					connectionPort = 0;
					
					// Reset the transaction details
					clientShouldEndTransaction = false;
					serverShouldEndTransaction = false;
				}
			}
		} catch (IOException e) {
			System.exit(1);
		} catch (Exception e) {
			System.exit(1);
		}
		
		
		
	}
		
	private boolean isAck(DatagramPacket p) {
		Packet packet = Packet.parsePacket(p);
		return (packet instanceof AcknowledgementPacket);
	}
	
	private boolean isFinalDataPacket(DatagramPacket p) {
		Packet packet = Packet.parsePacket(p);
		return (packet instanceof DataPacket && ((DataPacket)packet).getData().length < 512);
	}

	/**
	 * The main controller for the error simulator.
	 */
	public void forwardPackets() throws IOException {
		
		// TODO: Replace this with something better
		System.out.println("Starting ErrorSimulator");
		mainMenu();
		System.out.println("ErrorSimulator will run under these conditions");
		
		switch (mainMode) {
		case 1: normalExecution();
				break;
		case 2: packetErrorsMode();
				break;
		case 3: transferErrorsMode();
				break;
		default: break;
		}
		
	}
	
	/**
	 * The main entry point for the error controller.
	 * 
	 * @param args The command line arguments given to the error controller
	 */
	public static void main (String[] args) throws IOException {
		// The error simulator is always in verbose mode
		Settings.verbose = true;
		
		// Kick off the program
		(new ErrorSimulator()).forwardPackets();
		
	}
	
	/**
	 * Any method that Shawn has created for his menu has been added below here
	 * (easier to add after)
	 */

	/**
	 * Create a packet that has a larger block number than the current -> make
	 * it send an error.
	 * 
	 * @throws IOException
	 */
	private void sendHighData() throws IOException {

		try {
			forwardDataWithModification(1, true);
		} catch (UnknownPacketTypeException e) {
		}
	}

	private void sendLowAck() throws IOException {
		try{
			forwardDataWithModification(4, false);
		}catch (UnknownPacketTypeException e) {
		}
	}

	private void sendHighAck() throws IOException {
		try{
			forwardDataWithModification(3, true);
		}catch (UnknownPacketTypeException e) {
		}
	}

	private void sendLowData() throws IOException {
		try {
			forwardDataWithModification(2,false);
		} catch (UnknownPacketTypeException e) {
			e.printStackTrace();
		}
	}
	private void sendInvalidTid(){
		try{
			forwardDataWithModification(6,false);
		}catch (UnknownPacketTypeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Used Karim's normal running error sim as a base.
	 * @param num
	 *            is in relation to the method used. quick "hack" fix
	 * @param highLow
	 *            is a boolean to determine if you want a high/low modification
	 * @throws UnknownPacketTypeException
	 */
	private void forwardDataWithModification(int num, boolean highLow) throws UnknownPacketTypeException {
		System.out.println("READY TO RUMBLE!");
		boolean isHighLow = highLow; // high = true; low = false
		try {
			receiveSocket = new DatagramSocket(Definitions.ERROR_SIMULATOR_LISTEN_PORT);
			sendReceiveSocket = new DatagramSocket();
			sendPacket = new DatagramPacket(new byte[550], 550);
			receivePacket = new DatagramPacket(new byte[550], 550);

			while (true) {
				// Wait for a packet from the client
				GUI.printListening(receiveSocket.getLocalPort());
				receiveSocket.receive(receivePacket);
				GUI.printPacketInfo(Packet.parsePacket(receivePacket), true);
				if (isFinalDataPacket(receivePacket))
					serverShouldEndTransaction = true;

				// Keep track of the client's port
				clientPort = receivePacket.getPort();

				// Re-route it to the server and send it
				if (connectionPort > 0) {
					receivePacket.setPort(connectionPort);
				} else {
					receivePacket.setPort(Definitions.SERVER_LISTEN_PORT);
				}
				if (isHighLow == true && num == 3 && isAck(receivePacket)) {
					System.out.println("changing ack block to a higher num");
					num = 5;
					receivePacket.getData()[3] = 9;
					sendReceiveSocket.send(receivePacket);
				}
				else if (isHighLow == false && num == 4 && (receivePacket.getData()[3] != 1) && isAck(receivePacket)) {
					System.out.println("changing ack block to a lower num");//unneeded I guess. same as duplicate ack.
					num = 5;
					receivePacket.getData()[3] = 1;
					sendReceiveSocket.send(receivePacket);
				}
				else
					sendReceiveSocket.send(receivePacket);
				GUI.printPacketInfo(Packet.parsePacket(receivePacket), false);

				if (isAck(receivePacket) && clientShouldEndTransaction) {
					// Reset the port numbers
					clientPort = 0;
					connectionPort = 0;

					// Reset the transaction details
					clientShouldEndTransaction = false;
					serverShouldEndTransaction = false;

					continue;
				}

				// Wait for a packet from the server
				GUI.printListening(sendReceiveSocket.getLocalPort());
				sendReceiveSocket.receive(sendPacket);
				GUI.printPacketInfo(Packet.parsePacket(sendPacket), true);
				byte[] data = sendPacket.getData();
				/**
				 * the next couple of methods are gross looking(programming wise) but it was a quick implementation
				 */
				if(connectionPort == 0) connectionPort = sendPacket.getPort();
				//high data packet
				if (isHighLow == true && num == 1) {
					data[3] = 9;
					sendPacket = new DatagramPacket(data, data.length, receivePacket.getAddress(), connectionPort);
				}
				if (isHighLow == false && num == 2 && data[3] > 1){
					//break so it doesn't keep looping
					num = 5;
					data[3] = 1;
					sendPacket = new DatagramPacket(data, data.length, receivePacket.getAddress(), connectionPort);
				}
				
				if (isFinalDataPacket(sendPacket))
					clientShouldEndTransaction = true;

				// Store the port number of the server's connection
				connectionPort = sendPacket.getPort();

				// Re-route it to the client and send it
				sendPacket.setPort(clientPort);
				if (isHighLow == false && num == 6 && (sendPacket.getData()[3] > 1)){
					num = 5;
					DatagramSocket newSocket = new DatagramSocket();
					newSocket.send(sendPacket);
				}else
					sendReceiveSocket.send(sendPacket);
				GUI.printPacketInfo(Packet.parsePacket(sendPacket), false);

				if (serverShouldEndTransaction) {
					// Reset the port numbers
					clientPort = 0;
					connectionPort = 0;

					// Reset the transaction details
					clientShouldEndTransaction = false;
					serverShouldEndTransaction = false;
				}
			}
		} catch (IOException e) {
			System.exit(1);
		} catch (Exception e) {
			System.exit(1);
		}

	}
}