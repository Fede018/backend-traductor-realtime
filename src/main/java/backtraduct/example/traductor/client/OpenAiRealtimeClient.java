package backtraduct.example.traductor.client;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import backtraduct.example.traductor.dto.RealtimeSessionResponse;
import backtraduct.example.traductor.exception.RealtimeSessionException;

@Component
public class OpenAiRealtimeClient {

	private static final String SESSIONS_URL = "https://api.openai.com/v1/realtime/sessions";

	private final RestClient restClient;
	private final String model;
	private final String voice;

	public OpenAiRealtimeClient(
			@Value("${openai.api-key}") String apiKey,
			@Value("${openai.realtime.model}") String model,
			@Value("${openai.realtime.voice}") String voice) {
		this.restClient = RestClient.builder()
				.baseUrl(SESSIONS_URL)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.build();
		this.model = model;
		this.voice = voice;
	}

	public RealtimeSessionResponse createEphemeralSession(String sourceLanguage, String targetLanguage) {
		try {
			String instructions = "Sos un intérprete en tiempo real. No conversás, no respondés preguntas, no agregás comentarios propios. "
					+ "Tu única tarea es escuchar lo que se dice en " + sourceLanguage
					+ " y decir en voz alta, inmediatamente, la traducción literal en " + targetLanguage + ". Nada más.";

			SessionRequest request = new SessionRequest(model, voice, new TurnDetection("server_vad"), instructions);

			SessionResponse response = restClient.post()
					.contentType(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(SessionResponse.class);

			if (response == null || response.clientSecret() == null || response.clientSecret().value() == null) {
				throw new RealtimeSessionException("Respuesta vacía de OpenAI");
			}

			return new RealtimeSessionResponse(
					response.clientSecret().value(),
					Instant.ofEpochSecond(response.clientSecret().expiresAt()),
					response.model());
		} catch (RealtimeSessionException e) {
			throw e;
		} catch (Exception e) {
			throw new RealtimeSessionException("Fallo al crear sesión Realtime con OpenAI", e);
		}
	}

	private record SessionRequest(
			String model,
			String voice,
			@JsonProperty("turn_detection") TurnDetection turnDetection,
			String instructions) {}

	private record TurnDetection(String type) {}

	private record SessionResponse(String model, @JsonProperty("client_secret") ClientSecret clientSecret) {}

	private record ClientSecret(String value, @JsonProperty("expires_at") long expiresAt) {}
}
