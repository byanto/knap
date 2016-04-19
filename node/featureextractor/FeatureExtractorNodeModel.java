package org.knime.base.node.audio.node.featureextractor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.knime.base.node.audio.data.KNAudio;
import org.knime.base.node.audio.data.feature.FeatureType;
import org.knime.base.node.audio.data.feature.extractor.FeatureExtractor;
import org.knime.base.node.audio.data.node.AudioCell;
import org.knime.base.node.audio.data.node.AudioColumnSelection;
import org.knime.base.node.audio.ext.org.openimaj.feature.DoubleFV;
import org.knime.base.node.audio.util.AudioUtils;
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
 * This is the model implementation of FeatureExtractor.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class FeatureExtractorNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(FeatureExtractorNodeModel.class);

    private final AudioColumnSelection m_audioColumnSelection = new AudioColumnSelection();
    private final FeatureExtractorSettings m_settings = new FeatureExtractorSettings();

    /**
     * Constructor for the node model.
     */
    protected FeatureExtractorNodeModel() {
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

        Set<FeatureType> selFeatures = m_settings.getSelectedFeatures();
        final BufferedDataTable resultTable;
        if(selFeatures == null || selFeatures.isEmpty()){
            setWarningMessage("No feature is selected. Node returns the original unaltered table.");
            resultTable = dataTable;
        }else{
            final ColumnRearranger rearranger = createColumnRearranger(
                dataTable.getDataTableSpec());
            resultTable = exec.createColumnRearrangeTable(dataTable, rearranger, exec);
        }

        return new BufferedDataTable[]{resultTable};
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
            throw new InvalidSettingsException("Invalid input spec");
        }
        final Set<FeatureType> selectedTypes = m_settings.getSelectedFeatures();
        if(selectedTypes == null || selectedTypes.isEmpty()){
            setWarningMessage("No feature is selected.");
        }

        final DataTableSpec inSpec = inSpecs[0];
        m_audioColumnSelection.configure(inSpec);

        final ColumnRearranger rearranger = createColumnRearranger(inSpec);

        return new DataTableSpec[]{rearranger.createSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_audioColumnSelection.saveSettingsTo(settings);
        m_settings.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_audioColumnSelection.laodSettingsFrom(settings);
        m_settings.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_audioColumnSelection.validateSettings(settings);
        m_settings.validateSettings(settings);
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

    static SettingsModelString createAudioColumnSettingsModel(){
        return new SettingsModelString("audioColumn", null);
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec spec) {
        final ColumnRearranger rearranger = new ColumnRearranger(spec);
        final int colIdx = m_audioColumnSelection.getSelectedColumnIndex();

        rearranger.replace(new SingleCellFactory(spec.getColumnSpec(colIdx)) {

            @Override
            public DataCell getCell(final DataRow row) {
                AudioCell cell = (AudioCell) row.getCell(colIdx);
                if(!cell.isMissing()){
                    final KNAudio newAudio = ((AudioCell)row.getCell(colIdx))
                            .getAudio().clone();
                    try{
//                        final double[] samples = AudioUtils.getSamples(newAudio);
                        final double[] samples = AudioUtils.getSamplesMixedDownIntoOneChannel(newAudio);
                        Set<FeatureType> selFeatures = m_settings.getSelectedFeatures();
                        final FeatureType[] selectedFeatures = selFeatures.toArray(new FeatureType[selFeatures.size()]);
                        final FeatureExtractor[] extractors = FeatureExtractor.getFeatureExtractors(selectedFeatures);
                        m_settings.updateExtractorParameters(extractors);
                        final double sampleRate = newAudio.getFormat().getSampleRateKHz() * 1000;
                        final Map<FeatureType, double[]> features = AudioUtils.extractFeatures(samples,
                            sampleRate, extractors);

                        for(Entry<FeatureType, double[]> entry : features.entrySet()){
                            newAudio.setFeatureVector(entry.getKey(), new DoubleFV(entry.getValue()));
                        }

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
}

