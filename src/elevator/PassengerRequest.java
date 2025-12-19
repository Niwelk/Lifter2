package elevator;

public class PassengerRequest {
    private int floorCall;
    private int floorTarget;
    private long timeOfCreating;

    public PassengerRequest(int floorCall, int floorTarget) {

        if (floorCall == floorTarget) {
            throw new IllegalArgumentException("[ERROR}: нельзя, чтобы этажи совпали..");
        }

        this.floorCall = floorCall;
        this.floorTarget = floorTarget;
        this.timeOfCreating = System.currentTimeMillis();
    }

    public int getFloorCall() {
        return floorCall;
    }

    public int getFloorTarget() {
        return floorTarget;
    }

    public long getTimeOfCreating() {
        return timeOfCreating;
    }

    public Direction getDirection() {
        if (floorTarget > floorCall) {
            return Direction.UP;
        } else {
            return Direction.DOWN;
        }
    }

    public double getWaitingTimeSeconds() {
        return (System.currentTimeMillis() - timeOfCreating) / 1000.0;
    }

    @Override
    public String toString() {
        return floorCall + " → " + floorTarget;
    }
}
