package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.junit.Test;

import app.packets.ReadRequestPacket;

public class ReadRequestPacketTest {

	private int port = 13;
	private InetAddress address;
	
	@Test
	public void testCanBeInstantiatedWithoutMode() {
		String someFilename = "asdf.txt";
		ReadRequestPacket rrqPacket = new ReadRequestPacket(someFilename, port, address);
		if (! rrqPacket.getFilename().equals(someFilename)) {
			fail("RRQ Packet didn't get instantiated with correct filename");
		}
		if (! rrqPacket.getMode().equals("octet") && ! rrqPacket.getMode().equals("netascii")) {
			fail("RRQ Packet didn't get instantiated with correct mode");
		}

	}

	@Test
	public void testCanBeInstantiatedWithMode() {
		String someFilename = "asdf.txt";
		String someMode = "netascii";
		ReadRequestPacket rrqPacket = new ReadRequestPacket(someFilename, someMode, port, address);
		if (! rrqPacket.getFilename().equals(someFilename)) {
			fail("RRQ Packet didn't get instantiated with correct filename");
		}
		if (! rrqPacket.getMode().equals(someMode)) {
			fail("RRQ Packet didn't get instantiated with correct mode");
		}

	}
	
	@Test
	public void testDatagramPacketIsFormattedCorrectly() {
		String someFilename = "asdf.txt";
		String someMode = "netascii";
		DatagramPacket rrqPacket = (new ReadRequestPacket(someFilename, someMode, port, address)).getDatagramPacket();
		byte[] data = rrqPacket.getData();
		if (data[0] != 0 || data[1] != 1) {
			fail("RRQ Packet has malformed opcode");
		}
		ByteArrayOutputStream filename = new ByteArrayOutputStream();
		ByteArrayOutputStream mode = new ByteArrayOutputStream();
		int i;
		for (i = 2; data[i] != 0; ++i) {
			filename.write(data[i]);
		}
		if (! filename.toString().equals(someFilename)) {
			System.out.println(filename.toString());
			fail("RRQ Packet has malformed filename");
		}
		i += 1; // Skip past the 0-separator
		for (; data[i] != 0 || i >= rrqPacket.getLength(); ++i) {
			mode.write(data[i]);
		}
		if (! mode.toString().equals(someMode)) {
			fail("RRQ Packet has malformed mode");
		}
		if (data[rrqPacket.getLength() - 1] != 0) {
			fail("RRQ Packet isn't 0-terminated");
		}
	}

}
