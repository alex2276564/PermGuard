package uz.alex2276564.permguard.telegram;

import com.alibaba.fastjson2.JSONObject;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfig;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.utils.HttpUtils;
import uz.alex2276564.permguard.utils.runner.Runner;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Global send queue for Telegram Bot API.
 * <p>
 * - Ensures that only one HTTP request to Telegram is in-flight at any time.
 * - Applies global rate limiting between messages.
 * - Handles per-message retry logic (maxRetries + retryDelay + retry_after).
 * <p>
 * This class is intentionally package-private and used only by TelegramNotifier.
 */
final class TelegramSendQueue {

    private static final String TELEGRAM_SEND_MESSAGE_URL = "https://api.telegram.org/bot%s/sendMessage";

    private final PermGuard plugin;
    private final HttpUtils httpUtils;
    private final String userAgent;

    // Default delay between successful sends, used as a fallback.
    private final long defaultSuccessDelayMs;

    // Thread-safe double-ended queue of pending send jobs
    private final Deque<SendJob> queue = new ConcurrentLinkedDeque<>();

    // Simple "one worker at a time" guard
    private final Object workerLock = new Object();
    private boolean workerScheduled = false;

    TelegramSendQueue(PermGuard plugin, HttpUtils httpUtils, String userAgent, long defaultSuccessDelayMs) {
        this.plugin = plugin;
        this.httpUtils = httpUtils;
        this.userAgent = userAgent;
        this.defaultSuccessDelayMs = Math.max(0L, defaultSuccessDelayMs);
    }

    void enqueue(SendJob job) {
        queue.addLast(job);
        startWorkerIfNeeded();
    }

    /**
     * Ensure that at most one queue worker is scheduled/running.
     */
    private void startWorkerIfNeeded() {
        synchronized (workerLock) {
            if (workerScheduled) {
                return;
            }
            workerScheduled = true;
        }
        scheduleWorker(0L);
    }

    private void scheduleWorker(long delayMs) {
        long ticks = Runner.msToTicks(Math.max(0L, delayMs));
        plugin.getRunner().runAsyncLater(this::processQueue, ticks);
    }

    /**
     * Process a single queued job and reschedule if there is more work.
     * This method is always executed on an async thread (via Runner).
     */
    private void processQueue() {
        SendJob job = queue.pollFirst();

        if (job == null) {
            synchronized (workerLock) {
                workerScheduled = false;
            }
            if (!queue.isEmpty()) {
                startWorkerIfNeeded();
            }
            return;
        }

        long delayForNextRunMs = defaultSuccessDelayMs;

        try {
            Outcome outcome = sendJob(job);

            switch (outcome.type) {
                case SUCCESS -> delayForNextRunMs = outcome.delayMs;

                case RETRY -> {
                    if (job.attempt < job.maxAttempts) {
                        job.attempt++;
                        queue.addFirst(job);
                        delayForNextRunMs = outcome.delayMs;
                    }
                    // else: keep defaultSuccessDelayMs
                }

                case DROP -> {
                    // keep defaultSuccessDelayMs
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("[PermGuard] Unexpected error while sending Telegram message: " + e.getMessage());
            delayForNextRunMs = Math.max(1000L, defaultSuccessDelayMs);
        }

        synchronized (workerLock) {
            if (queue.isEmpty()) {
                workerScheduled = false;
            } else {
                scheduleWorker(delayForNextRunMs);
            }
        }
    }

    /**
     * Perform a single HTTP attempt for one job and decide what to do next.
     */
    private Outcome sendJob(SendJob job) {
        MainConfig.TelegramSection config = job.config;
        MessagesConfig.TelegramMessagesSection tmsg = job.tmsg;

        String url = String.format(TELEGRAM_SEND_MESSAGE_URL, config.botToken);

        JSONObject body = new JSONObject();
        body.put("chat_id", job.chatId);
        body.put("text", job.message);

        int maxAttempts = job.maxAttempts;

        long successDelayMs = config.minDelayMs;

        try {
            HttpUtils.HttpResponse response = httpUtils.postJson(url, body, userAgent);
            int status = response.statusCode();

            if (status == 200) {
                // Successful send, schedule the next message after successDelayMs
                return Outcome.success(successDelayMs);
            }

            long delayMs = config.retryDelay;

            if (status == 429) {
                // Rate limited by Telegram - respect retry_after if available
                delayMs = extractRetryAfterMillis(response, delayMs);

                if (job.attempt < maxAttempts) {
                    logRetryAttempt(tmsg, job.attempt, maxAttempts, "Rate limit (429)");
                    return Outcome.retry(delayMs);
                } else {
                    plugin.getLogger().warning(tmsg.tooManyRequests);
                    return Outcome.drop();
                }
            }

            // Any other non-200 HTTP status code
            String error = "HTTP " + status;
            if (job.attempt < maxAttempts) {
                logRetryAttempt(tmsg, job.attempt, maxAttempts, error);
                return Outcome.retry(delayMs);
            } else {
                String msg = tmsg.sendFailed.replace("<error>", error);
                plugin.getLogger().warning(msg);
                return Outcome.drop();
            }

        } catch (Exception e) {
            long delayMs = config.retryDelay;
            String error = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();

            if (job.attempt < maxAttempts) {
                logRetryAttempt(tmsg, job.attempt, maxAttempts, error);
                return Outcome.retry(delayMs);
            } else {
                String msg = tmsg.sendFailed.replace("<error>", error);
                plugin.getLogger().warning(msg);
                return Outcome.drop();
            }
        }
    }

    private long extractRetryAfterMillis(HttpUtils.HttpResponse response, long defaultDelayMs) {
        long nextDelayMs = defaultDelayMs;
        try {
            JSONObject json = response.jsonBody();
            if (json != null) {
                JSONObject params = json.getJSONObject("parameters");
                if (params != null) {
                    Long retryAfter = params.getLong("retry_after");
                    if (retryAfter != null) {
                        long retryAfterMs = retryAfter * 1000L;
                        nextDelayMs = Math.max(nextDelayMs, retryAfterMs);
                    }
                }
            }
        } catch (Exception ignored) {
            // Ignore parsing problems, fall back to default delay.
        }
        return nextDelayMs;
    }

    private void logRetryAttempt(MessagesConfig.TelegramMessagesSection tmsg,
                                 int attempt, int maxAttempts, String error) {
        String msg = tmsg.sendFailedAttempt
                .replace("<attempt>", String.valueOf(attempt))
                .replace("<max>", String.valueOf(maxAttempts))
                .replace("<error>", error);
        plugin.getLogger().warning(msg);
    }

    /**
     * Shutdown the send queue and discard pending messages.
     * Should be called on plugin disable.
     */
    void shutdown() {
        int pending = queue.size();
        if (pending > 0) {
            plugin.getLogger().info("Telegram send queue shutting down with " + pending + " pending message(s)");
        }
        queue.clear();

        synchronized (workerLock) {
            workerScheduled = false;
        }
    }

    enum OutcomeType {
        SUCCESS,
        RETRY,
        DROP
    }

    static final class Outcome {
        final OutcomeType type;
        final long delayMs;

        private Outcome(OutcomeType type, long delayMs) {
            this.type = type;
            this.delayMs = delayMs;
        }

        static Outcome success(long successDelayMs) {
            return new Outcome(OutcomeType.SUCCESS, successDelayMs);
        }

        static Outcome retry(long delayMs) {
            return new Outcome(OutcomeType.RETRY, delayMs);
        }

        static Outcome drop() {
            return new Outcome(OutcomeType.DROP, 0L);
        }
    }

    /**
     * Single Telegram send job with retry metadata.
     * Immutable except for the attempt counter.
     */
    static final class SendJob {
        final MainConfig.TelegramSection config;
        final MessagesConfig.TelegramMessagesSection tmsg;
        final String chatId;
        final String message;
        int attempt;
        final int maxAttempts;

        SendJob(MainConfig.TelegramSection config,
                MessagesConfig.TelegramMessagesSection tmsg,
                String chatId,
                String message,
                int attempt,
                int maxAttempts) {
            this.config = config;
            this.tmsg = tmsg;
            this.chatId = chatId;
            this.message = message;
            this.attempt = attempt;
            this.maxAttempts = maxAttempts;
        }
    }
}