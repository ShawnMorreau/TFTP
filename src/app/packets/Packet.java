package app.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;

import app.exceptions.CorruptedPacketException;
import app.exceptions.UnknownPacketTypeException;

public abstract class Packet {
	public static final String MODE_NETASCII = "netascii";
	public static final String MODE_OCTET = "octet";
	
	public abstract DatagramPacket getDatagramPacket();

	protected int port;
	protected InetAddress address;
	
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public InetAddress getAddress() {
		return this.address;
	}
	
	public int getPort() {
		return this.port;
	}

	/**
	 * Converts a DatagramPacket into the correct Packet instance.
	 * 
	 * @param packet The DatagramPacket to convert
	 * @return The converted DatagramPacket
	 * @throws UnknownPacketTypeException When a packet of unknown type is encountered
	 * @throws CorruptedPacketException 
	 */
	public static Packet parsePacket(DatagramPacket packet) {
		byte opCode = packet.getData()[1];
		byte[] data = packet.getData();
		int length = packet.getLength();
		int port = packet.getPort();
		InetAddress address = packet.getAddress();

		switch (opCode) {
			case 1:
				return ReadRequestPacket.parseData(data, port, address);
			case 2:
				return WriteRequestPacket.parseData(data, port, address);
			case 3:
				return DataPacket.parseData(data, length, port, address);
			case 4:
				return AcknowledgementPacket.parseData(data, port, address);
			case 5:
				return ErrorPacket.parseData(data, port, address);
			default:
				return CorruptedPacket.parseData(data, port, address);
		}
	}
}