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
package com.wbrawner.keecrack.lib;

import com.wbrawner.keecrack.lib.view.CrackingView;
import com.wbrawner.keecrack.lib.view.FormView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class KeeCrackTest {
    private CrackingView mockCrackingView;
    private FormView mockFormView;
    private KeeCrack keeCrack;

    @Before
    public void setUp() {
        mockCrackingView = mock(CrackingView.class);
        mockFormView = mock(FormView.class);
        keeCrack = new CrackerFactory().getCracker(false);
    }

    @After
    public void tearDown() {
        Utils.rmdir(Utils.getTmpDir());
    }

    @Test
    public void abortTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456.kdbx"));
        keeCrack.setWordListFile(Utils.getWordList("valid-words.txt"));
        keeCrack.abort();
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onError(Code.ERROR_CRACKING_INTERRUPTED);
    }

    /**
     * This ensures that both views receive any errors sent
     */
    @Test
    public void sendErrorTest() {
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.setFormView(mockFormView);
        keeCrack.sendErrorCode(Code.ERROR_CRACKING_IN_PROGRESS);
        verify(mockCrackingView, times(1)).onError(Code.ERROR_CRACKING_IN_PROGRESS);
        verify(mockFormView, times(1)).onError(Code.ERROR_CRACKING_IN_PROGRESS);
    }

    /**
     * This ensures that the form view still receives the error if the cracking view is null
     */
    @Test
    public void sendErrorWithoutCrackingViewTest() {
        keeCrack.setFormView(mockFormView);
        keeCrack.sendErrorCode(Code.ERROR_CRACKING_IN_PROGRESS);
        verify(mockFormView, times(1)).onError(Code.ERROR_CRACKING_IN_PROGRESS);
    }

    /**
     * This ensures that the cracking view still receives the error if the form view is null
     */
    @Test
    public void sendErrorWithoutFormViewTest() {
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.sendErrorCode(Code.ERROR_CRACKING_IN_PROGRESS);
        verify(mockCrackingView, times(1)).onError(Code.ERROR_CRACKING_IN_PROGRESS);
    }

    @Test
    public void errorWithoutDatabaseFileTest() throws IOException {
        keeCrack.setWordListFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onError(Code.ERROR_INVALID_DATABASE_FILE);
    }

    @Test
    public void errorWithoutWordlistFileTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456.kdbx"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onError(Code.ERROR_INVALID_WORD_LIST);
    }


    @Test
    public void guessCorrectPasswordFromFileTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456.kdbx"));
        keeCrack.setWordListFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onPasswordGuess("123456");
        verify(mockCrackingView, times(1)).onResult(eq("123456"), eq(1), any(Duration.class));
    }

    @Test
    public void guessCorrectPasswordFromFileAndKeyTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456-key.kdbx"));
        keeCrack.setKeyFile(Utils.getKeyFile());
        keeCrack.setWordListFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onPasswordGuess("123456");
        verify(mockCrackingView, times(1)).onResult(eq("123456"), eq(1), any(Duration.class));
    }


    @Test
    public void guessCorrectPasswordFromPatternTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("0000.kdbx"));
        keeCrack.setWordListPattern("[0-9]{4}");
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq("0000"), eq(1), any(Duration.class));
    }

    @Test
    public void guessCorrectPasswordFromPatternAndKeyTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("0000-key.kdbx"));
        keeCrack.setWordListPattern("[0-9]{4}");
        keeCrack.setKeyFile(Utils.getKeyFile());
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq("0000"), eq(1), any(Duration.class));
    }

    @Test
    public void keepGuessingUntilCorrectPasswordFromFileTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("redwings.kdbx"));
        keeCrack.setWordListFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(2)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq("redwings"), eq(2), any(Duration.class));
    }

    @Test
    public void keepGuessingUntilCorrectPasswordFromFileAndKeyTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("redwings-key.kdbx"));
        keeCrack.setKeyFile(Utils.getKeyFile());
        keeCrack.setWordListFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(2)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq("redwings"), eq(2), any(Duration.class));
    }

    @Test
    public void keepGuessingUntilCorrectPasswordFromPatternTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("ab.kdbx"));
        keeCrack.setWordListPattern("[a-z]{2}");
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(2)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq("ab"), eq(2), any(Duration.class));
    }

    @Test
    public void keepGuessingUntilCorrectPasswordFromPatternAndKeyTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("ab-key.kdbx"));
        keeCrack.setWordListPattern("[a-z]{2}");
        keeCrack.setKeyFile(Utils.getKeyFile());
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(2)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq("ab"), eq(2), any(Duration.class));
    }

    @Test
    public void failToCrackPasswordTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456.kdbx"));
        keeCrack.setWordListFile(Utils.getWordList("invalid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(3)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq(null), eq(3), any(Duration.class));
    }

    @Test
    public void failToCrackInvalidPasswordAndValidKeyTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456-key.kdbx"));
        keeCrack.setKeyFile(Utils.getKeyFile());
        keeCrack.setWordListFile(Utils.getWordList("invalid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(3)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq(null), eq(3), any(Duration.class));
    }

    @Test
    public void failToCrackValidPasswordAndInvalidKeyTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456-key.kdbx"));
        keeCrack.setKeyFile(Utils.getInvalidKeyFile());
        keeCrack.setWordListFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(3)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq(null), eq(3), any(Duration.class));
    }
}