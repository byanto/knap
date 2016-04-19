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
import java.awt.GridLayout;
import java.util.Map.Entry;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.knime.base.node.audio.data.KNAudio;
import org.knime.base.node.audio.data.feature.FeatureType;
import org.knime.base.node.audio.data.recognizer.RecognitionResult;
import org.knime.base.node.audio.data.recognizer.RecognizerInfo;
import org.knime.base.node.audio.ext.org.openimaj.feature.DoubleFV;
import org.knime.base.node.audio.util.AudioUtils;
import org.knime.core.node.NodeLogger;

/**
 *
 * @author Budi Yanto, KNIME.com
 */
class AudioCellView extends JPanel{

    /**
     * Automatically generated serial version UID
     */
    private static final long serialVersionUID = -5710478483144111413L;

    private static final NodeLogger LOGGER = NodeLogger.getLogger(AudioCellView.class);

    private final KNAudio m_audio;

    AudioCellView(final KNAudio audio){
        m_audio = audio;
        setLayout(new BorderLayout());
        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Viewer", createViewerPanel());
        tabbedPane.add("Features", createFeaturesPanel());
        tabbedPane.add("Recognition", createRecognitionPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JSplitPane createViewerPanel(){
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setPreferredSize(new Dimension(1000, 500));
        final JScrollPane waveScrollPane = new JScrollPane(createAudioWavePanel());
        waveScrollPane.setMinimumSize(new Dimension(700, 600));

        final JScrollPane infoScrollPane = new JScrollPane(createAudioInfoPanel());
        infoScrollPane.setMinimumSize(new Dimension(200, 300));

        splitPane.setLeftComponent(waveScrollPane);
        splitPane.setRightComponent(infoScrollPane);
        splitPane.setDividerLocation(0.8);
        return splitPane;
    }

    private JPanel createAudioWavePanel(){

        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
//        panel.setPreferredSize(new Dimension(600, 300));
        panel.setBorder(BorderFactory.createTitledBorder("Audio Wave"));
        try{
            final double[][] samples = AudioUtils.getSamples(m_audio);
            final double[] yData = AudioUtils.getSamplesMixedDownIntoOneChannel(m_audio);

            for(int channel = 0; channel < samples.length; channel++){
                final XYSeriesCollection dataset = new XYSeriesCollection();
                final XYSeries series = new XYSeries("Audio Wave");
//                series.
                for(int i = 0; i < samples[channel].length; i++){
                    series.add(i, samples[channel][i]);
                }
                dataset.addSeries(series);
                JFreeChart chart = ChartFactory.createXYLineChart(
                    "Channel " + (channel + 1), "Sample", "Value", dataset);
                chart.removeLegend();
                final JPanel chartPanel = new ChartPanel(chart);
                panel.add(chartPanel);
            }

        } catch(Exception ex){
            panel.add(new JLabel("Error generating audio wave panel for: "
                    + m_audio.getName()));
            LOGGER.error(ex.getMessage());
        }

        return panel;
    }

    private JPanel createAudioInfoPanel(){
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 300));
        panel.setBorder(BorderFactory.createTitledBorder("Audio Information"));

        final DefaultTableModel model = new DefaultTableModel(0, 2){

            private static final long serialVersionUID = 1L;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };

        model.addRow(new Object[]{"Name", m_audio.getName()});
        model.addRow(new Object[]{"Path", m_audio.getFilePath()});

        final AudioFormat format = m_audio.getFormat().getJavaAudioFormat();
        model.addRow(new Object[]{"Encoding", format.getEncoding()});
        model.addRow(new Object[]{"Sample Rate", format.getSampleRate()});
        model.addRow(new Object[]{"Sample Size in Bits", format.getSampleSizeInBits()});
        model.addRow(new Object[]{"Channels", format.getChannels()});
        model.addRow(new Object[]{"Frame Size", format.getFrameSize()});
        model.addRow(new Object[]{"Frame Rate", format.getFrameRate()});
        model.addRow(new Object[]{"Big Endian", format.isBigEndian()});

        final JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setTableHeader(null);
        table.getColumnModel().getColumn(0).setMinWidth(120);
        table.getColumnModel().getColumn(0).setMaxWidth(120);

        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel createFeaturesPanel(){
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(600, 400));
        panel.setBorder(BorderFactory.createTitledBorder("Extracted Features"));

        if(m_audio.getExtractedFeatures().isEmpty()){
            final JLabel label = new JLabel("No features is extracted yet.");
            panel.add(label, BorderLayout.CENTER);
        }else{
            final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Features");
            initializeFeaturesTree(root);
            final JTree tree = new JTree(root);
            final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
            renderer.setLeafIcon(null);
            final JScrollPane scrollPane = new JScrollPane(tree);
            panel.add(scrollPane, BorderLayout.CENTER);
        }

        return panel;
    }

    private void initializeFeaturesTree(final DefaultMutableTreeNode root){
        for(Entry<FeatureType, DoubleFV> entry: m_audio.getExtractedFeatures().entrySet()){
            final FeatureType key = entry.getKey();
            final DoubleFV value = entry.getValue();
            final DefaultMutableTreeNode type = new DefaultMutableTreeNode(key.getName());
            root.add(type);
            // Create definitions nodes
            final DefaultMutableTreeNode def = new DefaultMutableTreeNode("Definition");
            type.add(def);
            final DefaultMutableTreeNode typeName = new DefaultMutableTreeNode("Name: " + key.getName());
            final DefaultMutableTreeNode typeDesc = new DefaultMutableTreeNode("Description: " + key.getDescription());
            def.add(typeName);
            def.add(typeDesc);
            if(key.hasDependencies()){
                final DefaultMutableTreeNode dependencies = new DefaultMutableTreeNode("Dependencies");
                for(final FeatureType ft : key.getDependencies()){
                    final DefaultMutableTreeNode depNode = new DefaultMutableTreeNode(ft.getName());
                    dependencies.add(depNode);
                }
                def.add(dependencies);
            }
            if(key.hasParameters()){
                final DefaultMutableTreeNode parameters = new DefaultMutableTreeNode("Parameters");
                for(final String param : key.getParameters()){
                    final DefaultMutableTreeNode paramNode = new DefaultMutableTreeNode(param);
                    parameters.add(paramNode);
                }
                def.add(parameters);
            }

            // Create Values nodes
            final DefaultMutableTreeNode val = new DefaultMutableTreeNode("Values");
            type.add(val);
            for(final double d : value.asDoubleVector()){
                final DefaultMutableTreeNode valNode = new DefaultMutableTreeNode(d);
                val.add(valNode);
            }

        }
    }

    private JPanel createRecognitionPanel(){
        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(600, 400));

        if(!m_audio.hasRecognitionResult()){
            final JLabel label = new JLabel("No audio recognition is available yet.");
            mainPanel.add(label, BorderLayout.CENTER);
        }else{
            final JPanel descriptionPanel = new JPanel(new GridLayout(1, 1));
            final JEditorPane descriptionEditor = new JEditorPane("text/html", "No recognizer is selected.");
            descriptionEditor.setEditable(false);
            final JScrollPane descriptionScrollPane = new JScrollPane(descriptionEditor);
            descriptionScrollPane.setBorder(BorderFactory.createTitledBorder("Information"));
            descriptionScrollPane.setMinimumSize(new Dimension(400, 200));
            descriptionPanel.add(descriptionScrollPane);

            final JPanel recognizersPanel = new JPanel(new GridLayout(1, 1));
            final Set<String> recognizerSet = m_audio.getRecognitionResults().keySet();
//            final Set<String> recognizerSet = m_audio.getRecognizers().keySet();
            final JList<String> recognizerList = new JList<String>(
                    recognizerSet.toArray(new String[recognizerSet.size()]));
            recognizerList.setSelectedIndex(0);
            updateDescriptionEditor(descriptionEditor,
                recognizerList.getSelectedValue());
            recognizerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            recognizerList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    if(!e.getValueIsAdjusting()){
                        updateDescriptionEditor(descriptionEditor,
                            recognizerList.getSelectedValue());
                    }
                }
            });
            final JScrollPane recognizerScrollPane = new JScrollPane(recognizerList);
            recognizerScrollPane.setMinimumSize(new Dimension(250, 200));
            recognizerScrollPane.setBorder(BorderFactory.createTitledBorder("Recognizers"));
            recognizersPanel.add(recognizerScrollPane);

            final JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, recognizersPanel, descriptionPanel);

            mainPanel.add(splitPane, BorderLayout.CENTER);

        }

        return mainPanel;
    }

    private void updateDescriptionEditor(final JEditorPane editor,
            final String recognizerKey){
        final RecognitionResult result = m_audio.getRecognitionResult(recognizerKey);
        final StringBuilder builder = new StringBuilder()
        .append("<h2>").append(recognizerKey).append("</h2>")
        .append("<h3>Recognizer Info</h3>")
        .append("Type: ").append(result.getRecognizerInfo(RecognizerInfo.KEY_NAME))
        .append("<h3>Recognition Result</h3>")
        .append("Transcript: ").append(result.getTranscript()).append("<br/>")
        .append("Confidence: ");
        if(result.getConfidence() == RecognitionResult.UNKNOWN_CONFIDENCE_SCORE){
            builder.append("Unknown");
        }else{
            builder.append(result.getConfidence());
        }
        editor.setText(builder.toString());
    }

//    private JPanel createAudioWavePanel(){
//
//    }

}
