package utility;

import resources.Ticket;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataItem {
    List<Ticket> tickets;
    String date;
    Integer number;

    public DataItem(LocalDate date) {
        this.number = 0;
        this.date = date.getMonthValue() + "/" + date.getYear();
        this.tickets = new ArrayList<>();
    }

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
        this.number = this.tickets.size();
    }

    public int getNumber() {
        return this.number;
    }

    public String getDate() {
        return this.date;
    }
}
