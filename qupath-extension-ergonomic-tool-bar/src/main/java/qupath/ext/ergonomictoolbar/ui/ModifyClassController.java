package qupath.ext.ergonomictoolbar.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Spinner;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClass.StandardPathClasses;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionModel;
import qupath.lib.scripting.QP;


import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ModifyClassController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    private QP quPathApplication;

    @FXML
    private Spinner<Integer> threadSpinner;


    @FXML
    private ComboBox<String> class_ComboBox;

    @FXML
    private Label errorLabel;

    public static ModifyClassController createInstance() throws IOException {
        return new ModifyClassController();
    }

    public ModifyClassController() throws IOException {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setText("");

        // Récupération des classes de bases.
        PathClass tumor_Class = StandardPathClasses.TUMOR;
        PathClass stroma_Class = StandardPathClasses.STROMA;
        PathClass immune_Cells_Class = StandardPathClasses.IMMUNE_CELLS;
        PathClass necrosis_Class = StandardPathClasses.NECROSIS;
        PathClass other_Class = StandardPathClasses.OTHER;
        PathClass region_Class = StandardPathClasses.REGION;
        PathClass ignore_Class = StandardPathClasses.IGNORE;
        PathClass positive_Class = StandardPathClasses.POSITIVE;
        PathClass negative_Class = StandardPathClasses.NEGATIVE;

        // Créer une liste de noms de classes.
        List<String> class_Names = new ArrayList<>();
        class_Names.add(tumor_Class.getName());
        class_Names.add(stroma_Class.getName());
        class_Names.add(immune_Cells_Class.getName());
        class_Names.add(necrosis_Class.getName());
        class_Names.add(other_Class.getName());
        class_Names.add(region_Class.getName());
        class_Names.add(ignore_Class.getName());
        class_Names.add(positive_Class.getName());
        class_Names.add(negative_Class.getName());

        // Ajout les noms de classes à la ComboBox.
        class_ComboBox.setItems(FXCollections.observableArrayList(class_Names));
    }

    // Méthode permettant la modification de la classe d'une annotation.
    @FXML
    void valide_New_Class(ActionEvent event) {
        PathObjectHierarchy hierarchy = QP.getCurrentHierarchy();

        // On vérifie qu'une image a été ouverte.
        if(hierarchy == null) {
            errorLabel.setText("Aucun fichier n'est ouvert.");
        }
        else
        {
            PathObjectSelectionModel selectionModel = hierarchy.getSelectionModel();
            if(selectionModel != null)
            {
                PathObject object = selectionModel.getSelectedObject();

                // On vérifie qu'une annotation a été sélectionné.
                if(object == null) {
                    errorLabel.setText("Aucune annotation n'est sélectionnée.");
                }
                else
                {
                    // On vérifie qu'une classe a bien été sélectionné.
                    if(class_ComboBox.getValue() != null){
                        errorLabel.setText("");

                        // Modification de la classe de l'annotation sélectionnée
                        object.setPathClass(PathClass.fromString(class_ComboBox.getValue()));

                        // Actualisation des noms d'annotation dans QuPath
                        QP.refreshIDs();

                        // Pour fermer la fenêtre automatiquement une fois le nom de l'annotation actualisé
                        Stage stage = (Stage) class_ComboBox.getScene().getWindow();

                        stage.close();
                    }

                    else {
                        errorLabel.setText("Veuillez sélectionner une classe.");
                    }
                }
            }
        }
    }
}