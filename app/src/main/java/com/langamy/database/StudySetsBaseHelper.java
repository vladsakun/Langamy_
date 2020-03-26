package com.langamy.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.langamy.base.classes.StudySet;
import com.langamy.database.StudySetsScheme.StudySetsTable;

public class StudySetsBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 3;
    private static final String DB_NAME = "studysets.db";

    public static final String UI_UPDATE_BROADCAST = "com.langamy.activities";
    Cursor c = null;

    public StudySetsBaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + StudySetsTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                StudySetsTable.Cols.name + ", " +
                StudySetsTable.Cols.amount_of_words + ", " +
                StudySetsTable.Cols.words + ", " +
                StudySetsTable.Cols.marked_words + ", " +
                StudySetsTable.Cols.percent_of_studying + ", " +
                StudySetsTable.Cols.language_from + ", " +
                StudySetsTable.Cols.language_to + ", " +
                StudySetsTable.Cols.creator + ", " +
                StudySetsTable.Cols.sync_status + ", " +
                StudySetsTable.Cols.studied +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
