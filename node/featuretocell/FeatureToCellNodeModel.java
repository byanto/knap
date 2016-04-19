package org.knime.base.node.audio.node.featuretocell;

import java.io.File;
import java.io.IOException;

import org.knime.base.node.audio.data.feature.FeatureType;
import org.knime.base.node.audio.data.node.AudioFeatureCellExtractor;
import org.knime.base.node.audio.data.node.AudioFeatureExtractorCellFactory;
import org.knime.base.node.audio.data.node.AudioValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.filter.NameFilterConfiguration;

/**
 * This is the model implementation of AudioVector.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class FeatureToCellNodeModel extends NodeModel {

    private final SettingsModelString m_audioCol = createAudioColumnSettingsModel();
    private final FeatureToCellFilterConfiguration m_config = createAudioVectorFilterConfiguration();
    private DataColumnSpec[] m_featureColSpecs = null;

    /**
     * Constructor for the node model.
     */
    protected FeatureToCellNodeModel() {
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
        final DataTableSpec inSpec = dataTable.getDataTableSpec();

        final String audioColName = m_audioCol.getStringValue();
        final int audioColIdx = inSpec.findColumnIndex(audioColName);
        if (audioColIdx < 0) {
            throw new InvalidSettingsException("Invalid audio column: " + audioColName);
        }

        final BufferedDataTable resultTable;
        String[] includedList = m_config.getIncludedList();
        if(includedList == null || includedList.length == 0){
            setWarningMessage("No feature is selected. Node returns the original unaltered table.");
            resultTable = dataTable;
        }else{
            final ColumnRearranger rearranger = new ColumnRearranger(inSpec);
            final AudioFeatureCellExtractor[] extractors = AudioFeatureCellExtractor
                    .getExctractor(FeatureType.getFeatureTypes(includedList));
            final CellFactory cellFactory = new AudioFeatureExtractorCellFactory(
                audioColIdx, m_featureColSpecs, extractors);
            rearranger.append(cellFactory);
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

        String[] includedList = m_config.getIncludedList();
        if(includedList == null || includedList.length < 1){
            setWarningMessage("No feature is selected.");
        }
        final DataTableSpec inSpec = inSpecs[0];
        final String audioColName = m_audioCol.getStringValue();
        if(audioColName == null){
            // At the beginning, no audio column is selected
            // So, select the first audio column in the DataTableSpec
            for(final DataColumnSpec colSpec : inSpec){
                if(colSpec.getType().isCompatible(AudioValue.class)){
                    m_audioCol.setStringValue(colSpec.getName());
                    break;
                }
            }
        }else{
            // Check whether the selected audio column really exists in the DataTableSpec
            final int colIndex = inSpec.findColumnIndex(audioColName);
            if(colIndex < 0){
                throw new InvalidSettingsException("Invalid column name: " + audioColName);
            }
        }

        final AudioFeatureCellExtractor[] extractors = AudioFeatureCellExtractor.getExctractor(
            FeatureType.getFeatureTypes(includedList));
        m_featureColSpecs = createColumnSpecs(inSpec, extractors);

        return new DataTableSpec[]{createTableSpec(inSpec, m_featureColSpecs)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_audioCol.saveSettingsTo(settings);
         m_config.saveConfiguration(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_audioCol.loadSettingsFrom(settings);
        m_config.loadConfigurationInModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_audioCol.validateSettings(settings);
        final NameFilterConfiguration config = createAudioVectorFilterConfiguration();
        config.loadConfigurationInModel(settings);
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

    /**
     * @param origSpec the original {@link DataTableSpec}
     * @param extractors the extractors to use
     * @return the {@link DataColumnSpec} for the given
     * {@link AudioFeatureCellExtractor} in the same order as given
     */
    private static DataColumnSpec[] createColumnSpecs(
            final DataTableSpec origSpec,
            final AudioFeatureCellExtractor[] extractors) {
        if (extractors == null || extractors.length < 1) {
            return new DataColumnSpec[0];
        }
        final DataColumnSpec[] cols = new DataColumnSpec[extractors.length];
        for (int i = 0, length = extractors.length; i < length; i++) {
             final String name = DataTableSpec.getUniqueColumnName(origSpec,
                     extractors[i].getType().getName());
             final DataType type = extractors[i].getDataType();
             cols[i] = new DataColumnSpecCreator(name, type).createSpec();
        }
        return cols;
    }

    /**
     * @param origSpec the original {@link DataTableSpec}
     * @param columnSpecs the extractor {@link DataColumnSpec}s
     * @return the original {@link DataTableSpec} with a new column
     * specification per {@link AudioFeatureCellExtractor} attached to it
     */
    private static final DataTableSpec createTableSpec(final DataTableSpec origSpec,
            final DataColumnSpec[] columnSpecs) {
        if (columnSpecs == null || columnSpecs.length == 0) {
            return origSpec;
        }
        return new DataTableSpec(origSpec, new DataTableSpec(columnSpecs));
    }

    static FeatureToCellFilterConfiguration createAudioVectorFilterConfiguration(){
        return new FeatureToCellFilterConfiguration("features",
            FeatureType.getFeatureTypeNames());
    }

    static SettingsModelString createAudioColumnSettingsModel(){
        return new SettingsModelString("audioColumn", null);
    }

}

