import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final CategoryDAO categoryDAO = new CategoryDAO();
    private static final HallDAO hallDAO = new HallDAO();
    private static final CuratorDAO curatorDAO = new CuratorDAO();
    private static final ExhibitDAO exhibitDAO = new ExhibitDAO();
    private static final EventDAO eventDAO = new EventDAO();

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("   СИСТЕМА УПРАВЛІННЯ МУЗЕЄМ");
        System.out.println("   Лабораторна робота №9 - Варіант 6");
        System.out.println("===========================================");
        
        DBUtil.initDatabase();
        
        while (true) {
            printMainMenu();
            String cmd = sc.nextLine().trim();
            try {
                switch (cmd) {
                    case "1": manageExhibits(); break;
                    case "2": manageEvents(); break;
                    case "3": manageCategories(); break;
                    case "4": manageHalls(); break;
                    case "5": manageCurators(); break;
                    case "6": addTestData(); break;
                    case "0":
                        System.out.println("До побачення!");
                        return;
                    default:
                        System.out.println("Невідома команда!");
                }
            } catch (SQLException ex) {
                System.out.println("Помилка бази даних: " + ex.getMessage());
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n--- ГОЛОВНЕ МЕНЮ ---");
        System.out.println("1. Управління експонатами");
        System.out.println("2. Управління подіями");
        System.out.println("3. Управління категоріями");
        System.out.println("4. Управління залами");
        System.out.println("5. Управління кураторами");
        System.out.println("6. Додати тестові дані");
        System.out.println("0. Вихід");
        System.out.print("Ваш вибір: ");
    }

    // ==================== УПРАВЛІННЯ ЕКСПОНАТАМИ ====================
    private static void manageExhibits() throws SQLException {
        while (true) {
            System.out.println("\n--- ЕКСПОНАТИ ---");
            System.out.println("1. Додати експонат");
            System.out.println("2. Список всіх експонатів");
            System.out.println("3. Пошук за назвою");
            System.out.println("4. Пошук за періодом створення");
            System.out.println("5. Фільтр за залом");
            System.out.println("6. Фільтр за категорією");
            System.out.println("7. Сортування за датою надходження");
            System.out.println("8. Редагувати експонат");
            System.out.println("9. Видалити експонат");
            System.out.println("0. Назад");
            System.out.print("Ваш вибір: ");

            String cmd = sc.nextLine().trim();
            switch (cmd) {
                case "1": addExhibit(); break;
                case "2": listExhibits(null, null, null, null, "arrival_date", false); break;
                case "3": searchExhibitsByName(); break;
                case "4": searchExhibitsByPeriod(); break;
                case "5": filterExhibitsByHall(); break;
                case "6": filterExhibitsByCategory(); break;
                case "7": sortExhibitsByArrival(); break;
                case "8": editExhibit(); break;
                case "9": deleteExhibit(); break;
                case "0": return;
                default: System.out.println("Невідома команда!");
            }
        }
    }

    private static void addExhibit() throws SQLException {
        System.out.println("\n--- ДОДАВАННЯ ЕКСПОНАТУ ---");
        
        System.out.print("Назва: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Назва обов'язкова!"); return; }

        System.out.print("Опис: ");
        String desc = sc.nextLine().trim();

        System.out.print("Період створення (напр. 'XVIII століття'): ");
        String period = sc.nextLine().trim();

        System.out.print("Дата надходження (YYYY-MM-DD) [Enter = сьогодні]: ");
        String arrival = sc.nextLine().trim();
        if (arrival.isEmpty()) arrival = LocalDate.now().toString();

        // Вибір категорії
        System.out.println("\nДоступні категорії:");
        List<Category> categories = categoryDAO.findAll();
        for (Category c : categories) System.out.println("  " + c);
        System.out.print("ID категорії (або Enter для пропуску): ");
        Integer catId = readOptionalInt();

        // Вибір залу
        System.out.println("\nДоступні зали:");
        List<Hall> halls = hallDAO.findAll();
        for (Hall h : halls) System.out.println("  " + h);
        System.out.print("ID залу (або Enter для пропуску): ");
        Integer hallId = readOptionalInt();

        // Вибір куратора
        System.out.println("\nДоступні куратори:");
        List<Curator> curators = curatorDAO.findAll();
        for (Curator c : curators) System.out.println("  " + c);
        System.out.print("ID куратора (або Enter для пропуску): ");
        Integer curId = readOptionalInt();

        Exhibit e = new Exhibit(null, name, desc, period, arrival, catId, hallId, curId);
        exhibitDAO.create(e);
        System.out.println("Експонат додано з ID: " + e.getId());
    }

    private static void listExhibits(String nameLike, String periodLike, Integer hallId, 
                                      Integer categoryId, String sortBy, boolean asc) throws SQLException {
        List<Exhibit> list = exhibitDAO.findAll(nameLike, periodLike, hallId, categoryId, sortBy, asc);
        if (list.isEmpty()) {
            System.out.println("Експонатів не знайдено.");
        } else {
            System.out.println("\nЗнайдено " + list.size() + " експонат(ів):");
            for (Exhibit e : list) {
                System.out.println("  " + e);
            }
        }
    }

    private static void searchExhibitsByName() throws SQLException {
        System.out.print("Введіть назву для пошуку: ");
        String name = sc.nextLine().trim();
        listExhibits(name, null, null, null, "name", true);
    }

    private static void searchExhibitsByPeriod() throws SQLException {
        System.out.print("Введіть період для пошуку (напр. 'XVIII'): ");
        String period = sc.nextLine().trim();
        listExhibits(null, period, null, null, "creation_period", true);
    }

    private static void filterExhibitsByHall() throws SQLException {
        System.out.println("\nДоступні зали:");
        List<Hall> halls = hallDAO.findAll();
        for (Hall h : halls) System.out.println("  " + h);
        System.out.print("Введіть ID залу: ");
        Integer hallId = readOptionalInt();
        if (hallId != null) {
            listExhibits(null, null, hallId, null, "name", true);
        }
    }

    private static void filterExhibitsByCategory() throws SQLException {
        System.out.println("\nДоступні категорії:");
        List<Category> categories = categoryDAO.findAll();
        for (Category c : categories) System.out.println("  " + c);
        System.out.print("Введіть ID категорії: ");
        Integer catId = readOptionalInt();
        if (catId != null) {
            listExhibits(null, null, null, catId, "name", true);
        }
    }

    private static void sortExhibitsByArrival() throws SQLException {
        System.out.print("Сортувати за зростанням (спочатку старі)? (y/n): ");
        boolean asc = "y".equalsIgnoreCase(sc.nextLine().trim());
        listExhibits(null, null, null, null, "arrival_date", asc);
    }

    private static void editExhibit() throws SQLException {
        System.out.print("Введіть ID експонату для редагування: ");
        Integer id = readOptionalInt();
        if (id == null) return;

        Exhibit e = exhibitDAO.findById(id);
        if (e == null) { System.out.println("Експонат не знайдено!"); return; }

        System.out.println("Поточні дані: " + e);
        System.out.print("Нова назва [Enter = залишити]: ");
        String name = sc.nextLine().trim();
        if (!name.isEmpty()) e.setName(name);

        System.out.print("Новий опис [Enter = залишити]: ");
        String desc = sc.nextLine().trim();
        if (!desc.isEmpty()) e.setDescription(desc);

        if (exhibitDAO.update(e)) {
            System.out.println("Експонат оновлено!");
        } else {
            System.out.println("Помилка оновлення!");
        }
    }

    private static void deleteExhibit() throws SQLException {
        System.out.print("Введіть ID експонату для видалення: ");
        Integer id = readOptionalInt();
        if (id == null) return;

        System.out.print("Ви впевнені? (y/n): ");
        if ("y".equalsIgnoreCase(sc.nextLine().trim())) {
            if (exhibitDAO.delete(id)) {
                System.out.println("Експонат видалено!");
            } else {
                System.out.println("Експонат не знайдено!");
            }
        }
    }

    // ==================== УПРАВЛІННЯ ПОДІЯМИ ====================
    private static void manageEvents() throws SQLException {
        while (true) {
            System.out.println("\n--- ПОДІЇ ---");
            System.out.println("1. Запланувати подію");
            System.out.println("2. Список всіх подій");
            System.out.println("3. Майбутні події");
            System.out.println("4. Фільтр за типом події");
            System.out.println("5. Фільтр за залом");
            System.out.println("6. Редагувати подію");
            System.out.println("7. Скасувати подію");
            System.out.println("0. Назад");
            System.out.print("Ваш вибір: ");

            String cmd = sc.nextLine().trim();
            switch (cmd) {
                case "1": addEvent(); break;
                case "2": listEvents(null, null, null, null); break;
                case "3": listUpcomingEvents(); break;
                case "4": filterEventsByType(); break;
                case "5": filterEventsByHall(); break;
                case "6": editEvent(); break;
                case "7": deleteEvent(); break;
                case "0": return;
                default: System.out.println("Невідома команда!");
            }
        }
    }

    private static void addEvent() throws SQLException {
        System.out.println("\n--- ПЛАНУВАННЯ ПОДІЇ ---");

        System.out.print("Назва події: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Назва обов'язкова!"); return; }

        System.out.println("Типи подій: EXHIBITION (виставка), EXCURSION (екскурсія), LECTURE (лекція), OTHER (інше)");
        System.out.print("Тип події: ");
        String type = sc.nextLine().trim().toUpperCase();

        System.out.print("Дата події (YYYY-MM-DD): ");
        String date = sc.nextLine().trim();
        if (date.isEmpty()) { System.out.println("Дата обов'язкова!"); return; }

        System.out.println("\nДоступні зали:");
        List<Hall> halls = hallDAO.findAll();
        for (Hall h : halls) System.out.println("  " + h);
        System.out.print("ID залу: ");
        Integer hallId = readOptionalInt();

        System.out.println("\nДоступні куратори:");
        List<Curator> curators = curatorDAO.findAll();
        for (Curator c : curators) System.out.println("  " + c);
        System.out.print("ID куратора: ");
        Integer curId = readOptionalInt();

        System.out.print("Максимальна кількість відвідувачів: ");
        Integer maxVis = readOptionalInt();

        System.out.print("Опис події: ");
        String desc = sc.nextLine().trim();

        Event e = new Event(null, name, type, date, hallId, curId, maxVis, desc);
        
        try {
            eventDAO.createWithValidation(e);
            System.out.println("Подію заплановано з ID: " + e.getId());
        } catch (SQLException ex) {
            System.out.println("Помилка: " + ex.getMessage());
        }
    }

    private static void listEvents(String type, Integer hallId, String dateFrom, String dateTo) throws SQLException {
        List<Event> list = eventDAO.findAll(type, hallId, dateFrom, dateTo, "event_date", true);
        if (list.isEmpty()) {
            System.out.println("Подій не знайдено.");
        } else {
            System.out.println("\nЗнайдено " + list.size() + " подій:");
            for (Event e : list) {
                System.out.println("  " + e);
            }
        }
    }

    private static void listUpcomingEvents() throws SQLException {
        List<Event> list = eventDAO.findUpcoming();
        if (list.isEmpty()) {
            System.out.println("Майбутніх подій не знайдено.");
        } else {
            System.out.println("\nМайбутні події:");
            for (Event e : list) {
                System.out.println("  " + e);
            }
        }
    }

    private static void filterEventsByType() throws SQLException {
        System.out.println("Типи: EXHIBITION, EXCURSION, LECTURE, OTHER");
        System.out.print("Введіть тип: ");
        String type = sc.nextLine().trim().toUpperCase();
        listEvents(type, null, null, null);
    }

    private static void filterEventsByHall() throws SQLException {
        System.out.println("\nДоступні зали:");
        List<Hall> halls = hallDAO.findAll();
        for (Hall h : halls) System.out.println("  " + h);
        System.out.print("Введіть ID залу: ");
        Integer hallId = readOptionalInt();
        if (hallId != null) {
            listEvents(null, hallId, null, null);
        }
    }

    private static void editEvent() throws SQLException {
        System.out.print("Введіть ID події для редагування: ");
        Integer id = readOptionalInt();
        if (id == null) return;

        Event e = eventDAO.findById(id);
        if (e == null) { System.out.println("Подію не знайдено!"); return; }

        System.out.println("Поточні дані: " + e);
        System.out.print("Нова назва [Enter = залишити]: ");
        String name = sc.nextLine().trim();
        if (!name.isEmpty()) e.setName(name);

        System.out.print("Нова дата (YYYY-MM-DD) [Enter = залишити]: ");
        String date = sc.nextLine().trim();
        if (!date.isEmpty()) e.setEventDate(date);

        if (eventDAO.update(e)) {
            System.out.println("Подію оновлено!");
        } else {
            System.out.println("Помилка оновлення!");
        }
    }

    private static void deleteEvent() throws SQLException {
        System.out.print("Введіть ID події для скасування: ");
        Integer id = readOptionalInt();
        if (id == null) return;

        System.out.print("Ви впевнені? (y/n): ");
        if ("y".equalsIgnoreCase(sc.nextLine().trim())) {
            if (eventDAO.delete(id)) {
                System.out.println("Подію скасовано!");
            } else {
                System.out.println("Подію не знайдено!");
            }
        }
    }

    // ==================== УПРАВЛІННЯ КАТЕГОРІЯМИ ====================
    private static void manageCategories() throws SQLException {
        while (true) {
            System.out.println("\n--- КАТЕГОРІЇ ---");
            System.out.println("1. Додати категорію");
            System.out.println("2. Список категорій");
            System.out.println("3. Видалити категорію");
            System.out.println("0. Назад");
            System.out.print("Ваш вибір: ");

            String cmd = sc.nextLine().trim();
            switch (cmd) {
                case "1":
                    System.out.print("Назва категорії: ");
                    String name = sc.nextLine().trim();
                    System.out.print("Опис: ");
                    String desc = sc.nextLine().trim();
                    Category c = new Category(null, name, desc);
                    categoryDAO.create(c);
                    System.out.println("Категорію додано з ID: " + c.getId());
                    break;
                case "2":
                    List<Category> list = categoryDAO.findAll();
                    for (Category cat : list) {
                        System.out.println("  " + cat + " - " + (cat.getDescription() != null ? cat.getDescription() : ""));
                    }
                    break;
                case "3":
                    System.out.print("ID категорії: ");
                    Integer id = readOptionalInt();
                    if (id != null && categoryDAO.delete(id)) System.out.println("Видалено!");
                    break;
                case "0": return;
            }
        }
    }

    // ==================== УПРАВЛІННЯ ЗАЛАМИ ====================
    private static void manageHalls() throws SQLException {
        while (true) {
            System.out.println("\n--- ЗАЛИ ---");
            System.out.println("1. Додати зал");
            System.out.println("2. Список залів");
            System.out.println("3. Видалити зал");
            System.out.println("0. Назад");
            System.out.print("Ваш вибір: ");

            String cmd = sc.nextLine().trim();
            switch (cmd) {
                case "1":
                    System.out.print("Назва залу: ");
                    String name = sc.nextLine().trim();
                    System.out.print("Поверх: ");
                    int floor = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Місткість: ");
                    Integer cap = readOptionalInt();
                    Hall h = new Hall(null, name, floor, cap);
                    hallDAO.create(h);
                    System.out.println("Зал додано з ID: " + h.getId());
                    break;
                case "2":
                    List<Hall> list = hallDAO.findAll();
                    for (Hall hall : list) {
                        System.out.println("  " + hall + (hall.getCapacity() != null ? " [до " + hall.getCapacity() + " осіб]" : ""));
                    }
                    break;
                case "3":
                    System.out.print("ID залу: ");
                    Integer id = readOptionalInt();
                    if (id != null && hallDAO.delete(id)) System.out.println("Видалено!");
                    break;
                case "0": return;
            }
        }
    }

    // ==================== УПРАВЛІННЯ КУРАТОРАМИ ====================
    private static void manageCurators() throws SQLException {
        while (true) {
            System.out.println("\n--- КУРАТОРИ ---");
            System.out.println("1. Додати куратора");
            System.out.println("2. Список кураторів");
            System.out.println("3. Видалити куратора");
            System.out.println("0. Назад");
            System.out.print("Ваш вибір: ");

            String cmd = sc.nextLine().trim();
            switch (cmd) {
                case "1":
                    System.out.print("ПІБ: ");
                    String name = sc.nextLine().trim();
                    System.out.print("Email: ");
                    String email = sc.nextLine().trim();
                    System.out.print("Телефон: ");
                    String phone = sc.nextLine().trim();
                    System.out.print("Спеціалізація: ");
                    String spec = sc.nextLine().trim();
                    Curator cur = new Curator(null, name, email, phone, spec);
                    curatorDAO.create(cur);
                    System.out.println("Куратора додано з ID: " + cur.getId());
                    break;
                case "2":
                    List<Curator> list = curatorDAO.findAll();
                    for (Curator curator : list) {
                        System.out.println("  " + curator + " | " + curator.getEmail());
                    }
                    break;
                case "3":
                    System.out.print("ID куратора: ");
                    Integer id = readOptionalInt();
                    if (id != null && curatorDAO.delete(id)) System.out.println("Видалено!");
                    break;
                case "0": return;
            }
        }
    }

    // ==================== ТЕСТОВІ ДАНІ ====================
    private static void addTestData() throws SQLException {
        System.out.println("\nДодавання тестових даних...");

        // Категорії
        Category cat1 = categoryDAO.create(new Category(null, "Живопис", "Картини та полотна"));
        Category cat2 = categoryDAO.create(new Category(null, "Скульптура", "Скульптурні роботи"));
        Category cat3 = categoryDAO.create(new Category(null, "Археологія", "Археологічні знахідки"));
        Category cat4 = categoryDAO.create(new Category(null, "Декоративне мистецтво", "Предмети декору"));

        // Зали
        Hall hall1 = hallDAO.create(new Hall(null, "Античний зал", 1, 50));
        Hall hall2 = hallDAO.create(new Hall(null, "Зал імпресіоністів", 2, 40));
        Hall hall3 = hallDAO.create(new Hall(null, "Виставковий зал", 1, 100));
        Hall hall4 = hallDAO.create(new Hall(null, "Зал скульптури", 1, 30));

        // Куратори
        Curator cur1 = curatorDAO.create(new Curator(null, "Іванов Петро Сергійович", "ivanov@museum.ua", "+380501234567", "Античне мистецтво"));
        Curator cur2 = curatorDAO.create(new Curator(null, "Петренко Марія Іванівна", "petrenko@museum.ua", "+380507654321", "Живопис XIX-XX ст."));
        Curator cur3 = curatorDAO.create(new Curator(null, "Сидоренко Олег Вікторович", "sydorenko@museum.ua", "+380509876543", "Скульптура"));

        // Експонати
        exhibitDAO.create(new Exhibit(null, "Амфора грецька", "Червонофігурна амфора V ст. до н.е.", "V ст. до н.е.", "2020-03-15", cat3.getId(), hall1.getId(), cur1.getId()));
        exhibitDAO.create(new Exhibit(null, "Сонячний ранок", "Картина імпресіоніста", "XIX століття", "2019-06-22", cat1.getId(), hall2.getId(), cur2.getId()));
        exhibitDAO.create(new Exhibit(null, "Бронзова статуя воїна", "Римська скульптура", "II ст. н.е.", "2018-11-10", cat2.getId(), hall4.getId(), cur3.getId()));
        exhibitDAO.create(new Exhibit(null, "Золотий келих", "Середньовічний келих для вина", "XV століття", "2021-01-05", cat4.getId(), hall1.getId(), cur1.getId()));
        exhibitDAO.create(new Exhibit(null, "Пейзаж з вітряком", "Голландський живопис", "XVII століття", "2022-08-30", cat1.getId(), hall2.getId(), cur2.getId()));

        // Події
        eventDAO.create(new Event(null, "Ніч музеїв 2025", "EXHIBITION", "2025-05-18", hall3.getId(), cur1.getId(), 200, "Щорічна акція Ніч музеїв"));
        eventDAO.create(new Event(null, "Екскурсія для школярів", "EXCURSION", "2025-02-15", hall1.getId(), cur1.getId(), 25, "Екскурсія для учнів 5-7 класів"));
        eventDAO.create(new Event(null, "Лекція: Імпресіонізм", "LECTURE", "2025-03-10", hall2.getId(), cur2.getId(), 30, "Лекція про напрямок імпресіонізму"));

        System.out.println("Тестові дані успішно додано!");
    }

    // ==================== ДОПОМІЖНІ МЕТОДИ ====================
    private static Integer readOptionalInt() {
        String input = sc.nextLine().trim();
        if (input.isEmpty()) return null;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
