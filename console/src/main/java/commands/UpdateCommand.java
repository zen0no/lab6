package commands;

/**
 * Class of command that updates the value of a collection item whose id is equal to the specified one
 */

import dataClasses.Car;
import dataClasses.Coordinates;
import dataClasses.HumanBeing;
import exceptions.BuilderException;
import exceptions.BuilderIsBusyException;
import exceptions.ConsoleException;
import exceptions.IncorrectArgumentConsoleException;
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

public class UpdateCommand extends AbstractCommand{
    private final HumanBeingBuilder builder = new HumanBeingBuilder();


    @Override
    public boolean execute(List<String> args) throws ConsoleException {
        if (!validateArguments(args)){
            throw new IncorrectArgumentConsoleException("Incorrect format for UpdateCommand");
        }
        try
        {
            String key = args.get(0);

            Response response = ServerConnection.getConnection().sendRequest(new Request(Method.GET, new Predicates.Equal("PrimaryKey", key)));

            if (response.code == Response.StatusCode.ERROR){
                Console.printError((String) response.body);
                return false;
            }

            HumanBeing h = ((List<HumanBeing>) response.body).get(0);
            if (h == null) {
                Console.print("HumanBeing with this key was not found");
                return false;
            }
            builder.update(h);

            System.out.println("Enter element values:");
            for(String field: HumanBeing.getFields())
            {
                Map<String, String> fieldArgs = new HashMap<>();
                if (field.equals("car"))
                {
                    for (String carField : Car.getFields()) {
                        System.out.println("HumanBeing.car." + carField + ":");
                        if (scanner.hasNextLine()) {
                            fieldArgs.put(carField, scanner.nextLine());
                        }
                    }
                }
                else if (field.equals("coordinates")) {
                    for (String corField : Coordinates.getFields()) {
                        System.out.println("HumanBeing.coordinates." + corField + ":");
                        if (scanner.hasNextLine()) {
                            fieldArgs.put(corField, scanner.nextLine());
                        }
                    }

                }
                else
                {
                    System.out.println("HumanBeing." + field);
                    if (scanner.hasNextLine())
                    {
                        fieldArgs.put("value", scanner.nextLine());
                    }
                }
                builder.build(field, fieldArgs);
            }
            h = builder.get();
            response = ServerConnection.getConnection().sendRequest(new Request(Method.UPDATE, List.of(h)));
            Console.print("Updated: " + h.toString());
            return true;

        }


        catch (BuilderIsBusyException e){
            builder.clear();
            return false;
        }
        catch (BuilderException e){
            builder.clear();
            Console.printError(e.getMessage());
            return false;
        }

    }

    @Override
    public String getDescription() {
        return "updates the value of a collection item whose id is equal to the specified one";
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public boolean validateArguments(List<String> args) throws ConsoleException {
        return args.size() == 1;
    }


}
