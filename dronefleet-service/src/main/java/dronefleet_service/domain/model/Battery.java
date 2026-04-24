package dronefleet_service.domain.model;

public record Battery(int percent) {

    public Battery {
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
    }

    public int percent() { return percent; }

    public Battery consumePercent(int consumed) {
        int next = Math.max(0, percent - consumed);
        return new Battery(next);
    }

    public Battery consumeForKm(double km, double percentPerKm) {
        int consumed = (int) Math.ceil(km * percentPerKm);
        return consumePercent(consumed);
    }


    public Battery recharge() {
        return new Battery(100);
    }

    /*
    public Battery rechargePerMinutes(int minutes, int percentPerMinute) {
        int next = Math.min(100, percent + minutes * percentPerMinute);
        return new Battery(next);
    }
    */

}
