package app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import app.exceptions.UnknownPacketTypeException;
import app.packets.CorruptedPacket;
import app.packets.ErrorPacket;
import app.packets.Packet;

public class Socket {

	private DatagramSocket socket;

	public Socket() throws SocketException {
		this.socket = new DatagramSocket();
		GUI.printSocketOpened(this.socket.getLocalPort());
	}

	public Socket(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
		GUI.printSocketOpened(this.socket.getLocalPort());
	}
	
	/**
	 * Sends a packet and waits for a reply, retrying a specified number of times after timeouts.
	 * Returns null when it receives a corrupted packet.
	 * 
	 * @param toSend The packet to be sent
	 * @param T The type of packet to accept
	 * @param timeout The amount of time to wait, in milliseconds, before retrying
	 * @param numRetries The number of times to attempt retrying
	 * @return The packet that was received, or null if no response was received
	 * @throws IOException 
	 * @throws UnknownPacketTypeException 
	 */
	public Packet sendAndReceive(Packet toSend, Class<?> T, int timeout, int numRetries) throws IOException, UnknownPacketTypeException {
		// For some number of times
		int retriesLeft = numRetries;
		while (retriesLeft-- > 0) {
			System.out.println("Send attempt " + (numRetries - retriesLeft));
			// Send the packet
			this.socket.send(toSend.getDatagramPacket());

			// Tell the user which packet we just sent
			GUI.printPacketSent(toSend);
		
			
			// Wait for a response. If it's type the type we expect, great! Else, try again.
			byte[] bytes = new byte[1000];
			DatagramPacket p = new DatagramPacket(bytes, bytes.length);
			this.socket.setSoTimeout(timeout);
			GUI.printListening(this.socket.getLocalPort());
			try{
				this.socket.receive(p);
			}
			catch(SocketTimeoutException ste){
				System.out.println("socket timed out");
				continue;
			}
			Packet received = Packet.parsePacket(p);
			if (received instanceof CorruptedPacket) {
				GUI.printError(Definitions.RECEIVED_CORRUPTED_PACKET);
			    this.sendError(4, Definitions.RECEIVED_CORRUPTED_PACKET, p.getPort(), p.getAddress());
			    return null;
			}
			GUI.printPacketReceived(received);
			if (T.isInstance(received)) {
				return received;
			} else if (received instanceof ErrorPacket) {
				GUI.printError(((ErrorPacket)received).getErrorMessage());
				return null;
			} else {
				System.out.println("j");
				
				
//				// TODO: Tell the user we received the wrong type of packet and ignored it
			}
		}
		
		// TODO: Replace this with an error from the definitions
		System.out.println("No packet was received");
		return null;
	}
	
	/**
	 * Sends a packet and waits for a reply, retrying a specified number of times after timeouts.
	 * 
	 * 
	 * @param toSend The packet to be sent
	 * @return The packet that was received, or null if no response was received
	 * @throws IOException 
	 */
	public Packet sendAndReceive(Packet toSend, Class<?> T) throws UnknownPacketTypeException, IOException {
		return this.sendAndReceive(toSend, T, 3000, 3);
	}

	/**
	 * Sends a packet without expecting any response.
	 * 
	 * @param toSend The packet to send
	 * @throws IOException If the packet doesn't get sent for some reason
	 */
	public void send(Packet toSend) throws IOException {
		this.socket.send(toSend.getDatagramPacket());
		GUI.printPacketSent(toSend);
	}

	/**
	 * Close the socket, if it's open.
	 */
	public void close() {
		if (!this.socket.isClosed()) {
			this.socket.close();
		}
	}

	public void sendError(int code, String message, int port, InetAddress address) {
		try {
			Packet errorPacket = new ErrorPacket(code, message, port, address);
			this.send(errorPacket);
			this.socket.close();
		} catch (IOException e) {
			// If the error packet fails to send, we're in deep shit anyways
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Receive a packet from a socket. Returns null when it receives a corrupted packet.
	 * 
	 * @param timeout The timeout for the socket
	 * @return The packet received.
	 * @throws UnknownPacketTypeException
	 * @throws IOException
	 */
	public Packet receive(int timeout, boolean print) throws UnknownPacketTypeException, IOException {
		byte[] bytes = new byte[1000];
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
		if (print) { GUI.printListening(this.socket.getLocalPort()); }
		this.socket.setSoTimeout(timeout);
		try {
			this.socket.receive(packet);
		} catch (SocketTimeoutException e) {
			return null;
		}
		Packet received = Packet.parsePacket(packet);
		if (received instanceof CorruptedPacket) {
			GUI.printError(Definitions.RECEIVED_CORRUPTED_PACKET);
			this.sendError(4,  Definitions.RECEIVED_CORRUPTED_PACKET, packet.getPort(), packet.getAddress());
			return null;
		}
		GUI.printPacketReceived(received);
		return received;
	}

	public Packet receive(int timeout) throws UnknownPacketTypeException, IOException {
		return this.receive(timeout, true);
	}
	
	public Packet receive() throws UnknownPacketTypeException, IOException {
		return this.receive(Definitions.MAX_PORT_TIMEOUT);
	}
	
	public void ignoreAndCarryOnWithYourLife(Packet packet) throws IOException{
		ErrorPacket badPacket = new ErrorPacket(5,"I don't know who you are. I don't know what you want. If you are looking for ransom, I can tell you I don't have money. But what I do have are a very particular set of skills, skills I have acquired over a very long career. Skills that make me a nightmare for people like you. If you let my daughter go now, that'll be the end of it. I will not look for you, I will not pursue you. But if you don't, I will look for you, I will find you, and I will kill you.",packet.getPort(),packet.getAddress());
		this.send(badPacket);
	}
}