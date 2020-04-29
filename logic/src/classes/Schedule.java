package classes;

import enums.Recurrences;

public class Schedule {
    private int hour;
    private int min;
    private int day;
    private Recurrences recurrences;

    public Schedule(int hour, int day, String rec ){
        this.hour = hour;
        this.day = day;
    }

    public void setRecurrences(String rec) {

        switch(rec){
            case "OneTime" : {this.recurrences = Recurrences.ONE_TIME; break;}
            case "Daily" : {this.recurrences = Recurrences.DAILY; break;}
            case "BiDaily " : {this.recurrences = Recurrences.BIDAIILY; break;}
            case "Weekly " : {this.recurrences = Recurrences.WEEKLY; break;}
            case "Monthly " : {this.recurrences = Recurrences.MONTHLY; break;}

        }


    }
}
