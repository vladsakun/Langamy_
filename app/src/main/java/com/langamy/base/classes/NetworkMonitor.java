package com.langamy.base.classes;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.langamy.api.LangamyAPI;
import com.langamy.database.StudySetCursorWrapper;
import com.langamy.database.StudySetsBaseHelper;
import com.langamy.database.StudySetsScheme;
import com.langamy.database.StudySetsScheme.StudySetsTable.Cols;
import com.langamy.fragments.StudySetsFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NetworkMonitor extends BroadcastReceiver {

    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);
    private GoogleSignInAccount account;

    @Override
    public void onReceive(Context context, Intent intent) {

        account = GoogleSignIn.getLastSignedInAccount(context);

        if (BaseVariables.checkNetworkConnection(context)) {

            context.sendBroadcast(new Intent("com.langamy.fragments"));

            syncDb(context);

        }else{
            context.sendBroadcast(new Intent("com.langamy.fragments"));
        }
    }

    public void syncDb(Context context){

        account = GoogleSignIn.getLastSignedInAccount(context);

        SQLiteDatabase mDatabase = new StudySetsBaseHelper(context).getReadableDatabase();

        StudySetCursorWrapper cursor = BaseVariables.queryStudySets(Cols.sync_status + "=?", new String[]{"false"}, mDatabase);
        ArrayList<StudySet> unSyncedStudySets = new ArrayList<>();

        try {
            cursor.moveToFirst();
            if (cursor.getCount() == 1) {
                unSyncedStudySets.add(cursor.getStudySet());
            } else {
                do{
                    unSyncedStudySets.add(cursor.getStudySet());
                }while(cursor.moveToNext());
            }

        } catch (CursorIndexOutOfBoundsException ignored) {
        } finally {
            cursor.close();
        }
        cursor.close();

        for (StudySet studySet : unSyncedStudySets) {
            Call<StudySet> call = mLangamyAPI.patchStudySet(studySet.getId(), studySet);
            call.enqueue(new Callback<StudySet>() {
                @Override
                public void onResponse(Call<StudySet> call, Response<StudySet> response) {
                    if (!response.isSuccessful()) {
                        Log.d("NetworkFailure", "onResponse: " + response.code());
                        return;
                    }
                    mDatabase.update(StudySetsScheme.StudySetsTable.NAME, BaseVariables.getContentValuesForStudyset(studySet, true)
                            , Cols.id + "=?", new String[]{String.valueOf(studySet.getId())});
                }

                @Override
                public void onFailure(Call<StudySet> call, Throwable t) {
                    Log.d("NetworkFailure", t.toString());
                }
            });

        }

        Call<List<StudySet>> call2 = mLangamyAPI.getStudySetsNamesOfCurrentUser(account.getEmail());
        call2.enqueue(new Callback<List<StudySet>>() {
            @Override
            public void onResponse(Call<List<StudySet>> call, Response<List<StudySet>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(context, String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                    return;
                }

                for (StudySet studySet : response.body()) {

                    ContentValues values = BaseVariables.getContentValuesForStudyset(studySet, studySet.isSync_status());

                    String uuidString = String.valueOf(studySet.getId());

                    int id = (int) mDatabase.insertWithOnConflict(StudySetsScheme.StudySetsTable.NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id == -1) {
                        mDatabase.update(StudySetsScheme.StudySetsTable.NAME, values, "_id=?", new String[]{uuidString});
                    }

                }

            }

            @Override
            public void onFailure(Call<List<StudySet>> call, Throwable t) {
                Log.d("NetworkFailure", t.toString());
            }
        });
    }
}
