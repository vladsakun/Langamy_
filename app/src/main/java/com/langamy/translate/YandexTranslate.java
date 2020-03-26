package com.langamy.translate;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.langamy.base.classes.BaseVariables;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by DoguD on 01/07/2017.
 */

public class YandexTranslate extends AsyncTask<String, Void, String> {
    //Declare Context

    @Override
    public String doInBackground(String... params) {
        //String variables
        String textToBeTranslated = params[0].replaceAll("\\[", "").replaceAll("]", "");
        String languageFrom = params[1];
        String languageTo = params[2];

        String jsonString;

        try {
            //Set up the translation call URL
            String yandexUrl = BaseVariables.LOCAL_URL + "api/translate/" + textToBeTranslated +
                    "/" + languageFrom + "/" + languageTo + "/";
            URL yandexTranslateURL = new URL(yandexUrl);

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            //Set Http Conncection, Input Stream, and Buffered Reader
            HttpURLConnection httpJsonConnection = (HttpURLConnection) yandexTranslateURL.openConnection();
            InputStream inputStream = httpJsonConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            //Set string builder and insert retrieved JSON result into it
            StringBuilder jsonStringBuilder = new StringBuilder();
            while ((jsonString = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(jsonString + "\n");
            }

            //Close and disconnect
            bufferedReader.close();
            inputStream.close();
            httpJsonConnection.disconnect();

            //Making result human readable
            String resultString = jsonStringBuilder.toString().trim();

            Log.d("Translation Result:", resultString);
            JSONObject jsonObject = new JSONObject(resultString);
            jsonObject.get("translation");
            Log.d("(2) Translation Result:", jsonObject.get("translation").toString());

            return resultString;

        } catch (JSONException | IOException e) {
            return "";
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
