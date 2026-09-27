package backtraduct.example.traductor.dto;

import java.time.Instant;

public record RealtimeSessionResponse(
		String clientSecret,
		Instant expiresAt,
		String model
) {}
