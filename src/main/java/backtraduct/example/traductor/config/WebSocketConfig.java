package backtraduct.example.traductor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import backtraduct.example.traductor.websocket.AudioStreamHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final AudioStreamHandler audioStreamHandler;

	public WebSocketConfig(AudioStreamHandler audioStreamHandler) {
		this.audioStreamHandler = audioStreamHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(audioStreamHandler, "/ws/audio")
				.setAllowedOrigins("http://localhost:5173");
	}
}
