package com.wbrawner.keecrack.gui;

import com.wbrawner.keecrack.lib.Code;
import com.wbrawner.keecrack.lib.KeeCrack;
import com.wbrawner.keecrack.lib.view.CrackingView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

import java.net.URL;
import java.time.Duration;
import java.util.Locale;
import java.util.ResourceBundle;

public class CrackingController implements Initializable, CrackingView {
    @FXML
    private Label passwordLabel;
    @FXML
    private Label password;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private Label timeElapsed;

    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final KeeCrack keeCrack = KeeCrack.getInstance();
        keeCrack.setCrackingView(this);
        new Thread(keeCrack::attack).start();
    }

    @Override
    public void onPasswordGuess(String password) {
        Platform.runLater(() -> setPassword(password));
    }

    @Override
    public void onResult(final String password, final int guessCount, final Duration timeElapsed) {
        Platform.runLater(() -> {
            progress.setVisible(false);
            setTimeElapsed(timeElapsed);
            String message;
            String title;
            if (password != null) {
                message = String.format(
                        Locale.US,
                        "Cracked in %d attempts.",
                        guessCount
                );
                title = "Keecrack - Complete";
            } else {
                message = String.format(
                        Locale.US,
                        "Failed to crack password in %d attempts.",
                        guessCount
                );
                title = "Keecrack - Failed";
            }
            setPasswordLabel(message);
            setPassword(password);
            stage.setTitle(title);
        });
    }

    @Override
    public void onError(Code code) {

    }

    private void onClose() {
        KeeCrack keeCrack = KeeCrack.getInstance();
        keeCrack.abort();
        keeCrack.setCrackingView(null);
    }

    private void setPasswordLabel(String text) {
        passwordLabel.setText(text);
    }

    private void setPassword(String text) {
        password.setText(text);
    }

    private void setTimeElapsed(Duration timeElapsed) {
        this.timeElapsed.setText(timeElapsed.toString().toLowerCase().substring(2));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnHidden(event -> onClose());
    }
}
