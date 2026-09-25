package backtraduct.example.traductor.controller;

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
			@RequestParam("audio") MultipartFile audio,
			@RequestParam String sourceLanguage,
			@RequestParam String targetLanguage
	) {
		AudioUploadResponse response = audioService.receive(audio, sourceLanguage, targetLanguage);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
