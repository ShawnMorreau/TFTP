package test;

import static org.junit.Assert.*;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.junit.Test;

import app.packets.DataPacket;

public class DataPacketTest {

	private int port = 13;
	private InetAddress address;
	
	@Test
	public void testCanBeInstantiatedWithData() {
		int arbitraryBlockNumber = 7;
		byte[] arbitraryData = new byte[] { 2, 4, 6 };
		DataPacket dataPacket = new DataPacket(arbitraryBlockNumber, arbitraryData, port, address);
		if (dataPacket.getBlock() != arbitraryBlockNumber) {
			fail("DATA Packet didn't get instantiated with correct block number");
		}
		for (int i=0;i<arbitraryData.length;++i) {
			if (dataPacket.getData()[i] != arbitraryData[i]) {
				fail("DATA Packet didn't get instantiated with the correct data");
			}
		}
	}
	
	@Test
	public void testDatagramPacketIsFormattedCorrectly() {
		int aBlockNumber = 7;
		byte[] aDataArray = new byte[] { 2, 4, 6 };
		DatagramPacket dataPacket = (new DataPacket(aBlockNumber, aDataArray, port, address)).getDatagramPacket();
		byte[] data = dataPacket.getData();
		if (data[0] != 0 || data[1] != 3) {
			fail("DATA Packet's opcode was malformed");
		}
		if (data[2] != 0 || data[3] != aBlockNumber) {
			fail("DATA Packet's block number was malformed");
		}
		for (int i = 4; i < dataPacket.getLength(); ++i) {
			if (aDataArray[i - 4] != data[i]) {
				fail("DATA Packet's data block was malformed");
			}
		}
	}
	
	@Test
	public void testDatagramPacketCanContainHugeBlockNumbers() {
		int bigBlockNumber = 65535;
		byte[] someData = new byte[] { 2, 4, 6 };
		DatagramPacket dataPacket = (new DataPacket(bigBlockNumber, someData, port, address)).getDatagramPacket();
		byte[] data = dataPacket.getData();
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.put(data[2]);
		bb.put(data[3]);
		bb.position(0);
		short shortVal = bb.getShort();
		int intVal = shortVal >= 0 ? shortVal : 0x10000 + shortVal;
		if (intVal != bigBlockNumber) {
			fail("DATA Packet didn't store two-byte numbers correctly");
		}
	}

	@Test
	public void testDatagramPacketCanBeMadeOfJustZeroes() {
		int aBlockNumber = 4;
		byte[] zeroData = new byte[] { 0, 0, 0 };
		DatagramPacket dataPacket = (new DataPacket(aBlockNumber, zeroData, port, address)).getDatagramPacket();
		if (dataPacket.getLength() != 4 + zeroData.length) {
			fail("DATA Packet doesn't transfer file data that's made of zeroes");
		}
	}
}
