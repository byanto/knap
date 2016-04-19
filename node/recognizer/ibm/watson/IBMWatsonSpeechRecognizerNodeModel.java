package org.knime.base.node.audio.node.recognizer.ibm.watson;

import java.io.File;
import java.io.IOException;

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
 * This is the model implementation of IBMWatsonSpeechRecognizer.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class IBMWatsonSpeechRecognizerNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(IBMWatsonSpeechRecognizerNodeModel.class);

    private AudioColumnSelection m_audioColumnSelection = new AudioColumnSelection();
    private SettingsModelString m_userNameSettingsModel = createUserNameSettingsModel();
    private SettingsModelString m_passwordSettingsModel = createPasswordSettingsModel();

    private final IBMWatsonSpeechRecognizer m_recognizer = new IBMWatsonSpeechRecognizer();

    /**
     * Constructor for the node model.
     */
    protected IBMWatsonSpeechRecognizerNodeModel() {
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
        m_recognizer.setUserName(m_userNameSettingsModel.getStringValue());
        m_recognizer.setPassword(m_passwordSettingsModel.getStringValue());
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

        if(StringUtils.isBlank(m_userNameSettingsModel.getStringValue())){
            throw new InvalidSettingsException("User name cannot be empty.");
        }

        if(StringUtils.isBlank(m_passwordSettingsModel.getStringValue())){
            throw new InvalidSettingsException("Password cannot be empty.");
        }

        return new DataTableSpec[]{createColumnRearranger(inSpec).createSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         m_audioColumnSelection.saveSettingsTo(settings);
         m_userNameSettingsModel.saveSettingsTo(settings);
         m_passwordSettingsModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_audioColumnSelection.laodSettingsFrom(settings);
        m_userNameSettingsModel.loadSettingsFrom(settings);
        m_passwordSettingsModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_audioColumnSelection.validateSettings(settings);
        m_userNameSettingsModel.validateSettings(settings);
        m_passwordSettingsModel.validateSettings(settings);
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

    static SettingsModelString createUserNameSettingsModel(){
        return new SettingsModelString("UserName", null);
    }

    static SettingsModelString createPasswordSettingsModel(){
        return new SettingsModelString("Password", null);
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec inSpec){
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

}

