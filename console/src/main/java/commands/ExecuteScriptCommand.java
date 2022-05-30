package commands;

/**
 * Class of command that reads and executes the script from the specified file
 */

import exceptions.ConsoleException;
import exceptions.IncorrectArgumentConsoleException;
import manager.Console;
import utils.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;

public class ExecuteScriptCommand extends AbstractCommand{

    /**
     * Constructor of class
     */

    private final Set<Pair<String, Integer>> fileNames;
    private final int level;


    public ExecuteScriptCommand(Set<Pair<String, Integer>> fileNames, int level) {
        this.fileNames = fileNames;
        this.level = level;
    }

    @Override
    public boolean execute(List<String> args) {
        if (!validateArguments(args)){
            throw new IncorrectArgumentConsoleException("Incorrect file path for ExecuteScriptCommand");
        }
        try{

            String path = args.get(0);

            for (Pair<String, Integer> p: fileNames){
                if(p.getLeft().equals(path) && p.getRight() < level ) {
                    System.out.println("This script can cause looping");
                    return false;
                }
            }

            File f = new File(args.get(0));
            Console.swapInputStream(new FileInputStream(f));
            return true;
        }
        catch (java.io.FileNotFoundException e){
            return false;
        }
    }

    @Override
    public boolean validateArguments(List<String> args) throws ConsoleException{
        if (args.size() == 1){
            return (new File(args.get(0))).exists();
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "reads and executes the script from the specified file";
    }

    @Override
    public String getName() {
        return "execute_script";
    }



}
