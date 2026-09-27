package backtraduct.example.traductor.client;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

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

	public String transcribe(MultipartFile audio, String languageHint) {
		try {
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", toResource(audio));
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

	private Resource toResource(MultipartFile audio) throws IOException {
		String filename = audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio";
		return new ByteArrayResource(audio.getBytes()) {
			@Override
			public String getFilename() {
				return filename;
			}
		};
	}

	private record TranscriptionResponse(String text) {}
}
