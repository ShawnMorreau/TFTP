package app.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * This class represents the error packet that would be sent when an Exception is thrown.
 * 
 * @author SYSC3303 Project Team 12
 */
public class ErrorPacket extends Packet {

	private int errorCode;
	private String errorMessage;
	
	public ErrorPacket(int errorCode, String errorMessage, int port, InetAddress address) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.port = port;
		this.address = address;
	}
	
	public DatagramPacket getDatagramPacket() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		
		// Every packet starts with a 0
		bytes.write(0);
		
		// The opcode
		bytes.write(5);
		
		// Every error code starts with 0
		bytes.write(0);
		
		// The error code
		bytes.write((byte) this.errorCode);
		
		// The error message
		try {
			bytes.write(this.errorMessage.getBytes());
		} catch (IOException e) { }
		
		// The zero terminator
		bytes.write(0);
		
		// Get these bytes as an array
		byte[] buf = bytes.toByteArray();
		
		// Return the DatagramPacket
		return new DatagramPacket(buf, buf.length, this.address, this.port);
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public String toString() {
		return "05 - 0" + this.errorCode + " - " + this.errorMessage + " - 0";
	}

	public static Packet parseData(byte[] data, int port, InetAddress address) {
		// Create a stream where we can store the filename and mode
		ByteArrayOutputStream errorMessage = new ByteArrayOutputStream();
	
		// Verify that the opcode isn't messed up and that it's not too short
		if (data[0] != 0 || data[1] != 5 || data.length < 6) {
			return new CorruptedPacket(data, port, address);
		}
		
		int errorCode = data[3];
		// Verify that we have a valid error code
		if (data[2] != 0 || errorCode > 7) {
			return new CorruptedPacket(data, port, address);
		}
		
		// Go until the first 0 separator
		for (int i = 4; data[i] != 0 && i < data.length; ++i) {
			errorMessage.write(data[i]);
		}
		
		return new ErrorPacket(errorCode, errorMessage.toString(), port, address);
	}
}
