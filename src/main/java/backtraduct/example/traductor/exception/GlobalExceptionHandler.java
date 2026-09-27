package backtraduct.example.traductor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ProblemDetail handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo supera el tamaño máximo permitido (10MB)");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(SttException.class)
	public ProblemDetail handleSttException(SttException ex) {
		ex.printStackTrace();
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo transcribir el audio");
	}

	@ExceptionHandler(TranslationException.class)
	public ProblemDetail handleTranslationException(TranslationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo traducir el texto");
	}

	@ExceptionHandler(TtsException.class)
	public ProblemDetail handleTtsException(TtsException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el audio de la traducción");
	}

	@ExceptionHandler(RealtimeSessionException.class)
	public ProblemDetail handleRealtimeSessionException(RealtimeSessionException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo crear la sesión de tiempo real");
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleException(Exception ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}
}
