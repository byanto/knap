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

import java.util.ArrayList;
import java.util.List;

import org.knime.base.node.audio.data.feature.FeatureType;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.DoubleCell;

/**
 *
 * @author Budi Yanto, KNIME.com
 */
public enum AudioFeatureCellExtractor {
    /**
     *
     */
    POWER_SPECTRUM (FeatureType.POWER_SPECTRUM, new CellExtractor() {

        @Override
        public DataCell getValue(final double[] featureValues) {
            if(featureValues == null || featureValues.length == 0){
                return DataType.getMissingCell();
            }
            final List<DoubleCell> cells = new ArrayList<DoubleCell>();
            for(double val : featureValues){
                cells.add(new DoubleCell(val));
            }
            return CollectionCellFactory.createListCell(cells);
        }

        @Override
        public DataType getDataType() {
            return ListCell.getCollectionType(DoubleCell.TYPE);
        }

    }),

    /**
     *
     */
    MAGNITUDE_SPECTRUM (FeatureType.MAGNITUDE_SPECTRUM, new CellExtractor() {

        @Override
        public DataCell getValue(final double[] featureValues) {
            if(featureValues == null || featureValues.length == 0){
                return DataType.getMissingCell();
            }

            final List<DoubleCell> cells = new ArrayList<DoubleCell>();
            for(double val : featureValues){
                cells.add(new DoubleCell(val));
            }
            return CollectionCellFactory.createListCell(cells);
        }

        @Override
        public DataType getDataType() {
            return ListCell.getCollectionType(DoubleCell.TYPE);
        }

    }),

    /**
     *
     */
    MFCC (FeatureType.MFCC, new CellExtractor() {

        @Override
        public DataCell getValue(final double[] featureValues) {
            final List<DoubleCell> cells = new ArrayList<DoubleCell>();
            if(featureValues != null && featureValues.length > 0){
                for(double val : featureValues){
                    cells.add(new DoubleCell(val));
                }
            }

            return CollectionCellFactory.createListCell(cells);
        }

        @Override
        public DataType getDataType() {
            return ListCell.getCollectionType(DoubleCell.TYPE);
        }

    });


    private interface CellExtractor {
        /**
         * @param featureValues the feature values to extract the data from
         * @return the extracted data as {@link DataCell}
         */
        public DataCell getValue(final double[] featureValues);

        /**
         * @return the {@link DataType}
         */
        public DataType getDataType();
    }

    private final FeatureType m_type;
    private final CellExtractor m_extractor;

    private AudioFeatureCellExtractor(final FeatureType type, final CellExtractor extractor){
        if (type == null) {
            throw new IllegalArgumentException("Type must not be empty");
        }
        if (extractor == null) {
            throw new NullPointerException("Extractor must not be null");
        }
        m_type = type;
        m_extractor = extractor;
    }

    /**
     * @return the name of the extractor
     */
    public FeatureType getType() {
        return m_type;
    }

    /**
     * @return the {@link DataType} the extractor returns as result
     */
    public DataType getDataType() {
        return m_extractor.getDataType();
    }

    /**
     * @param featureValues the feature values to extract the data from
         * @return the extracted data as {@link DataCell}
     */
    public DataCell getValue(final double[] featureValues) {
        return m_extractor.getValue(featureValues);
    }

    /**
     * @return the type of all cell extractors
     */
    public static FeatureType[] getCellExtractorTypes() {
        final AudioFeatureCellExtractor[] values = values();
        final FeatureType[] types = new  FeatureType[values.length];
        for (int i = 0, length = values.length; i < length; i++) {
            types[i] = values[i].getType();
        }
        return types;
    }

    /**
     * @param types the type of the cell extractors to get
     * @return the extractors with the given type in the same order
     */
    public static AudioFeatureCellExtractor[] getExctractor(final FeatureType...types) {
        if (types == null) {
            return null;
        }
        final AudioFeatureCellExtractor[] extractors =
            new AudioFeatureCellExtractor[types.length];
        for (int i = 0, length = types.length; i < length; i++) {
            final FeatureType type = types[i];
            for (final AudioFeatureCellExtractor extractor : values()) {
                if (extractor.getType().equals(type)) {
                    extractors[i] = extractor;
                    break;
                }
            }
            if (extractors[i] == null) {
                throw new IllegalArgumentException(
                        "Invalid extractor type: " + type);
            }
        }
        return extractors;
    }

}
