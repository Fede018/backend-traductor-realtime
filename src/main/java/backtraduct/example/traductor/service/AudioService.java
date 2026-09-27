package backtraduct.example.traductor.service;

import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;

import backtraduct.example.traductor.client.OpenAiSttClient;
import backtraduct.example.traductor.client.OpenAiTranslationClient;
import backtraduct.example.traductor.client.OpenAiTtsClient;
import backtraduct.example.traductor.dto.AudioUploadResponse;
import backtraduct.example.traductor.exception.TranslationException;
import backtraduct.example.traductor.exception.TtsException;

@Service
public class AudioService {

	private final OpenAiSttClient openAiSttClient;
	private final OpenAiTranslationClient openAiTranslationClient;
	private final OpenAiTtsClient openAiTtsClient;

	public AudioService(
			OpenAiSttClient openAiSttClient,
			OpenAiTranslationClient openAiTranslationClient,
			OpenAiTtsClient openAiTtsClient) {
		this.openAiSttClient = openAiSttClient;
		this.openAiTranslationClient = openAiTranslationClient;
		this.openAiTtsClient = openAiTtsClient;
	}

	public AudioUploadResponse process(byte[] audioBytes, String contentType, String sourceLanguage, String targetLanguage) {
		if (audioBytes == null || audioBytes.length == 0) {
			throw new IllegalArgumentException("El archivo de audio no puede estar vacío");
		}
		if (sourceLanguage == null || sourceLanguage.isBlank()) {
			throw new IllegalArgumentException("sourceLanguage no puede estar vacío");
		}
		if (targetLanguage == null || targetLanguage.isBlank()) {
			throw new IllegalArgumentException("targetLanguage no puede estar vacío");
		}

		String transcript = openAiSttClient.transcribe(audioBytes, "audio.webm", sourceLanguage);

		if (transcript.isBlank()) {
			throw new TranslationException("No se detectó voz en el audio");
		}

		String translation = openAiTranslationClient.translate(transcript, sourceLanguage, targetLanguage);

		if (translation.isBlank()) {
			throw new TtsException("No hay texto traducido para sintetizar");
		}

		byte[] translationAudio = openAiTtsClient.synthesize(translation);
		String translationAudioBase64 = Base64.getEncoder().encodeToString(translationAudio);

		return new AudioUploadResponse(
				UUID.randomUUID(),
				(long) audioBytes.length,
				contentType,
				sourceLanguage,
				targetLanguage,
				transcript,
				translation,
				translationAudioBase64
		);
	}
}
