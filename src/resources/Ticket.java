package resources;

public class Ticket {
    private final String key;
    private Commit commit;

    public Ticket(String key) {
        this.key = key;
        this.commit = null;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    public Commit getCommit() {
        return this.commit;
    }

    public String getKey() {
        return this.key;
    }
}
