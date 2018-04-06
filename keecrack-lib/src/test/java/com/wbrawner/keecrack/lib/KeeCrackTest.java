package com.wbrawner.keecrack.lib;

import com.wbrawner.keecrack.lib.view.CrackingView;
import com.wbrawner.keecrack.lib.view.FormView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
        keeCrack = KeeCrack.getInstance();
    }

    @After
    public void tearDown() {
        keeCrack.reset();
        Utils.rmdir(Utils.getTmpDir());
    }

    @Test
    public void resetTest() {
        keeCrack.setDatabaseFile(new File("Database"));
        keeCrack.setKeyFile(new File("Keyfile"));
        keeCrack.setWordlistFile(new File("WordList"));
        assertNotNull(keeCrack.getDatabaseFile());
        assertNotNull(keeCrack.getKeyFile());
        assertNotNull(keeCrack.getWordlistFile());
        keeCrack.reset();
        assertNull(keeCrack.getDatabaseFile());
        assertNull(keeCrack.getKeyFile());
        assertNull(keeCrack.getWordlistFile());
    }

    @Test
    public void abortTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456.kdbx"));
        keeCrack.setWordlistFile(Utils.getWordList("valid-words.txt"));
        keeCrack.abort();
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onError(Code.ERROR_CRACKING_INTERRUPTED);
    }

    @Test
    public void sendErrorTest() {
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.setFormView(mockFormView);
        keeCrack.sendErrorCode(Code.ERROR_CRACKING_IN_PROGRESS);
        verify(mockCrackingView, times(1)).onError(Code.ERROR_CRACKING_IN_PROGRESS);
        verify(mockFormView, times(1)).onError(Code.ERROR_CRACKING_IN_PROGRESS);
    }

    @Test
    public void sendErrorWithoutCrackingViewTest() {
        keeCrack.setFormView(mockFormView);
        keeCrack.sendErrorCode(Code.ERROR_CRACKING_IN_PROGRESS);
        verify(mockFormView, times(1)).onError(Code.ERROR_CRACKING_IN_PROGRESS);
    }

    @Test
    public void sendErrorWithoutFormViewTest() {
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.sendErrorCode(Code.ERROR_CRACKING_IN_PROGRESS);
        verify(mockCrackingView, times(1)).onError(Code.ERROR_CRACKING_IN_PROGRESS);
    }

    @Test
    public void errorWithoutDatabaseFileTest() throws IOException {
        keeCrack.setWordlistFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onError(Code.ERROR_MISSING_DATABASE_FILE);
    }

    @Test
    public void errorWithoutWordlistFileTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456.kdbx"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onError(Code.ERROR_MISSING_WORD_LIST_FILE);
    }


    @Test
    public void guessCorrectPasswordTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456.kdbx"));
        keeCrack.setWordlistFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onPasswordGuess("123456");
        verify(mockCrackingView, times(1)).onResult(eq("123456"), eq(1), any(Duration.class));
    }

    @Test
    public void guessCorrectPasswordAndKeyTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456-key.kdbx"));
        keeCrack.setKeyFile(Utils.getKeyFile());
        keeCrack.setWordlistFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(1)).onPasswordGuess("123456");
        verify(mockCrackingView, times(1)).onResult(eq("123456"), eq(1), any(Duration.class));
    }

    @Test
    public void keepGuessingUntilCorrectPasswordTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("redwings.kdbx"));
        keeCrack.setWordlistFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(2)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq("redwings"), eq(2), any(Duration.class));
    }

    @Test
    public void keepGuessingUntilCorrectPasswordAndKeyTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("redwings-key.kdbx"));
        keeCrack.setKeyFile(Utils.getKeyFile());
        keeCrack.setWordlistFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(2)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq("redwings"), eq(2), any(Duration.class));
    }

    @Test
    public void failToCrackPasswordTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456.kdbx"));
        keeCrack.setWordlistFile(Utils.getWordList("invalid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(3)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq(null), eq(3), any(Duration.class));
    }

    @Test
    public void failToCrackInvalidPasswordAndValidKeyTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456-key.kdbx"));
        keeCrack.setKeyFile(Utils.getKeyFile());
        keeCrack.setWordlistFile(Utils.getWordList("invalid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(3)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq(null), eq(3), any(Duration.class));
    }

    @Test
    public void failToCrackValidPasswordAndInvalidKeyTest() throws IOException {
        keeCrack.setDatabaseFile(Utils.getDatabase("123456-key.kdbx"));
        keeCrack.setKeyFile(Utils.getInvalidKeyFile());
        keeCrack.setWordlistFile(Utils.getWordList("valid-words.txt"));
        keeCrack.setCrackingView(mockCrackingView);
        keeCrack.attack();
        verify(mockCrackingView, times(3)).onPasswordGuess(anyString());
        verify(mockCrackingView, times(1)).onResult(eq(null), eq(3), any(Duration.class));
    }
}