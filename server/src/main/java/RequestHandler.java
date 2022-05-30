
import dataClasses.HumanBeing;
import net.Request;
import net.Response;
import other.Predicates;
import repositories.DataBase;
import specifications.HumanBeingSpecifications;
import specifications.base.Specification;

import java.util.ArrayList;
import java.util.List;

public class RequestHandler {

    private static DataBase database = DataBase.getRepository();

    public static Response handle(Request request){
        try{
        switch (request.method){

            case GET -> {
                Specification<HumanBeing> specification = HumanBeingSpecifications.fromPredicate((Predicates.Predicate) request.body);
                List<HumanBeing> result = (specification == null) ? database.query() : database.query(specification);
                return new Response(Response.StatusCode.OK, request.method, result);
            }
            case POST -> {
                List<HumanBeing> toPost = (ArrayList<HumanBeing>) request.body;
                List<HumanBeing> res = database.insertEntity(toPost);
                database.save();
                return new Response(Response.StatusCode.OK, request.method, res);
            }
            case EXIT -> {
                database.save();
                return new Response(Response.StatusCode.OK, request.method, null);
            }
            case UPDATE -> {
                List<HumanBeing> toUpdate = (ArrayList<HumanBeing>) request.body;
                List<HumanBeing> res = database.updateEntity(toUpdate);

                database.save();
                return new Response(Response.StatusCode.OK, request.method,  res);
            }

            case INFO -> {
                return new Response(Response.StatusCode.OK, request.method, database.getInfo());
            }

            case DELETE -> {
                ArrayList<HumanBeing> toRemove = (ArrayList<HumanBeing>) request.body;
                database.removeEntity(toRemove);
                database.save();
                return new Response(Response.StatusCode.OK, request.method, toRemove);
            }
        }
    }
        catch (RuntimeException e){
            return new Response(Response.StatusCode.ERROR, request.method, e.toString());
        }
        return null;
    }
}
