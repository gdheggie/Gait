package com.example.gheggie.gait;

import android.content.Context;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

class GaitUtils {

    private static final String FILE_NAME = "gait.txt";
    private static final String FILE_ARRAY = "schedule.txt";

    static void savePerson(Context context, GaitInfo gait) {

        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(gait);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    static GaitInfo loadGait(Context context) {
        GaitInfo gait = null;

        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            gait = (GaitInfo) ois.readObject();
            ois.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return gait;
    }

    static void saveSchedule(Context context, Schedule schedule) {
        ArrayList<Schedule> schedules = loadSchedules(context);
        schedules.add(schedule);
        saveSchedules(context, schedules);
    }

    private static void saveSchedules(Context context, ArrayList<Schedule> schedules) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_ARRAY, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(schedules);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    static ArrayList<Schedule> loadSchedules(Context context) {

        ArrayList<Schedule> schedules = null;

        try {
            FileInputStream fis = context.openFileInput(FILE_ARRAY);
            ObjectInputStream ois = new ObjectInputStream(fis);
            schedules = (ArrayList<Schedule>)ois.readObject();
            ois.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(schedules == null) {
            schedules = new ArrayList<>();
        }

        return schedules;
    }
}
