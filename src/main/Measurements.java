package main;

import resources.Project;

public class Measurements {

    public static void main(String[] args) throws Exception {
        Project myProject = new Project("beam");

        //Extracting tickets.
        myProject.extractTickets();

        //Extracting commits with corresponding ticket, sorted by date.
        myProject.extractCommits();

        myProject.computeData();
    }
}
