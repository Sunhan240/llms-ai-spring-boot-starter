package com.eastrobot.arch.llms.chat.memory;

import com.eastrobot.arch.llms.chat.messages.Message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The InMemoryChatMemory class is an implementation of the ChatMemory interface that
 * represents an in-memory storage for chat conversation history.
 * <p>
 * This class stores the conversation history in a ConcurrentHashMap, where the keys are
 * the conversation IDs and the values are lists of messages representing the conversation
 * history.
 *
 * @author Christian Tzolov
 * @see ChatMemory
 * @since 1.0.0 M1
 */
public class InMemoryChatMemory implements ChatMemory {

    Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        this.conversationHistory.putIfAbsent(conversationId, new LinkedList<>());
        List<Message> history = this.conversationHistory.get(conversationId);
        if (messages.size() > 20) messages = messages.subList(0, 20);
        if (history.size() + messages.size() >= 5)
            history.subList(0, messages.size()).clear();
        history.addAll(messages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> all = this.conversationHistory.get(conversationId);
        return all != null ? (lastN <= 0 ? all : all.stream().skip(Math.max(0, all.size() - lastN)).collect(Collectors.toList())) : new ArrayList<>();
    }

    @Override
    public void clear(String conversationId) {
        this.conversationHistory.remove(conversationId);
    }

}