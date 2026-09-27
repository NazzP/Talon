package org.mvs.talons.talonchecker.controller;

import lombok.RequiredArgsConstructor;
import org.mvs.talons.talonchecker.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class UserSessionController {

    private final DepartmentService departmentService;

    @PostMapping("/{chatId}/cookie")
    public void updateCookie(@PathVariable String chatId, @RequestBody String cookie) {
        departmentService.updateSessionCookie(chatId, cookie);
    }
}
