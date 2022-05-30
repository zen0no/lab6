package net;

import com.google.gson.*;
import dataClasses.HumanBeing;
import other.Predicates;
import other.typeAdapters.DateAdapter;
import other.typeAdapters.ZonedDateTimeAdapter;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Request implements Serializable {

    public Method method;
    public Object body;

    public Request(Method method, Object o) {
        this.method = method;
        this.body = o;
    }

    public static Request fromJson(String s){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateAdapter());
        builder.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter());
        Gson gson = builder.create();

        JsonObject json = JsonParser.parseString(s).getAsJsonObject();
        JsonElement methodString = json.get("method");
        Method method = gson.fromJson(methodString, Method.class);

        JsonElement o = json.get("body");

        if (o == null) return new Request(method, null);

        switch (method){
            case POST, DELETE, UPDATE ->{
                JsonArray humanBeings = (JsonArray) o;
                List<HumanBeing> res = new ArrayList<>();
                for (JsonElement elem: humanBeings){
                    HumanBeing h = gson.fromJson(elem, HumanBeing.class);
                    res.add(h);
                }
                return new Request(method, res);
            }

            case GET -> {
                JsonObject object = (JsonObject) o;
                Class<?> t = Predicates.Predicate.class;
                for (Class<?> c: Predicates.class.getClasses()){
                    if (c.getName().toLowerCase(Locale.ROOT).contains(object.get("method").getAsString())){
                        t = c;
                        break;
                    }
                }
                return new Request(method, gson.fromJson(o, t));
            }

            case EXIT, INFO -> {
                return new Request(method, null);
            }
            default -> {
                return null;
            }
        }

    }

    public static String toJson(Request request){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateAdapter());
        builder.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter());
        Gson gson = builder.create();

        return gson.toJson(request);
    }
}
