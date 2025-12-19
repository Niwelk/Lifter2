package elevator;

import java.util.Random;

/**
 * Класс PassengerRequestGenerator представляет генератор случайных запросов пассажиров
 * для лифтовой системы. Работает в отдельном потоке и автоматически создает запросы
 * через случайные интервалы времени.
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Автоматическая генерация запросов с случайными этажами вызова и назначения</li>
 *   <li>Работа в фоновом потоке с настраиваемой частотой генерации</li>
 *   <li>Возможность генерации пакета запросов по требованию</li>
 *   <li>Интеграция с {@link Dispatcher} для передачи сгенерированных запросов</li>
 * </ul>
 *
 * @see Dispatcher
 * @see PassengerRequest
 * @see Random
 * @see Thread
 */
 public class PassengerRequestGenerator implements Runnable {
    private final Dispatcher dispatcher;
    private final Random random = new Random();
    private volatile boolean running = true;
    private Thread generatorThread;

    private static final int MIN_FLOOR = 0;
    private static final int MAX_FLOOR = 9;
    private static final int MIN_INTERVAL = 2000;
    private static final int MAX_INTERVAL = 8000;

    public PassengerRequestGenerator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void start() {
        generatorThread = new Thread(this, "RequestGenerator");
        generatorThread.start();
    }

    public void stop() {
        running = false;
        if (generatorThread != null) {
            generatorThread.interrupt();
        }
    }

    @Override
    public void run() {
        int requestId = 1;

        while (running) {
            try {

                int delay = MIN_INTERVAL + random.nextInt(MAX_INTERVAL - MIN_INTERVAL);
                Thread.sleep(delay);

                int fromFloor, toFloor;

                do {
                    fromFloor = MIN_FLOOR + random.nextInt(MAX_FLOOR - MIN_FLOOR + 1);
                    toFloor = MIN_FLOOR + random.nextInt(MAX_FLOOR - MIN_FLOOR + 1);
                } while (fromFloor == toFloor);

                System.out.println("[i] Поступил запрос #" + requestId + " с " + fromFloor + " на " + toFloor);
                dispatcher.callElevator(fromFloor, toFloor);
                requestId++;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void generateBatch(int count) {

        for (int i = 0; i < count; i++) {
            int fromFloor, toFloor;

            do {
                fromFloor = MIN_FLOOR + random.nextInt(MAX_FLOOR - MIN_FLOOR + 1);
                toFloor = MIN_FLOOR + random.nextInt(MAX_FLOOR - MIN_FLOOR + 1);
            } while (fromFloor == toFloor);

            dispatcher.callElevator(fromFloor, toFloor);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
