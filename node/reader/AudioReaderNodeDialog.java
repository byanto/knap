package org.knime.base.node.audio.node.reader;



import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.knime.base.node.audio.panel.DialogComponentMultiFileChooser;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "AudioReader" Node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author Budi Yanto, KNIME.com
 */
public class AudioReaderNodeDialog extends DefaultNodeSettingsPane {

    public static final String CFG_DIR_HISTORY = "audioReaderDirHistory";

    private static final FileFilter FILEFILTER;

//    private static final String[] EXTENSIONS = new String[]{
//        "mp3", "ogg", "aiff", "aifc", "wav", "au", "snd"};

    private static final String[] EXTENSIONS = new String[]{
        "aiff", "aifc", "wav", "au", "snd"};

    static {
        FILEFILTER = new FileNameExtensionFilter("Audio files", EXTENSIONS);
    }

    private final DialogComponentMultiFileChooser m_fileChooser;

    /**
     * New pane for configuring the AudioReader node.
     */
    protected AudioReaderNodeDialog() {
        m_fileChooser = new DialogComponentMultiFileChooser(
            AudioReaderNodeModel.createFileListModel(), FILEFILTER, CFG_DIR_HISTORY);
        addDialogComponent(m_fileChooser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
            throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        super.saveAdditionalSettingsTo(settings);
    }
}

