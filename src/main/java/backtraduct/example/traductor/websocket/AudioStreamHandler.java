package backtraduct.example.traductor.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import backtraduct.example.traductor.dto.AudioUploadResponse;
import backtraduct.example.traductor.service.AudioService;

@Component
public class AudioStreamHandler extends BinaryWebSocketHandler {

	private static final long MAX_AUDIO_BYTES = 10L * 1024 * 1024;

	private final AudioService audioService;
	private final ObjectMapper objectMapper = JsonMapper.builder().build();
	private final Map<String, SessionState> sessions = new ConcurrentHashMap<>();

	public AudioStreamHandler(AudioService audioService) {
		this.audioService = audioService;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessions.put(session.getId(), new SessionState());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		sessions.remove(session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		SessionState state = sessions.get(session.getId());
		JsonNode json = objectMapper.readTree(message.getPayload());
		String type = json.path("type").asText();

		switch (type) {
			case "start" -> {
				state.reset();
				state.sourceLanguage = json.path("sourceLanguage").asText(null);
				state.targetLanguage = json.path("targetLanguage").asText(null);
			}
			case "stop" -> handleStop(session, state);
			default -> sendError(session, "Tipo de mensaje desconocido: " + type);
		}
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		SessionState state = sessions.get(session.getId());
		byte[] chunk = message.getPayload().array();

		if (state.buffer.size() + chunk.length > MAX_AUDIO_BYTES) {
			sendError(session, "El audio supera el límite de 10MB");
			state.reset();
			return;
		}

		state.buffer.writeBytes(chunk);
	}

	private void handleStop(WebSocketSession session, SessionState state) {
		try {
			AudioUploadResponse response = audioService.process(
					state.buffer.toByteArray(),
					"audio/webm",
					state.sourceLanguage,
					state.targetLanguage
			);
			sendResult(session, response);
		} catch (Exception e) {
			e.printStackTrace();
			sendError(session, e.getMessage());
		} finally {
			state.reset();
		}
	}

	private void sendResult(WebSocketSession session, AudioUploadResponse response) {
		Map<String, Object> payload = objectMapper.convertValue(response, Map.class);
		payload.put("type", "result");
		sendMessage(session, objectMapper.writeValueAsString(payload));
	}

	private void sendError(WebSocketSession session, String detail) {
		Map<String, Object> payload = Map.of("type", "error", "detail", detail != null ? detail : "Error desconocido");
		sendMessage(session, objectMapper.writeValueAsString(payload));
	}

	private void sendMessage(WebSocketSession session, String payload) {
		try {
			session.sendMessage(new TextMessage(payload));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static class SessionState {
		private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		private String sourceLanguage;
		private String targetLanguage;

		private void reset() {
			buffer = new ByteArrayOutputStream();
		}
	}
}
