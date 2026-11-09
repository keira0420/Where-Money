package shuhuai.wheremoney.service.ai;

import jakarta.annotation.PreDestroy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shuhuai.wheremoney.service.excep.ai.*;
import java.util.Map;
import java.util.concurrent.*;

/** Shared per-instance bulkhead; virtual threads do not impose a concurrency limit. */
@Component
public class AiModelGateway {
    private final ChatClient client;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore permits;
    private final long timeout;
    @Value("${spring.ai.openai.api-key:not-configured}") private String apiKey;

    public AiModelGateway(ChatClient.Builder builder,
                          @Value("${ai.bill.parse.timeout-ms:20000}") long timeout,
                          @Value("${ai.bill.max-concurrent:8}") int concurrent) {
        this.client = builder.build();
        this.timeout = Math.max(1000, Math.min(timeout, 60000));
        this.permits = new Semaphore(Math.max(1, concurrent));
    }

    public String complete(String system, String user) {
        if ("not-configured".equals(apiKey)) throw new AiInvokeException("尚未配置AI_API_KEY，请使用自己的模型服务密钥");
        if (!permits.tryAcquire()) throw new AiRateLimitException("AI服务繁忙，请稍后重试");
        Future<String> task;
        try {
            task = executor.submit(() -> {
                try {
                    return client.prompt()
                        .options(OpenAiChatOptions.builder().temperature(0.1)
                            .extraBody(Map.of("enable_thinking", false)).build())
                        .system(system).user(user).call().content();
                } finally {
                    permits.release();
                }
            });
        } catch (RuntimeException e) {
            permits.release();
            throw new AiInvokeException("AI服务正在关闭");
        }
        try {
            return task.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            task.cancel(true);
            throw new AiTimeoutException("AI响应超时，请稍后重试");
        } catch (InterruptedException e) {
            task.cancel(true);
            Thread.currentThread().interrupt();
            throw new AiInvokeException("AI请求已取消");
        } catch (ExecutionException e) {
            String message = e.getCause() == null ? "" : String.valueOf(e.getCause().getMessage());
            if (message.contains("Arrearage")) throw new AiInvokeException("模型服务账号额度不足，请更换可用密钥或恢复服务额度");
            throw new AiInvokeException("模型服务暂不可用，请检查模型配置或稍后重试");
        }
    }

    @PreDestroy
    public void close() { executor.shutdownNow(); }
}
