package org.knime.base.node.audio.node.featuretocell;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "AudioVector" Node.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class FeatureToCellNodeFactory
        extends NodeFactory<FeatureToCellNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureToCellNodeModel createNodeModel() {
        return new FeatureToCellNodeModel();
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
    public NodeView<FeatureToCellNodeModel> createNodeView(final int viewIndex,
            final FeatureToCellNodeModel nodeModel) {
        return new FeatureToCellNodeView(nodeModel);
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
        return new FeatureToCellNodeDialog();
    }

}

