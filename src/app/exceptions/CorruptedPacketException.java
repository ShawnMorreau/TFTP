package app.exceptions;

public class CorruptedPacketException extends Exception {

	private static final long serialVersionUID = 1L;
	private String message;
	
	public CorruptedPacketException(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}
