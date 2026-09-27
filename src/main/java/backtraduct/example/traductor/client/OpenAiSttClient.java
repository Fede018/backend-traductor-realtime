package backtraduct.example.traductor.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import backtraduct.example.traductor.exception.SttException;

@Component
public class OpenAiSttClient {

	private static final String TRANSCRIPTIONS_URL = "https://api.openai.com/v1/audio/transcriptions";

	private final RestClient restClient;
	private final String model;

	public OpenAiSttClient(
			@Value("${openai.api-key}") String apiKey,
			@Value("${openai.stt.model}") String model) {
		this.restClient = RestClient.builder()
				.baseUrl(TRANSCRIPTIONS_URL)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.build();
		this.model = model;
	}

	public String transcribe(byte[] audioBytes, String filename, String languageHint) {
		try {
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", toResource(audioBytes, filename));
			body.add("model", model);
			body.add("language", languageHint);

			TranscriptionResponse response = restClient.post()
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.body(body)
					.retrieve()
					.body(TranscriptionResponse.class);

			if (response == null || response.text() == null) {
				throw new SttException("Respuesta vacía de OpenAI", null);
			}
			return response.text();
		} catch (SttException e) {
			throw e;
		} catch (Exception e) {
			throw new SttException("Fallo al transcribir audio con OpenAI", e);
		}
	}

	private ByteArrayResource toResource(byte[] audioBytes, String filename) {
		String resolvedFilename = filename != null ? filename : "audio";
		return new ByteArrayResource(audioBytes) {
			@Override
			public String getFilename() {
				return resolvedFilename;
			}
		};
	}

	private record TranscriptionResponse(String text) {}
}
