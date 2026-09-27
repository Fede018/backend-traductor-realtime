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

	private static final String CLIENT_SECRETS_URL = "https://api.openai.com/v1/realtime/client_secrets";

	private final RestClient restClient;
	private final String model;
	private final String voice;

	public OpenAiRealtimeClient(
			@Value("${openai.api-key}") String apiKey,
			@Value("${openai.realtime.model}") String model,
			@Value("${openai.realtime.voice}") String voice) {
		this.restClient = RestClient.builder()
				.baseUrl(CLIENT_SECRETS_URL)
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

			SessionSpec session = new SessionSpec(
					"realtime",
					model,
					instructions,
					new AudioSpec(new OutputAudio(voice), new InputAudio(new TurnDetection("server_vad"))));

			ClientSecretResponse response = restClient.post()
					.contentType(MediaType.APPLICATION_JSON)
					.body(new ClientSecretRequest(session))
					.retrieve()
					.body(ClientSecretResponse.class);

			if (response == null || response.value() == null || response.session() == null) {
				throw new RealtimeSessionException("Respuesta vacía de OpenAI");
			}

			return new RealtimeSessionResponse(
					response.value(),
					Instant.ofEpochSecond(response.expiresAt()),
					response.session().model());
		} catch (RealtimeSessionException e) {
			throw e;
		} catch (Exception e) {
			throw new RealtimeSessionException("Fallo al crear sesión Realtime con OpenAI", e);
		}
	}

	private record ClientSecretRequest(SessionSpec session) {}

	private record SessionSpec(String type, String model, String instructions, AudioSpec audio) {}

	private record AudioSpec(OutputAudio output, InputAudio input) {}

	private record OutputAudio(String voice) {}

	private record InputAudio(@JsonProperty("turn_detection") TurnDetection turnDetection) {}

	private record TurnDetection(String type) {}

	private record ClientSecretResponse(
			String value,
			@JsonProperty("expires_at") long expiresAt,
			SessionInfo session) {}

	private record SessionInfo(String model) {}
}
