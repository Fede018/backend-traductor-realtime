package backtraduct.example.traductor.controller;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import backtraduct.example.traductor.dto.AudioUploadResponse;
import backtraduct.example.traductor.service.AudioService;

@RestController
public class AudioController {

	private final AudioService audioService;

	public AudioController(AudioService audioService) {
		this.audioService = audioService;
	}

	@PostMapping("/api/audio")
	public ResponseEntity<AudioUploadResponse> uploadAudio(
			@RequestParam(value = "audio", required = false) MultipartFile audio,
			@RequestParam(required = false) String sourceLanguage,
			@RequestParam(required = false) String targetLanguage
	) {
		if (audio == null || audio.isEmpty()) {
			throw new IllegalArgumentException("El archivo de audio no puede estar vacío");
		}

		byte[] audioBytes;
		try {
			audioBytes = audio.getBytes();
		} catch (IOException e) {
			throw new UncheckedIOException("No se pudo leer el archivo de audio", e);
		}

		AudioUploadResponse response = audioService.process(audioBytes, audio.getContentType(), sourceLanguage, targetLanguage);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
