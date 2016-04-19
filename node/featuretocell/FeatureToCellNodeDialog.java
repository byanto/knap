package org.knime.base.node.audio.node.featuretocell;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.knime.base.node.audio.data.node.AudioValue;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.util.filter.StringFilterPanel;

/**
 * <code>NodeDialog</code> for the "AudioVector" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple dialog with standard
 * components. If you need a more complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author Budi Yanto, KNIME.com
 */
public class FeatureToCellNodeDialog extends NodeDialogPane {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(FeatureToCellNodeDialog.class);

    private final FeatureToCellFilterConfiguration m_config;
    private final StringFilterPanel m_filterPanel;
    private final DialogComponentColumnNameSelection m_audioColumnComponent;

    /**
     * New pane for configuring the AudioVector node.
     */
    @SuppressWarnings("unchecked")
    protected FeatureToCellNodeDialog() {
        m_config = FeatureToCellNodeModel.createAudioVectorFilterConfiguration();
        final JPanel mainPanel = new JPanel(new BorderLayout());
        m_audioColumnComponent = new DialogComponentColumnNameSelection(
            FeatureToCellNodeModel.createAudioColumnSettingsModel(),
            "Audio column: ", 0, true, AudioValue.class);
        final JPanel audioColumnPanel = m_audioColumnComponent.getComponentPanel();
        audioColumnPanel.setBorder(BorderFactory.createTitledBorder("Select Audio Column"));
        m_filterPanel = new StringFilterPanel();
        m_filterPanel.setBorder(BorderFactory.createTitledBorder("Select Features"));
        mainPanel.add(audioColumnPanel, BorderLayout.NORTH);
        mainPanel.add(m_filterPanel, BorderLayout.CENTER);

        addTab("Features", mainPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        m_filterPanel.saveConfiguration(m_config);
        m_config.saveConfiguration(settings);
        m_audioColumnComponent.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs) throws NotConfigurableException {
        m_config.loadSettingsForDialog(settings);
        m_filterPanel.loadConfiguration(m_config, m_config.getFeatures());
        m_audioColumnComponent.loadSettingsFrom(settings, specs);
    }
}
