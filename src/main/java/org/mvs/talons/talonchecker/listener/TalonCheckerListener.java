package org.mvs.talons.talonchecker.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mvs.talons.talonchecker.entity.Department;
import org.mvs.talons.talonchecker.entity.SentNotification;
import org.mvs.talons.talonchecker.entity.UserSubscription;
import org.mvs.talons.talonchecker.mapper.JsonUtils;
import org.mvs.talons.talonchecker.repository.SentNotificationRepository;
import org.mvs.talons.talonchecker.service.DepartmentService;
import org.mvs.talons.talonchecker.service.TelegramService;
import org.mvs.talons.talonchecker.utils.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TalonCheckerListener {

    private final WebClient webClient;
    private final TelegramService telegramService;
    private final Resource resource;
    private final DepartmentService departmentService;
    private final SentNotificationRepository sentNotificationRepository;

    @Value("${mvs.talon-checker-cookie}")
    private String cookie;
    @Value("${my.telegram.id.to.send.personally}")
    private String myTelegramId;

    /**
     * Перевірка талонів кожні 40 секунд
     */
    @Scheduled(fixedRate = 32_000)
    public void checkSlots() {
        Map<Integer, List<UserSubscription>> groupedSubscriptions = departmentService.getSubscriptionsByDepartment();
        if (groupedSubscriptions.isEmpty()) {
            System.out.println("Підписки відсутні.");
            return;
        }

        for (Map.Entry<Integer, List<UserSubscription>> entry : groupedSubscriptions.entrySet()) {
            Integer departmentId = entry.getKey();
            List<UserSubscription> subscriptions = entry.getValue();

            Department department = departmentService.getDepartmentById(departmentId);
            String departmentName = department != null ? department.getName() : "ID " + departmentId;
            String city = department != null ? department.getCity() : "";

            List<String> availableDatesFromGlobal = fetchAvailableDates(departmentId, departmentName, city, this.cookie);

            if (!availableDatesFromGlobal.isEmpty()) {
                for (String date : availableDatesFromGlobal) {
                    String formattedDate = resource.formatSlotDate(date);
                    String message = String.format("Знайдено талон у %s (%s), дата: %s", departmentName, city, formattedDate);
                    
                    for (UserSubscription subscription : subscriptions) {
                        // Перевірка прав доступу
                        if (!subscription.isAccess()) {
                            continue;
                        }

                        // Перевірка фільтру по датах
                        if (subscription.getStartDate() != null || subscription.getEndDate() != null || 
                            (subscription.getDepartmentTargetDates() != null && !subscription.getDepartmentTargetDates().isEmpty())) {
                            try {
                                LocalDate slotLocalDate;
                                if (date.contains("T")) {
                                    slotLocalDate = LocalDateTime.parse(date).toLocalDate();
                                } else {
                                    slotLocalDate = LocalDate.parse(date);
                                }
                                
                                // Перевірка конкретних дат для цього департаменту
                                boolean hasTargetDatesForThisDept = subscription.getDepartmentTargetDates() != null && 
                                        subscription.getDepartmentTargetDates().stream().anyMatch(dtd -> dtd.getDepartmentId().equals(departmentId));

                                if (hasTargetDatesForThisDept) {
                                    boolean dateMatch = subscription.getDepartmentTargetDates().stream()
                                            .anyMatch(dtd -> dtd.getDepartmentId().equals(departmentId) && dtd.getTargetDate().equals(slotLocalDate));
                                    if (!dateMatch) {
                                        continue;
                                    }
                                } else {
                                    // Перевірка діапазону дат (тільки якщо для цього департаменту немає конкретних дат)
                                    if (subscription.getStartDate() != null && slotLocalDate.isBefore(subscription.getStartDate())) {
                                        continue;
                                    }
                                    if (subscription.getEndDate() != null && slotLocalDate.isAfter(subscription.getEndDate())) {
                                        continue;
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("Не вдалося розпарсити дату слота: {}", date);
                            }
                        }

                        // Перевірка, чи ми вже інформували цього юзера про цей слот
                        if (sentNotificationRepository.findByChatIdAndDepartmentIdAndSlotDate(
                                subscription.getChatId(), departmentId, date).isPresent()) {
                            log.info("Про талон, department: {} , date: {} вже проінформовано юзера {}",
                                    departmentId, date, subscription.getChatId());
                            continue;
                        }

                        // Якщо у користувача є власні куки, пробуємо перевірити/взяти для нього персонально
                        String finalMessage = message;
                        Mono<Void> sendMono;
                        if (subscription.getSessionCookie() != null && !subscription.getSessionCookie().isBlank()) {
                            List<String> personalDates = fetchAvailableDates(departmentId, departmentName, city, subscription.getSessionCookie());
                            if (!personalDates.isEmpty()) {
                                sendMono = telegramService.sendExpiredMessage(subscription.getChatId(), finalMessage + " (Персональна сесія валідна!)");
                            } else {
                                sendMono = telegramService.sendExpiredMessage(subscription.getChatId(), finalMessage + " (Увага: Ваша сесія могла застаріти)");
                            }
                        } else {
                            sendMono = telegramService.sendSuccessMessage(subscription.getChatId(), finalMessage);
                        }

                        sendMono.doOnSuccess(v -> {
                            // Зберігаємо факт інформування ТІЛЬКИ після успішної відправки
                            sentNotificationRepository.save(SentNotification.builder()
                                    .chatId(subscription.getChatId())
                                    .departmentId(departmentId)
                                    .slotDate(date)
                                    .sentAt(LocalDateTime.now())
                                    .build());
                        }).subscribe();

                        sleep(300);
                    }
                }
            }
            sleep(10000);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Регламент очищення старих сповіщень кожні 20 хвилин.
     * Видаляє записи, яким більше 20 хвилин, щоб дозволити повторне інформування.
     */
    @Scheduled(fixedRate = 600_000)
    @Transactional
    public void clearOldNotifications() {
        LocalDateTime expiryDate = LocalDateTime.now().minusMinutes(10);
        sentNotificationRepository.deleteBySentAtBefore(expiryDate);
        log.info("Очищено застарілі сповіщення (старші за 20 хв)");
    }

    /**
     * Регламент очищення неактивних користувачів.
     * Якщо isAccess = false вже більше 5 днів — видаляємо з бази.
     *//*
    @Scheduled(cron = "0 0 1 * * ?") // Запускати щодня о першій ночі
    @Transactional
    public void cleanupInactiveUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(5);
        departmentService.getAllSubscriptions().stream()
                .filter(sub -> !sub.isAccess())
                .filter(sub -> sub.getAccessStatusChangedAt() != null && sub.getAccessStatusChangedAt().isBefore(cutoff))
                .forEach(sub -> {
                    log.info("Видалення неактивного користувача {} (немає доступу більше 5 днів)", sub.getChatId());
                    departmentService.deleteSubscription(sub.getChatId());
                });
    }*/

    private List<String> fetchAvailableDates(Integer departmentId, String departmentName, String city, String currentCookie) {
        List<String> availableDates = new ArrayList<>();
        try {
            String url = String.format("https://eqn.hsc.gov.ua/api/v2/equeue/days?serviceId=49&departmentId=%d", departmentId);

            String body = webClient.get()
                    .uri(url)
                    .header("Cookie", currentCookie)
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Encoding", "gzip, deflate, br, zstd")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Referer", "https://eqn.hsc.gov.ua/")
                    .header("Sec-CH-UA", "\"Not(A:Brand\";v=\"99\", \"Chromium\";v=\"145\", \"Google Chrome\";v=\"145\"")
                    .header("Sec-CH-UA-Mobile", "?0")
                    .header("Sec-CH-UA-Platform", "\"macOS\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof WebClientRequestException || throwable instanceof io.netty.handler.timeout.ReadTimeoutException))
                    .block();

            if (body == null || body.isBlank()) {
                String errorMsg = "Сесія закінчилася (порожня відповідь) для " + departmentName + ". Потрібно оновити куки.";
                telegramService.sendExpiredMessage(myTelegramId, errorMsg).subscribe();
                return availableDates;
            }

            MvsResponse response = JsonUtils.fromJson(body, MvsResponse.class);

            if (response.getData() == null || response.getData().isEmpty()) {
                System.out.println("Талони відсутні для " + departmentName + " (" + city + ")");
                return availableDates;
            }

            for (MvsData slot : response.getData()) {
                if (slot.getDate() != null && !slot.getDate().isBlank()) {
                    availableDates.add(slot.getDate());
                }
            }

        } catch (WebClientResponseException e) {
            String errorMsg = String.format("HTTP помилка %s для %s.", e.getStatusCode(), departmentName);
            telegramService.sendExpiredMessage(myTelegramId, errorMsg).subscribe();
        } catch (WebClientRequestException e) {
            log.error("Network error (timeout?) checking slots for department {}: {}", departmentId, e.getMessage());
        } catch (Exception e) {
            log.error("Error checking slots for department {}", departmentId, e);
        }
        return availableDates;
    }

    // Класи для десеріалізації JSON
    public static class MvsResponse {
        private List<MvsData> data;

        public List<MvsData> getData() { return data; }
        public void setData(List<MvsData> data) { this.data = data; }
    }

    public static class MvsData {
        private String date;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }
}
