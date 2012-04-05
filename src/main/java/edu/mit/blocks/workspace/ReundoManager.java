package edu.mit.blocks.workspace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Date;
import java.util.Collections;

/**
 * RedoUndoManager manges the redoing and undoing for the WorkspaceController.
 * @author Thomas Robinson
 */
public class ReundoManager implements WorkspaceListener {
	
	//Workspace events are grouped together into a single undo event
    //if they occur close together in time
    //specifically, if a given event occured less than
    //SECONDS_BETWEEN_EVENT_GROUPS before the previous event
	private static int SECONDS_BETWEEN_EVENT_GROUPS = 1;

    // Member Variables

    protected ISupportMemento managedClass;
    protected List<Object> currentStateMemento;
    protected Stack<List<Object>> undoMementoStack;
    protected Stack<List<Object>> redoMementoStack;
    
    //This lock object prevents events raised during undoing/redoing
    //from being registered as user events. Set it to true to lock
    //out event registering
    private boolean lock = false;
    
    //The time the workspace was last changed
    //Used for grouping events; see note above.
    private Date lastUserEventDate = null;

    public ReundoManager(ISupportMemento aManagedClass) {
        managedClass = aManagedClass;

        this.reset();
    }

    public void reset() {
        undoMementoStack = new Stack<List<Object>>();
        redoMementoStack = new Stack<List<Object>>();

        currentStateMemento = null;
        //Initial state, nothing in the undo stack, and no current state
        //There is a workspace completed loading event that fires so that
        //the current state will be valid. (hence the is null test below.
    }


    public void workspaceEventOccurred(WorkspaceEvent event) {
        if (!lock) {            
			Date currentDate = new Date();
			
			boolean shouldAddToExistingGroup = true;
			
			if(currentStateMemento == null || lastUserEventDate == null) {
				shouldAddToExistingGroup = false;
			}
				
			if(shouldAddToExistingGroup && event.isUserEvent() && 
				currentDate.getTime() - lastUserEventDate.getTime() >
								 SECONDS_BETWEEN_EVENT_GROUPS * 1000) {
				shouldAddToExistingGroup = false;
			}
			
			if(shouldAddToExistingGroup) {
				//this event occured shortly after the previous event
				//so, it should be grouped together with the previous event
				//A single invoction of undo() will undo both events
				if(managedClass != null) {
					currentStateMemento.add(managedClass.getState());
				}
			}
			else {     
				//create a new group for this event       	
				if (currentStateMemento != null) {
					undoMementoStack.add(currentStateMemento);
				}
				currentStateMemento = new ArrayList<Object>();

				if(managedClass != null) {
					currentStateMemento.add(managedClass.getState());
				}
		   }
		   if (event.isUserEvent()) {
				lastUserEventDate = currentDate;
			}
		}
	}

    public void undo() {
        if (canUndo() && !lock) {
            lock = true;
            {
                //Get the undo state
                List<Object> olderStates = undoMementoStack.pop();
                //Load all the states in the group, in reverse order
                //(neweset to oldest)
                List<Object> statesRev = new ArrayList<Object>(olderStates);
                Collections.reverse(statesRev);
                for (Object aState : statesRev) {
                    managedClass.loadState(aState);
                }

                //Put the newer current state on the redo
                redoMementoStack.push(currentStateMemento);
                //And make the new current state the one we just loaded
                currentStateMemento = olderStates;
            }
            lock = false;
        }
    }

    public void redo() {
        if (canRedo() && !lock) {
            lock = true;
            {
            	//FIXME: I think this redo implementation is correct
            	//but for common actions, like connecting blocks,
            	//it puts the canvas in an inconsistant state with
            	//blocks missing tags on their connectors (and possibly
            	//other problems)
            	//Commenting this out until that issue can be investigated
            	
				//Get the redo state
	    		List<Object> newerStates = redoMementoStack.pop();
                //Load all the states in the group, in order 
                //(oldest to newest)
                for (Object aState : newerStates) {
                    managedClass.loadState(aState);
                }
                
	    		//Put the older current state onto the undo stack
	    		undoMementoStack.push(currentStateMemento);
	    		//And make the new current state the the one we just loaded
	    		currentStateMemento = newerStates;
	    		
            }
            lock = false;
        }
    }

    public boolean canUndo() {
        return (undoMementoStack.size() > 0);
    }

    public boolean canRedo() {
        return (redoMementoStack.size() > 0);
    }

    public String getUndoText() {
        return "";
    }

    public String getRedoText() {
        return "";
    }
}
