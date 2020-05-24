package resources;

import java.time.LocalDate;

public class Commit {
    private final String hash;
    private final LocalDate date;

    public Commit(String hash, LocalDate date) {
        this.hash = hash;
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }
}
