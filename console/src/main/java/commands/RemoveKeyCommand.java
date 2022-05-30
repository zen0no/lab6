package commands;

/**
 * Class of command that removes an element from the collection by its key
 */

import dataClasses.HumanBeing;
import exceptions.ConsoleException;
import exceptions.IncorrectArgumentConsoleException;
import manager.Console;
import net.Method;
import net.Request;
import net.Response;
import other.Predicates;
import server.ServerConnection;

import java.util.ArrayList;
import java.util.List;

public class RemoveKeyCommand extends AbstractCommand {




    @Override
    public boolean execute(List<String> args) throws ConsoleException {
        if (!validateArguments(args)) {
            throw new IncorrectArgumentConsoleException("Incorrect argument exception for RemoveKeyCommandasd");
        }
        String key = args.get(0);
        HumanBeing toRemove = ((ArrayList<HumanBeing>) ServerConnection.getConnection().sendRequest(new Request(Method.GET, new Predicates.Equal("PrimaryKey", key))).body).get(0);
        Response response = ServerConnection.getConnection().sendRequest(new Request(Method.DELETE, List.of(toRemove)));
        if (response.code == Response.StatusCode.ERROR){
            Console.printError((String) response.body);
            return false;
        }
        Console.print("success");
        return true;

    }

    @Override
    public String getDescription() {
        return "removes an element from the collection by its key";
    }

    @Override
    public String getName() {
        return "remove_key";
    }

    @Override
    public boolean validateArguments(List<String> args) throws ConsoleException {
        return args.size() == 1;
    }
}
