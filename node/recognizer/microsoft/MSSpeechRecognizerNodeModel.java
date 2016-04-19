package org.knime.base.node.audio.node.recognizer.microsoft;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.knime.base.node.audio.data.KNAudio;
import org.knime.base.node.audio.data.node.AudioCell;
import org.knime.base.node.audio.data.node.AudioColumnSelection;
import org.knime.base.node.audio.data.recognizer.RecognitionResult;
import org.knime.base.node.audio.node.recognizer.microsoft.util.MSSpeechRecognizer;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of MSSpeechRecognizer.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class MSSpeechRecognizerNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(MSSpeechRecognizerNodeModel.class);

    private final AudioColumnSelection m_audioColumnSelection = new AudioColumnSelection();
    private final SettingsModelString m_subscriptionKeyModel = createSubscriptionKeySettingsModel();
    private final SettingsModelString m_audioLanguageModel = createAudioLanguageSettingsModel();
    private final SettingsModelString m_scenarioModel = createScenarioSettingsModel();
    private final SettingsModelIntegerBounded m_maxNBestModel = createMaxNBestSettingsModel();
    private final SettingsModelIntegerBounded m_profinityMarkupModel = createProfanityMarkupSettingsModel();
    private final MSSpeechRecognizer m_recognizer = new MSSpeechRecognizer();

    /**
     * Constructor for the node model.
     */
    protected MSSpeechRecognizerNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        if (inData == null || inData.length < 1) {
            throw new IllegalArgumentException("Invalid input data");
        }

        final BufferedDataTable dataTable = inData[0];
        m_recognizer.setSubscriptionKey(m_subscriptionKeyModel.getStringValue());
        m_recognizer.setLanguage(m_audioLanguageModel.getStringValue());
        m_recognizer.setScenario(m_scenarioModel.getStringValue());
        m_recognizer.setMaxNBest(m_maxNBestModel.getIntValue());
        m_recognizer.setProfanityMarkup(m_profinityMarkupModel.getIntValue());
        final ColumnRearranger rearranger = createColumnRearranger(
            dataTable.getDataTableSpec());

        return new BufferedDataTable[]{
            exec.createColumnRearrangeTable(dataTable, rearranger, exec)};
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
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        if (inSpecs == null || inSpecs.length < 1) {
            throw new IllegalArgumentException("Invalid input data table spec");
        }

        final DataTableSpec inSpec = inSpecs[0];
        m_audioColumnSelection.configure(inSpec);

        if(StringUtils.isBlank(m_subscriptionKeyModel.getStringValue())){
            throw new InvalidSettingsException("Subscription key cannot be empty.");
        }

        return new DataTableSpec[]{createColumnRearranger(inSpec).createSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         m_audioColumnSelection.saveSettingsTo(settings);
         m_subscriptionKeyModel.saveSettingsTo(settings);
         m_audioLanguageModel.saveSettingsTo(settings);
         m_scenarioModel.saveSettingsTo(settings);
         m_maxNBestModel.saveSettingsTo(settings);
         m_profinityMarkupModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_audioColumnSelection.laodSettingsFrom(settings);
        m_subscriptionKeyModel.loadSettingsFrom(settings);
        m_audioLanguageModel.loadSettingsFrom(settings);
        m_scenarioModel.loadSettingsFrom(settings);
        m_maxNBestModel.loadSettingsFrom(settings);
        m_profinityMarkupModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_audioColumnSelection.validateSettings(settings);
        m_subscriptionKeyModel.validateSettings(settings);
        m_audioLanguageModel.validateSettings(settings);
        m_scenarioModel.validateSettings(settings);
        m_maxNBestModel.validateSettings(settings);
        m_profinityMarkupModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec inSpec){
        final ColumnRearranger rearranger = new ColumnRearranger(inSpec);
        final int colIdx = m_audioColumnSelection.getSelectedColumnIndex();
        rearranger.replace(new SingleCellFactory(inSpec.getColumnSpec(colIdx)) {

            @Override
            public DataCell getCell(final DataRow row) {
                AudioCell cell = (AudioCell) row.getCell(colIdx);
                if(!cell.isMissing()){
                    try{
                        final KNAudio newAudio = cell.getAudio().clone();
                        final RecognitionResult result = m_recognizer.recognize(newAudio);
                        newAudio.addRecognitionResult(result);
                        cell = new AudioCell(newAudio);
                    } catch(Exception ex){
                        LOGGER.error(ex.getMessage());
                    }
                }
                return cell;
            }
        }, colIdx);
        return rearranger;
    }

    static SettingsModelString createSubscriptionKeySettingsModel(){
        return new SettingsModelString("SubscriptionKey", null);
    }

    static SettingsModelString createAudioLanguageSettingsModel(){
        return new SettingsModelString("AudioLanguage", MSSpeechRecognizer.DEFAULT_LANGUAGE);
    }

    static SettingsModelString createScenarioSettingsModel(){
        return new SettingsModelString("Scenario", MSSpeechRecognizer.DEFAULT_SCENARIO);
    }

    static SettingsModelIntegerBounded createMaxNBestSettingsModel(){
        return new SettingsModelIntegerBounded("MaxNBest",
            MSSpeechRecognizer.DEFAULT_MAXNBEST, 1, 5);
    }

    static SettingsModelIntegerBounded createProfanityMarkupSettingsModel(){
        return new SettingsModelIntegerBounded("ProfanityMarkup",
            MSSpeechRecognizer.DEFAULT_PROFANITY_MARKUP, 0, 1);
    }

}

