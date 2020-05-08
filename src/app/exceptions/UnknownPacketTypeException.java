package app.exceptions;

public class UnknownPacketTypeException extends Exception {

	private static final long serialVersionUID = 1L;
	private String message;
	
	public UnknownPacketTypeException(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}

}
