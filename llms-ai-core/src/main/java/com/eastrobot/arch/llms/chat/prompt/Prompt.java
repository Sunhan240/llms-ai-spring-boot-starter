/*
 * Copyright 2023 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eastrobot.arch.llms.chat.prompt;

import com.eastrobot.arch.llms.chat.messages.*;
import com.eastrobot.arch.llms.model.ModelRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The Prompt class represents a prompt used in AI model requests. A prompt consists of
 * one or more messages and additional chat options.
 *
 * @author Mark Pollack
 * @author luocongqiu
 */
public class Prompt implements ModelRequest<List<Message>> {

	private final List<Message> messages;

	private ChatOptions chatOptions;

	public Prompt(String contents) {
		this(new UserMessage(contents));
	}

	public Prompt(Message message) {
		this(Collections.singletonList(message));
	}

	public Prompt(List<Message> messages) {
		this.messages = messages;
	}

	public Prompt(String contents, ChatOptions chatOptions) {
		this(new UserMessage(contents), chatOptions);
	}

	public Prompt(Message message, ChatOptions chatOptions) {
		this(Collections.singletonList(message), chatOptions);
	}

	public Prompt(List<Message> messages, ChatOptions chatOptions) {
		this.messages = messages;
		this.chatOptions = chatOptions;
	}

	public String getContents() {
		StringBuilder sb = new StringBuilder();
		for (Message message : getInstructions()) {
			sb.append(message.getContent());
		}
		return sb.toString();
	}

	@Override
	public ChatOptions getOptions() {
		return this.chatOptions;
	}

	@Override
	public List<Message> getInstructions() {
		return this.messages;
	}

	@Override
	public String toString() {
		return "Prompt{" + "messages=" + this.messages + ", modelOptions=" + this.chatOptions + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Prompt))
			return false;
		Prompt prompt = (Prompt) o;
		return  Objects.equals(this.messages, prompt.messages) && Objects.equals(this.chatOptions, prompt.chatOptions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.messages, this.chatOptions);
	}

	public Prompt copy() {
		return new Prompt(instructionsCopy(), this.chatOptions);
	}

	private List<Message> instructionsCopy() {
		List<Message> messagesCopy = new ArrayList<>();
		this.messages.forEach(message -> {
			if (message instanceof UserMessage) {
				messagesCopy.add(new UserMessage(message.getContent(), message.getMetadata()));
			}
			else if (message instanceof SystemMessage) {
				messagesCopy.add(new SystemMessage(message.getContent()));
			}
			else if (message instanceof AssistantMessage) {
				messagesCopy.add(new AssistantMessage(message.getContent(), message.getMetadata()));
			}
			else if (message instanceof FunctionMessage) {
				messagesCopy.add(new FunctionMessage(message.getContent(), message.getMetadata()));
			}
			else {
				throw new IllegalArgumentException("Unsupported message type: " + message.getClass().getName());
			}
		});

		return messagesCopy;
	}

}
