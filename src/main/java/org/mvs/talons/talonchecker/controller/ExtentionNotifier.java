package org.mvs.talons.talonchecker.controller;

import org.mvs.talons.talonchecker.listener.TalonCheckerListener;
import org.mvs.talons.talonchecker.service.TelegramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ExtentionNotifier {

    private final TelegramService telegramService;

    public ExtentionNotifier(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

//    @PostMapping("/notify")
//    public ResponseEntity<String> notifySlot(@RequestBody TalonCheckerListener.MvsData data) {
//        System.out.println("Новий талон на дату: " + data.getDate());
//        // тут можна викликати метод для Telegram
//        telegramService.sendSuccessMessage("605521264", "🎉 Вільний талон на " + data.getDate());
//        return ResponseEntity.ok("Received");
//    }
}

