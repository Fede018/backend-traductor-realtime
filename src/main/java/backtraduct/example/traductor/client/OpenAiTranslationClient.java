package backtraduct.example.traductor.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import backtraduct.example.traductor.exception.TranslationException;

@Component
public class OpenAiTranslationClient {

	private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

	private final RestClient restClient;
	private final String model;

	public OpenAiTranslationClient(
			@Value("${openai.api-key}") String apiKey,
			@Value("${openai.translation.model}") String model) {
		this.restClient = RestClient.builder()
				.baseUrl(CHAT_COMPLETIONS_URL)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.build();
		this.model = model;
	}

	public String translate(String text, String sourceLanguage, String targetLanguage) {
		try {
			String systemPrompt = "Traducí literalmente el siguiente texto de " + sourceLanguage + " a "
					+ targetLanguage
					+ ". Devolvé únicamente la traducción, sin comillas, sin explicaciones, sin agregar nada que no esté en el texto original.";

			ChatCompletionRequest request = new ChatCompletionRequest(
					model,
					List.of(
							new ChatMessage("system", systemPrompt),
							new ChatMessage("user", text)));

			ChatCompletionResponse response = restClient.post()
					.contentType(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(ChatCompletionResponse.class);

			if (response == null || response.choices() == null || response.choices().isEmpty()
					|| response.choices().get(0).message() == null
					|| response.choices().get(0).message().content() == null) {
				throw new TranslationException("Respuesta vacía de OpenAI");
			}
			return response.choices().get(0).message().content();
		} catch (TranslationException e) {
			throw e;
		} catch (Exception e) {
			throw new TranslationException("Fallo al traducir texto con OpenAI", e);
		}
	}

	private record ChatCompletionRequest(String model, List<ChatMessage> messages) {}

	private record ChatMessage(String role, String content) {}

	private record ChatCompletionResponse(List<Choice> choices) {}

	private record Choice(ChatMessage message) {}
}
