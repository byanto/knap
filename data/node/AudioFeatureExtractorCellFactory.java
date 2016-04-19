/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Mar 24, 2016 (budiyanto): created
 */
package org.knime.base.node.audio.data.node;

import org.knime.base.node.audio.data.KNAudio;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.AbstractCellFactory;

/**
 * The {@link AbstractCellFactory} implementation of the AudioFeatureExtractor node
 * that creates a cell for each selected document property.
 *
 * @author Budi Yanto, KNIME.com
 */
public class AudioFeatureExtractorCellFactory extends AbstractCellFactory{

    private final int m_audioColIdx;
    private final AudioFeatureCellExtractor[] m_extractors;

    /**
     * Constructor for {@link AudioFeatureExtractorCellFactory}
     * @param audioColIdx the index of the audio column
     * @param colSpecs the {@link DataColumnSpec} to return in the same order
     * as the extractors
     * @param extractors the {@link AudioFeatureCellExtractor}s to use
     */
    public AudioFeatureExtractorCellFactory(final int audioColIdx,
            final DataColumnSpec[] colSpecs,
            final AudioFeatureCellExtractor[] extractors) {
        super(colSpecs);
        if (audioColIdx < 0) {
            throw new IllegalArgumentException("Invalid audio column");
        }
        if (extractors == null || extractors.length < 1) {
            throw new IllegalArgumentException("extractors must not be empty");
        }
        if (colSpecs.length != extractors.length) {
            throw new IllegalArgumentException(
                    "Column specs and extractors must have the same sice");
        }
        m_audioColIdx = audioColIdx;
        m_extractors = extractors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        final DataCell cell = row.getCell(m_audioColIdx);
        KNAudio audio = null;
        if(cell.getType().isCompatible(AudioValue.class)){
            audio = ((AudioCell) cell).getAudio();
        }else{
            throw new IllegalStateException("Invalid column type");
        }

        final DataCell[] cells = new DataCell[m_extractors.length];
        for(int i = 0; i < m_extractors.length; i++){
            final AudioFeatureCellExtractor extractor = m_extractors[i];
            final double[] values = audio.getFeatureVector(extractor.getType()).asDoubleVector();
            cells[i] = extractor.getValue(values);
        }
        return cells;
    }

}
