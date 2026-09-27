package backtraduct.example.traductor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import backtraduct.example.traductor.client.OpenAiRealtimeClient;
import backtraduct.example.traductor.dto.RealtimeSessionResponse;

@RestController
public class RealtimeController {

	private final OpenAiRealtimeClient realtimeClient;

	public RealtimeController(OpenAiRealtimeClient realtimeClient) {
		this.realtimeClient = realtimeClient;
	}

	@PostMapping("/api/realtime/session")
	public ResponseEntity<RealtimeSessionResponse> createSession(@RequestBody RealtimeSessionRequest request) {
		RealtimeSessionResponse response = realtimeClient.createEphemeralSession(
				request.sourceLanguage(), request.targetLanguage());
		return ResponseEntity.ok(response);
	}

	public record RealtimeSessionRequest(String sourceLanguage, String targetLanguage) {}
}
