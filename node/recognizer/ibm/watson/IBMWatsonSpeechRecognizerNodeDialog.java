package org.knime.base.node.audio.node.recognizer.ibm.watson;

import org.knime.base.node.audio.data.node.AudioColumnSelection;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentPasswordField;
import org.knime.core.node.defaultnodesettings.DialogComponentString;

/**
 * <code>NodeDialog</code> for the "IBMWatsonSpeechRecognizer" Node.
 *
 * @author Budi Yanto, KNIME.com
 */
public class IBMWatsonSpeechRecognizerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the IBMWatsonSpeechRecognizer node.
     */
    protected IBMWatsonSpeechRecognizerNodeDialog() {
        createNewGroup("Select Audio Column");
        /* Create dialog component to choose the audio column to extract the features from */
        addDialogComponent(AudioColumnSelection.createDialogComponent());
        closeCurrentGroup();

        // Create authentication group
        createNewGroup("Authentication");
        addDialogComponent(new DialogComponentString(
            IBMWatsonSpeechRecognizerNodeModel.createUserNameSettingsModel(),
            "User Name: ", false, 40));
        addDialogComponent(new DialogComponentPasswordField(
            IBMWatsonSpeechRecognizerNodeModel.createPasswordSettingsModel(),
            "Password: ", 40));
        closeCurrentGroup();
    }
}

