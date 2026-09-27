package org.mvs.talons.talonchecker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mvs.talons.talonchecker.dto.DefaultSubscriptionConfig;
import org.mvs.talons.talonchecker.entity.Department;
import org.mvs.talons.talonchecker.entity.UserSubscription;
import org.mvs.talons.talonchecker.repository.DepartmentRepository;
import org.mvs.talons.talonchecker.repository.UserSubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        if (departmentRepository.count() == 0) {
            loadDepartments();
        }
        
        if (userSubscriptionRepository.count() == 0) {
            initDefaultSubscriptions();
        }
    }

    private void loadDepartments() {
        String json = """
            {
                "data": [
                    {"id": 2, "name": "ТСЦ МВС № 3242", "region": "Київська область", "district": "Білоцерківський район", "city": "м. Біла Церква", "street": " вул. Сухоярська", "building": "20", "allowOnlineCount": 1},
                    {"id": 3, "name": "ТСЦ МВС № 3243", "region": "Київська область", "district": "Броварський район", "city": "м. Бровари", "street": "вул. Броварської Сотні", "building": "4А", "allowOnlineCount": 1},
                    {"id": 6, "name": "ТСЦ МВС № 3246", "region": "Київська область", "district": "Бучанський район", "city": "с. Софіївська Борщагівка", "street": "вул. Малишко", "building": "2", "allowOnlineCount": 1},
                    {"id": 7, "name": "ТСЦ МВС № 3247", "region": "Київська область", "district": "Бучанський район", "city": "м. Буча", "street": "вул. Депутатська", "building": "1 в", "allowOnlineCount": 1},
                    {"id": 8, "name": "ТСЦ МВС № 3248", "region": "Київська область", "district": "Бориспільський район", "city": "м. Переяслав", "street": "вул. Ново-Київське шосе", "building": "46 ", "allowOnlineCount": 1},
                    {"id": 11, "name": "ТСЦ МВС № 0541", "region": "Вінницька область", "district": "Вінницький район", "city": "м. Вінниця", "street": "вул. Ботанічна", "building": "24 ", "allowOnlineCount": 1},
                    {"id": 12, "name": "ТСЦ МВС № 0542", "region": "Вінницька область", "district": "Гайсинський район", "city": "м. Гайсин", "street": "вул. Південна", "building": "67 ", "allowOnlineCount": 1},
                    {"id": 13, "name": "ТСЦ МВС № 0543", "region": "Вінницька область", "district": "Жмеринський район", "city": "м. Шаргород", "street": "вул. Героїв Майдану", "building": "224 ", "allowOnlineCount": 1},
                    {"id": 17, "name": "ТСЦ МВС № 0741", "region": "Волинська область", "district": "Луцький район", "city": "с. Луцький р-н, с. Струмівка", "street": "вул. Рівненська", "building": "74 ", "allowOnlineCount": 0},
                    {"id": 18, "name": "ТСЦ МВС № 0742", "region": "Волинська область", "district": "Ковельський район", "city": "м. Ковель", "street": "вул. Володимирська", "building": "133 ", "allowOnlineCount": 0},
                    {"id": 22, "name": "ТСЦ МВС № 1242", "region": "Дніпропетровська область", "district": "Дніпровський район", "city": "м. Дніпро", "street": "пр. Праці", "building": "16 ", "allowOnlineCount": 1},
                    {"id": 25, "name": "ТСЦ МВС № 1245", "region": "Дніпропетровська область", "district": "Нікопольський район", "city": "м. Покров", "street": "вул. Уральська", "building": "1 ", "allowOnlineCount": 1},
                    {"id": 27, "name": "ТСЦ МВС № 1247", "region": "Дніпропетровська область", "district": "Павлоградський район", "city": "м. Павлоград", "street": "вул. Дніпровська", "building": "334 а", "allowOnlineCount": 1},
                    {"id": 28, "name": "ТСЦ МВС № 1248", "region": "Дніпропетровська область", "district": "Криворізький район", "city": "м. Кривий Ріг", "street": "вул. Стрельнікова", "building": "15 ", "allowOnlineCount": 1},
                    {"id": 29, "name": "ТСЦ МВС № 1249", "region": "Дніпропетровська область", "district": "Дніпровський район", "city": "м. Дніпро", "street": "пр-т. Слобожанський", "building": "31 д", "allowOnlineCount": 1},
                    {"id": 32, "name": "ТСЦ МВС № 1841", "region": "Житомирська область", "district": "Бердичівський район", "city": "с. Довжик", "street": "вул. Богунська", "building": "1 а", "allowOnlineCount": 1},
                    {"id": 34, "name": "ТСЦ МВС № 1843", "region": "Житомирська область", "district": "Коростенський район", "city": "м. Коростень", "street": "вул. Сергія Кемського", "building": "3 ", "allowOnlineCount": 0},
                    {"id": 35, "name": "ТСЦ МВС № 1844", "region": "Житомирська область", "district": "Новоград-Волинський район", "city": "м. Звягель", "street": "вул. Відродження", "building": "2 ", "allowOnlineCount": 1},
                    {"id": 37, "name": "ТСЦ МВС № 2141", "region": "Закарпатська область", "district": "Ужгородський район", "city": "м. Ужгород", "street": "вул. О. Кошового", "building": "4 ", "allowOnlineCount": 1},
                    {"id": 38, "name": "ТСЦ МВС № 2142", "region": "Закарпатська область", "district": "Берегівський район", "city": "с. Мукачівський р-н., с. Клячаново", "street": "вул. Автомобілістів", "building": "28 ", "allowOnlineCount": 1},
                    {"id": 40, "name": "ТСЦ МВС № 2144", "region": "Закарпатська область", "district": "Хустський район", "city": "м. Хуст", "street": "вул. Окружна", "building": "34 ", "allowOnlineCount": 1},
                    {"id": 42, "name": "ТСЦ МВС № 2341", "region": "Запорізька область", "district": "Пологівський район", "city": "м. Запоріжжя", "street": "вул. Олександрівська", "building": "84 ", "allowOnlineCount": 1},
                    {"id": 43, "name": "ТСЦ МВС № 2342", "region": "Запорізька область", "district": "Пологівський район", "city": "м. Запоріжжя", "street": "вул. Бабури козака", "building": "9 а", "allowOnlineCount": 1},
                    {"id": 46, "name": "ТСЦ МВС № 2641", "region": "Івано-Франківська область", "district": "Івано-Франківський район", "city": "м. Івано-Франківськ", "street": "вул. Коновальця", "building": "229 у", "allowOnlineCount": 0},
                    {"id": 48, "name": "ТСЦ МВС № 2643", "region": "Івано-Франківська область", "district": "Коломийський район", "city": "м. Коломия", "street": "вул. Симоненка", "building": "2 в", "allowOnlineCount": 0},
                    {"id": 60, "name": "ТСЦ МВС № 3541", "region": "Кіровоградська область", "district": "Кропивницький район", "city": "м. Кропивницький", "street": "пр-т. Інженерів", "building": "9/92 ", "allowOnlineCount": 0},
                    {"id": 61, "name": "ТСЦ МВС № 3542", "region": "Кіровоградська область", "district": "Олександрійський район", "city": "м. Олександрія", "street": "пр-т. Будівельників", "building": "38 ", "allowOnlineCount": 1},
                    {"id": 65, "name": "ТСЦ МВС № 4641", "region": "Львівська область", "district": "Львівський район", "city": "м. Львів", "street": "вул. Д. Апостола", "building": "11 ", "allowOnlineCount": 0},
                    {"id": 68, "name": "ТСЦ МВС № 4644", "region": "Львівська область", "district": "Самбірський район", "city": "м. Самбір", "street": "вул. Східна", "building": "216 ", "allowOnlineCount": 0},
                    {"id": 69, "name": "ТСЦ МВС № 4645", "region": "Львівська область", "district": "Самбірський район", "city": "м. Стрий", "street": "вул. Багряного", "building": "4 ", "allowOnlineCount": 0},
                    {"id": 70, "name": "ТСЦ МВС № 4646", "region": "Львівська область", "district": "Червоноградський район", "city": "м. Шептицький", "street": "вул. Корольова", "building": "14 а", "allowOnlineCount": 0},
                    {"id": 74, "name": "ТСЦ МВС № 4651", "region": "Львівська область", "district": "Самбірський район", "city": "м. Львів", "street": "вул. Богданівська", "building": "44 ", "allowOnlineCount": 0},
                    {"id": 75, "name": "ТСЦ МВС № 4841", "region": "Миколаївська область", "district": "Миколаївський район", "city": "м. Миколаїв", "street": "провулок. Транспортний", "building": "1а/1", "allowOnlineCount": 1},
                    {"id": 76, "name": "ТСЦ МВС № 4842", "region": "Миколаївська область", "district": "Вознесенський район", "city": "м. Первомайськ", "street": "вул. Вознесенська", "building": "73 б", "allowOnlineCount": 0},
                    {"id": 78, "name": "ТСЦ МВС № 4844", "region": "Миколаївська область", "district": "Вознесенський район", "city": "м. Вознесенськ", "street": "вул. Молодогвардійська", "building": "45 ", "allowOnlineCount": 1},
                    {"id": 79, "name": "ТСЦ МВС № 4845", "region": "Миколаївська область", "district": "Баштанський район", "city": "м. Новий Буг", "street": "вул. Площа Свободи", "building": "1 ", "allowOnlineCount": 1},
                    {"id": 82, "name": "ТСЦ МВС № 5143", "region": "Одеська область", "district": "Білгород-Дністровський район", "city": "м. Білгород-Дністровський", "street": "вул. Ігоря Іванова", "building": "4 ", "allowOnlineCount": 1},
                    {"id": 83, "name": "ТСЦ МВС № 5144", "region": "Одеська область", "district": "Ізмаїльський район", "city": "м. Ізмаїл", "street": "пр-т. Незалежності", "building": "362 ", "allowOnlineCount": 1},
                    {"id": 87, "name": "ТСЦ МВС № 5149", "region": "Одеська область", "district": "Подільський район", "city": "м. Подільськ", "street": "вул. Соборна", "building": "202 б", "allowOnlineCount": 1},
                    {"id": 88, "name": "ТСЦ МВС № 5150", "region": "Одеська область", "district": "Одеський район", "city": "м. Чорноморськ", "street": "вул. Промислова", "building": "14", "allowOnlineCount": 1},
                    {"id": 91, "name": "ТСЦ МВС № 5154", "region": "Одеська область", "district": "Одеський район", "city": "м. Одеса", "street": "вул. Аеропортівська", "building": "27/1", "allowOnlineCount": 1},
                    {"id": 92, "name": "ТСЦ МВС № 5341", "region": "Полтавська область", "district": "Лубенський район", "city": "с. Розсошенці", "street": "вул. Кременчуцька", "building": "2 в", "allowOnlineCount": 0},
                    {"id": 93, "name": "ТСЦ МВС № 5342", "region": "Полтавська область", "district": "Лубенський район", "city": "м. Кременчук", "street": "вул. Покровська", "building": "14", "allowOnlineCount": 0},
                    {"id": 94, "name": "ТСЦ МВС № 5343", "region": "Полтавська область", "district": "Лубенський район", "city": "м. Лубни", "street": "пр-т. Володимирський", "building": "178 б", "allowOnlineCount": 1},
                    {"id": 100, "name": "ТСЦ МВС № 5641", "region": "Рівненська область", "district": "Рівненський район", "city": "м. Рівне", "street": "вул. Київська", "building": "108 б", "allowOnlineCount": 0},
                    {"id": 102, "name": "ТСЦ МВС № 5643", "region": "Рівненська область", "district": "Сарненський район", "city": "м. Сарни", "street": "вул. Костопільська", "building": "100 ", "allowOnlineCount": 0},
                    {"id": 106, "name": "ТСЦ МВС № 5943", "region": "Сумська область", "district": "Охтирський район", "city": "м. Охтирка", "street": "вул. Київська", "building": "164 в", "allowOnlineCount": 1},
                    {"id": 108, "name": "ТСЦ МВС № 5945", "region": "Сумська область", "district": "Шосткинський район", "city": "м. Шостка", "street": "вул. Героїв Шосткинщини", "building": "24-А", "allowOnlineCount": 1},
                    {"id": 109, "name": "ТСЦ МВС № 5946", "region": "Сумська область", "district": "Сумський район", "city": "м. Суми", "street": "вул. Білопільський шлях", "building": "18/1", "allowOnlineCount": 1},
                    {"id": 110, "name": "ТСЦ МВС № 6141", "region": "Тернопільська область", "district": "Тернопільський район", "city": "с. Підгородне", "street": "вул. Стрийська", "building": "5 ", "allowOnlineCount": 0},
                    {"id": 112, "name": "ТСЦ МВС № 6143", "region": "Тернопільська область", "district": "Кременецький район", "city": "м. Кременець", "street": "вул. Шевченка", "building": "67 ", "allowOnlineCount": 0},
                    {"id": 114, "name": "ТСЦ МВС № 6145", "region": "Тернопільська область", "district": "Чортківський район", "city": "с. Угринь", "street": "вул. Об'їзна", "building": "1 ", "allowOnlineCount": 1},
                    {"id": 115, "name": "ТСЦ МВС № 6341", "region": "Харківська область", "district": "Харківський район", "city": "м. Харків", "street": "вул. Шевченка", "building": "26 ", "allowOnlineCount": 1},
                    {"id": 116, "name": "ТСЦ МВС № 6342", "region": "Харківська область", "district": "Чугуївський район", "city": "м. Харків", "street": "вул. Станція метро «Київська» вестибюль №1", "building": " ", "allowOnlineCount": 1},
                    {"id": 122, "name": "ТСЦ МВС № 6348", "region": "Харківська область", "district": "Лозівський район", "city": "м. Лозова", "street": "вул. мр-н 1", "building": "21", "allowOnlineCount": 1},
                    {"id": 124, "name": "ТСЦ МВС № 6350", "region": "Харківська область", "district": "Харківський район", "city": "смт. Пісочин", "street": "вул. Транспортна", "building": "116", "allowOnlineCount": 1},
                    {"id": 127, "name": "ТСЦ МВС № 6841", "region": "Хмельницька область", "district": "Хмельницький район", "city": "м. Хмельницький", "street": "вул. Західно-Окружна,", "building": "11/1", "allowOnlineCount": 1},
                    {"id": 129, "name": "ТСЦ МВС № 6843", "region": "Хмельницька область", "district": "Кам'янець-Подільський район", "city": "м. Кам'янець-Подільський", "street": "пр-т. Грушевського", "building": "45323 ", "allowOnlineCount": 1},
                    {"id": 130, "name": "ТСЦ МВС № 6844", "region": "Хмельницька область", "district": "Хмельницький район", "city": "м. Старокостянтинів", "street": "вул. Чайковського", "building": "1 а", "allowOnlineCount": 0},
                    {"id": 132, "name": "ТСЦ МВС № 7141", "region": "Черкаська область", "district": "Черкаський район", "city": "м. Черкаси", "street": "вул. Лесі Українки", "building": "21 ", "allowOnlineCount": 1},
                    {"id": 133, "name": "ТСЦ МВС № 7142", "region": "Черкаська область", "district": "Уманський район", "city": "с. Уманський р-н., с/рада Родниківська", "street": "вул. автодорога Київ-Одеса 203 км 800 м", "building": "б/н ", "allowOnlineCount": 1},
                    {"id": 137, "name": "ТСЦ МВС № 7341", "region": "Чернівецька область", "district": "Чернівецький район", "city": "м. Чернівці", "street": "вул. Руська", "building": "248 м", "allowOnlineCount": 1},
                    {"id": 141, "name": "ТСЦ МВС № 7345", "region": "Чернівецька область", "district": "Дністровський район", "city": "с. Кельменці", "street": "вул. О.Паламаря", "building": "9 в", "allowOnlineCount": 1},
                    {"id": 142, "name": "ТСЦ МВС № 7441", "region": "Чернігівська область", "district": "Чернігівський район", "city": "м. Чернігів", "street": "Шевченка", "building": "162а", "allowOnlineCount": 1},
                    {"id": 143, "name": "ТСЦ МВС № 7442", "region": "Чернігівська область", "district": "Прилуцький район", "city": "м. Прилуки", "street": "вул. Індустріальна", "building": "6", "allowOnlineCount": 1},
                    {"id": 146, "name": "ТСЦ МВС № 8041", "region": "м. Київ", "district": null, "city": "м. Київ", "street": "вул. Перемоги", "building": "20 ", "allowOnlineCount": 1},
                    {"id": 147, "name": "ТСЦ МВС № 8042", "region": "м. Київ", "district": null, "city": "м. Київ", "street": "вул. Мрії", "building": "19", "allowOnlineCount": 1},
                    {"id": 154, "name": "ТСЦ МВС № 8049", "region": "м. Київ", "district": null, "city": "м. Київ", "street": "вул. Павла Усенка", "building": "8 ", "allowOnlineCount": 1},
                    {"id": 155, "name": "ТСЦ МВС № 5942  вул Вирівська, 19 А (лише практичний іспит) ", "region": "Сумська область", "district": "Сумський район", "city": "м. Конотоп", "street": "Вирівська", "building": "19 А", "allowOnlineCount": 1}
                ]
            }
            """;
        try {
            JsonNode root = objectMapper.readTree(json);
            List<Department> departments = objectMapper.convertValue(root.get("data"), new TypeReference<List<Department>>() {});
            departmentRepository.saveAll(departments);
            log.info("Loaded {} departments into database", departments.size());
        } catch (Exception e) {
            log.error("Failed to load departments", e);
        }
    }

    private void initDefaultSubscriptions() {
        // Конфігурація підписок: chatId -> об'єкт з налаштуваннями
        Map<String, DefaultSubscriptionConfig> defaultMappings = Map.of(
                "605521264", new DefaultSubscriptionConfig(java.util.Set.of(65), java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(21)), // Me
                "593936886", new DefaultSubscriptionConfig(java.util.Set.of(65), java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(21)
                ) // Sanya
        );

        defaultMappings.forEach((chatId, config) -> {
            UserSubscription subscription = userSubscriptionRepository.findById(chatId)
                    .orElse(UserSubscription.builder()
                            .chatId(chatId)
                            .departmentIds(new java.util.HashSet<>())
                            .departmentTargetDates(new java.util.HashSet<>())
                            .isAccess(true)
                            .build());

            subscription.getDepartmentIds().addAll(config.getDepartmentIds());
            subscription.setStartDate(config.getStartDate());
            subscription.setEndDate(config.getEndDate());
            if (config.getTargetDatesPerDepartment() != null) {
                config.getTargetDatesPerDepartment().forEach((deptId, dates) -> {
                    dates.forEach(date -> subscription.getDepartmentTargetDates().add(
                            org.mvs.talons.talonchecker.entity.DepartmentTargetDate.builder()
                                    .departmentId(deptId)
                                    .targetDate(date)
                                    .build()
                    ));
                });
            }

            userSubscriptionRepository.save(subscription);
            log.info("Initialized default subscriptions for chat ID {} with departments {} and dates {} - {}, target dates per department {}",
                    chatId, config.getDepartmentIds(), config.getStartDate(), config.getEndDate(), config.getTargetDatesPerDepartment());
        });
    }

    public List<UserSubscription> getAllSubscriptions() {
        return userSubscriptionRepository.findAll();
    }

    public Map<Integer, List<UserSubscription>> getSubscriptionsByDepartment() {
        Map<Integer, List<UserSubscription>> result = new java.util.HashMap<>();
        userSubscriptionRepository.findAll().forEach(sub -> {
            for (Integer deptId : sub.getDepartmentIds()) {
                result.computeIfAbsent(deptId, k -> new java.util.ArrayList<>()).add(sub);
            }
        });
        return result;
    }

    public UserSubscription subscribe(String chatId, Integer departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new IllegalArgumentException("Сервісний центр з ID " + departmentId + " не знайдено");
        }
        UserSubscription subscription = userSubscriptionRepository.findById(chatId)
                .orElse(UserSubscription.builder()
                        .chatId(chatId)
                        .departmentIds(new java.util.HashSet<>())
                        .build());
        subscription.getDepartmentIds().add(departmentId);
        return userSubscriptionRepository.save(subscription);
    }

    public void unsubscribe(String chatId, Integer departmentId) {
        userSubscriptionRepository.findById(chatId).ifPresent(subscription -> {
            subscription.getDepartmentIds().remove(departmentId);
            if (subscription.getDepartmentIds().isEmpty() && (subscription.getSessionCookie() == null || subscription.getSessionCookie().isBlank())) {
                userSubscriptionRepository.delete(subscription);
            } else {
                userSubscriptionRepository.save(subscription);
            }
        });
    }

    public void updateSessionCookie(String chatId, String cookie) {
        UserSubscription subscription = userSubscriptionRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Підписку для чату " + chatId + " не знайдено"));
        subscription.setSessionCookie(cookie);
        userSubscriptionRepository.save(subscription);
        log.info("Оновлено куки для користувача {}", chatId);
    }

    public void updateAccess(String chatId, boolean isAccess) {
        UserSubscription subscription = userSubscriptionRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Підписку для чату " + chatId + " не знайдено"));
        
        if (subscription.isAccess() != isAccess) {
            subscription.setAccess(isAccess);
            subscription.setAccessStatusChangedAt(java.time.LocalDateTime.now());
            userSubscriptionRepository.save(subscription);
            log.info("Оновлено статус доступу для користувача {} на {}", chatId, isAccess);
        }
    }

    public void deleteSubscription(String chatId) {
        userSubscriptionRepository.deleteById(chatId);
    }

    public List<UserSubscription> getSubscriptionsByChatId(String chatId) {
        return userSubscriptionRepository.findById(chatId)
                .map(List::of)
                .orElse(List.of());
    }

    public Department getDepartmentById(Integer id) {
        return departmentRepository.findById(id).orElse(null);
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
}
