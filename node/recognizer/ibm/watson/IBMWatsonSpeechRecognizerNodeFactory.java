package org.knime.base.node.audio.node.recognizer.ibm.watson;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "IBMWatsonSpeechRecognizer" Node.
 * 
 *
 * @author Budi Yanto, KNIME.com
 */
public class IBMWatsonSpeechRecognizerNodeFactory 
        extends NodeFactory<IBMWatsonSpeechRecognizerNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public IBMWatsonSpeechRecognizerNodeModel createNodeModel() {
        return new IBMWatsonSpeechRecognizerNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<IBMWatsonSpeechRecognizerNodeModel> createNodeView(final int viewIndex,
            final IBMWatsonSpeechRecognizerNodeModel nodeModel) {
        return new IBMWatsonSpeechRecognizerNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new IBMWatsonSpeechRecognizerNodeDialog();
    }

}

