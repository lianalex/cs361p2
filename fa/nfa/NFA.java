package fa.nfa;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import fa.State;
import fa.dfa.DFA;

/**
 * @author Alex Liang
 * @class CS361 
 */

/**
 * Implementation of NFA class to be used
 */

public class NFA implements NFAInterface {
    private Set<NFAState> NFA_states; // finite set of states
    private NFAState NFA_start; // start state of NFA
    private Set<Character> NFA_alphabet; // alphabet of NFA

    private final char EMPTY_TRANS = 'e'; // empty transition state
	private final String TRANS_STATE = "[]"; // transition state

    /**
     * Constructor for NFA which has states and the alphabet
     */
    public NFA() {
        NFA_states = new LinkedHashSet<NFAState>();
        NFA_alphabet = new LinkedHashSet<Character>();
    }

    /**
     * Adds a final state 
     */
    public void addFinalState(String name) {
        NFAState newState = new NFAState(name, true);
		NFA_states.add(newState);
    }

    /**
     * checks if the state exists, if null then add the state to the set of states
     * and then set it as a start state
     */
    public void addStartState(String name) {
        NFAState s = checkIfExists(name);
        if (s == null) {
            s = new NFAState(name);
            NFA_states.add(s);
        }
        NFA_start = s;
    }

    /**
     * checks if the state exists
     * 
     * @param name --> String name to be searched for
     * @return NFAState
     */
    private NFAState checkIfExists(String name) {
        NFAState ret = null;
        for (NFAState s : NFA_states) {
            if (s.getName().equals(name)) {
                ret = s;
                break;
            }
        }
        return ret;
    }


    /**
     * Adds a non-final state to the DFA
     */
    public void addState(String name) {
		NFAState s = checkIfExists(name);
		if(s == null){
			s = new NFAState(name);
			addState(s);
		} else {
			System.out.println("WARNING: A state with name " + name + " already exists in the DFA");
		}
    }

    private void addState(NFAState s){
		NFA_states.add(s);
	}

    /**
     * Adds transition to the DFA
     */
    public void addTransition(String fromState, char onSymb, String toState) {
        getState(fromState).addTransition(onSymb, getState(toState));

		if (!NFA_alphabet.contains(onSymb) && onSymb != EMPTY_TRANS) {
			NFA_alphabet.add(onSymb);
		}
    }

    /**
     * implement the breadth-first search (BFS) algorithm 
     * (a loop iterating over a queue; an element of a queue is a set of NFA states).
     * 
     * @return
     */
    public DFA getDFA() {
        // INITIALIZE ALL VARIABLES
        Set<Set<NFAState>> visitedStates = new LinkedHashSet<Set<NFAState>>();
        Set<String> knownStates = new LinkedHashSet<String>();
        Queue<Set<NFAState>> input = new LinkedList<Set<NFAState>>();
        DFA dfa = new DFA();
        boolean transition_Set = false;

        // Get the new start state
        Set<NFAState> startState = eClosure(NFA_start);
        visitedStates.add(startState);
        // if start state is a final state then add it to the DFA and print it
        if (containsFinal(startState)) {
            dfa.addFinalState(startState.toString());
        } else { // else add the start state as a state to the DFA and print it
            dfa.addState(startState.toString());
        }

        // add a start state to the DFA and print it
        dfa.addStartState(startState.toString());
        // add the start start state to the known set of states using Set -> Linked Hash Set
        knownStates.add(startState.toString());

        // Searching for every character in the alphabet
        for (char c : NFA_alphabet) {
            Set<NFAState> transition = new LinkedHashSet<NFAState>();
            for (NFAState ns : startState) {
                Set<NFAState> aTransition = ns.getTo(c);
                // if the state that is transitioned to is not null
                if (ns.getTo(c) != null) {
                    // set of epsilon transitions that "jumps" to the next state --> eclosure()
                    Set<NFAState> epsilon_Transition = eclosure(aTransition);
                    // if the set of epsilon transitions is not null then add them all to the set of transitions
                    if (epsilon_Transition != null) {
                        transition.addAll(epsilon_Transition);
                    }

                }
            }
            // if the known states do not contain the transition and it is not empty
            // add the transition to the set of inputs Queue
            if (!knownStates.contains(transition.toString()) && !transition.isEmpty()) {
                input.add(transition);
                // if the transition is to a final state then add that to the DFA as a final state
                if (containsFinal(transition)) {
                    dfa.addFinalState(transition.toString());
                } else {
                    // else just add the state to the DFA
                    dfa.addState(transition.toString());
                }
                // add all transitions to the set of known states
                knownStates.add(transition.toString());
            }
            // if the set of transition is not empty, add a transition to the DFA with the start state to the new transition state
            if (!transition.isEmpty()) {
                dfa.addTransition(startState.toString(), c, transition.toString());
            } else {
                // add the transition state to the DFA and sets the transition to TRUE
                if (!transition_Set) {
                    dfa.addState(TRANS_STATE);
                    transition_Set = true;
                }
                // add a transition to the DFA with the start state to the new transition state
                dfa.addTransition(startState.toString(), c, TRANS_STATE);
            }
        }
        visitedStates.add(startState);
        // while the set of inputs is not empty
        while (!input.isEmpty()) {
            // retrieves and removes the head of the queue
            Set<NFAState> not_Visited_states = input.poll();
            // if knownStates does not contain the set of not visited states from the queue AND the set of inputs does not 
            // also contain the set of not visited states
            if (!knownStates.contains(not_Visited_states.toString()) && !input.contains(not_Visited_states)) {
                // if the not visited states contains a final state
                if (containsFinal(not_Visited_states)) {
                    // add that polled state as a final state to the DFA
                    dfa.addFinalState(not_Visited_states.toString());
                } else {
                    // else add that polled state to the DFA
                    dfa.addState(not_Visited_states.toString());
                }

            }
            // for every character in the NFA alphabet
            for (char c : NFA_alphabet) {
                Set<NFAState> transition = new LinkedHashSet<NFAState>();
                for (NFAState ns : not_Visited_states) {
                    Set<NFAState> char_Transition = ns.getTo(c);
                    // if the state that is transitioned to is not null
                    if (char_Transition != null) {
                        // set of epsilon transitions that "jumps" to the next state --> eclosure()
                        Set<NFAState> epsilon_Transition = eclosure(char_Transition);
                        // if the set of epsilon transitions is not null then add them all to the set of transitions
                        if (epsilon_Transition != null) {
                            transition.addAll(epsilon_Transition);
                        }
                    }
                }
                // if the known states do not contain the transition and it is not empty
                // add the transition to the set of inputs Queue
                if (!knownStates.contains(transition.toString()) && !transition.isEmpty()) {
                    // set the set of inputs does not contain the transition then add the transition to the set of inputs
                    if (!input.contains(transition)) {
                        input.add(transition);
                    }
                    // if the transition is to a final state then add that to the DFA as a final state
                    if (containsFinal(transition)) {
                        dfa.addFinalState(transition.toString());
                    } else {
                        // else add that to the state to the DFA
                        dfa.addState(transition.toString());
                    }
                    // add the transition to the set of known states
                    knownStates.add(transition.toString());
                }
                // if the transition set is not empty then add the transition to the DFA and print out from --> not_Visited_states
                // to --> transition state
                if (!transition.isEmpty()) {
                    dfa.addTransition(not_Visited_states.toString(), c, transition.toString());
                } else {
                    // else add the TRANS_STATE to the DFA and set the transition to TRUE
                    if (!transition_Set) {
                        dfa.addState(TRANS_STATE);
                        transition_Set = true;
                    }
                    // add the transition to the DFA and print them
                    dfa.addTransition(not_Visited_states.toString(), c, TRANS_STATE);
                }
                // add all the non visited states to the set of visited states
                visitedStates.add(not_Visited_states);
            }
            
            if (transition_Set) {
                for (char c : NFA_alphabet) {
                    dfa.addTransition(TRANS_STATE, c, TRANS_STATE);
                }
            }

        }
        return dfa;
    }

    @Override
    public Set<? extends State> getStates() {
        return NFA_states;
    }

    @Override
    public Set<? extends State> getFinalStates() {
        Set<NFAState> ret = new LinkedHashSet<NFAState>();
        for (NFAState s : NFA_states) {
            if (s.isFinal()) {
                ret.add(s);
            }
        }
        return ret;
    }

    @Override
    public State getStartState() {
        return NFA_start;
    }

    @Override
    public Set<Character> getABC() {
        return NFA_alphabet;
    }

    @Override
    public Set<NFAState> getToState(NFAState from, char onSymb) {
        return from.getTo(onSymb);
    }

    /**
     * Gets the desired state from the NFA
     * 
     * @param name
     * @return desired NFAState
     */
    private NFAState getState(String name) {
        NFAState return_State_Name = null;
        for (NFAState s : NFA_states) {
            if (s.getName().equals(name)) {
                return_State_Name = s;
            }
        }
        return return_State_Name;
    }

    /**
     * Checks if the set of NFAStates has a final state
     * 
     * @param name
     * @return true if there is a final state in the set of NFAStates
     */
    private boolean containsFinal(Set<NFAState> name) {
        boolean hasFinal = false;
        for (NFAState s : name) {
            if (s.isFinal()) {
                hasFinal = true;
            }
        }
        return hasFinal;

    }

    /*
     * The method public Set<NFAState> eClosure(NFAState s), i.e., the epsilon
     * closure function,
     * computes the set of NFA states that can be reached from the argument state s
     * by going only along ε transitions, including s itself.
     * You must implement it using the depth-first search algorithm (DFS) using a
     * stack in a loop,
     * i.e., eClosure’s loop should push children of the current node on the stack,
     * but only those children that are reachable through an ε transition.
     */
    @Override
    public Set<NFAState> eClosure(NFAState s) {
        Set<NFAState> epsilon_closure = new LinkedHashSet<NFAState>();
        Stack<NFAState> stack = new Stack<NFAState>();
        stack.add(s);
        while (!stack.isEmpty()) {
            NFAState n = stack.pop();
            if (!epsilon_closure.contains(n)) {
                epsilon_closure.add(n);
                Set<NFAState> goTo = n.getTo(EMPTY_TRANS);
                if (goTo != null)
                    stack.addAll(n.getTo(EMPTY_TRANS));
            }
        }
        return epsilon_closure;
    }

    /**
     * Gets all of the empty transitions
     * 
     * @param s
     * @return set of epsilon transitions
     */
    private Set<NFAState> eclosure(Set<NFAState> s) {
        Set<NFAState> empty_Trans = new LinkedHashSet<NFAState>();
        for (NFAState ns : s) {
            Set<NFAState> epsilon_Trans = eClosure(ns);
            if (epsilon_Trans != null) {
                empty_Trans.addAll(eClosure(ns));
            }
        }
        return empty_Trans;
    }

}
