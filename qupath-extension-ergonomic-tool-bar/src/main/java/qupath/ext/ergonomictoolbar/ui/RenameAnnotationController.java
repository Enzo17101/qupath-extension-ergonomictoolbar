package qupath.ext.ergonomictoolbar.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionModel;
import qupath.lib.scripting.QP;

import java.awt.*;
import java.io.IOException;

public class RenameAnnotationController extends AnchorPane{

    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    @FXML
    private Spinner<Integer> threadSpinner;

    @FXML
    private TextField nameTextField;

    @FXML
    private Label errorLabel;

    public void initialize()
    {
        //Permet de valider en cliquant sur la touche "entrée"
        nameTextField.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER)
            {
                validateNewName();
            }
        });
    }

    @FXML
    void validateNewName() {
        try
        {
            QP quPathApplication;

            PathObjectHierarchy hierarchy = QP.getCurrentHierarchy();

            if(hierarchy == null)
            {
                errorLabel.setText("No file are open.");
            }
            else
            {
                PathObjectSelectionModel selectionModel = hierarchy.getSelectionModel();

                if(selectionModel != null)
                {
                    PathObject object = selectionModel.getSelectedObject();

                    String newName = nameTextField.getText();

                    if(object == null) {
                        errorLabel.setText("No annotations are selected");
                    }
                    else if(newName.isEmpty())
                    {
                        errorLabel.setText("Invalid name.");
                    }
                    else
                    {
                        errorLabel.setText("");

                        //Modification du nom de l'annotation sélectionnée
                        object.setName(newName);

                        //Actualisation des noms d'annotation dans QuPath
                        QP.refreshIDs();

                        //Pour fermer la fenêtre automatiquement une fois le nom de l'annotation actualisé
                        //Stage stage = (Stage) nameTextField.getScene().getWindow();
                        Stage stage = InterfaceController.getSharedRenameAnnotationStage();
                        stage.close();

                        nameTextField.setText("");
                    }
                }
            }
        }
        catch (Exception e)
        {
            Dialogs.showErrorMessage("Extension Error", "Cannot rename annotation");
            logger.error("Cannot rename annotation", e);
        }
    }
}
