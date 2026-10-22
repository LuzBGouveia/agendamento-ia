package br.com.fiap.agendamentocomia.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final PgVectorStore vectorStore;

    public ChatService(ChatClient.Builder builder, PgVectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
        this.vectorStore = vectorStore;
    }

    public String sendMessage(String message) {
        return chatClient.prompt()
                .user(u -> u
                        .text(message)
                        .media(MimeType.valueOf("application/pdf"), new ClassPathResource("doc/regras.pdf")))
                .call()
                .content();
    }
}
