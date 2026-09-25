package backtraduct.example.traductor.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import backtraduct.example.traductor.dto.AudioUploadResponse;

@Service
public class AudioService {

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

		return new AudioUploadResponse(
				UUID.randomUUID(),
				audio.getSize(),
				audio.getContentType(),
				sourceLanguage,
				targetLanguage
		);
	}
}
