package app.exceptions;

public class DiskFullException extends Exception{

	private static final long serialVersionUID = 1L;
	private String message;
	
	public DiskFullException(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
	
}

