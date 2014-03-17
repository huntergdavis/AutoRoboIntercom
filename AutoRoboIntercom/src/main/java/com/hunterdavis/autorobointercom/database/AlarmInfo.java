package com.hunterdavis.autorobointercom.database;

/**
 * Created by hunter on 3/16/14.
 */

public class AlarmInfo {
    int whichDays;
    long whatTimeDuringDay;

    public static final String ALARM_DELIMINATOR = ";;";

    // decimal flags for daily
    // this is crazy inefficient but better than strings and easy to read in database queries
    public static final int SUN_FLAG =   1;
    public static final int MON_FLAG =   10;
    public static final int TUES_FLAG =  100;
    public static final int WEDS_FLAG =  1000;
    public static final int THURS_FLAG = 10000;
    public static final int FRI_FLAG =   100000;
    public static final int SAT_FLAG =   1000000;

    public AlarmInfo(int whatDays, long whatTimes) {
        whichDays = whatDays;
        whatTimeDuringDay = whatTimes;
    }

    public AlarmInfo() {
        whichDays = 0;
        whatTimeDuringDay = 0;
    }
    public AlarmInfo getInfo() {
        return this;
    }
    public int getWhichDays() {
        return whichDays;
    }
    public boolean getSun() {
        return getWhichDay(0);
    }
    public boolean getMon() {
        return getWhichDay(1);
    }
    public boolean getTues() {
        return getWhichDay(2);
    }
    public boolean getWeds() {
        return getWhichDay(3);
    }
    public boolean getThurs() {
        return getWhichDay(4);
    }
    public boolean getFri() {
        return getWhichDay(5);
    }
    public boolean getSat() {
        return getWhichDay(6);
    }

    public boolean getWhichDay(int dayOrdinal) {

        // first divide by 10^ordinal to get down to the place we're interested in
        // then divide by 10 and stick into a float to get our remainder
        float remainderWithDecimal = (whichDays / ((int) Math.pow(10,dayOrdinal))) / 10;

        // subtracting the int from the float version gives us knowledge of
        // whether the ordinal position was positive or not
        return (remainderWithDecimal - ((int) remainderWithDecimal)) > 0;
    };

    public long getWhatTimeDuringDay() {
        return whatTimeDuringDay;
    }

    public void setWhatTimeDuringDay(long whatTime) {
        whatTimeDuringDay = whatTime;
    }

    public void setWhichDays(int whichDaysToUse) {
        whichDays = whichDaysToUse;
    }

    // set which days to use
    public void setWhichDays(boolean Sun, boolean Mon, boolean Tue, boolean Wed, boolean Thu, boolean Fri, boolean Sat) {
        whichDays = 0;

        // decimal bitmasks
        if(Sun) {
            whichDays += SUN_FLAG;
        }
        if(Mon) {
            whichDays += MON_FLAG;
        }
        if(Tue) {
            whichDays += TUES_FLAG;
        }
        if(Wed) {
            whichDays += WEDS_FLAG;
        }
        if(Thu) {
            whichDays += THURS_FLAG;
        }
        if(Fri) {
            whichDays += FRI_FLAG;
        }
        if(Sat) {
            whichDays += SAT_FLAG;
        }
    }

    @Override
    public String toString() {
        return whichDays + ALARM_DELIMINATOR + whatTimeDuringDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlarmInfo alarmInfo = (AlarmInfo) o;

        if (whatTimeDuringDay != alarmInfo.whatTimeDuringDay) return false;
        if (whichDays != alarmInfo.whichDays) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = whichDays;
        result = 31 * result + (int) (whatTimeDuringDay ^ (whatTimeDuringDay >>> 32));
        return result;
    }

    public AlarmInfo getAlarmInfoFromString(String alarmString) {
      String alarmSplitString[] = alarmString.split(ALARM_DELIMINATOR);
      int whatDays =   Integer.valueOf(alarmSplitString[0]);
      long whatTime = Long.valueOf(alarmSplitString[1]);
      return new AlarmInfo(whatDays,whatTime);
    };

}
