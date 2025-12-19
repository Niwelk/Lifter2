package elevator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Класс Dispatcher представляет собой центральный диспетчер лифтовой системы,
 * который управляет распределением вызовов между доступными лифтами.
 *
 * <p>Основные функции диспетчера:
 * <ul>
 *   <li>Прием и очередь запросов пассажиров на вызов лифта</li>
 *   <li>Выбор оптимального лифта для каждого запроса на основе алгоритма оценки</li>
 *   <li>Координация работы нескольких лифтов одновременно</li>
 *   <li>Мониторинг состояния системы и очереди запросов</li>
 * </ul>
 *
 * <p>Диспетчер работает в отдельном потоке и использует механизмы синхронизации
 * для безопасной обработки запросов в многопоточной среде.
 *
 * @see Elevator
 * @see PassengerRequest
 * @see Status
 * @see Direction
 */

 public class Dispatcher implements Runnable {
    private List<Elevator> elevators;
    private Queue<PassengerRequest> passengerRequests;
    private Thread management;
    private volatile boolean launch = true;
    private final Object lock = new Object();

    public Dispatcher() {
        this.elevators = new ArrayList<>();
        this.passengerRequests = new LinkedList<>();
        this.management = new Thread(this, "Dispatcher");
    }

    public void addElevator(Elevator elevator) {
        elevators.add(elevator);
        System.out.println("Добавлен лифт #" + elevator.getId());
    }

    public void callElevator(int floorCall, int floorTarget) {
        synchronized (lock) {
            PassengerRequest request = new PassengerRequest(floorCall, floorTarget);
            passengerRequests.add(request);
            System.out.println("Вызов: " + floorCall + " → " + floorTarget);
            lock.notifyAll();
        }
    }

    public void start() {
        for (Elevator elevator : elevators) {
            elevator.start();
        }
        management.start();
        System.out.println("[!] Система запущена");
    }

    public void stop() {
        launch = false;
        management.interrupt();

        for (Elevator elevator : elevators) {
            elevator.stop();
        }
        System.out.println("[!] Система остановлена");
    }

    @Override
    public void run() {
        while (launch) {
            try {
                PassengerRequest request = waitForRequest();

                if (request != null) {
                    processRequest(request);
                }

            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private PassengerRequest waitForRequest() throws InterruptedException {
        synchronized (lock) {
            while (passengerRequests.isEmpty() && launch) {
                lock.wait(100);
            }

            if (!passengerRequests.isEmpty()) {
                return passengerRequests.poll();
            }

            return null;
        }
    }

    private void processRequest(PassengerRequest request) {
        System.out.println("Обработка: " + request);

        Elevator bestElevator = findBestElevator(request);

        if (bestElevator != null) {

            bestElevator.callToFloor(request.getFloorCall());


            bestElevator.addTargetFloor(request.getFloorTarget());

            System.out.println("Назначен лифт #" + bestElevator.getId() + " для " + request);
        } else {
            System.out.println("Нет подходящего лифта для " + request);
        }
    }

    private Elevator findBestElevator(PassengerRequest request) {
        if (elevators.isEmpty()) {
            return null;
        }

        Elevator bestElevator = null;
        int bestScore = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            int score = calculateScore(elevator, request);

            if (score < bestScore) {
                bestScore = score;
                bestElevator = elevator;
            }
        }

        return bestElevator;
    }

    private int calculateScore(Elevator elevator, PassengerRequest request) {
        int distance = Math.abs(elevator.getCurrentFloor() - request.getFloorCall());
        int score = distance * 10;

        if (elevator.getStatus() == Status.STOPPED &&
                elevator.getDirection() == Direction.NO_ACTIVE) {
            score -= 50;
        }

        if (elevator.getDirection() == request.getDirection()) {
            if (elevator.getDirection() == Direction.UP &&
                    request.getFloorCall() >= elevator.getCurrentFloor()) {
                score -= 30;
            } else if (elevator.getDirection() == Direction.DOWN &&
                    request.getFloorCall() <= elevator.getCurrentFloor()) {
                score -= 30;
            }
        }

        score += elevator.getFloors().size() * 5;

        return score;
    }

    public void showStatus() {
        synchronized (lock) {
            System.out.println("\n\\/\\/\\/\\ СТАТУС \\/\\/\\/\\");
            System.out.println("[i] Запросов в очереди: " + passengerRequests.size());

            for (Elevator elevator : elevators) {
                System.out.println("Лифт #" + elevator.getId() +
                        " на " + elevator.getCurrentFloor() +
                        " этаже, " + elevator.getStatus());
            }
            System.out.println("\\/\\/\\/\\\n");
        }
    }

    public int getQueueSize() {
        synchronized (lock) {
            return passengerRequests.size();
        }
    }
}
