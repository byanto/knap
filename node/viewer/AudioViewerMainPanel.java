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
 *   Apr 5, 2016 (budiyanto): created
 */
package org.knime.base.node.audio.node.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.knime.base.node.audio.data.KNAudio;
import org.knime.core.node.KNIMEConstants;

/**
 *
 * @author Budi Yanto, KNIME.com
 */
class AudioViewerMainPanel extends JPanel{

    /**
     *  Automatically generated serial version UID.
     */
    private static final long serialVersionUID = -5256636234914633749L;

    private final List<KNAudio> m_audios;
    private final JTable m_table = new JTable();
    private int m_selectedRowIndex;

    /**
     *
     */
    AudioViewerMainPanel(final List<KNAudio> audios) {
        if(audios == null){
            m_audios = new ArrayList<KNAudio>(0);
        }else{
            m_audios = audios;
        }
        setLayout(new BorderLayout());
        add(createMainPanel(), BorderLayout.CENTER);
    }


    private JPanel createMainPanel(){
        final JPanel panel = new JPanel();
        m_table.setModel(initializeTableModel());
        m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_table.setOpaque(false);
        m_table.setToolTipText("Double click to open audio view");
        m_table.getColumnModel().getColumn(0).setPreferredWidth(50);
        m_table.getColumnModel().getColumn(1).setPreferredWidth(750);
        m_table.addMouseListener(new TableListener());
        final JScrollPane scrollPane = new JScrollPane(m_table);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        panel.add(scrollPane);
        return panel;
    }

    private TableModel initializeTableModel(){
        final DefaultTableModel model = new DefaultTableModel(
            new String[]{"No", "Audio Path"}, 0){
            /**
             * Automatically generated serial version UID
             */
            private static final long serialVersionUID = -4564933999458666811L;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };

        int idx = 1;
        for(final KNAudio audio : m_audios){
            model.addRow(new Object[]{idx++, audio.getFilePath()});
        }

        return model;
    }

    private class TableListener extends MouseAdapter{
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(final MouseEvent e) {
            // if double clicked
            if (e.getClickCount() == 2) {
                m_selectedRowIndex = m_table.getSelectedRow();
                viewAudio(m_audios.get(m_selectedRowIndex));
            }
        }

        private void viewAudio(final KNAudio audio){
            JFrame detailsFrame = new JFrame(audio.getName());
            if (KNIMEConstants.KNIME16X16 != null) {
                detailsFrame.setIconImage(KNIMEConstants.KNIME16X16.getImage());
            }

            final AudioCellView view = new AudioCellView(audio);
            detailsFrame.setContentPane(view);
            detailsFrame.pack();
            detailsFrame.setVisible(true);
        }
    }

}
