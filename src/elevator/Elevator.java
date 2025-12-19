package elevator;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;

/**
 * Класс, представляющий лифт в системе управления лифтами.
 * <p>
 * Каждый лифт работает в отдельном потоке ({@link Runnable}) и управляет своим движением
 * между этажами в здании. Лифт получает задания от диспетчера ({@link Dispatcher}) и
 * выполняет их в порядке оптимизации маршрута.
 * </p>
 *
 * <p><b>Основные возможности:</b></p>
 * <ul>
 *   <li>Движение вверх/вниз между этажами с проверкой границ</li>
 *   <li>Остановка на заданных этажах для посадки/высадки пассажиров</li>
 *   <li>Открытие/закрытие дверей с симуляцией задержек</li>
 *   <li>Управление очередью целевых этажей</li>
 *   <li>Потокобезопасное взаимодействие с диспетчером</li>
 * </ul>
 *
 * @see Dispatcher
 * @see PassengerRequest
 * @see Direction
 * @see Status
 * @since 1.0
 */

 public class Elevator implements Runnable {
    private int id;
    private static int nextId = 1;
    private static int sizeFloors = 9;
    private int currentFloor;
    public Direction direction;
    public Status status;
    public Set<Integer> floors;
    public List<Integer> passengers;
    private static final int MIN_FLOOR = 0;
    private static final int MAX_FLOOR = 9;

    private Thread elevatorThread;
    private volatile boolean running = true;

    public Elevator() {
        this.id = nextId++;
        this.currentFloor = 1;
        direction = Direction.NO_ACTIVE;
        status = Status.STOPPED;
        this.floors = new TreeSet<>();
        this.passengers = new ArrayList<>();
    }

    public Status getStatus() {
        return status;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public Set<Integer> getFloors() {
        return floors;
    }

    public int getId() {
        return id;
    }


    public void start() {
        elevatorThread = new Thread(this, "Elevator-" + id);
        running = true;
        elevatorThread.start();
    }

    public void stop() {
        running = false;
        if (elevatorThread != null) {
            elevatorThread.interrupt();
        }
    }

    public void callToFloor(int floor) {
        if (floor < MIN_FLOOR || floor > MAX_FLOOR) {
            System.out.println("[ERROR]: Этаж " + floor + " не существует (диапазон: "
                    + MIN_FLOOR + "-" + MAX_FLOOR + ")");
            return;
        }
        synchronized (this) {
            floors.add(floor);
            System.out.println("Лифт #" + id + " вызван на этаж " + floor);
            notify();
        }
    }

    public void addTargetFloor(int floor) {
        if (floor < MIN_FLOOR || floor > MAX_FLOOR) {
            System.out.println("[ERROR]: Целевой этаж " + floor + " не существует (диапазон: "
                    + MIN_FLOOR + "-" + MAX_FLOOR + ")");
            return;
        }
        synchronized (this) {
            floors.add(floor);
            System.out.println("Лифт #" + id + " получил цель: этаж " + floor);
            notify();
        }
    }


    public void moveUp() {
        if (this.currentFloor >= MAX_FLOOR) {  // Было == sizeFloors-1 // для улучшение теперь константа
            System.out.println("[ERROR]: Лифт не может подняться вверх (уже на максимальном этаже)..");
            return;
        }
        this.currentFloor++;
        direction = Direction.UP;
        status = Status.MOVING;
        System.out.println("[!] Лифт #"+id+" поднялся на этаж "+currentFloor);
    }

    public void moveDown() {
        if (this.currentFloor <= 0) {  // Было == 0, теперь <= 0  // возникал баг при работе обработки значений <0
            System.out.println("[ERROR]: Лифт не может опуститься вниз (уже на минимальном этаже)..");
            return;
        }
        this.currentFloor--;
        direction = Direction.DOWN;
        status = Status.MOVING;
        System.out.println("[!] Лифт #"+id+" опустился на этаж "+currentFloor);
    }

    public void moveStop() {
        direction = Direction.NO_ACTIVE;
        status = Status.STOPPED;
        System.out.println("[!] Остановка лифта #"+id+" на этаже "+currentFloor);
    }

    public void openDoors() {
        status = Status.DOORS_OPENING;
        System.out.println("[!] Лифт #"+id+" открывает двери на этаже "+currentFloor);
    }

    public void closeDoors() {
        status = Status.DOORS_CLOSING;
        System.out.println("[!] Лифт #"+id+" закрывает двери на этаже "+currentFloor);
    }


    @Override
    public void run() {
        System.out.println("Лифт #" + id + " запущен");

        while (running) {
            try {
                performCycle();
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                System.out.println("Лифт #" + id + " остановлен");
                break;
            }
        }
    }

    private void performCycle() {
        if (floors.isEmpty()) {

            if (status != Status.STOPPED || direction != Direction.NO_ACTIVE) {
                moveStop();
            }
            return;
        }

        Integer target = getNextTarget();
        if (target != null) {
            if (target > currentFloor) {
                moveUp();
            } else if (target < currentFloor) {
                moveDown();
            }

            if (currentFloor == target) {
                handleArrival(target);
            }
        }
    }

    private Integer getNextTarget() {
        if (floors.isEmpty()) return null;

        if (direction == Direction.UP) {
            for (int floor : floors) {
                if (floor > currentFloor) return floor;
            }
        } else if (direction == Direction.DOWN) {
            for (int floor : floors) {
                if (floor < currentFloor) return floor;
            }
        }

        return floors.iterator().next();
    }

    private void handleArrival(int floor) {
        moveStop();
        openDoors();

        System.out.println("Лифт #" + id + " на этаже " + floor +
                ": пассажиры выходят/заходят");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        closeDoors();
        floors.remove(floor);

        System.out.println("Лифт #" + id + " покинул этаж " + floor);
    }
}
