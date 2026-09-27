package backtraduct.example.traductor.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import backtraduct.example.traductor.client.OpenAiSttClient;
import backtraduct.example.traductor.client.OpenAiTranslationClient;
import backtraduct.example.traductor.dto.AudioUploadResponse;
import backtraduct.example.traductor.exception.TranslationException;

@Service
public class AudioService {

	private final OpenAiSttClient openAiSttClient;
	private final OpenAiTranslationClient openAiTranslationClient;

	public AudioService(OpenAiSttClient openAiSttClient, OpenAiTranslationClient openAiTranslationClient) {
		this.openAiSttClient = openAiSttClient;
		this.openAiTranslationClient = openAiTranslationClient;
	}

	public AudioUploadResponse receive(MultipartFile audio, String sourceLanguage, String targetLanguage) {
		if (audio == null || audio.isEmpty()) {
			throw new IllegalArgumentException("El archivo de audio no puede estar vacío");
		}
		if (sourceLanguage == null || sourceLanguage.isBlank()) {
			throw new IllegalArgumentException("sourceLanguage no puede estar vacío");
		}
		if (targetLanguage == null || targetLanguage.isBlank()) {
			throw new IllegalArgumentException("targetLanguage no puede estar vacío");
		}

		String transcript = openAiSttClient.transcribe(audio, sourceLanguage);

		if (transcript.isBlank()) {
			throw new TranslationException("No se detectó voz en el audio");
		}

		String translation = openAiTranslationClient.translate(transcript, sourceLanguage, targetLanguage);

		return new AudioUploadResponse(
				UUID.randomUUID(),
				audio.getSize(),
				audio.getContentType(),
				sourceLanguage,
				targetLanguage,
				transcript,
				translation
		);
	}
}
