package app;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import app.exceptions.DiskFullException;

/** 
 * @author Shawn Morreau - 100975400
 * @date 2016/09/28
 */
public class FileParser {
	 
	public static byte[][] readFileBytes(String mode, String path) 
	throws FileNotFoundException,IOException, SecurityException {
		
		ArrayList<byte[]> fileBytes = new ArrayList<byte[]>();
		//this should separate the filename from the path given
		File file = new File(path);
		String fileName = file.getAbsolutePath();
		Path p = Paths.get(path);
		if(Files.notExists(p)){
			throw new FileNotFoundException(path+" is not found.");
		}
		if(!Files.isReadable(p)){
			System.out.println("I'M BEING THROWN POINTLESSLY");
			throw new SecurityException(Definitions.ACCESS_VIOLATION_ERROR);
		}
		/*
		 * read blocks of 512 and put into an array of arrays
		 * if the file size is a multiple of 512 there will be an empty block stored
		 */
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName));
		byte[] data = new byte[512];
		int n;
		while ((n = in.read(data)) != -1) {
			if (n < 512) { // If it's smaller than 512 bytes, use a smaller byte array
				byte[] smallerArray = new byte[n];
				System.arraycopy(data, 0, smallerArray, 0, n);
				fileBytes.add(smallerArray);
			} else {
				fileBytes.add(data);
			}
			data = new byte[512];
		}
		in.close();

		// Let's store the length of the file so far
		int length = fileBytes.size();
		
		// Create an empty array when the fileBytes were empty or if they're a multiple of 512
		if (fileBytes.size() == 0 || fileBytes.get(length - 1).length == 512) {
			fileBytes.add(new byte[0]);
			++length;
		}
		
		// Copy it from the ArrayList to a real array of the correct length
		byte[][] bytesToReturn = new byte[length][];
		fileBytes.toArray(bytesToReturn);
		return bytesToReturn;
	}
	
	public static void writeFileBytes (String mode, byte[] bytes, String path)
		throws FileNotFoundException,IOException, DiskFullException, SecurityException {
		//not sure if we should write to the file byte by byte or just convert to string.
			String text = new String(bytes);
			
			// Open the file
			File file = new File(path).getAbsoluteFile();
			File dir = file.getParentFile();
			
			//check if directory can be written to
			if(!dir.canWrite()){
				throw new SecurityException(Definitions.ACCESS_VIOLATION_ERROR);
			}
			// Check if there is enough disk space to perform the write operation
			if (dir.getParentFile().getUsableSpace() < bytes.length) {
				throw new DiskFullException("Disk Full. Can't write to " + path);
			}
			
			// Write the data
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter buf = new BufferedWriter(fw);
			buf.write(text);

			// Close the file
			buf.close();
	}
}
