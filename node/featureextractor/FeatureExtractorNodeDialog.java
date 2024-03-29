package org.knime.base.node.audio.node.featureextractor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.knime.base.node.audio.data.feature.FeatureType;
import org.knime.base.node.audio.data.node.AudioColumnSelection;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

/**
 * <code>NodeDialog</code> for the "FeatureExtractor" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple dialog with standard
 * components. If you need a more complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author Budi Yanto, KNIME.com
 */
public class FeatureExtractorNodeDialog extends NodeDialogPane {

    private final DialogComponentColumnNameSelection m_audioColumnComponent;

    private final JTable m_featuresTable;

    private final JEditorPane m_descriptionPane;

    private final JPanel m_parameterPanel;

    private final JLabel m_parameterLabel;

    private final JButton m_parameterButton;

    private final FeatureExtractorSettings m_settings;

    /**
     * New pane for configuring the FeatureExtractor node.
     */
    @SuppressWarnings("unchecked")
    protected FeatureExtractorNodeDialog() {
        super();
        m_settings = new FeatureExtractorSettings();
        final JPanel mainPanel = new JPanel(new BorderLayout());

        /* Create dialog component to choose the audio column to extract the features from */
        m_audioColumnComponent = AudioColumnSelection.createDialogComponent();


//        m_audioColumnComponent = new DialogComponentColumnNameSelection(
//            FeatureExtractorNodeModel.createAudioColumnSettingsModel(),
//            "Audio Column", 0, AudioValue.class);
        final JPanel audioColumnPanel = m_audioColumnComponent.getComponentPanel();
        audioColumnPanel.setBorder(BorderFactory.createTitledBorder("Select Audio Column"));

        /* Content Panel to hold the features selection and description */
        final JPanel contentPanel = new JPanel(new GridLayout(1, 1));

        /* Create left panel for the selection of the available features */
        final JPanel leftPanel = new JPanel(new GridLayout(1, 1));
        m_featuresTable = new JTable(new FeaturesTableModel());
        initFeaturesTable();

        // Create list of available features
        final JScrollPane featuresScrollPane = new JScrollPane(m_featuresTable);
        featuresScrollPane.setMinimumSize(new Dimension(200, 155));
        featuresScrollPane.setBorder(BorderFactory.createTitledBorder("Select Features"));
        leftPanel.add(featuresScrollPane);

        /* Create right panel for the description and parameters of the selected feature */
        final JPanel rightPanel = new JPanel(new GridLayout(1, 1));
        final Box rightBox = Box.createVerticalBox();
        rightPanel.add(rightBox);
        m_descriptionPane = new JEditorPane("text/html", "No feature is selected.");
        m_descriptionPane.setEditable(false);

        // Create panel for features description
        final JScrollPane descriptionScrollPane = new JScrollPane(m_descriptionPane);
        descriptionScrollPane.setPreferredSize(new Dimension(400, 300));
        descriptionScrollPane.setMinimumSize(new Dimension(100, 200));
        descriptionScrollPane.setBorder(BorderFactory.createTitledBorder("Feature Description"));
        rightBox.add(descriptionScrollPane);

        // Create panel for editing parameters
        m_parameterPanel = new JPanel();
        m_parameterPanel.setBorder(BorderFactory.createTitledBorder("Feature Parameters"));
        m_parameterLabel = new JLabel("No feature is selected");
        m_parameterPanel.add(m_parameterLabel, 0);
        m_parameterButton = new JButton("Edit");
        m_parameterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                // figure out the parent to be able to make the dialog modal
                Frame f = null;
                Container c = contentPanel.getParent();
                final Component root = SwingUtilities.getRoot(c);
                if (root instanceof Frame) {
                    f = (Frame)root;
                }
                while (f == null && c != null) {
                    if (c instanceof Frame) {
                        f = (Frame)c;
                        break;
                    }
                    c = c.getParent();
                }
                FeaturesTableModel model = (FeaturesTableModel)m_featuresTable.getModel();
                FeatureType type = model.getFeatureType(m_featuresTable.getSelectedRow());

                final ParameterSettingsDialog dialog = new ParameterSettingsDialog(f, type);
                dialog.setLocationRelativeTo(c);
                dialog.pack();
                dialog.setVisible(true);
            }
        });
        rightBox.add(m_parameterPanel);

        // Create panel for aggregators (TODO)

        /* Add to DialogPane */
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        contentPanel.add(splitPane);

        mainPanel.add(audioColumnPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        addTab("Features", mainPanel);

    }

    private void initFeaturesTable() {
        m_featuresTable.setFillsViewportHeight(true);
        m_featuresTable.setPreferredScrollableViewportSize(m_featuresTable.getPreferredSize());
        m_featuresTable.setShowGrid(false);
        m_featuresTable.setTableHeader(null);
        m_featuresTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_featuresTable.getColumnModel().getColumn(0).setMinWidth(16);
        m_featuresTable.getColumnModel().getColumn(0).setMaxWidth(16);
        m_featuresTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(final ListSelectionEvent evt) {
                if(!evt.getValueIsAdjusting()){
                    FeaturesTableModel model = (FeaturesTableModel)m_featuresTable.getModel();
                    int row = m_featuresTable.getSelectedRow();
                    FeatureType type = model.getFeatureType(row);
                    updateDescriptionPane(type);
                    updateParametersPanel(type);
                }
            }
        });
    }

    private void updateDescriptionPane(final FeatureType type) {
        StringBuilder builder = new StringBuilder();
        builder.append("<h3>" + type.getName() + "</h3>");
        builder.append(type.getDescription());
        builder.append("<h4>Dependencies</h4>");
        if (type.hasDependencies()) {
            builder.append("<ul>");
            for (FeatureType dep : type.getDependencies()) {
                builder.append("<li>" + dep.getName() + "</li>");
            }
        } else {
            builder.append(type.getName() + " has no dependencies.");
        }
        builder.append("</ul>");
        m_descriptionPane.setText(builder.toString());
    }

    private void updateParametersPanel(final FeatureType type) {
        if ((!type.hasParameters()) || (type.getParameters().length < 1)) {
            m_parameterLabel.setText("This feature doesn't have any parameter to set.");
            if (m_parameterPanel.getComponentCount() == 2) {
                m_parameterPanel.remove(m_parameterButton);
            }
        } else {
            m_parameterLabel.setText("Edit parameter(s) for this feature");
            if (m_parameterPanel.getComponentCount() == 1) {
                m_parameterPanel.add(m_parameterButton, 1);
            }
        }
    }

    private class FeaturesTableModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        private static final int SELECTED_IDX = 0;

        private static final int TYPE_IDX = 1;

        private final Class<?>[] m_colClasses = new Class<?>[]{Boolean.class, String.class};

        private FeaturesTableModel() {
            super(0, 2);
            for (FeatureType type : m_settings.getAudioFeatureTypes()) {
                addRow(new Object[]{m_settings.isSelected(type), type});
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return m_colClasses[columnIndex];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCellEditable(final int row, final int column) {
            if (column == SELECTED_IDX) {
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getColumnName(final int column) {
            return "Undefined";
        }

        private FeatureType getFeatureType(final int row) {
            return (FeatureType)getValueAt(row, TYPE_IDX);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValueAt(final Object aValue, final int row, final int column) {
            super.setValueAt(aValue, row, column);
            if (column == SELECTED_IDX) {
                FeatureType type = getFeatureType(row);
                m_settings.setSelected(type, ((Boolean)aValue).booleanValue());
            }
        }
    }

    private class ParameterSettingsDialog extends JDialog {

        private static final long serialVersionUID = 1L;

        private static final int MIN_VALUE = 1;

        private static final int MAX_VALUE = 100;

        private final Map<String, SpinnerModel> m_spinnerMap;

        ParameterSettingsDialog(final Frame owner, final FeatureType type) {
            super(owner, "Parameter Settings", true);
            m_spinnerMap = new HashMap<String, SpinnerModel>();
            setLayout(new BorderLayout());
            setResizable(false);
            final JPanel centerPanel = new JPanel();
            for (String param : type.getParameters()) {
                final JLabel label = new JLabel(param);
                final SpinnerModel model = new SpinnerNumberModel(m_settings.getParameterValue(type, param).intValue(),
                    MIN_VALUE, MAX_VALUE, 1);
                m_spinnerMap.put(param, model);
                final JSpinner spinner = new JSpinner(model);
                centerPanel.add(label);
                centerPanel.add(spinner);
            }
            final JButton okButton = new JButton("Ok");
            okButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    for (Entry<String, SpinnerModel> entry : m_spinnerMap.entrySet()) {
                        m_settings.setParameterValue(type, entry.getKey(),
                            ((Integer)entry.getValue().getValue()).intValue());
                    }
                    closeDialog();
                }
            });
            final JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    closeDialog();
                }
            });

            final JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            add(centerPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

        }

        private void closeDialog() {
            setVisible(false);
            dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        m_audioColumnComponent.saveSettingsTo(settings);
        m_settings.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
            throws NotConfigurableException {
        m_audioColumnComponent.loadSettingsFrom(settings, specs);
        m_settings.loadSettingsFrom(settings);
    }

}
