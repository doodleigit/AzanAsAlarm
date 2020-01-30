package azanasalarm.net.doodlei.android.azanasalarm.util;

public class PrepareTwelveHourFormat {


    private String am_pm = "am";

    public String twelveHourformat(int hour, int min) {
        String minitue;
        if (min < 10) {
            minitue = "0" + min;
        } else {
            minitue = min + "";
        }

        String hourformat;
        if (hour == 0) {
            hourformat = "12";
            this.am_pm = "am";
        } else if (hour == 12) {
            this.am_pm = "pm";
            hourformat = "12";
        } else if (hour < 12) {
            this.am_pm = "am";
            hourformat = hour + "";
        } else {
            hourformat = hour - 12 + "";
            this.am_pm = "pm";
        }


        return hourformat + ":" + minitue;
    }

    public String AmPm() {
        return this.am_pm;
    }


    public String differerence(long difference) {
        String decesion = " Alarm is Set";
        if (difference < 0) {
            difference = difference + 86400000L;
            long sec = difference / 1000;
            long min;
            long hour = sec / 60 / 60;
            min = sec / 60 % 60;
            if (hour == 0) {
                decesion = decesion + " after " + min + " minutes ";
            } else if (hour == 1) {
                decesion = decesion + " after " + (hour) + " hour " + (min) + " minutes ";
            } else {
                decesion = decesion + " after " + (hour) + " hours " + (min) + " minutes ";
            }


        } else if (difference > 0) {

            long sec = difference / 1000;
            long min;
            long hour = sec / 60 / 60;
            min = sec / 60 % 60;
            if (hour == 0) {
                decesion = decesion + " after " + min + " minutes ";
            } else if (hour == 1) {
                decesion = decesion + " after " + (hour) + " hour " + (min) + " minutes ";
            } else {
                decesion = decesion + " after " + (hour) + " hours " + (min) + " minutes ";
            }


        }


        return decesion;
    }


}
