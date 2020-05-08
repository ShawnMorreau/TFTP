package app;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import app.packets.Packet;

/**
 * This class controls all of the drawing to the screen
 * 
 * @author SYSC3303 Project Team 12
 */
public class GUI {
	
	// This is a static class and thus cannot be instantiated
	private GUI(){}
	
	/**
	 * A scanner for reading the user's input.
	 */
	private static Scanner input = new Scanner(System.in);
	
	/**
	 * Gets the user's filename input.
	 * 
	 * @return The file path as a string of a file
	 */
	public static String getFilename() {
		// Ask the user nicely which file they'd like to send or receive
		System.out.println("TFTP");
		System.out.println("====");
		System.out.println();
		System.out.println("Please type a filename");
		
		// Get that info from the user
		System.out.print("> ");
		input = new Scanner(System.in);
		return input.nextLine();
	}
	
	/**
	 * Gets the user's intended working directory for the program.
	 * 
	 * @return The file path as a string of the user's working directory
	 */
	public static String getWorkingDirectory() {
		while (true) {
			// Ask the user nicely where they'd like to work
			System.out.println("TFTP");
			System.out.println("====");
			System.out.println();
			System.out.println("Please type the full working directory path");
			
			// Get that info from the user
			System.out.print("> ");
			input = new Scanner(System.in);
			return input.nextLine();
		}
	}

	/**
	 * Gets the user's choice of a particular menu item for the client application.
	 * 
	 * @return The integer value of the selected choice in the menu
	 */
	public static int openClientMenu() {
		// Print a menu
		System.out.println("TFTP");
		System.out.println("====");
		System.out.println();
		System.out.println("Please select one of the following options:");
		System.out.println("1) Read a file");
		System.out.println("2) Write a file");
		System.out.println("3) Toggle test mode");
		System.out.println("4) Toggle verbose mode");
		System.out.println("5) Change server IP address");
		System.out.println("6) Exit");
		
		// Get input from the user
		while (true) {
			System.out.print("> ");
			input = new Scanner(System.in);
			String number = input.nextLine();
			try {
				int n = Integer.parseInt(number);
				if (n <= 6 && n > 0) {
					return n;
				}
			} catch (NumberFormatException e) {}
			System.out.println("Invalid option selected");
		}
	}

	/**
	 * Gets the user's choice of a particular menu item for the server application.
	 * 
	 * @return The integer value of the selected choice in the menu
	 */
	public static int openServerMenu () {
		System.out.println();
		System.out.println("1. Change display mode");
		System.out.println("2. Shut down");

		// Loop until a valid menu item is selected
		while (true) {
			System.out.print("> ");
			input = new Scanner(System.in);
			String number = input.nextLine();
			try {
				int n = Integer.parseInt(number);
				if (n <= 2 && n > 0) {
					return n;
				}
			} catch (NumberFormatException e) {}
			System.out.println("Invalid option selected");
		}
	}
	
	/**
	 * This method is used to print a packet's content
	 * @param packet - packet to print
	 * @param receive - boolean
	 * 				  - true: print information for packet received
	 * 				  - false: print information for packet sent
	 */
	public static void printPacketInfo(Packet packet, boolean received) {
		if (! Settings.verbose) return;
		System.out.println();
		if (received) {
			System.out.println("Packet received from: " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
			System.out.println("---");
		} else {
			System.out.println("Packet sent to: " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
			System.out.println("---");
		}
		System.out.println(packet);
		System.out.println();
	}
	
	/**
	 * This method is used to display error information to the console
	 * @param error - error to display
	 */
	public static void printError(String error) {
		if (! Settings.verbose) return;
		System.out.println();
		System.out.println("Error: " + error);
	}
	
	/**
	 * This method is used to display a message on the console
	 * @param message - message to print
	 */
	public static void printMessage (String message) {
		System.out.println(message);
	}
	
	/**
	 * This method is used to display server information to the console
	 * @param server - server to display
	 */
	public static void printServerInfo (Server server){
		System.out.println();
		System.out.println(server);
	}
	
	/**
	 * This method is used to read integer input from the console
	 * @return integer read from the console
	 */
	private static int getInt () {
		return input.nextInt();
	}

	public static void printPacketSent (Packet sent) {
		GUI.printPacketInfo(sent, false);
	}

	public static void printPacketReceived (Packet received) {
		GUI.printPacketInfo(received, true);
	}

	public static void printSocketOpened(int localPort) {
		System.out.println("Opened a socket on port " + localPort);
	}

	public static void printListening(int localPort) {
		System.out.println("Listening on port " + localPort);		
	}

	public static InetAddress askForIP(String message) {
		while (true) {
			System.out.println(message);
			System.out.print("> ");
			try {
				input = new Scanner(System.in);
				String ip = input.nextLine();
				if (isValid(ip)) {
					return InetAddress.getByName(ip);
				}
			} catch (UnknownHostException e) {
			}
			System.out.println("The IP address given was invalid.");
		}
	}
	
	private static boolean isValid(String ip) {
		try {
		    Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
		    Matcher matcher = pattern.matcher(ip);
		    return matcher.matches() || ip.toLowerCase().equals("localhost");
		} catch (PatternSyntaxException ex) {
		    return false;
		}
	}
	
}
