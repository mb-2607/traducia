package co.edu.unbosque.traducia.exception;

public class SelfActionNotAllowedException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public SelfActionNotAllowedException() {
		super("No puedes realizar esta acción sobre tu propia cuenta");
	}

	public SelfActionNotAllowedException(String message) {
		super(message);
	}

}
