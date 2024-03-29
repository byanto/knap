package org.knime.base.node.audio.node.viewer;

import javax.swing.JPanel;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "AudioViewer" Node.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class AudioViewerNodeView extends NodeView<AudioViewerNodeModel> {

    /**
     * Creates a new view.
     *
     * @param nodeModel The model (class: {@link AudioViewerNodeModel})
     */
    protected AudioViewerNodeView(final AudioViewerNodeModel nodeModel) {
        super(nodeModel);
        JPanel panel = new AudioViewerMainPanel(nodeModel.getAudioList());
        setComponent(panel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // TODO: generated method stub
    }

}

