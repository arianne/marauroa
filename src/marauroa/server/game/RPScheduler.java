/* $Id: RPScheduler.java,v 1.8 2007/01/14 22:07:53 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

import org.apache.log4j.Logger;

/**
 * This class represents a scheduler to deliver action by turns, so every action
 * added to the scheduler is executed on the next turn. Each object can cast as
 * many actions as it wants.
 * 
 * TODO: Simplify it.
 */
public class RPScheduler {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(RPScheduler.class);

	/** a HashMap<RPObject.ID,RPActionList> of entries for this turn */
	private HashMap<RPObject.ID, List<RPAction>> actualTurn;

	/** a HashMap<RPObject.ID,RPActionList> of entries for next turn */
	private HashMap<RPObject.ID, List<RPAction>> nextTurn;

	/** Turn we are executing now */
	private int turn;

	/** Constructor */
	public RPScheduler() {
		turn = 0;
		actualTurn = new HashMap<RPObject.ID, List<RPAction>>();
		nextTurn = new HashMap<RPObject.ID, List<RPAction>>();
	}

	/**
	 * Add an RPAction to the scheduler for the next turn
	 * 
	 * @param action
	 *            the RPAction
	 * @throws ActionInvalidException
	 *             if the action lacks of sourceid attribute.
	 */
	public synchronized void addRPAction(RPAction action,
			IRPRuleProcessor ruleProcessor) throws ActionInvalidException {
		Log4J.startMethod(logger, "addRPAction");
		try {
			RPObject.ID id = new RPObject.ID(action);

			logger.debug("Add RPAction(" + action + ") from RPObject(" + id
					+ ")");
			if (nextTurn.containsKey(id)) {
				List<RPAction> list = nextTurn.get(id);
				if (ruleProcessor.onActionAdd(action, list)) {
					list.add(action);
				}
			} else {
				List<RPAction> list = new LinkedList<RPAction>();
				if (ruleProcessor.onActionAdd(action, list)) {
					list.add(action);
					nextTurn.put(id, list);
				}
			}
		} catch (AttributeNotFoundException e) {
			logger.error("cannot add action to RPScheduler, Action(" + action
					+ ") is missing a required attributes", e);
			throw new ActionInvalidException(e.getAttribute());
		} finally {
			Log4J.finishMethod(logger, "addRPAction");
		}
	}

	/**
	 * Add an RPAction to the scheduler for the next turn
	 * An incomplete action is one that has been executed and the result was not completed.
	 * 
	 * @param action
	 *            the RPAction
	 * @throws ActionInvalidException
	 *             if the action lacks of sourceid attribute.
	 */
	public synchronized void addIncompleteRPAction(RPAction action,
			IRPRuleProcessor ruleProcessor) throws ActionInvalidException {
		Log4J.startMethod(logger, "addIncompleteRPAction");
		try {
			RPObject.ID id = new RPObject.ID(action);

			logger.debug("Add RPAction(" + action + ") from RPObject(" + id
					+ ")");
			if (nextTurn.containsKey(id)) {
				List<RPAction> list = nextTurn.get(id);
				if (ruleProcessor.onIncompleteActionAdd(action, list)) {
					list.add(action);
				}
			} else {
				List<RPAction> list = new LinkedList<RPAction>();
				if (ruleProcessor.onIncompleteActionAdd(action, list)) {
					list.add(action);
					nextTurn.put(id, list);
				}
			}
		} catch (AttributeNotFoundException e) {
			logger.error("cannot add action to RPScheduler, Action(" + action
					+ ") is missing a required attributes", e);
			throw new ActionInvalidException(e.getAttribute());
		} finally {
			Log4J.finishMethod(logger, "addIncompleteRPAction");
		}
	}

	public synchronized void clearRPActions(RPObject.ID id) {
		if (nextTurn.containsKey(id)) {
			nextTurn.remove(id);
		}

		if (actualTurn.containsKey(id)) {
			actualTurn.remove(id);
		}
	}

	/**
	 * For each action in the actual turn, make it to be run in the
	 * ruleProcessor Depending on the result the action needs to be added for
	 * next turn.
	 */
	public void visit(IRPRuleProcessor ruleProcessor) {
		Log4J.startMethod(logger, "visit");
		try {
			logger.debug(actualTurn.size() + " players running actions");
			for (Map.Entry<RPObject.ID, List<RPAction>> entry : actualTurn.entrySet()) {
				RPObject.ID id = entry.getKey();
				List<RPAction> list = entry.getValue();

				logger.debug(list.size() + " actions to visit for " + id);
				for (RPAction action : list) {
					logger.debug("visit action " + action);
					try {
						RPAction.Status status = ruleProcessor.execute(id,action);

						/* If state is incomplete add for next turn */
						if (status.equals(RPAction.Status.INCOMPLETE)) {
							addIncompleteRPAction(action, ruleProcessor);
						}
					} catch (Exception e) {
						logger.error("error in visit()", e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("error in visit", e);
		} finally {
			Log4J.finishMethod(logger, "visit");
		}
	}

	/**
	 * This method moves to the next turn and deletes all the actions in the
	 * actual turn
	 */
	public synchronized void nextTurn() {
		Log4J.startMethod(logger, "nextTurn");
		++turn;

		/*
		 * we cross-exchange the two turns and erase the contents of the next
		 * turn
		 */
		HashMap<RPObject.ID, List<RPAction>> tmp = actualTurn;
		actualTurn = nextTurn;
		nextTurn = tmp;
		nextTurn.clear();

		Log4J.finishMethod(logger, "nextTurn");
	}
}
