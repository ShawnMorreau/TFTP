package test;

import static org.junit.Assert.*;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.junit.Test;

import app.packets.AcknowledgementPacket;

public class AcknowledgementPacketTest {

	/**
	 * Arbitrary values required for instantiation
	 */
	private int port = 13;
	private InetAddress address;
	
	@Test
	public void testCanBeInstantiatedWithCustomBlockNumber() {
		int arbitraryBlockNumber = 7;
		AcknowledgementPacket ackPacket = new AcknowledgementPacket(arbitraryBlockNumber, port, address);
		if (ackPacket.getBlock() != arbitraryBlockNumber) {
			fail("ACK Packet didn't get instantiated with correct block number");
		}
	}
	
	@Test
	public void testDatagramPacketIsFormattedCorrectly() {
		int aBlockNumber = 7;
		DatagramPacket ackPacket = (new AcknowledgementPacket(aBlockNumber, port, address)).getDatagramPacket();
		byte[] data = ackPacket.getData();
		if (data[0] != 0 || data[1] != 4) {
			fail("ACK Packet's opcode was malformed");
		}
		if (data[2] != 0 || data[3] != aBlockNumber) {
			fail("ACK Packet didn't have a correctly formatted block number");
		}
		
	}

	@Test
	public void testDatagramPacketCanContainHugeBlockNumbers() {
		int bigBlockNumber = 65535;
		DatagramPacket ackPacket = (new AcknowledgementPacket(bigBlockNumber, port, address)).getDatagramPacket();
		byte[] data = ackPacket.getData();
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.put(data[2]);
		bb.put(data[3]);
		bb.position(0);
		short shortVal = bb.getShort();
		int intVal = shortVal >= 0 ? shortVal : 0x10000 + shortVal;
		if (intVal != bigBlockNumber) {
			fail("ACK Packet didn't store two-byte numbers correctly");
		}
	}

}
