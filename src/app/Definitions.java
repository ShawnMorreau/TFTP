package app;

public class Definitions {
	// Numerical constants
	public static final int ACK_SIZE			         = 4;	// Size of an acknowledgement packet
	public static final int MAX_DATA_PACKET_SIZE 	     = 516;	// Maximum size of a data packet
	public static final int SERVER_LISTEN_PORT 		     = 69;	// Port used by the server to listen to client requests
	public static final int ERROR_SIMULATOR_LISTEN_PORT  = 23; // Port used by the error simulator
	public static final int MAX_PORT_TIMEOUT             = 1000000000; // The longest amount of time we can wait for packets
	
	// Error messages
	public static final String CREATE_SOCKET_ERROR 		 = "Create socket operation failed";
	public static final String SEND_DATAGRAM_ERROR 		 = "Send datagram operation failed";
	public static final String RECEIVE_DATAGRAM_ERROR 	 = "Receive datagram operation failed";
	public static final String WRONG_PACKET_TYPE_ERROR 	 = "Wrong datagram type encountered";
	public static final String WRONG_BLOCK_NUMBER_ERROR  = "Wrong block number encountered in acknowledgement packet";
	public static final String UNKNOWN_ERROR			 = "Unknown error encountered";
	public static final String FILE_ALREADY_EXISTS		 = "File already exists";
	public static final String FILE_NOT_FOUND_ERROR		 = "File not found";
	public static final String SERVER_START_MESSAGE		 = "Server module started";
	public static final String TRANSFER_FINISHED		 = "Transfer operation finished";
	public static final String ACCESS_VIOLATION_ERROR	 = "Invalid permissions";
	public static final String DISK_FULL_ERROR 			 = "Disk is full";
	public static final String INVALID_PATH              = "Invalid path";
	public static final String DATA_PACKET_OUT_OF_ORDER  = "Received out of order data packet";
	public static final String ACK_PACKET_OUT_OF_ORDER   = "Received out of order ack packet";
	public static final String IO_ERROR                  = "An IO error occurred";
	public static final String RECEIVED_CORRUPTED_PACKET = "Corrupted packet received";
	
	private Definitions(){}
}
