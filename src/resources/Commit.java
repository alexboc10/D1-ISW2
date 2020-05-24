package resources;

import java.time.LocalDate;

public class Commit {
    private final LocalDate date;

    public Commit(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }
}
