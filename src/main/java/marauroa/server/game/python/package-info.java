/**
 * This package provides a helper implementation for a Python RP Rules Manager and
 * a Python World storage.
 *
 * You need to extend in Python the classes:<ul>
 * <li>PythonRP for the IRPRuleProcessor implementation.
 * <li>PythonWorld for the RPWorld implementation.
 * </ul>
 *
 * Make sure you fill correctly server.ini by setting:<ul>
 * <li>world=marauroa.server.game.python.PythonRPWorld
 * <li>ruleprocessor=marauroa.server.game.python.PythonRPRuleProcessor
 * </ul>
 *
 * You are expected to define the file that contains your python script at
 * server.ini in the field python_script
 *
 * For example:
 * <pre>
 *  python_script=foo.py
 *  python_script_world=MyPythonRPWorld
 *  python_script_ruleprocessor=MyPythonRuleProcessor
 * </pre>
 */
package marauroa.server.game.python;

