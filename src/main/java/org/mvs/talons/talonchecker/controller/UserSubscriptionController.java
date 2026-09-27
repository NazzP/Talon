package org.mvs.talons.talonchecker.controller;

import lombok.RequiredArgsConstructor;
import org.mvs.talons.talonchecker.entity.Department;
import org.mvs.talons.talonchecker.entity.UserSubscription;
import org.mvs.talons.talonchecker.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final DepartmentService departmentService;

    @GetMapping
    public List<UserSubscription> getAllSubscriptions() {
        return departmentService.getAllSubscriptions();
    }

    @GetMapping("/{chatId}")
    public List<UserSubscription> getSubscriptionsByChatId(@PathVariable String chatId) {
        return departmentService.getSubscriptionsByChatId(chatId);
    }

    @PostMapping
    public UserSubscription subscribe(@RequestParam String chatId, @RequestParam Integer departmentId) {
        return departmentService.subscribe(chatId, departmentId);
    }

    @DeleteMapping
    public void unsubscribe(@RequestParam String chatId, @RequestParam Integer departmentId) {
        departmentService.unsubscribe(chatId, departmentId);
    }

    @GetMapping("/departments")
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }
}
