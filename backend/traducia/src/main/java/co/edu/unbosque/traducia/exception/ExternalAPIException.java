package co.edu.unbosque.traducia.exception;

public class ExternalAPIException extends RuntimeException {
	
private static final long serialVersionUID = 1L;
	
	private final int statusCode;
	
	public ExternalAPIException(int statusCode) {
        super("Error al consumir API externa. HTTP: " + statusCode);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
