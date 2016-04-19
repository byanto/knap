package org.knime.base.node.audio.node.recognizer.microsoft;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "MSSpeechRecognizer" Node.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class MSSpeechRecognizerNodeFactory
        extends NodeFactory<MSSpeechRecognizerNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MSSpeechRecognizerNodeModel createNodeModel() {
        return new MSSpeechRecognizerNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<MSSpeechRecognizerNodeModel> createNodeView(final int viewIndex,
            final MSSpeechRecognizerNodeModel nodeModel) {
        return new MSSpeechRecognizerNodeView(nodeModel);
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
        return new MSSpeechRecognizerNodeDialog();
    }

}

