import elevator.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n/\\/\\/\\/ АВТОМАТИЧЕСКИ СИСТЕМА УПРАВЛЕНИЯ ЛИФТАМИ ЗАПУЩЕНА /\\/\\/\\/");

        System.out.print("[i] Введите количество лифтов (1-5): ");
        int elevatorCount = getValidInput(scanner, 1, 5, 3);

        System.out.println("[!] Выбирите режим работы:");
        System.out.println("\t[ФИКСИРОВАННЫЙ] программа работает засчёт конкретного кол-ва итераций:  выберите 1");
        System.out.println("\t[НЕОГРАНИЧЕННЫЙ] программа работает постоянно:  выберите 2");

        int mode = getValidInput(scanner, 1, 2, 1);

        int iterations = 0;
        if (mode == 1) {
            System.out.print("[!] Введите количество итераций (5-50): ");
            iterations = getValidInput(scanner, 5, 50, 20);
            System.out.println("[!] Будет выполнено итераций: " + iterations + "\n");
        } else {
            System.out.println("[!] Режим: бесконечный (остановите вручную)\n");
        }

        Dispatcher dispatcher = new Dispatcher();
        PassengerRequestGenerator generator = new PassengerRequestGenerator(dispatcher);

        for (int i = 0; i < elevatorCount; i++) {
            dispatcher.addElevator(new Elevator());
        }

        System.out.println("\n[!] ЗАПУСК..");

        dispatcher.start();
        generator.start();
        Thread.sleep(1000);

        if (mode == 1) {

            for (int i = 1; i <= iterations; i++) {
                System.out.println("= = = = = = = = =");
                Thread.sleep(3000);

                if (i % 5 == 0 || i == iterations) {
                    dispatcher.showStatus();
                }

                if (i % 3 == 0) {
                    generator.generateBatch(2);
                }
            }

            System.out.println("\nПрограмма завершена успешно..");

        } else {

            dispatcher.showStatus();

            scanner.nextLine();
        }

        System.out.println("\n[!] Аварийная остановка программы..");

        generator.stop();
        Thread.sleep(1000);

        dispatcher.showStatus();

        dispatcher.stop();
        scanner.close();

        System.out.println("\nПрограмма завершена успешно..");
    }

    private static int getValidInput(Scanner scanner, int min, int max, int defaultValue) {
        while (true) {
            try {
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Используется значение по умолчанию: " + defaultValue);
                    return defaultValue;
                }

                int value = Integer.parseInt(input);

                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.printf("[ERROR]: Введите число от %d до %d: ", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.printf("[ERROR]: Введите число от %d до %d: ", min, max);
            }
        }
    }
}