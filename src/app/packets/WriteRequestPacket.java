package app.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class WriteRequestPacket extends Packet {
	private String filename;
	private String mode;
	
	public WriteRequestPacket(String filename, String mode, int port, InetAddress address){
		this.filename = filename;
		this.mode = mode;
		this.port = port;
		this.address = address;
	}
	
	public WriteRequestPacket(String filename, int port, InetAddress address) {
		this(filename, "netascii", port, address);
	}
	
	public DatagramPacket getDatagramPacket() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		
		// Every packet starts with a 0
		bytes.write(0);
		
		// The opcode 
		bytes.write(2);
		
		// The next n bytes are the filename
		try {
			bytes.write(this.filename.getBytes());
		} catch (IOException e) { }
		
		// The zero separator
		bytes.write(0);

		// The mode
		try {
			bytes.write(this.mode.getBytes());
		} catch (IOException e) { }

		// The zero terminator
		bytes.write(0);
		
		// Get these bytes as an array
		byte[] buf = bytes.toByteArray();
		
		// Return the DatagramPacket
		return new DatagramPacket(buf, buf.length, this.address, this.port);
	}
	
	public String getFilename(){
		return filename;
	}
	
	public String getMode(){
		return mode;
	}
	
	@Override 
	public String toString(){
		return "02 - " + this.filename + " - 0 - " + this.mode + " - 0";
	}

	public static Packet parseData(byte[] data, int port, InetAddress address) {
		// Create a stream where we can store the filename and mode
		ByteArrayOutputStream filename = new ByteArrayOutputStream();
		ByteArrayOutputStream mode = new ByteArrayOutputStream();

		// Verify that the opcode isn't messed up
		if (data[0] != 0 || data[1] != 2 || data.length < 6) {
			return new CorruptedPacket(data, port, address);
		}

		// The counter
		int i;
		
		// Go until the first 0 separator
		for (i = 2; data[i] != 0 && i < data.length; ++i) {
			filename.write(data[i]);
		}
		
		// Go until the next 0 separator
		for (i += 1; data[i] != 0 && i < data.length; ++i) {
			mode.write(data[i]);
		}
		
		// Verify that the mode is valid
		if (mode.toString().toLowerCase().equals("netascii") || mode.toString().toLowerCase().equals("octet")) {
			return new WriteRequestPacket(filename.toString(), mode.toString(), port, address);
		}

		return new CorruptedPacket(data, port, address);
		
	}
}
