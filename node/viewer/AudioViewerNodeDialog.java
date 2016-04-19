package org.knime.base.node.audio.node.viewer;

import org.knime.base.node.audio.data.node.AudioValue;
import org.knime.base.node.audio.util.DefaultValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

/**
 * <code>NodeDialog</code> for the "AudioViewer" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author Budi Yanto, KNIME.com
 */
public class AudioViewerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the AudioViewer node.
     */
    protected AudioViewerNodeDialog() {
        @SuppressWarnings("unchecked")
        final DialogComponentColumnNameSelection comp =
                new DialogComponentColumnNameSelection(
                    AudioViewerNodeModel.createAudioColumnSettingsModel(),
                    DefaultValue.AUDIO_COL_LABEL,
                    0, AudioValue.class);
        comp.setToolTipText("Select audio column to view");
        addDialogComponent(comp);
    }
}

