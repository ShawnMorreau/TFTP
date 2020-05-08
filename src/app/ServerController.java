package app;

import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is responsible for managing the user's input to initialize, terminate
 * and set operational properties for the server class
 * @author SYSC3303 Project Team 12
 *
 */
public class ServerController {

	private Server server;	// Server model
	
	public ServerController() {
		try {
			server = new Server();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * This method is used to manage the flow of execution
	 * of the server
	 * @throws InterruptedException 
	 */
	public void start(boolean verbose) throws InterruptedException{
		// Get a working directory path from the user and validate its existence
		while (true) {
			String path = GUI.getWorkingDirectory();
			String checker = path;
			if(path.isEmpty() || checker.replaceAll("\\s", "").isEmpty()){
				System.out.println("Invalid path");
				Thread.sleep(200);
			}
			else if (Files.isDirectory(Paths.get(path))) {
				this.server.setWorkingDirectory(path);
				break;
			} else {
				System.out.println("Invalid path.");
			}
		}
		 
		boolean running = true;	// Flag used to determine if the server should continue execution
		boolean serverActive = false;
		
		while (running) {
			GUI.printServerInfo(server);
			if(!serverActive) {
				server.start();
				server.setVerbose(verbose);
				serverActive = true;
			}
			Thread.sleep(200);
			
			int choice = GUI.openServerMenu();
			
			switch (choice) {
			case 1:
				server.setVerbose(!server.isVerbose());
				break;
			case 2:
				server.close();
				running = false;
				Thread.sleep(200);
				break;
			default:
				break;
			}
		}
	}
	
	public static void main (String[] args) throws InterruptedException {
		// By default, testing and verbose mode are off
		boolean verbose = false;
		
		// Go through the arguments
		for (String arg : args) {
			switch (arg) {
				case "--verbose":
					verbose = true;
					break;
			}
		}

		ServerController controller = new ServerController();
		controller.start(verbose);
	}
	
}
