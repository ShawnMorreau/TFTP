package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.junit.Test;

import app.packets.ErrorPacket;

public class ErrorPacketTest {

	private int port = 13;
	private InetAddress address;
	
	@Test
	public void testCanBeInstantiatedWithErrorCodeAndMessage() {
		int arbitraryErrorCode = 7;
		String arbitraryErrorMessage = "Something went wrong";
		ErrorPacket errPacket = new ErrorPacket(arbitraryErrorCode, arbitraryErrorMessage, port, address);
		if (errPacket.getErrorCode() != arbitraryErrorCode) {
			fail("ERR Packet didn't get instantiated with correct error code");
		}
		if (errPacket.getErrorMessage() != arbitraryErrorMessage) {
			fail("ERR Packet didn't get instantiated with correct error message");
		}
	}
	
	@Test
	public void testDatagramPacketIsFormattedCorrectly() {
		int anErrorCode = 7;
		String anErrorMessage = "Something went wrong";
		DatagramPacket errPacket = (new ErrorPacket(anErrorCode, anErrorMessage, port, address)).getDatagramPacket();
		byte[] data = errPacket.getData();
		if (data[0] != 0 || data[1] != 5) {
			fail("ERR Packet's opcode was malformed");
		}
		if (data[2] != 0 || data[3] != anErrorCode) {
			fail("ERR Packet's error code was malformed");
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		for (int i = 4; i < errPacket.getLength() - 1; ++i) {
			bytes.write(data[i]);
		}
		if (!bytes.toString().equals(anErrorMessage)) {
			System.out.println(bytes.toString());
			fail("ERR Packet's error message was malformed");
		}
		if (data[errPacket.getLength() - 1] != 0) {
			fail("ERR Packet isn't null terminated");
		}
	}

}
