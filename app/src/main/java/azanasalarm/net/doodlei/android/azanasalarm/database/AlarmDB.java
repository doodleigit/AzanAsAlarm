package azanasalarm.net.doodlei.android.azanasalarm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import azanasalarm.net.doodlei.android.azanasalarm.model.AlarmModel;

public class AlarmDB extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "AlarmHistory";
    // Contacts table name
    private static final String TABLE_ALARM_HISTORY = "AlarmEntries";


    // Shops Table Columns names
    private static final String id = "id";
    private static final String time = "time";
    private static final String ampm = "am_pm";
    private static final String notes = "notes";
    private static final String isSet = "isSet";
    private static final String isAuto = "isAuto";
    private static final String min = "minute";

    private static final String ringtone = "ringtone";
    private static final String alarmtype = "alarmtype";
    private static final String repeat = "repeat";
    private static final String name = "name";
    private static final String salahreminder = "salahreminder";


    public AlarmDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_ALARM_HISTORY + "("
                + id + " INTEGER PRIMARY KEY," + time + " TEXT," + ampm + " TEXT," + notes + " TEXT," + isSet + " TEXT,"+ isAuto + " TEXT," + min + " TEXT," + ringtone + " TEXT," + alarmtype + " TEXT," + repeat + " TEXT," + name + " TEXT," + salahreminder + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);


    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (newVersion > oldVersion) {
//            db.execSQL("ALTER TABLE " + TABLE_ALARM_HISTORY + " ADD COLUMN " + isAuto + " TEXT " + "DEFAULT " + "yes");
//        }

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARM_HISTORY);
        onCreate(db);
    }


    public void addHistory(AlarmModel history) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(id, history.getId());
        values.put(time, history.getTime());
        values.put(ampm, history.getAmpm());
        values.put(notes, history.getNotes());
        values.put(isSet, history.getIsSet());
        values.put(isAuto, history.getIsAuto());
        values.put(min, history.getMin());
        values.put(ringtone, history.getRingtone());
        values.put(alarmtype, history.getAlarmtype());
        values.put(repeat, history.getRepeat());
        values.put(name, history.getName());
        values.put(salahreminder, history.getSalahreminder());
        db.insert(TABLE_ALARM_HISTORY, null, values);
        db.close();
    }

    public List<AlarmModel> getAllHistory() {
        List<AlarmModel> historyList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_ALARM_HISTORY;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                AlarmModel historyModelClass = new AlarmModel();
                historyModelClass.setId(Integer.parseInt(cursor.getString(0)));
                historyModelClass.setTime(cursor.getString(1));
                historyModelClass.setAmpm(cursor.getString(2));
                historyModelClass.setNotes(cursor.getString(3));
                historyModelClass.setIsSet(cursor.getString(4));
                historyModelClass.setIsAuto(cursor.getString(5));
                historyModelClass.setMin(cursor.getString(6));
                historyModelClass.setRingtone(cursor.getString(7));
                historyModelClass.setAlarmtype(cursor.getString(8));
                historyModelClass.setRepeat(cursor.getString(9));
                historyModelClass.setName(cursor.getString(10));
                historyModelClass.setSalahreminder(cursor.getString(11));
                historyList.add(historyModelClass);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return historyList;
    }

    public int getHistoryCount() {
        String countQuery = "SELECT * FROM " + TABLE_ALARM_HISTORY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }


    public int getIdForNextAlarm() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        long seq = 0;
        try {
            String sql = "select seq from sqlite_sequence where name=?";
            c = db.rawQuery(sql, new String[]{TABLE_ALARM_HISTORY});
            if (c.moveToFirst()) {
                seq = c.getLong(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        db.close();
        c.close();
        return (int) (seq + 1);
        //cursor.close();

        //db.close();
        //return 1;

    }


    public AlarmModel getAlarm(int alarmid) {
        SQLiteDatabase db = this.getReadableDatabase();
        AlarmModel history = new AlarmModel();
        Cursor cursor = db.query(TABLE_ALARM_HISTORY, new String[]{id,
                        time, ampm, notes, isSet, isAuto, min, ringtone, alarmtype, repeat, name, salahreminder}, id + "=?",
                new String[]{String.valueOf(alarmid)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            history = new AlarmModel(cursor.getString(1), cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), cursor.getString(10), cursor.getString(11), Integer.parseInt(cursor.getString(0)));
        }
        cursor.close();
        db.close();
        return history;
    }

    public void updateAlarm(int alarmid, AlarmModel alarmModel) {

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(id, alarmid);
        values.put(time, alarmModel.getTime());
        values.put(ampm, alarmModel.getAmpm());
        values.put(notes, alarmModel.getNotes());
        values.put(isSet, alarmModel.getIsSet());
        values.put(isAuto, alarmModel.getIsAuto());
        values.put(min, alarmModel.getMin());
        values.put(ringtone, alarmModel.getRingtone());
        values.put(alarmtype, alarmModel.getAlarmtype());
        values.put(repeat, alarmModel.getRepeat());
        values.put(salahreminder, alarmModel.getSalahreminder());
        db.update(TABLE_ALARM_HISTORY, values, id + "=?", new String[]{String.valueOf(alarmid)});
        db.close();


    }

    public void updateCheckedAlarm(AlarmModel alarmModel) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(id, alarmModel.getId());
        values.put(time, alarmModel.getTime());
        values.put(ampm, alarmModel.getAmpm());
        values.put(notes, alarmModel.getNotes());
        values.put(isSet, alarmModel.getIsSet());
        values.put(isAuto, alarmModel.getIsAuto());
        values.put(min, alarmModel.getMin());
        values.put(ringtone, alarmModel.getRingtone());
        values.put(alarmtype, alarmModel.getAlarmtype());
        values.put(repeat, alarmModel.getRepeat());
        values.put(salahreminder, alarmModel.getSalahreminder());
        db.update(TABLE_ALARM_HISTORY, values, id + "=?", new String[]{String.valueOf(alarmModel.getId())});
        db.close();
    }

    public void deleteAlarm(AlarmModel alarmModel) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_ALARM_HISTORY, id + "=?", new String[]{String.valueOf(alarmModel.getId())});
        db.close();
    }

    public void deleteAlarm() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_ALARM_HISTORY, null, null);
        db.close();
    }


}
