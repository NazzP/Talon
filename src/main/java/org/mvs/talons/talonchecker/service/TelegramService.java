package org.mvs.talons.talonchecker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class TelegramService {

    private final WebClient webClient;

    @Value("${telegram.bot.token:}")
    private String botToken;

    public TelegramService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Void> sendSuccessMessage(String chatId, String text) {
        return sendMessage(chatId, text);
    }

    public Mono<Void> sendExpiredMessage(String chatId, String text) {
        return sendMessage(chatId, text);
    }

    private Mono<Void> sendMessage(String chatId, String text) {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            log.warn("Telegram bot token or chat ID is not configured. Skipping notification.");
            return Mono.empty();
        }

        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

        return webClient.post()
                .uri(url)
                .bodyValue(new TelegramMessage(chatId, text))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(30))
                .retryWhen(reactor.util.retry.Retry.backoff(5, java.time.Duration.ofSeconds(2))
                        .maxBackoff(java.time.Duration.ofSeconds(30))
                        .filter(throwable -> {
                            if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException e) {
                                // Retry on server errors (5xx) or rate limiting (429)
                                return e.getStatusCode().is5xxServerError() || e.getStatusCode().value() == 429;
                            }
                            return throwable instanceof java.io.IOException || 
                                   throwable instanceof java.util.concurrent.TimeoutException ||
                                   throwable instanceof org.springframework.web.reactive.function.client.WebClientRequestException;
                        })
                        .doBeforeRetry(retrySignal -> log.warn("Retrying Telegram notification to {} (attempt {}): {}", 
                                chatId, retrySignal.totalRetries() + 1, retrySignal.failure().getMessage())))
                .doOnSuccess(response -> log.info("Telegram notification sent successfully to {}", chatId))
                .doOnError(error -> log.error("Failed to send Telegram notification to {}: {}", chatId, error.getMessage()))
                .then();
    }

    private record TelegramMessage(String chat_id, String text) {}
}

/*
.springframework.scheduling.annotation.Scheduled(fixedRate = 5000)
public void pollUpdates() {
    if (botToken == null || botToken.isBlank()) return;

    String url = String.format("https://api.telegram.org/bot%s/getUpdates?offset=%d", botToken, lastUpdateId + 1);

    webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(com.fasterxml.jackson.databind.JsonNode.class)
            .flatMap(response -> {
                if (response.has("result") && response.get("result").isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode update : response.get("result")) {
                        lastUpdateId = Math.max(lastUpdateId, update.get("update_id").asLong());
                        handleUpdate(update);
                    }
                }
                return Mono.empty();
            })
            .subscribe();
}

private void handleUpdate(com.fasterxml.jackson.databind.JsonNode update) {
    if (update.has("message") && update.get("message").has("text")) {
        String chatId = update.get("message").get("from").get("id").asText();
        String text = update.get("message").get("text").asText();
        processCommand(chatId, text);
    }
}

private void processCommand(String chatId, String text) {
    String[] parts = text.split(" ");
    String command = parts[0].toLowerCase();

    switch (command) {
        case "/start" -> sendMessage(chatId, "Вітаю! Я бот для відстеження талонів МВС.\n\n" +
                "Команди:\n" +
                "/list - список сервісних центрів та ваших підписок\n" +
                "/subscribe [ID] - підписатися на СЦ\n" +
                "/unsubscribe [ID] - відписатися від СЦ").subscribe();
        case "/list" -> {
            StringBuilder sb = new StringBuilder("Ваші підписки:\n");
            java.util.List<org.mvs.talons.talonchecker.entity.UserSubscription> subs = departmentService.getSubscriptionsByChatId(chatId);
            if (subs.isEmpty() || subs.get(0).getDepartmentIds().isEmpty()) {
                sb.append("- Немає активних підписок\n");
            } else {
                for (Integer id : subs.get(0).getDepartmentIds()) {
                    org.mvs.talons.talonchecker.entity.Department dept = departmentService.getDepartmentById(id);
                    sb.append(String.format("- [%d] %s (%s)\n", id, dept.getName(), dept.getCity()));
                }
            }
            sb.append("\nДоступні СЦ (топ 10):\n");
            departmentService.getAllDepartments().stream().limit(10).forEach(dept ->
                    sb.append(String.format("%d: %s (%s)\n", dept.getId(), dept.getName(), dept.getCity()))
            );
            sb.append("\nВикористовуйте /subscribe [ID] щоб додати новий СЦ.");
            sendMessage(chatId, sb.toString()).subscribe();
        }
        case "/subscribe" -> {
            if (parts.length < 2) {
                sendMessage(chatId, "Використовуйте: /subscribe [ID сервісного центру]").subscribe();
                return;
            }
            try {
                Integer deptId = Integer.parseInt(parts[1]);
                departmentService.subscribe(chatId, deptId);
                departmentService.updateAccess(chatId, true); // Надаємо доступ при підписці
                sendMessage(chatId, "Ви успішно підписалися на СЦ " + deptId).subscribe();
            } catch (Exception e) {
                sendMessage(chatId, "Помилка підписки: " + e.getMessage()).subscribe();
            }
        }
        case "/unsubscribe" -> {
            if (parts.length < 2) {
                sendMessage(chatId, "Використовуйте: /unsubscribe [ID сервісного центру]").subscribe();
                return;
            }
            try {
                Integer deptId = Integer.parseInt(parts[1]);
                departmentService.unsubscribe(chatId, deptId);
                sendMessage(chatId, "Ви відписалися від СЦ " + deptId).subscribe();
            } catch (Exception e) {
                sendMessage(chatId, "Помилка: " + e.getMessage()).subscribe();
            }
        }
        default -> sendMessage(chatId, "Невідома команда. Спробуйте /start").subscribe();
    }
}

private record TelegramMessage(String chat_id, String text) {}
}
*/

