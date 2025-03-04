package com.paragon.client.systems.command;

public abstract class Command {

    private String name;
    private String syntax;

    public Command(String name, String syntax) {
        this.name = name;
        this.syntax = syntax;
    }

    public abstract void whenCalled(String[] args, boolean fromConsole);

    /**
     * Gets the command's name
     *
     * @return The command's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the command's syntax
     *
     * @return The command's syntax
     */
    public String getSyntax() {
        return syntax;
    }
}
