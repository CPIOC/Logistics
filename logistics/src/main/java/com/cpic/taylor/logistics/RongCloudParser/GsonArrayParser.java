package com.cpic.taylor.logistics.RongCloudParser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sea_monster.exception.InternalException;
import com.sea_monster.exception.ParseException;

import org.apache.http.Header;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by DragonJ on 14-7-15.
 */
public class GsonArrayParser<T extends Serializable> extends JsonObjectParser<ArrayList<T>> {

    TypeToken<ArrayList<T>> type;
    Gson gson;

    public GsonArrayParser(TypeToken<ArrayList<T>> typeToken) {
        gson = new Gson();
        this.type = typeToken;
    }

    @Override
    public ArrayList<T> jsonParse(JsonReader reader) throws JSONException, IOException, ParseException, InternalException {

        return gson.fromJson(reader, type.getType());
    }


    @Override
    public void onHeaderParsed(Header[] headers) {

    }
}
