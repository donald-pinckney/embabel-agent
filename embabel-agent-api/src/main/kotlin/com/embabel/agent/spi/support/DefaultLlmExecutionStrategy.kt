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
package com.embabel.agent.spi.support

import com.embabel.agent.spi.LlmExecutionStrategy
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private const val LLM_TIMEOUT_MESSAGE = "LLM {}: attempt {} timed out after {}ms"
private const val LLM_INTERRUPTED_MESSAGE = "LLM {}: attempt {} was interrupted"

/**
 * Default execution strategy that uses CompletableFuture with timeout.
 *
 * This is the standard execution strategy for running LLM operations in a
 * regular JVM environment. It wraps operations in a CompletableFuture and
 * enforces a timeout using `future.get(timeout, TimeUnit.MILLISECONDS)`.
 *
 * For Temporal workflow environments, use a strategy that executes directly
 * without blocking (since ActivityChatModel handles the async execution).
 */
class DefaultLlmExecutionStrategy : LlmExecutionStrategy {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun <T> execute(
        interactionId: String,
        timeout: Duration,
        attempt: Int,
        operation: () -> T,
    ): T {
        val timeoutMillis = timeout.toMillis()
        val future = CompletableFuture.supplyAsync { operation() }

        return try {
            future.get(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            future.cancel(true)
            logger.warn(LLM_TIMEOUT_MESSAGE, interactionId, attempt, timeoutMillis)
            throw RuntimeException(
                "LLM call for interaction $interactionId timed out after ${timeoutMillis}ms",
                e
            )
        } catch (e: InterruptedException) {
            future.cancel(true)
            Thread.currentThread().interrupt()
            logger.warn(LLM_INTERRUPTED_MESSAGE, interactionId, attempt)
            throw RuntimeException(
                "LLM call for interaction $interactionId was interrupted",
                e
            )
        } catch (e: ExecutionException) {
            future.cancel(true)
            val cause = e.cause
            when (cause) {
                is RuntimeException -> throw cause
                is Exception -> throw RuntimeException(
                    "LLM call for interaction $interactionId failed",
                    cause
                )
                else -> throw RuntimeException(
                    "LLM call for interaction $interactionId failed with unknown error",
                    e
                )
            }
        }
    }
}
