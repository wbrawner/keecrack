package com.wbrawner.keecrack.gui;

import com.wbrawner.keecrack.lib.Code;
import com.wbrawner.keecrack.lib.KeeCrack;
import com.wbrawner.keecrack.lib.view.FormView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, FormView {

    @FXML private TextField database;
    @FXML private TextField key;
    @FXML private TextField wordlist;
    @FXML private Button crackButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        KeeCrack keeCrack = KeeCrack.getInstance();
        keeCrack.setFormView(this);
        database.setOnMouseClicked(event -> {
            if (KeeCrack.getInstance().isCracking()) {
                return;
            }

            File databaseFile = getFile("KeePass Database Files", "kdbx");
            keeCrack.setDatabaseFile(databaseFile);
        });

        key.setOnMouseClicked(event -> {
            if (KeeCrack.getInstance().isCracking()) {
                return;
            }

            File keyFile = getFile("KeePass Database Key Files", "*");
            keeCrack.setKeyFile(keyFile);
        });

        wordlist.setOnMouseClicked(event -> {
            if (KeeCrack.getInstance().isCracking()) {
                return;
            }

            File wordlistFile = getFile("Text Files", "txt");
            keeCrack.setWordlistFile(wordlistFile);
        });

        crackButton.setOnMouseClicked(event -> {
            try {
                if (KeeCrack.getInstance().isCracking()) {
                    return;
                }
                Stage stage = new Stage();
                stage.setTitle("Keecrack - Cracking...");
                stage.initModality(Modality.WINDOW_MODAL);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cracking.fxml"));
                loader.setControllerFactory(param -> {
                    try {
                        Object controller = param.newInstance();
                        if (controller instanceof CrackingController) {
                            ((CrackingController) controller).setStage(stage);
                        }
                        return controller;
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
                Parent root = loader.load();
                stage.setScene(new Scene(root, 200, 200));
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (keeCrack.getDatabaseFile() != null)
            onDatabaseFileSet(keeCrack.getDatabaseFile().getName());
        if (keeCrack.getKeyFile() != null)
            onKeyFileSet(keeCrack.getKeyFile().getName());
        if (keeCrack.getWordlistFile() != null)
            onWordListFileSet(keeCrack.getWordlistFile().getName());
    }


    private File getFile(String description, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Keecrack - Select a file");
        fileChooser.setSelectedExtensionFilter(
                new FileChooser.ExtensionFilter(description, extensions)
        );
        Stage stage = new Stage();
        stage.setOnHidden(e -> KeeCrack.getInstance().setFormView(null));
        return fileChooser.showOpenDialog(stage);
    }

    @Override
    public void onDatabaseFileSet(String name) {
        database.setText(name);
    }

    @Override
    public void onKeyFileSet(String name) {
        key.setText(name);
    }

    @Override
    public void onWordListFileSet(String name) {
        wordlist.setText(name);
    }

    @Override
    public void onError(Code code) {

    }
}
