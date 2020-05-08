package test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.Client;
import app.Server;

public class ReadFileAcceptanceTest {

	/**
	 * The client and the server we'll use.
	 */
	private Client client;
	private Server server;
	
	/**
	 * The separate working directory names for each host.
	 */
	private String clientWorkingDirectory = "." + File.separator + "client";
	private String serverWorkingDirectory = "." + File.separator + "server";
	
	/**
	 * The test file that will be sent back and forth.
	 */
	private String testFilename = "testfile.txt";
	
	@Before
	public void setUp() throws Exception {
		// Create a folder for the each, the server and the client
		File c = new File(clientWorkingDirectory);
		File s = new File(serverWorkingDirectory);
		// In case they exist
		deleteFolderAndItsContent(c.toPath());
		deleteFolderAndItsContent(s.toPath());
		c.mkdir();
		s.mkdir();
		
		// Create a new server
		server = new Server();
		server.setVerbose(true);
		server.setWorkingDirectory(Paths.get(serverWorkingDirectory).toString());
		server.start();
		
		// Create a new client
		boolean testing = false;
		boolean verbose = false;
		client = new Client(testing, verbose);
		client.setWorkingDirectory(Paths.get(clientWorkingDirectory).toString());
	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		// Remove the folders for the client and the server
		File c = new File(clientWorkingDirectory);
		File s = new File(serverWorkingDirectory);
		deleteFolderAndItsContent(c.toPath());
		deleteFolderAndItsContent(s.toPath());

		// Close and stop the server and the client
		server.close();
		server.stop(); // Because it's a thread, we have to stop it manually
		client.close();
	}

	@Test
	public void testSendingAReadRequest() throws FileNotFoundException {
		// Create a test file in the server's folder to be read
		PrintWriter writer = new PrintWriter(serverWorkingDirectory + File.separator + testFilename);
		writer.print("This is a test file");
		writer.close();
		
		// Ensure the file doesn't exist before we receive it
		File receivedFile = new File(clientWorkingDirectory + File.separator + testFilename);
		if (receivedFile.exists()) {
			fail("The file to be read existed ahead of time");
		}

		// Send the read request with that file's path
		client.sendReadRequest(testFilename);
		
		if (!receivedFile.exists()) {
			fail("The client doesn't read files to its own working directory");
		}
	}
	
	@Test
	public void testReadingWhenFileAlreadyExists() throws FileNotFoundException {
		// The test files' contents
		String serverTestFileContents = "This is a test file";
		String clientTestFileContents = "This is another test file";
		
		// Create a test file in the server's folder to read
		PrintWriter writer = new PrintWriter(clientWorkingDirectory + File.separator + testFilename);
		writer.print(clientTestFileContents);
		writer.close();
		writer = new PrintWriter(serverWorkingDirectory + File.separator + testFilename);
		writer.print(serverTestFileContents);
		writer.close();
		
		// Send the read request with that file's path
		client.sendReadRequest(testFilename);

		// Confirm that both files exist, but make sure they're not the same (one wasn't overwritten)
		File clientFile = new File(clientWorkingDirectory + File.separator + testFilename);
		File serverFile = new File(serverWorkingDirectory + File.separator + testFilename);
		if (! clientFile.exists() || ! serverFile.exists()) {
			fail("The file wasn't transferred properly");
		}
		
		if (clientFile.length() != clientTestFileContents.length() || serverFile.length() != serverTestFileContents.length()) {
			fail("The file was overwritten on the server side");
		}
	}

	/**
	 * Deletes Folder with all of its content
	 *
	 * @param folder path to folder which should be deleted
	 */
	public static void deleteFolderAndItsContent(final Path folder) throws IOException {
		if (folder.toFile().exists()) {
		    Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
		        @Override
		        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		            Files.delete(file);
		            return FileVisitResult.CONTINUE;
		        }

		        @Override
		        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		            if (exc != null) {
		                throw exc;
		            }
		            Files.delete(dir);
		            return FileVisitResult.CONTINUE;
		        }
		    });
		}
	}

}
