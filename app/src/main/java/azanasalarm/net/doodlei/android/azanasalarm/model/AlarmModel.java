package azanasalarm.net.doodlei.android.azanasalarm.model;

public class AlarmModel {


    private String time;
    private String ampm;
    private String notes;
    private String isSet;
    private String isAuto;
    private String min;
    private String ringtone;
    private String alarmtype;
    private String repeat;
    private String name;
    private String salahreminder;
    private int id;

    public AlarmModel() {
    }

    public AlarmModel(String time, String ampm, String notes, String isSet, String isAuto, String min, String ringtone, String alarmtype, String repeat, String name, String salahreminder, int id) {
        this.time = time;
        this.ampm = ampm;
        this.notes = notes;
        this.isSet = isSet;
        this.isAuto = isAuto;
        this.min = min;
        this.ringtone = ringtone;
        this.alarmtype = alarmtype;
        this.repeat = repeat;
        this.id = id;
        this.name = name;
        this.salahreminder = salahreminder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAmpm() {
        return ampm;
    }

    public void setAmpm(String ampm) {
        this.ampm = ampm;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIsSet() {
        return isSet;
    }

    public void setIsSet(String isSet) {
        this.isSet = isSet;
    }

    public String getIsAuto() {
        return isAuto;
    }

    public void setIsAuto(String isAuto) {
        this.isAuto = isAuto;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getRingtone() {
        return ringtone;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public String getAlarmtype() {
        return alarmtype;
    }

    public void setAlarmtype(String alarmtype) {
        this.alarmtype = alarmtype;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getSalahreminder() {
        return salahreminder;
    }

    public void setSalahreminder(String salahreminder) {
        this.salahreminder = salahreminder;
    }
}
