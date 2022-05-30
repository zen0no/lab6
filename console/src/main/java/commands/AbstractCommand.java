package commands;

import java.util.Scanner;


/**
 * Command interface realization. Base command class
 */
public abstract class AbstractCommand implements Command {
    protected Scanner scanner;

    /**
     * @param scanner input scanner
     */
    public void setScanner(Scanner scanner){
        this.scanner = scanner;
    }
}
