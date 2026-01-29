/*
 * Copyright 2024-2026 Embabel Pty Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.agent.spi

import org.springframework.ai.chat.model.ChatModel

/**
 * Strategy interface for decorating/wrapping ChatModel instances.
 *
 * This abstraction allows different execution environments to wrap ChatModels
 * with custom behavior:
 * - Default: Returns the ChatModel as-is (no decoration)
 * - Temporal: Wraps with a context-aware ChatModel that routes to ActivityChatModel
 *   when inside a workflow thread
 * - Custom: Any other decoration (logging, metrics, caching, etc.)
 *
 * The decorator is applied when creating LlmMessageSender instances, ensuring
 * all LLM calls go through the decorated ChatModel.
 *
 * @see com.embabel.agent.spi.support.springai.SpringAiLlmService
 */
fun interface ChatModelDecorator {

    /**
     * Decorate the given ChatModel.
     *
     * @param chatModel the original ChatModel to decorate
     * @return the decorated ChatModel (may be the same instance if no decoration needed)
     */
    fun decorate(chatModel: ChatModel): ChatModel

    companion object {
        /**
         * Default decorator that returns the ChatModel unchanged.
         */
        @JvmField
        val IDENTITY: ChatModelDecorator = ChatModelDecorator { it }
    }
}
