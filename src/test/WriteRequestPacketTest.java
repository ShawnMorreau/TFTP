package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.junit.Test;

import app.packets.WriteRequestPacket;

public class WriteRequestPacketTest {

	private int port = 13;
	private InetAddress address;
	
	@Test
	public void testCanBeInstantiatedWithoutMode() {
		String someFilename = "asdf.txt";
		WriteRequestPacket wrqPacket = new WriteRequestPacket(someFilename, port, address);
		if (! wrqPacket.getFilename().equals(someFilename)) {
			fail("WRQ Packet didn't get instantiated with correct filename");
		}
		if (! wrqPacket.getMode().equals("octet") && ! wrqPacket.getMode().equals("netascii")) {
			fail("WRQ Packet didn't get instantiated with correct mode");
		}

	}

	@Test
	public void testCanBeInstantiatedWithMode() {
		String someFilename = "asdf.txt";
		String someMode = "netascii";
		WriteRequestPacket wrqPacket = new WriteRequestPacket(someFilename, someMode, port, address);
		if (! wrqPacket.getFilename().equals(someFilename)) {
			fail("WRQ Packet didn't get instantiated with correct filename");
		}
		if (! wrqPacket.getMode().equals(someMode)) {
			fail("WRQ Packet didn't get instantiated with correct mode");
		}

	}
	
	@Test
	public void testDatagramPacketIsFormattedCorrectly() {
		String someFilename = "asdf.txt";
		String someMode = "netascii";
		DatagramPacket wrqPacket = (new WriteRequestPacket(someFilename, someMode, port, address)).getDatagramPacket();
		byte[] data = wrqPacket.getData();
		if (data[0] != 0 || data[1] != 2) {
			fail("WRQ Packet has malformed opcode");
		}
		ByteArrayOutputStream filename = new ByteArrayOutputStream();
		ByteArrayOutputStream mode = new ByteArrayOutputStream();
		int i;
		for (i = 2; data[i] != 0; ++i) {
			filename.write(data[i]);
		}
		if (! filename.toString().equals(someFilename)) {
			System.out.println(filename.toString());
			fail("WRQ Packet has malformed filename");
		}
		i += 1; // Skip past the 0-separator
		for (; data[i] != 0 || i >= wrqPacket.getLength(); ++i) {
			mode.write(data[i]);
		}
		if (! mode.toString().equals(someMode)) {
			fail("WRQ Packet has malformed mode");
		}
		if (data[wrqPacket.getLength() - 1] != 0) {
			fail("WRQ Packet isn't 0-terminated");
		}
	}

}
