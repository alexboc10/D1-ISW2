package resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utility.CommandLine;
import utility.DataItem;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Project {
    private static final String WRITING = "Writing on file";
    private static final Logger logger = Logger.getLogger(Project.class.getName());
    private final String name;
    private int totalTickets;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Ticket> tickets;
    private List<DataItem> data;
    private Double[] statistics;
    private int taggedTickets;

    public Project(String name) {
        this.name = name;
        this.totalTickets = 0;
        this.tickets = new ArrayList<>();
        this.data = new ArrayList<>();
        this.startDate = LocalDate.MAX;
        this.endDate = LocalDate.MIN;
        this.statistics = new Double[3];
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static JSONObject readJsonFromUrl (String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();

        try(BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    private void writeStatistics() {
        try(FileWriter csvFeatures = new FileWriter("/home/alex/code/intelliJ/projects/D1-ISW2/data/" + this.name + "_NewFeature_Statistics.csv")) {

            logger.log(Level.FINE, WRITING);

            csvFeatures.append("Tagged");
            csvFeatures.append(",");
            csvFeatures.append("Mean");
            csvFeatures.append(",");
            csvFeatures.append("Standard Deviation");
            csvFeatures.append("\n");

            int i = 0;
            for (double statIndex: this.statistics) {
                if (i > 0) {
                    csvFeatures.append(",");
                }
                csvFeatures.append(Double.toString(statIndex));

                i++;
            }

            csvFeatures.append("\n\n");

            csvFeatures.append("Date");
            csvFeatures.append(",");
            csvFeatures.append("Number");
            csvFeatures.append("\n");

            for (DataItem item : this.data) {
                csvFeatures.append(item.getDate());
                csvFeatures.append(",");
                csvFeatures.append(Integer.toString(item.getNumber()));
                csvFeatures.append("\n");
            }

            csvFeatures.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void computeData() {
        LocalDate actDate;

        logger.log(Level.FINE, "Fixing tagged tickets");

        this.data.add(new DataItem(this.startDate));
        actDate = startDate.plusMonths(1);

        while (actDate.getMonthValue() != endDate.getMonthValue() || actDate.getYear() != endDate.getYear()) {
            this.data.add(new DataItem(actDate));
            actDate = actDate.plusMonths(1);
        }

        this.data.add(new DataItem(this.endDate));

        for (Ticket ticket : this.tickets) {
            if (ticket.getCommit() == null) {
                continue;
            }
            for (DataItem item : data) {
                if ((ticket.getCommit().getDate().getMonthValue() + "/" + ticket.getCommit().getDate().getYear()).equals(item.getDate())) {
                    item.addTicket(ticket);
                }
            }
        }

        computeStatistics();
        writeStatistics();
    }

    public void computeStatistics() {
        int monthNum = this.data.size();
        double stDev = 0.0;

        logger.log(Level.FINE, "Computing statistics");

        //% of tagged commits
        BigDecimal bdPerc = BigDecimal.valueOf((double) this.taggedTickets / (double) this.totalTickets);
        bdPerc = bdPerc.setScale(2, RoundingMode.HALF_UP);
        statistics[0] = bdPerc.doubleValue();

        //Data mean
        BigDecimal bdMean = BigDecimal.valueOf((double) (this.taggedTickets) / (double) monthNum);
        bdMean = bdMean.setScale(2, RoundingMode.HALF_UP);
        statistics[1] = bdMean.doubleValue();

        //Data standard deviation
        for (DataItem item : data) {
            stDev = stDev + pow(((double) (item.getNumber())) - statistics[1], 2);
        }

        BigDecimal bdDev = BigDecimal.valueOf(sqrt(stDev / monthNum));
        bdDev = bdDev.setScale(2, RoundingMode.HALF_UP);
        statistics[2] = bdDev.doubleValue();
    }

    public void extractCommits() throws IOException {
        CommandLine command = new CommandLine();
        String output;
        String[] lines;
        String[] info;

        logger.log(Level.FINE, "Extracting Commits");

        int index=0;
        for (Ticket ticket : this.tickets) {
            index++;
            System.out.println("Ticket " + index + "/" + this.tickets.size());

            command.setCommand("git log --date=iso-strict --grep=" + ticket.getKey() + " -F --pretty=format:'%H'BREAK'%cd'END | sort -r", "/home/alex/code/ISW2/" + this.name);
            output = command.executeCommand();

            if (output.equals("")) {
                continue;
            }

            this.taggedTickets++;

            lines = output.split("END", 0);
            info = lines[0].split("BREAK", 0);

            if (info.length == 2) {
                ticket.setCommit(new Commit(info[0], LocalDate.parse(info[1].substring(0,10))));
                if (LocalDate.parse(info[1].substring(0,10)).isBefore(this.startDate)) {
                    this.startDate = LocalDate.parse(info[1].substring(0,10));
                } else if (LocalDate.parse(info[1].substring(0,10)).isAfter(this.endDate)) {
                    this.endDate = LocalDate.parse(info[1].substring(0,10));
                }
            }
        }
    }

    public void extractTickets() throws IOException {
        int j;
        int i = 0;

        logger.log(Level.FINE, "Extracting Tickets");
        do {
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + this.name;
            url += "%22AND%22issueType%22=%22Bug%22";
            url += "AND(%22status%22=%22resolved%22OR%22status%22=%22closed%22)";
            url += "AND%22resolution%22=%22fixed%22&fields=key,resolutiondate&startAt=" + i + "&maxResults=" + j;

            JSONObject json = readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");

            this.totalTickets = json.getInt("total");

            for (; i < this.totalTickets && i < j; i++) {

                System.out.println("Ticket " + i + "/" + this.totalTickets);

                logger.log(Level.FINE, "{}", i+1 + "/" + this.totalTickets);

                String key = issues.getJSONObject(i%1000).get("key").toString();

                this.tickets.add(new Ticket(key));
            }
        } while (i < this.totalTickets);
    }
}
