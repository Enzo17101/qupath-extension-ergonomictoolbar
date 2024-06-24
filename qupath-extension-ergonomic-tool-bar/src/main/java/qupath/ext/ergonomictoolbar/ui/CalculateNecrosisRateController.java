package qupath.ext.ergonomictoolbar.ui;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import javafx.scene.layout.AnchorPane;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClass.StandardPathClasses;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionModel;
import qupath.lib.scripting.QP;


import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class CalculateNecrosisRateController {

    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    private QP quPathApplication;

    @FXML
    private Label label_Necrosis_Rate;

    @FXML
    private TextField text_Distance;

    @FXML
    private CheckBox check_Graph_Display;

    public static CalculateNecrosisRateController createInstance() throws IOException {
        return new CalculateNecrosisRateController();
    }

    public CalculateNecrosisRateController() throws IOException {
    }

    /**
     * Method that allows to know if the str is a double > to 0
     * @param str
     * @return
     */
    public boolean is_Dist_Ok(String str){
        try {
            double number = Double.parseDouble(str);
            if (number > 0) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Method that allows to get the gravity centers of cells with a groovy script
     * @param groovyFilePath
     * @return Gravity centers cells
     */
    public String get_Gravity_Centers() {

        String groovyFilePath = "qupath-extension-ergonomic-tool-bar/src/main/java/qupath/ext/ergonomictoolbar/groovy/GetGravityCenters.groovy";

        // Redirect standard output to capture script's print statements
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        // Create a binding for Groovy script variables
        Binding binding = new Binding();
        binding.setVariable("out", printWriter);

        // Create a GroovyShell instance with the binding
        GroovyShell shell = new GroovyShell(binding);

        try {
            // Parse and run the Groovy script
            Script script = shell.parse(new File(groovyFilePath));
            script.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the captured output as a String
        return stringWriter.toString();
    }

    /**
     * Method that allows to get the rate necrosis from a python file and to display it with a graph or not
     * @param gravity_Centers_Cells
     * @param display_Graph
     */
    public void get_And_Display_Rate_Necrosis(String gravity_Centers_Cells, String display_Graph) {
        try {
            // Command to execute the Python script with arguments
            String[] command = {
                    "python",
                    "qupath-extension-ergonomic-tool-bar/src/main/java/qupath/ext/ergonomictoolbar/python/CalculateNecrosisRate.py",
                    gravity_Centers_Cells,
                    text_Distance.getText(),
                    display_Graph
            };

            // Create a ProcessBuilder instance
            ProcessBuilder pb = new ProcessBuilder(command);

            // Redirect error stream to the standard output stream
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Read the process output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            label_Necrosis_Rate.setText("Results :");

            // Append each line of the process output to the label
            while ((line = reader.readLine()) != null) {
                label_Necrosis_Rate.setText(label_Necrosis_Rate.getText() + "\n" + line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that allows to display the necrosis rate
     */
    public void display_Rate_Necrosis() {

        String display_Graph = "0";

        if(is_Dist_Ok(text_Distance.getText())){
            String gravity_Centers_Cells = get_Gravity_Centers();

            if(check_Graph_Display.isSelected()){
                display_Graph = "1";
            }

            get_And_Display_Rate_Necrosis(gravity_Centers_Cells, display_Graph);

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Distance error.");
            alert.setHeaderText(null);
            alert.setContentText("Please fill a distance with a float superior to 0.");

            Stage currentStage = (Stage) check_Graph_Display.getScene().getWindow();
            alert.initOwner(currentStage);
            alert.showAndWait();
        }
    }
}