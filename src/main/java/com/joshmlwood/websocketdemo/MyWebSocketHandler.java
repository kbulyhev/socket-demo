package com.joshmlwood.websocketdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshmlwood.websocketdemo.dto.MessagePayloadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;


public class MyWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyWebSocketHandler.class);

    private List<WebSocketSession> sessions = new ArrayList<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println(message.getPayload());
        ObjectMapper objectMapper = new ObjectMapper();
        MessagePayloadDTO messagePayloadDTO = objectMapper.readValue(message.getPayload(), MessagePayloadDTO.class);
        if (messagePayloadDTO.getMessageType().equals("greeting")) {
            messagePayloadDTO.setMessage(messagePayloadDTO.getName() + " has joined to the chat");
            String json = objectMapper.writeValueAsString(messagePayloadDTO);

            message = new TextMessage(json);
        }

        session.sendMessage(message);

        sendMesssages(message, session);
    }

    private void sendMesssages(TextMessage textMessage, WebSocketSession session) {
        sessions.stream()
                .filter(sessionTarget -> !sessionTarget.getId().equals(session.getId()))
                .forEach(sessionTarget -> {
                    try {
                        sessionTarget.sendMessage(textMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        InetSocketAddress clientAddress = session.getRemoteAddress();
        HttpHeaders handshakeHeaders = session.getHandshakeHeaders();

        //the messages will be broadcasted to all users.
        logger.info("Accepted connection from: {}:{}", clientAddress.getHostString(), clientAddress.getPort());
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessions.remove(session);
    }
}
