package org.knime.base.node.audio.node.recognizer.microsoft;

import org.knime.base.node.audio.data.node.AudioColumnSelection;
import org.knime.base.node.audio.node.recognizer.microsoft.util.MSSpeechRecognizer;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentPasswordField;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "MSSpeechRecognizer" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author Budi Yanto, KNIME.com
 */
public class MSSpeechRecognizerNodeDialog extends DefaultNodeSettingsPane {

    private SettingsModelIntegerBounded m_maxNBestModel =
            MSSpeechRecognizerNodeModel.createMaxNBestSettingsModel();
    private SettingsModelIntegerBounded m_profanityMarkupModel =
            MSSpeechRecognizerNodeModel.createProfanityMarkupSettingsModel();

    /**
     * New pane for configuring the MSSpeechRecognizer node.
     */
    protected MSSpeechRecognizerNodeDialog() {
        createNewGroup("Select Audio Column");
        /* Create dialog component to choose the audio column to work with */
        addDialogComponent(AudioColumnSelection.createDialogComponent());
        closeCurrentGroup();

        // Create authentication group
        createNewGroup("Authentication");
        addDialogComponent(new DialogComponentPasswordField(
            MSSpeechRecognizerNodeModel.createSubscriptionKeySettingsModel(),
            "Subscription Key: ", 40));
        closeCurrentGroup();

        // Create recognition group
        createNewGroup("Recognition Parameters");
        addDialogComponent(new DialogComponentStringSelection(
            MSSpeechRecognizerNodeModel.createAudioLanguageSettingsModel(),
            "Audio Language: ",
            MSSpeechRecognizer.getSupportedLanguages()));
        addDialogComponent(new DialogComponentStringSelection(
            MSSpeechRecognizerNodeModel.createScenarioSettingsModel(),
            "Scenario: ",
            MSSpeechRecognizer.getSupportedScenarios()));
        addDialogComponent(new DialogComponentNumber(
            m_maxNBestModel,
            "MaxNBest: ", 1));
        addDialogComponent(new DialogComponentNumber(
            m_profanityMarkupModel,
            "Profanity Markup: ", 1));
        closeCurrentGroup();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        if(m_maxNBestModel.getIntValue() < 1 || m_maxNBestModel.getIntValue() > 5){
            m_maxNBestModel.setIntValue(MSSpeechRecognizer.DEFAULT_MAXNBEST);
            m_maxNBestModel.saveSettingsTo(settings);
        }

        if(m_profanityMarkupModel.getIntValue() < 0 ||
                m_profanityMarkupModel.getIntValue() > 1){
            m_profanityMarkupModel.setIntValue(
                MSSpeechRecognizer.DEFAULT_PROFANITY_MARKUP);
            m_profanityMarkupModel.saveSettingsTo(settings);
        }

        super.saveAdditionalSettingsTo(settings);
    }
}

