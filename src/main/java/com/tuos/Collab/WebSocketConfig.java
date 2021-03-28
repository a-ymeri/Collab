package com.tuos.Collab;

import com.tuos.Collab.collabuser.CollabUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/collab-editor").setAllowedOrigins("http://localhost:3000").withSockJS();

	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor =
						MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
					LinkedMultiValueMap headers = (LinkedMultiValueMap)message.getHeaders().get("nativeHeaders");
					String token = (String)((ArrayList) headers.get("auth")).get(0);// access authentication header(s)
					String key = "securesecuresecuresecuresecuresecuresecuresecure";
					SecretKey secureKey = Keys.hmacShaKeyFor(key.getBytes());

					Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(secureKey).build().parseClaimsJws(token);

					Claims body = claimsJws.getBody();
					String username = body.getSubject();

					var authorities = (List<Map<String, String>>) body.get("authorities");

					Set<SimpleGrantedAuthority> simpleGrantedAuthorities = authorities.stream()
							.map(m -> new SimpleGrantedAuthority(m.get("authority")))
							.collect(Collectors.toSet());
					Authentication authentication = new UsernamePasswordAuthenticationToken(
							username,
							null,
							simpleGrantedAuthorities

					);
					accessor.setUser(authentication);
					System.out.println(authentication);
				}
				return message;
			}
		});
	}

}
