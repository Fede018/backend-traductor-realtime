package backtraduct.example.traductor.dto;

import java.util.UUID;

public record AudioUploadResponse(
		UUID id,
		long receivedBytes,
		String contentType,
		String sourceLanguage,
		String targetLanguage
) {}
