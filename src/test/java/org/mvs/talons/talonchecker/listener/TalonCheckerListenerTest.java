package org.mvs.talons.talonchecker.listener;

import org.junit.jupiter.api.Test;
import org.mvs.talons.talonchecker.entity.Department;
import org.mvs.talons.talonchecker.entity.UserSubscription;
import org.mvs.talons.talonchecker.repository.SentNotificationRepository;
import org.mvs.talons.talonchecker.service.DepartmentService;
import org.mvs.talons.talonchecker.service.TelegramService;
import org.mvs.talons.talonchecker.utils.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class TalonCheckerListenerTest {

    @Autowired
    private TalonCheckerListener talonCheckerListener;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private TelegramService telegramService;

    @MockitoBean
    private WebClient webClient;

    @MockitoBean
    private Resource resource;

    @MockitoBean
    private SentNotificationRepository sentNotificationRepository;

    @Test
    void testCheckSlotsSendsNotificationsOnlyToSubscribedUsers() throws InterruptedException {
        // Arrange
        Integer dept1Id = 65;
        Integer dept2Id = 74;

        UserSubscription sub1 = UserSubscription.builder().chatId("chat1").departmentIds(new java.util.HashSet<>(List.of(dept1Id))).isAccess(true).build();
        UserSubscription sub2 = UserSubscription.builder().chatId("chat2").departmentIds(new java.util.HashSet<>(List.of(dept2Id))).isAccess(true).build();
        UserSubscription sub3 = UserSubscription.builder().chatId("chat3").departmentIds(new java.util.HashSet<>(List.of(dept1Id))).isAccess(true).build();

        Map<Integer, List<UserSubscription>> grouped = new HashMap<>();
        grouped.put(dept1Id, List.of(sub1, sub3));
        grouped.put(dept2Id, List.of(sub2));

        when(departmentService.getSubscriptionsByDepartment()).thenReturn(grouped);
        when(departmentService.getDepartmentById(dept1Id)).thenReturn(new Department(dept1Id, "Dept 1", "Region", "District", "City 1", "Street", "1", 1));
        when(departmentService.getDepartmentById(dept2Id)).thenReturn(new Department(dept2Id, "Dept 2", "Region", "District", "City 2", "Street", "2", 1));
        
        when(resource.formatSlotDate(anyString())).thenReturn("2026-02-10");
        when(telegramService.sendSuccessMessage(anyString(), anyString())).thenReturn(Mono.empty());
        when(telegramService.sendExpiredMessage(anyString(), anyString())).thenReturn(Mono.empty());

        // Mock SentNotificationRepository to return empty for all checks
        when(sentNotificationRepository.findByChatIdAndDepartmentIdAndSlotDate(anyString(), anyInt(), anyString()))
                .thenReturn(java.util.Optional.empty());

        // Mock WebClient for dept1 (has slots with datetime format)
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec1 = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec1 = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec1 = mock(WebClient.ResponseSpec.class);
        Mono<String> mono1 = mock(Mono.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec1);
        when(requestHeadersUriSpec1.uri(anyString())).thenReturn(requestHeadersSpec1);
        when(requestHeadersSpec1.header(anyString(), any())).thenReturn(requestHeadersSpec1);
        when(requestHeadersSpec1.retrieve()).thenReturn(responseSpec1);
        when(responseSpec1.bodyToMono(String.class)).thenReturn(mono1);
        when(mono1.retryWhen(any())).thenReturn(mono1);
        
        // Return results based on departmentId in URL
        when(mono1.block()).thenAnswer(invocation -> {
            return "{\"data\": []}"; // Default
        });

        // Use more specific matchers to differentiate departments
        WebClient.RequestHeadersSpec requestHeadersSpecDept1 = mock(WebClient.RequestHeadersSpec.class);
        when(requestHeadersUriSpec1.uri(contains("departmentId=" + dept1Id))).thenReturn(requestHeadersSpecDept1);
        when(requestHeadersSpecDept1.header(anyString(), any())).thenReturn(requestHeadersSpecDept1);
        
        // We need separate ResponseSpec and Mono for Dept 1
        WebClient.ResponseSpec responseSpecDept1 = mock(WebClient.ResponseSpec.class);
        when(requestHeadersSpecDept1.retrieve()).thenReturn(responseSpecDept1);
        
        Mono<String> monoDept1 = mock(Mono.class);
        when(responseSpecDept1.bodyToMono(String.class)).thenReturn(monoDept1);
        when(monoDept1.retryWhen(any())).thenReturn(monoDept1);
        when(monoDept1.block()).thenReturn("{\"data\": [{\"date\": \"2026-02-10T00:00:00\"}]}");

        WebClient.RequestHeadersSpec requestHeadersSpecDept2 = mock(WebClient.RequestHeadersSpec.class);
        when(requestHeadersUriSpec1.uri(contains("departmentId=" + dept2Id))).thenReturn(requestHeadersSpecDept2);
        when(requestHeadersSpecDept2.header(anyString(), any())).thenReturn(requestHeadersSpecDept2);
        WebClient.ResponseSpec responseSpec2 = mock(WebClient.ResponseSpec.class);
        when(requestHeadersSpecDept2.retrieve()).thenReturn(responseSpec2);
        Mono<String> monoDept2 = mock(Mono.class);
        when(responseSpec2.bodyToMono(String.class)).thenReturn(monoDept2);
        when(monoDept2.retryWhen(any())).thenReturn(monoDept2);
        when(monoDept2.block()).thenReturn("{\"data\": []}");

        // Act
        talonCheckerListener.checkSlots();

        // Assert
        // chat1 and chat3 should get notification for dept1
        verify(telegramService, atLeastOnce()).sendSuccessMessage(eq("chat1"), contains("Dept 1"));
        verify(telegramService, atLeastOnce()).sendSuccessMessage(eq("chat3"), contains("Dept 1"));
        
        // chat2 should NOT get notification for dept1
        verify(telegramService, never()).sendSuccessMessage(eq("chat2"), contains("Dept 1"));
        
        // No one should get notification for dept2 (no slots)
        verify(telegramService, never()).sendSuccessMessage(anyString(), contains("Dept 2"));
    }

    @Test
    void testCheckSlotsFiltersByTargetDatesPerDepartment() throws InterruptedException {
        // Arrange
        Integer dept65 = 65;
        Integer dept74 = 74;
        java.time.LocalDate dateFeb24 = java.time.LocalDate.of(2026, 2, 24);
        java.time.LocalDate dateFeb25 = java.time.LocalDate.of(2026, 2, 25);
        
        UserSubscription sub = UserSubscription.builder()
                .chatId("chat_per_dept_dates")
                .departmentIds(new java.util.HashSet<>(List.of(dept65, dept74)))
                .isAccess(true)
                .departmentTargetDates(new java.util.HashSet<>(List.of(
                        new org.mvs.talons.talonchecker.entity.DepartmentTargetDate(dept65, dateFeb25),
                        new org.mvs.talons.talonchecker.entity.DepartmentTargetDate(dept74, dateFeb24)
                )))
                .build();

        Map<Integer, List<UserSubscription>> grouped = new HashMap<>();
        grouped.put(dept65, List.of(sub));
        grouped.put(dept74, List.of(sub));

        when(departmentService.getSubscriptionsByDepartment()).thenReturn(grouped);
        when(departmentService.getDepartmentById(dept65)).thenReturn(new Department(dept65, "Dept 65", "Region", "District", "City", "Street", "1", 1));
        when(departmentService.getDepartmentById(dept74)).thenReturn(new Department(dept74, "Dept 74", "Region", "District", "City", "Street", "2", 1));
        when(telegramService.sendSuccessMessage(anyString(), anyString())).thenReturn(Mono.empty());
        when(telegramService.sendExpiredMessage(anyString(), anyString())).thenReturn(Mono.empty());

        // Mock WebClient behavior for different calls
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        
        // Mock for Dept 65
        WebClient.RequestHeadersSpec requestHeadersSpec65 = mock(WebClient.RequestHeadersSpec.class);
        when(requestHeadersUriSpec.uri(contains("departmentId=" + dept65))).thenReturn(requestHeadersSpec65);
        when(requestHeadersSpec65.header(anyString(), any())).thenReturn(requestHeadersSpec65);
        WebClient.ResponseSpec responseSpec65 = mock(WebClient.ResponseSpec.class);
        when(requestHeadersSpec65.retrieve()).thenReturn(responseSpec65);
        Mono<String> mono65 = mock(Mono.class);
        when(responseSpec65.bodyToMono(String.class)).thenReturn(mono65);
        when(mono65.retryWhen(any())).thenReturn(mono65);
        // Dept 65 has slots for Feb 24 (not in target) and Feb 25 (in target)
        when(mono65.block()).thenReturn("{\"data\": [{\"date\": \"2026-02-24T10:00:00\"}, {\"date\": \"2026-02-25T10:00:00\"}]}");

        // Mock for Dept 74
        WebClient.RequestHeadersSpec requestHeadersSpec74 = mock(WebClient.RequestHeadersSpec.class);
        when(requestHeadersUriSpec.uri(contains("departmentId=" + dept74))).thenReturn(requestHeadersSpec74);
        when(requestHeadersSpec74.header(anyString(), any())).thenReturn(requestHeadersSpec74);
        WebClient.ResponseSpec responseSpec74 = mock(WebClient.ResponseSpec.class);
        when(requestHeadersSpec74.retrieve()).thenReturn(responseSpec74);
        Mono<String> mono74 = mock(Mono.class);
        when(responseSpec74.bodyToMono(String.class)).thenReturn(mono74);
        when(mono74.retryWhen(any())).thenReturn(mono74);
        // Dept 74 has slots for Feb 24 (in target) and Feb 25 (not in target)
        when(mono74.block()).thenReturn("{\"data\": [{\"date\": \"2026-02-24T10:00:00\"}, {\"date\": \"2026-02-25T10:00:00\"}]}");

        when(sentNotificationRepository.findByChatIdAndDepartmentIdAndSlotDate(anyString(), anyInt(), anyString()))
                .thenReturn(java.util.Optional.empty());

        // Act
        talonCheckerListener.checkSlots();

        // Assert
        // Use atLeastOnce() in case the scheduler fires during the test execution in Spring context
        // Should get notification for Dept 65 @ Feb 25
        verify(telegramService, atLeastOnce()).sendSuccessMessage(eq("chat_per_dept_dates"), contains("Dept 65"));
        // Should get notification for Dept 74 @ Feb 24
        verify(telegramService, atLeastOnce()).sendSuccessMessage(eq("chat_per_dept_dates"), contains("Dept 74"));
        
        // Total at least 2 notifications
        verify(telegramService, atLeast(2)).sendSuccessMessage(eq("chat_per_dept_dates"), anyString());
    }
}
