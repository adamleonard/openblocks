package edu.mit.blocks.workspace;

import java.awt.Component;

/**
 * RBParents have methods for adding any Component to either the BlockLayer
 * or HighlightLayer.  
 * The HighlightLayer must be rendered in front of the BlockLayer
 * such that all Components on the HighlightLayer are rendered in front of ALL
 * Components on the BlockLayer. 
 * This allows the highlight to shine through and appear above all other blocks
 * @author Daniel
 *
 */
public interface RBParent {

    /**
     * Add this Component the BlockLayer, which is understood to be above the
     * HighlightLayer, although no guarantee is made about its order relative
     * to any other layers this RBParent may have.
     * @param c the Component to add
     */
    public void addToBlockLayer(Component c);

    /**
     * Add this Component to the HighlightLayer, which is understood to be
     * directly and completely in front of the BlockLayer, 
     * such that all Components on the HighlightLayer are in front of ALL
     * Components on the BlockLayer.
     * @param c the Component to add
     */
    public void addToHighlightLayer(Component c);
}
