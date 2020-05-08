package test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Test;

import app.FileParser;
import app.exceptions.DiskFullException;

public class FileParserWriteTest {

	private String testFilename = "test.txt";

	private byte[] readFile(String filename) throws IOException {
		File file = new File(filename);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		return data;
	}
	
	@After
	public void tearDown() throws Exception {
		Files.deleteIfExists(Paths.get(testFilename));
	}

	@Test
	public void testWriteWorks() throws FileNotFoundException, IOException, SecurityException, DiskFullException {
		String someMode = "netascii";
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
		FileParser.writeFileBytes(someMode, bytes, Paths.get(testFilename).toString());
		
		byte[] contents = readFile(testFilename);
		if (bytes.length != contents.length) {
			fail("FileParser doesn't write all given bytes to a file");
		}
		for (int i=0;i<bytes.length; ++i) {
			if (bytes[i] != contents[i]) {
				fail("FileParser doesn't write the correct data to a file");
			}
		}
	}
	
	@Test
	public void testWriteSeveralTimes() throws FileNotFoundException, IOException, SecurityException, DiskFullException {
		String someMode = "netascii";
		byte[] bytes = new byte[] { 1, 2, 3 };
		FileParser.writeFileBytes(someMode, bytes, Paths.get(testFilename).toString());
		
		byte[] firstContents = readFile(testFilename);
		if (bytes.length != firstContents.length) {
			fail("FileParser doesn't write all given bytes to a file");
		}
		
		byte[] moreBytes = new byte[] { 4, 5, 6 };
		FileParser.writeFileBytes(someMode, moreBytes, Paths.get(testFilename).toString());
		
		byte[] secondContents = readFile(testFilename);
		if (bytes.length + moreBytes.length != secondContents.length) {
			fail("FileParser doesn't add to existing files when given a byte array");
		}
	}
	
	@Test
	public void testWriteEmptyFile() throws FileNotFoundException, IOException, SecurityException, DiskFullException {
		String someMode = "netascii";
		byte[] bytes = new byte[0];
		
		FileParser.writeFileBytes(someMode, bytes, Paths.get(testFilename).toString());
		
		byte[] contents = readFile(testFilename);
		if (contents.length != 0) {
			fail("FileParser doesn't create files when given zero length arrays");
		}
		
	}

}
