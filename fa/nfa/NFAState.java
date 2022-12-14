package fa.nfa;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import fa.State;

/**
 * @author Alex Liang
 * @class CS361 
 */

public class NFAState extends State {

    private HashMap<Character, Set<NFAState>> delta; // set of transition states
    private boolean isFinal; // final state boolean check

    /**
     * Default constructor
     * 
     * @param name The name of the state
     */
    public NFAState(String name) {
        this.name = name;
        isFinal = false;
        delta = new HashMap<Character, Set<NFAState>>();

    }

    /**
     * Constructor that has name, and sets state type --> isFinal
     * 
     * @param name    state name
     * @param isFinal boolean state: true -> isFinal, false -> isFinal
     */
    public NFAState(String name, boolean isFinal) {
        this.name = name;
        this.isFinal = isFinal;
        delta = new HashMap<Character, Set<NFAState>>();
    }

    // private String initDefault(String name) {
    //     this.name = name;
    //     delta = new HashMap<Character, Set<NFAState>>();
    //     return this.name;

    // }

    /**
     * Accessor for the state type
     * 
     * @return true if final and false otherwise
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Sets the final state to true
     * 
     * @return true that the final state is set
     */
    public boolean setFinal() {
        return isFinal = true;
    }

    // public String toString() {
    // return initDefault(name);
    // // return this.name;
    // }

    /**
     * Add the transition from <code> this </code> object
     * 
     * @param onSymb  the alphabet symbol
     * @param toState to NFA state
     */
    public void addTransition(char onSymb, NFAState toState) {
        Set<NFAState> transition = delta.get(onSymb);
        if (transition == null) {
            Set<NFAState> newTransition = new LinkedHashSet<NFAState>();
            newTransition.add(toState);
            delta.put(onSymb, newTransition);
        } else {
            transition.add(toState);

        }
    }

    /**
     * Retrieves the state that <code>this</code> transitions to
     * on the given symbol
     * 
     * @param symb - the alphabet symbol
     * @return the new state
     */
    public Set<NFAState> getTo(char symb) {
        return delta.get(symb);
    }

}
