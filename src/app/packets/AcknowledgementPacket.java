package app.packets;

import java.io.ByteArrayOutputStream;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * @author SYSC3303 Project Team 12
 */
public class AcknowledgementPacket extends Packet{

	private int block;
	
	public AcknowledgementPacket(int blockNumber, int port, InetAddress address) {
		this.block = blockNumber;
		this.port = port;
		this.address = address;
	}

	public DatagramPacket getDatagramPacket() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		
		// Every packet starts with a 0
		bytes.write(0);
		
		// The opcode
		bytes.write(4);
		
		// The 2 bytes that represent the block number
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort((short) this.block);
		bytes.write(bb.get(0));
		bytes.write(bb.get(1));

		// Get these bytes as an array
		byte[] buf = bytes.toByteArray();
		
		// Return the DatagramPacket
		return new DatagramPacket(buf, buf.length, this.address, this.port);
	}

	public int getBlock() {
		return block;
	}
	
	@Override
	public String toString() {
		return "04 - " + this.block;
	}

	public static Packet parseData(byte[] data, int port, InetAddress address) {
		ByteBuffer bb = ByteBuffer.allocate(2);

		// Verify that the opcode isn't messed up
		if (data[0] != 0 || data[1] != 4 || data.length < 4) {
			return new CorruptedPacket(data, port, address);
		}
				
		// Get the block number
		bb.put(data[2]);
		bb.put(data[3]);
		bb.position(0);
		short shortVal = bb.getShort();
		int blockNumber = shortVal >= 0 ? shortVal : 0x10000 + shortVal;

		return new AcknowledgementPacket(blockNumber, port, address);
	}
}
