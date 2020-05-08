package test;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import app.FileParser;

public class FileParserReadTest {

	// The various file sizes we'll test the FileParser with
	private static int[] sizesOfFiles = new int[] { 0, 20, 512, 513, 1024, 512*65536-1 };
	
	@BeforeClass
	public static void setUp() throws Exception {
		// Create the files that we're going to read from and write to
		for (int size : sizesOfFiles) {
			PrintWriter writer = new PrintWriter(size + "-bytes.txt");
			for (int i = 0; i < size; ++i) {
				writer.print(" ");
			}
			writer.close();
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// Remove the files that we read from and wrote to
		for (int size : sizesOfFiles) {
			Files.deleteIfExists(Paths.get(size + "-bytes.txt"));
		}
	}

	@Test
	public void testReadFileContentsPossible() throws FileNotFoundException, IOException {
		byte[][] bytes = FileParser.readFileBytes("netascii", "20-bytes.txt");
		if (bytes[0][0] != " ".getBytes()[0]) {
			fail("FileParser isn't reading files accurately");
		}
	}
	
	@Test
	public void testReadFileContentsAreActualSizeOfFile() throws FileNotFoundException, IOException {
		byte[][] bytes = FileParser.readFileBytes("netascii", "20-bytes.txt");
		if (bytes.length != 1) {
			fail("FileParser isn't getting the right number of blocks");
		}
		if (bytes[0].length != 20) {
			fail("FileParser isn't getting the right sized blocks");
		}
	}
	
	@Test
	public void testRead512ByteFileAndSeeEmptyArray() throws FileNotFoundException, IOException {
		String someMode = "netascii";
		byte[][] bytes = FileParser.readFileBytes(someMode, "512-bytes.txt");
		if (bytes.length != 2 || bytes[0].length != 512 || bytes[1].length != 0) {
			fail("FileParser doesn't give empty block arrays for block-multiple-sized files");
		}
	}
	
	@Test
	public void testReadZeroByteFileToMakeOneEmptyBlock() throws FileNotFoundException, IOException {
		String someMode = "netascii";
		byte[][] bytes = FileParser.readFileBytes(someMode, "0-bytes.txt");
		if (bytes.length != 1 || bytes[0].length != 0) {
			fail("FileParser doesn't give empty blocks for zero-length files");
		}
	}
	
	@Test
	public void testReadMultiSizedArraysWithOddSizedFile() throws FileNotFoundException, IOException {
		String someMode = "netascii";
		byte[][] bytes = FileParser.readFileBytes(someMode,  "513-bytes.txt");
		if (bytes.length != 2) {
			fail("FileParser finds incorrect number of blocks for 513-byte file");
		}
		if (bytes[0].length != 512 || bytes[1].length != 1) {
			fail("FileParser doesn't make non-512-byte arrays for smaller blocks");
		}
	}
	
	@Test
	public void testRead1024ByteFile() throws FileNotFoundException, IOException {
		String someMode = "netascii";
		byte[][] bytes = FileParser.readFileBytes(someMode,  "1024-bytes.txt");
		if (bytes.length != 3) {
			fail("FileParser finds incorrect number of blocks for 1024-byte file");
		}
		if (bytes[0].length != 512 || bytes[1].length != 512 || bytes[2].length != 0) {
			fail("FileParser doesn't add empty arrays for 1024-byte files");
		}
	}

	@Test
	public void testReadBiggestFilePossible() throws FileNotFoundException, IOException {
		String someMode = "netascii";
		byte[][] bytes = FileParser.readFileBytes(someMode, (512 * 65536 - 1) + "-bytes.txt");
		if (bytes.length != 65536) {
			System.out.println(bytes.length);
			fail("FileParser finds incorrect number of blocks for a max-sized file");
		}
		if (bytes[65535].length != 511) { // Because one more byte and we'd need an empty block
			fail("FileParser didn't fill up all blocks for a max-sized file");
		}

	}
}
