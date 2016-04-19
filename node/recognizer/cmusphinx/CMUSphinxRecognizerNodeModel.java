package org.knime.base.node.audio.node.recognizer.cmusphinx;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.knime.base.node.audio.data.KNAudio;
import org.knime.base.node.audio.data.node.AudioCell;
import org.knime.base.node.audio.data.node.AudioColumnSelection;
import org.knime.base.node.audio.data.recognizer.RecognitionResult;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of CMUSphinxRecognizer.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class CMUSphinxRecognizerNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CMUSphinxRecognizerNodeModel.class);

    private final AudioColumnSelection m_audioColumnSelection = new AudioColumnSelection();

    private final SettingsModelString m_acousticModelPath = createAcousticModelPathSettingsModel();

    private final SettingsModelString m_dictionaryPath = createDictionaryPathSettingsModel();

    private final SettingsModelString m_languageModelPath = createLanguageModelPathSettingsModel();

    private final CMUSphinxRecognizer m_recognizer = new CMUSphinxRecognizer();

    /**
     * Constructor for the node model.
     */
    protected CMUSphinxRecognizerNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        if (inData == null || inData.length < 1) {
            throw new IllegalArgumentException("Invalid input data");
        }

        final BufferedDataTable dataTable = inData[0];
        m_recognizer.setAcoustisModelPath(m_acousticModelPath.getStringValue());
        m_recognizer.setLanguageModelPath(m_languageModelPath.getStringValue());
        m_recognizer.setDictionaryPath(m_dictionaryPath.getStringValue());
        final ColumnRearranger rearranger = createColumnRearranger(
            dataTable.getDataTableSpec());

        return new BufferedDataTable[]{exec.createColumnRearrangeTable(dataTable, rearranger, exec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

        final DataTableSpec inSpec = inSpecs[0];
        m_audioColumnSelection.configure(inSpec);
        validateAcousticModelPath(m_acousticModelPath.getStringValue());
        validateDictionaryPath(m_dictionaryPath.getStringValue());
        validateLanguageModelPath(m_languageModelPath.getStringValue());
        return new DataTableSpec[]{createColumnRearranger(inSpec).createSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_audioColumnSelection.saveSettingsTo(settings);
        m_acousticModelPath.saveSettingsTo(settings);
        m_dictionaryPath.saveSettingsTo(settings);
        m_languageModelPath.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_audioColumnSelection.laodSettingsFrom(settings);
        m_acousticModelPath.loadSettingsFrom(settings);
        m_dictionaryPath.loadSettingsFrom(settings);
        m_languageModelPath.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_audioColumnSelection.validateSettings(settings);
        m_acousticModelPath.validateSettings(settings);
        m_dictionaryPath.validateSettings(settings);
        m_languageModelPath.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // TODO: generated method stub
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec inSpec) {
        final ColumnRearranger rearranger = new ColumnRearranger(inSpec);
        final int colIdx = m_audioColumnSelection.getSelectedColumnIndex();
        rearranger.replace(new SingleCellFactory(inSpec.getColumnSpec(colIdx)) {

            @Override
            public DataCell getCell(final DataRow row) {
                AudioCell cell = (AudioCell) row.getCell(colIdx);
                if(!cell.isMissing()){
                    try {
                        final KNAudio newAudio = cell.getAudio().clone();
                        final RecognitionResult result = m_recognizer.recognize(newAudio);
                        newAudio.addRecognitionResult(result);
                        cell = new AudioCell(newAudio);
                    } catch (Exception ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }

                return cell;
            }
        }, colIdx);
        return rearranger;
    }

    static SettingsModelString createAcousticModelPathSettingsModel() {
        return new SettingsModelString("AcousticModelPath", null);
    }

    static SettingsModelString createDictionaryPathSettingsModel() {
        return new SettingsModelString("DictionaryPath", null);
    }

    static SettingsModelString createLanguageModelPathSettingsModel() {
        return new SettingsModelString("LanguageModelPath", null);
    }

    static SettingsModelString createAudioColumnSettingsModel() {
        return new SettingsModelString("AudioColumn", null);
    }

    void validateAcousticModelPath(final String acousticModelPath) throws InvalidSettingsException {
        String error = "";
        final String message = "The default acoustic model will be used.";

        if (StringUtils.isBlank(acousticModelPath)) {
            error = "Acoustic model is empty.";
            setWarningMessage(error + " " + message);
            return;
//            throw new InvalidSettingsException("Acoustic model path cannot be empty.");
        }
        final File f = new File(acousticModelPath);
        if (!f.isDirectory()) {
            error = "Selected directory: " + acousticModelPath + " is not a directory.";
//            throw new InvalidSettingsException("Selected directory: " + acousticModelPath + " is not a directory!");
        } else if (!f.exists()) {
            error = "Selected directory: " + acousticModelPath + " does not exist.";
//            throw new InvalidSettingsException("Selected directory: " + acousticModelPath + " does not exist!");
        } else if (f.listFiles().length < 1) {
            error = "Selected directory: " + acousticModelPath + " is empty.";
//            throw new InvalidSettingsException("Selected directory: " + acousticModelPath + " is empty!");
        }
        setWarningMessage(error+ " " + message);
    }

    void validateDictionaryPath(final String dictionaryPath) throws InvalidSettingsException {
        String error = "";
        final String message = "The default dictinary will be used.";
        if (StringUtils.isBlank(dictionaryPath)) {
            error = "Dictionary path is empty.";
            setWarningMessage(error + " " + message);
            return;
//            throw new InvalidSettingsException("Dictionary path cannot be empty.");
        }
        final File f = new File(dictionaryPath);
        if (!f.isFile()) {
            error = "Selected file: " + dictionaryPath + " is not a file.";
//            throw new InvalidSettingsException("Selected file: " + dictionaryPath + " is not a file!");
        } else if (!f.exists()) {
            error = "Selected file: " + dictionaryPath + " does not exist.";
//            throw new InvalidSettingsException("Selected file: " + dictionaryPath + " does not exist!");
        } else if (!FilenameUtils.getExtension(dictionaryPath).equalsIgnoreCase("dict")) {
            error = "Selected file: " + dictionaryPath + " doesn't have the extension \".dict\".";
//            throw new InvalidSettingsException(
//                "Selected file: " + dictionaryPath + " doesn't have the extension \".dict\"!");
        }
        setWarningMessage(error + " " + message);
    }

    void validateLanguageModelPath(final String languageModelPath) throws InvalidSettingsException {
        String error = "";
        final String message = "The default language model will be used.";
        if (StringUtils.isEmpty(languageModelPath)) {
            error = "Language model path is empty.";
            setWarningMessage(error + " " + message);
            return;
//            throw new InvalidSettingsException("Language model path cannot be empty.");
        }

        final File f = new File(languageModelPath);
        if (!f.isFile()) {
            error = "Selected file: " + languageModelPath + " is not a file.";
//            throw new InvalidSettingsException("Selected file: " + languageModelPath + " is not a file!");
        } else if (!f.exists()) {
            error = "Selected file: " + languageModelPath + " does not exist.";
//            throw new InvalidSettingsException("Selected file: " + languageModelPath + " does not exist!");
        } else if (!FilenameUtils.getExtension(languageModelPath).equalsIgnoreCase("lm")) {

            error = "Selected file: " + languageModelPath + " doesn't have the extension \".lm\".";
//            throw new InvalidSettingsException(
//                "Selected file: " + languageModelPath + " doesn't have the extension \".lm\"!");
        }
        setWarningMessage(error + " " + message);
    }

}
