package backtraduct.example.traductor.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import backtraduct.example.traductor.exception.TtsException;

@Component
public class OpenAiTtsClient {

	private static final String SPEECH_URL = "https://api.openai.com/v1/audio/speech";

	private final RestClient restClient;
	private final String model;
	private final String voice;

	public OpenAiTtsClient(
			@Value("${openai.api-key}") String apiKey,
			@Value("${openai.tts.model}") String model,
			@Value("${openai.tts.voice}") String voice) {
		this.restClient = RestClient.builder()
				.baseUrl(SPEECH_URL)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.build();
		this.model = model;
		this.voice = voice;
	}

	public byte[] synthesize(String text) {
		try {
			SpeechRequest request = new SpeechRequest(model, voice, text);

			byte[] response = restClient.post()
					.contentType(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(byte[].class);

			if (response == null || response.length == 0) {
				throw new TtsException("Respuesta vacía de OpenAI");
			}
			return response;
		} catch (TtsException e) {
			throw e;
		} catch (Exception e) {
			throw new TtsException("Fallo al sintetizar audio con OpenAI", e);
		}
	}

	private record SpeechRequest(String model, String voice, String input) {}
}
