package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandLine {
    String command;
    String baseDir;
    ProcessBuilder builder;

    public CommandLine() {
        this.command = "";
        this.baseDir = "/.";
        this.builder = null;
    }

    public void setCommand(String command, String baseDir) {
        this.command = command;
        this.baseDir = baseDir;
    }

    public String executeCommand() throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", this.command);
        processBuilder.directory(new File(this.baseDir));

        Process p = processBuilder.start();

        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        StringBuilder processOutput = new StringBuilder();

        while ((line = r.readLine()) != null) {
            processOutput.append(line);
        }

        return processOutput.toString();

    }

}
