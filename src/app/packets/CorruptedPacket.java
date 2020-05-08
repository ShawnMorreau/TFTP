package app.packets;

import java.net.*;
/*
 * @author Shawn Morreau
 */
public class CorruptedPacket extends Packet{

	byte[] data;
	
	public CorruptedPacket(byte[] data, int port, InetAddress address) {
		this.data = data;
		this.port = port;
		this.address = address;
	}

	public DatagramPacket getDatagramPacket() {
		
		// Return the DatagramPacket
		return new DatagramPacket(data, data.length, this.address, this.port);
	}

	@Override
	public String toString() {
		String value = "";
		for (byte b : this.data) {
			value += b + " ";
		}
		return value;
	}

	public static Packet parseData(byte[] data, int port, InetAddress address) {
		return new CorruptedPacket(data, port, address);
	}
}
