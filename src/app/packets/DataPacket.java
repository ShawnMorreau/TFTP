package app.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * 
 * @author SYSC3303 Project Team 12
 *
 */

public class DataPacket extends Packet {
	private byte[] data;
	private int block;
	
	public DataPacket(int blockNumber, byte data[], int port, InetAddress address) {
		this.data = data;
		this.block = blockNumber;
		this.port = port;
		this.address = address;
	}

	public DatagramPacket getDatagramPacket() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		
		// Every packet starts with a 0
		bytes.write(0);
		
		// The opcode
		bytes.write(3);
		
		// The 2 bytes that represent the block number
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort((short) this.block);
		bytes.write(bb.get(0));
		bytes.write(bb.get(1));

		// The next n bits are for the data
		try {
			bytes.write(this.data);
		} catch (IOException e) { }

		// Get these bytes as an array
		byte[] buf = bytes.toByteArray();
		
		// Return the DatagramPacket
		return new DatagramPacket(buf, buf.length, this.address, this.port);
	}
	
	public byte[] getData() {
		return data;
	}
	
	public int getBlock() {
		return block;
	}
	
	public String toString() {
		String packageString = "03 - " + this.block + " - ";
		for (byte b : this.data) {
			packageString += b + " ";
		}
		return packageString;
	}

	public static Packet parseData(byte[] byteData, int length, int port, InetAddress address) {
		// Create streams for the block number and the data
		int blockNumber;
		ByteBuffer bb = ByteBuffer.allocate(2);
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		
		// Verify that the opcode isn't messed up
		if (byteData[0] != 0 || byteData[1] != 3 || byteData.length < 4) {
			return new CorruptedPacket(byteData, port, address);
		}
		
		// Get the block number
		bb.put(byteData[2]);
		bb.put(byteData[3]);
		bb.position(0);
		short shortVal = bb.getShort();
		blockNumber = shortVal >= 0 ? shortVal : 0x10000 + shortVal;
		
		// Get the data from the packet
		for (int i = 4; i < length; ++i) {
			data.write(byteData[i]);
		}
		
		// Verify that the packet wasn't messed up
		if (data.size() > 512) {
			return new CorruptedPacket(byteData, port, address);
		}
		
		return new DataPacket(blockNumber, data.toByteArray(), port, address);
	}
}