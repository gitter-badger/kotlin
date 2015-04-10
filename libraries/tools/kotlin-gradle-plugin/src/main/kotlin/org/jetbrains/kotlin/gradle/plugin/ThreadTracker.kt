package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.junit.Assert
import java.util.HashSet

public class ThreadTracker {
    val log = Logging.getLogger(this.javaClass)
    private var before: Collection<Thread>? = null

    init {
        before = getThreads()
    }

    private fun getThreads(): Collection<Thread> = Thread.getAllStackTraces().keySet()

    public fun checkThreadLeak(gradle: Gradle?) {
        try {
            val testThreads = gradle != null &&
                    gradle.getRootProject().hasProperty("kotlin.gradle.test") &&
                    !gradle.getRootProject().hasProperty("no_thread_test")

            Thread.sleep(if (testThreads) 200L else 50L)

            val after = HashSet(getThreads())
            after.removeAll(before!!)

            for (thread in after) {
                if (thread == Thread.currentThread()) continue
                if (thread.isInterrupted()) continue

                val name = thread.getName()
                var trace = "Thread leaked: $thread: $name\n ${thread.getStackTrace().joinToString(separator = "\n", prefix = " at ")}"

                if (testThreads) {
                    throw RuntimeException(trace)
                }
                else {
                    log.info(trace)
                }
            }
        } finally {
            before = null
        }
    }


}