package app;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class contains the control flow for the client application.
 * 
 * @author SYSC3303 Project Team 12
 */
public class ClientController {

	/**
	 * The instance of the client model.
	 */
	private Client client;
	
	/**
	 * The main entry point of the client application.
	 * 
	 * @param args The command line arguments that this program was ran with
	 */
	public static void main(String[] args) {
		// By default, testing and verbose mode are off
		boolean testing = false, verbose = false;
		
		// Go through the arguments
		for (String arg : args) {
			switch (arg) {
				case "--testing":
					testing = true;
					break;
				case "--verbose":
					verbose = true;
					break;
			}
		}
		(new ClientController()).start(testing, verbose);
	}
	
	/**
	 * Starts the client application.
	 */
	public void start(boolean testing, boolean verbose) {
		this.client = new Client(testing, verbose);
		
		try {
			String path;
			// Get a working directory path from the user and validates its existence
			while (true) {
				path = GUI.getWorkingDirectory();
				String checker = path;
				if(path.isEmpty() || checker.replaceAll("\\s", "").isEmpty()){
					GUI.printError(Definitions.INVALID_PATH);
					Thread.sleep(200);
				}
				else if (Files.isDirectory(Paths.get(path))) {
					this.client.setWorkingDirectory(path);
					break;
				}
				else {
					GUI.printError(Definitions.INVALID_PATH);
					Thread.sleep(200);
				}
			}
			
			this.client.setServerIP(GUI.askForIP("What is the IP address of the server you would like to connect to?"));

			// Ask the user what they'd like to do
			String filename;
			while (true) {
				switch (GUI.openClientMenu()) {
					case 1: // Send Read Request
						filename = GUI.getFilename();
						client.sendReadRequest(filename);
						Thread.sleep(200);
						break;
					case 2: // Send Write Request
						while (true) {
							filename = GUI.getFilename();
							File f = new File(path + File.separator + filename);
							if(f.exists() && !f.isDirectory()) { 
								System.out.println("That exists!");
								break;
							}
							GUI.printError(Definitions.FILE_NOT_FOUND_ERROR);
						}
						client.sendWriteRequest(filename);
						Thread.sleep(200);
						break;
					case 3: // Toggle Test
						this.client.setTesting(! this.client.isTesting());
						System.out.println("Test mode is now " + 
								(this.client.isTesting() ? "enabled" : "disabled"));
						Thread.sleep(200);
						break;
					case 4: // Toggle Verbose
						this.client.setVerbose(! this.client.isVerbose());
						System.out.println("Verbose mode is now " + 
								(this.client.isVerbose() ? "enabled" : "disabled"));
						Thread.sleep(200);
						break;
					case 5: // Change IP
						this.client.setServerIP(GUI.askForIP("What is the IP address of the server you would like to connect to?"));
						Thread.sleep(200);
						break;
					case 6: // Exit
						System.out.println("Goodbye!");
						Thread.sleep(200);
						System.exit(0);
					default:
						System.out.println("Invalid option. Please enter valid input only.");
						Thread.sleep(200);
				}
			}
		} catch (InterruptedException e) { }
	}

}
