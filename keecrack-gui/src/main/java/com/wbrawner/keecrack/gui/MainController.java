/*
 * Copyright 2018 William Brawner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wbrawner.keecrack.gui;

import com.wbrawner.keecrack.lib.Code;
import com.wbrawner.keecrack.lib.KeeCrack;
import com.wbrawner.keecrack.lib.view.FormView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, FormView {

    private static KeeCrack keeCrack;
    @FXML
    private TextField database;
    @FXML
    private TextField key;
    @FXML
    private TextField wordlist;
    @FXML
    private Button crackButton;
    @FXML
    private ToggleGroup wordlistType;
    @FXML
    private RadioButton wordlistFile;
    @FXML
    private RadioButton wordlistPattern;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        keeCrack = KeeCrack.getInstance();
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

        updateWordListHandler();

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
                stage.setScene(new Scene(root, 400, 350));
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        wordlistType.selectedToggleProperty().addListener((observableValue, oldToggle, newToggle) ->
                updateWordListHandler());

        if (keeCrack.getDatabaseFile() != null)
            onDatabaseFileSet(keeCrack.getDatabaseFile().getName());
        if (keeCrack.getKeyFile() != null)
            onKeyFileSet(keeCrack.getKeyFile().getName());
        if (keeCrack.getWordListName() != null)
            onWordListSet(keeCrack.getWordListName());
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

    private void updateWordListHandler() {
        wordlist.clear();
        if (wordlistFile.isSelected()) {
            wordlist.setEditable(false);
            wordlist.setCursor(Cursor.HAND);
            wordlist.setOnMouseClicked(event -> {
                if (KeeCrack.getInstance().isCracking()) {
                    return;
                }
                File wordlistFile = getFile("Text Files", "txt");
                keeCrack.setWordListFile(wordlistFile);
            });
        } else if (wordlistPattern.isSelected()) {
            wordlist.setEditable(true);
            wordlist.setCursor(Cursor.TEXT);
            wordlist.setOnMouseExited(mouseEvent -> keeCrack.setWordListPattern(wordlist.getText()));
            wordlist.setOnMouseClicked(null);
        }
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
    public void onWordListSet(String name) {
        wordlist.setText(name);
    }

    @Override
    public void onError(Code code) {

    }
}
