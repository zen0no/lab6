package commands;

/**
 * Class of command that inserts new element with the specified key
 */

import dataClasses.Car;
import dataClasses.Coordinates;
import dataClasses.HumanBeing;
import exceptions.*;
import manager.Console;
import net.Method;
import net.Request;
import net.Response;
import other.Predicates;
import server.ServerConnection;
import utils.HumanBeingBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertCommand extends AbstractCommand{
    private HumanBeingBuilder builder = new HumanBeingBuilder();

    @Override
    public boolean execute(List<String> args) throws ConsoleException {
        if (!validateArguments(args)){
            throw new IncorrectArgumentConsoleException("Incorrect format for InsertCommand");
        }
        try {
            HumanBeingBuilder builder = new HumanBeingBuilder();
            builder.create(args.get(0));

            Console.print("Enter element values:");
            for (String field : HumanBeing.getFields()) {

                Map<String, String> fieldArgs = new HashMap<>();
                if (field.equals("car")) {
                    for (String carField : Car.getFields()) {
                        System.out.println("HumanBeing.car." + carField + ":");
                        fieldArgs.put(carField, Console.input());
                        }
                } else if (field.equals("coordinates")) {
                    for (String corField : Coordinates.getFields()) {
                        System.out.println("HumanBeing.coordinates." + corField + ":");
                        fieldArgs.put(corField, Console.input());
                    }

                } else {
                    System.out.println("HumanBeing." + field);
                    fieldArgs.put("value", Console.input());
                }
                try {
                    builder.build(field, fieldArgs);
                } catch (ModelFieldException e) {
                    Console.printError(e.getMessage());
                    return false;
                }
            }
            HumanBeing h = builder.get();
            Response response = ServerConnection.getConnection().sendRequest(new Request(Method.POST, List.of(h)));
            if (response.code == Response.StatusCode.OK) Console.print("Added: " + h.toString()); else Console.printError((String) response.body);
            return true;

        }

        catch (BuilderIsBusyException e){
            builder.clear();
            return false;
        }
        catch (BuilderException e) {
            builder.clear();
            Console.printError(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateArguments(List<String> args) throws ConsoleException {
        return args.size() == 1 && !((ServerConnection.getConnection().sendRequest(new Request(Method.GET, new Predicates.Equal("PrimaryKey", args.get(0))))).body == null);

    }

    @Override
    public String getDescription() {
        return "inserts new element with the specified key";
    }

    @Override
    public String getName() {
        return "insert";
    }
}
