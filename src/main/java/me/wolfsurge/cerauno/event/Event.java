package me.wolfsurge.cerauno.event;

/**
 * A basic event
 *
 * @author Wolfsurge
 * @since 05/03/22
 */
public class Event {

    //TODO uhm why is there like a state of the event? I mean I get it but it's private? There is no setter, just a getter huh?

    // The state of the event
    private State state;

    /**
     * Gets the state of the event
     * @return The state of the event
     */
    public State getState() {
        return this.state;
    }

    public enum State {
        /**
         * Before something happens
         */
        PRE,

        /**
         * After something happens
         */
        POST,

        /**
         * During something happening
         */
        PERI
    }

}
