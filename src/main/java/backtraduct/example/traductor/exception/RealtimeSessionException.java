package backtraduct.example.traductor.exception;

public class RealtimeSessionException extends RuntimeException {
	public RealtimeSessionException(String message) {
		super(message);
	}

	public RealtimeSessionException(String message, Throwable cause) {
		super(message, cause);
	}
}
