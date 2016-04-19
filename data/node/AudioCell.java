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
 *   Mar 3, 2016 (budiyanto): created
 */
package org.knime.base.node.audio.data.node;

import java.io.File;
import java.net.URL;

import org.knime.base.node.audio.data.KNAudio;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;

/**
 *
 * @author Budi Yanto, KNIME.com
 */
public class AudioCell extends DataCell implements AudioValue, StringValue {

    private static final long serialVersionUID = 2622699975348511091L;

    /**
     * Convenience access member for
     * <code>DataType.getType(AudioCell.class)</code>.
     *
     * @see DataType#getType(Class)
     */
    public static final DataType TYPE = DataType.getType(AudioCell.class);

    private final KNAudio m_audio;

    /**
     *
     * @param audio
     */
    public AudioCell(final KNAudio audio){
        m_audio = audio;
    }

    /**
     * @param url
     */
    public AudioCell(final URL url) {
        if(url == null){
            throw new NullPointerException("Audio url can't be null.");
        }
        m_audio = new KNAudio(url);
    }

    /**
     * Creates a new Audio Cell based on the given audio path.
     * @param audioPath path to the audio file
     * @throws NullPointerException if the given audio path is <code>null</code> or empty
     */
    public AudioCell(final String audioPath) {
        if(audioPath == null){
            throw new NullPointerException("Audio Path can't be null.");
        }

        if(audioPath.isEmpty()){
            throw new IllegalArgumentException("Audio path can't be empty.");
        }
        m_audio = new KNAudio(audioPath);
    }

    /**
     *
     * @param file
     */
    public AudioCell(final File file) {
        m_audio = new KNAudio(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getStringValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        if(dc == null){
            return false;
        }

        AudioCell cell = (AudioCell) dc;
        if(!cell.getAudio().equals(m_audio)){
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return AudioValue.hashCode(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KNAudio getAudio() {
        return m_audio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue() {
        StringBuilder builder = new StringBuilder();
        builder.append("Audio[\npath=");
        builder.append(m_audio.getFilePath());
        builder.append("\n]");
        return builder.toString();
    }

}
