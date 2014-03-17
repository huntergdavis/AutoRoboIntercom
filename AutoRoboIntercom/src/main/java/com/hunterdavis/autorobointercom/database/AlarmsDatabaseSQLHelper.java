package com.hunterdavis.autorobointercom.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.hunterdavis.autorobointercom.network.NetworkTransmissionUtilities;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by hunter on 3/16/14.
 */
public class AlarmsDatabaseSQLHelper extends SQLiteOpenHelper {

    public static final String ALARMS = "Alarms";
    public static final String COLUMN_ALARM_ID = "_id";
    public static final String COLUMN_ALARM_RECIPIENTS = "_tofield";
    public static final String COLUMN_ALARM_TEXT = "_bodytext";
    public static final String COLUMN_ALARM_RULES = "_alarmrules";
    public static final String COLUMN_ALARM_METADATA = "_alarmmedata";
    public static final String COLUMN_NEXT_ALARM = "_nextAlarm";

    private static final String DATABASE_NAME = "Students.db";
    private static final int DATABASE_VERSION = 1;
    private static SQLiteDatabase database;

    private static final String ALARM_RECIPIENTS_DELIMINATOR = ";;";

    // creation SQLite statement
    private static final String DATABASE_CREATE = "create table " + ALARMS
            + "(" + COLUMN_ALARM_ID + " integer primary key autoincrement, "
            + COLUMN_ALARM_RECIPIENTS + " text not null,"
            + COLUMN_ALARM_TEXT + " text not null,"
            + COLUMN_ALARM_RULES + " text not null,"
            + COLUMN_ALARM_METADATA + " text not null,"
            + COLUMN_NEXT_ALARM + " integer not null"
            +");";

    public AlarmsDatabaseSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void processAllAlarms() {

        // test columns for now
        String testCols[] = {"test","test"};

        Cursor cursor = new MatrixCursor(testCols);

        // here we select * from alarms where time isn't too far away

        while (cursor.moveToNext()) {
            int nextAlarmId = cursor.getInt(0);
            processSingleAlarm(null,"","",new AlarmInfo(),nextAlarmId);
        }
    }


    public void processSingleAlarm(String alarmRecipients[], String alarmText, String metadata, AlarmInfo alarm, int alarmDatabaseId) {

        // first, we'll need to actually send out the alarm
        NetworkTransmissionUtilities.sendAlarmTextToAllClients(null,alarmText,metadata);

        long nextTime = getNextDayAndTimeInEpochTimeFromAlarm(alarm);
        updateAlarmWithNewTime(alarm, nextTime,alarmDatabaseId);
    }

    public void updateAlarmWithNewTime(AlarmInfo alarm, long nextTime, int alarmDatabaseId) {
        // database update
    }

    public long getNextDayAndTimeInEpochTimeFromAlarm(AlarmInfo alarm) {
        // get time from alarm
        int seconds = (int) alarm.getWhatTimeDuringDay() / 1000;
        int minutesTotal = seconds / 60;
        int hours = minutesTotal / 60;
        int minutesOffset = minutesTotal % 60;

        // get our current date
        Calendar dateCalendar = new GregorianCalendar();
        Date date = new Date();
        dateCalendar.setTime(date);

        // see if it's later today
        int totalMinutesPassedToday = 60 * dateCalendar.get(Calendar.HOUR_OF_DAY);
        totalMinutesPassedToday += dateCalendar.get(Calendar.MINUTE);


        int dateForNextAlarm = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

        if(alarm.getWhichDay(dateForNextAlarm)) {
            if (minutesTotal < (totalMinutesPassedToday + 1)) {
                dateForNextAlarm = 7;
            }
        } else {
            // why does java use 1-based days indexing??
            // convert to 0-based
            dateForNextAlarm++;
            while(!alarm.getWhichDay(dateForNextAlarm)) {
                if(dateForNextAlarm == 7) {
                    // we've gone full circle, somethings wrong
                    return -1;
                }
                dateForNextAlarm++;
            }
        }

        // add in the number of days till our next alarm, converted to 1 based
        dateCalendar.add(Calendar.DAY_OF_WEEK,dateForNextAlarm+1);

        // set the absolute hour of day and minute for the alarm
        dateCalendar.set(Calendar.HOUR_OF_DAY,hours);
        dateCalendar.set(Calendar.MINUTE, minutesOffset);

        return dateCalendar.getTimeInMillis();
    };

    public long insertAlarm(String recipients[],String text, int whatDays, long whatTimes, String metaData) {

        // create a single string from the recipeints list
        StringBuilder recipeintsBuilder = new StringBuilder();
        for(String s : recipients) {
            recipeintsBuilder.append(s);
            recipeintsBuilder.append(ALARM_RECIPIENTS_DELIMINATOR);
        }

        return insertAlarm(recipeintsBuilder.toString(), text, whatDays, whatTimes, metaData);
    }


    // insert alarm and automatically create our stringified alarm rules representation
    public long insertAlarm(String recipients, String text, int whatDays, long whatTimes, String metaData) {
        String rules = new AlarmInfo(whatDays,whatTimes).toString();
        return insertAlarm(recipients, text, rules, metaData);
    }

    // insert an alarm into the database
    public long insertAlarm(String recipients, String text, String rules, String metaData) {
        ContentValues values = new ContentValues();

        if(TextUtils.isEmpty(metaData)) {
            metaData = "";
        }

        values.put(COLUMN_ALARM_RECIPIENTS, recipients);
        values.put(COLUMN_ALARM_TEXT, text);
        values.put(COLUMN_ALARM_RULES, rules);
        values.put(COLUMN_ALARM_METADATA, metaData);

        // insert next alarm time
        long nextAlarmTime = calculateNextAlarmFromRules(rules);
        values.put(COLUMN_NEXT_ALARM,nextAlarmTime);

        long lastInsertedRow = database.insert(ALARMS, null, values);
        return lastInsertedRow;

    };

    public long calculateNextAlarmFromRules(String rules) {
        return 0;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public void open() throws SQLException {
        database = getWritableDatabase();
    }

    public void close() {
        close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // you should do some logging in here
        // ..

        // we won't be ugprading this table anytime soon
        //db.execSQL("DROP TABLE IF EXISTS " + STUDENTS);
        onCreate(db);
    }

}