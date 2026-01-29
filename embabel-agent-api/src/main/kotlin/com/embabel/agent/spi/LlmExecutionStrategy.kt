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

import java.time.Duration

/**
 * Strategy interface for executing LLM operations with timeout handling.
 *
 * This abstraction allows different execution strategies to be plugged in:
 * - Default: Uses CompletableFuture with timeout for standard JVM execution
 * - Temporal: Executes directly, relying on Temporal's activity mechanism for durability
 * - Custom: Any other execution strategy (reactive, virtual threads, etc.)
 *
 * @see com.embabel.agent.spi.support.DefaultLlmExecutionStrategy
 */
interface LlmExecutionStrategy {

    /**
     * Execute an LLM operation with the specified timeout.
     *
     * @param T the return type of the operation
     * @param interactionId identifier for logging and debugging
     * @param timeout maximum duration to wait for the operation
     * @param attempt current retry attempt number (for logging)
     * @param operation the LLM operation to execute
     * @return the result of the operation
     * @throws RuntimeException if the operation fails or times out
     */
    fun <T> execute(
        interactionId: String,
        timeout: Duration,
        attempt: Int = 1,
        operation: () -> T,
    ): T
}
