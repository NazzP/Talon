package org.mvs.talons.talonchecker.utils;

import org.mvs.talons.talonchecker.entity.MonthName;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class Resource {

    public String formatSlotDate(String dateStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            int day = dateTime.getDayOfMonth();
            String month = MonthName.getNameUaByValue(dateTime.getMonthValue());
            int year = dateTime.getYear();
            return String.format("%d %s %d", day, month, year);
        } catch (Exception e) {
            return dateStr;
        }
    }
}
