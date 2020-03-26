package com.langamy.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.langamy.base.classes.StudySet;
import com.langamy.database.StudySetsScheme.StudySetsTable.Cols;

public class StudySetCursorWrapper extends CursorWrapper {

    public StudySetCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public StudySet getStudySet(){

        String uuidString = getString(getColumnIndex(Cols.id));
        String name = getString(getColumnIndex(Cols.name));
        String words = getString(getColumnIndex(Cols.words));
        String marked_words = getString(getColumnIndex(Cols.marked_words));
        String language_from = getString(getColumnIndex(Cols.language_from));
        String language_to = getString(getColumnIndex(Cols.language_to));
        String creator = getString(getColumnIndex(Cols.creator));
        int amount_of_words = getInt(getColumnIndex(Cols.amount_of_words));
        boolean sync_status = Boolean.parseBoolean(getString(getColumnIndex(Cols.sync_status)));

        StudySet studySet = new StudySet(creator, name, words, language_to, language_from, amount_of_words);
        studySet.setSync_status(sync_status);
        studySet.setMarked_words(marked_words);
        studySet.setId(Integer.parseInt(uuidString));

        return studySet;
    }
}
