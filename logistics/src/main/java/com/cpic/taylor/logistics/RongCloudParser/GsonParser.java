package com.cpic.taylor.logistics.RongCloudParser;

import android.content.SharedPreferences;
import android.util.Log;

import com.cpic.taylor.logistics.base.RongYunContext;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.sea_monster.exception.InternalException;
import com.sea_monster.exception.ParseException;

import org.apache.http.Header;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;



/**
 * Created by DragonJ on 14-7-15.
 */
public class GsonParser<T extends Serializable> extends JsonObjectParser<T> {


    Type type;
    Gson gson;

    public GsonParser(Class<T> type) {
        gson = new Gson();
        this.type = type;
    }

    @Override
    public T jsonParse(JsonReader reader) throws JSONException, IOException, ParseException, InternalException,JsonSyntaxException {
        try {
            Log.d("GsonParser", reader.toString());
            return gson.fromJson(reader, this.type);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return null;
    }

    @Override
    public void onHeaderParsed(Header[] headers) {

        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].getName().equals("Set-Cookie")) {
                    String[] cookieValues = headers[i].getValue().split(";");
                    SharedPreferences.Editor edit = RongYunContext.getInstance().getSharedPreferences().edit();
                    edit.putString("DEMO_COOKIE", cookieValues[0]);
                    edit.apply();
                }
            }
        }
    }


}

