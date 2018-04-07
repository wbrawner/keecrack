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
import com.wbrawner.keecrack.lib.wordlist.WordList;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.stream_3_1.KdbxHeader;
import org.linguafranca.pwdb.kdbx.stream_3_1.KdbxSerializer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The main class responsible for handling the brute forcing of the KeePass database. You do not contruct the
 * KeeCrack instance directly, but rather call {@link #getInstance()}. To begin, you should set the form view and
 * cracking view with {@link #setFormView(FormView)} and {@link #setCrackingView(CrackingView)} respectively. These
 * views will be responsible for displaying information like error messages and status updates. The cracking will
 * work without these, though it's highly recommended to set them prior to beginning. The database and wordlist must
 * be set, while the key file is also optional. If either of the required parameters are missing, the
 * {@link #attack()} operation will abort, sending either {@link Code#ERROR_INVALID_DATABASE_FILE} or
 * {@link Code#ERROR_INVALID_WORD_LIST}, respectively. The word list can either be a pattern, in which case
 * incremental guessing will take place, or a file, in which case each line of the file will be considered a password
 * to guess. Use {@link #setWordListPattern(String)} or {@link #setWordListFile(File)} respectively to achieve the
 * desired guess strategy. If you need to interrupt the attack for any reason, you can call {@link #abort} and the
 * cracking will stop on the next guess, sending {@link Code#ERROR_CRACKING_INTERRUPTED} to the views, provided they
 * are set. For each guess, {@link CrackingView#onPasswordGuess(String)} is called, in case you'd like to do
 * something with the passwords that have already been attempted. Upon a successful password guess, the
 * {@link CrackingView#onResult(String, int, Duration)} method will be called with the first parameter as the correct
 * password. If the password cannot be guessed with the words provided, then the same method will be called but the
 * first parameter will be null.
 */
public class KeeCrack {
    private static final AtomicReference<KeeCrack> singleton = new AtomicReference<>(null);
    private final Object keyFileLock = new Object();
    private final AtomicBoolean isCracking = new AtomicBoolean(false);
    /**
     * This is used to abort cracking
     */
    private final AtomicBoolean abort = new AtomicBoolean(false);
    private final AtomicReference<FormView> formView = new AtomicReference<>(null);
    private final AtomicReference<CrackingView> crackingView = new AtomicReference<>(null);
    private File databaseFile;
    private File keyFile;
    private byte[] databaseBytes;
    private byte[] keyBytes;
    private WordList wordList;
    private int guessCount = 0;

    private KeeCrack() {
    }

    public static KeeCrack getInstance() {
        if (singleton.get() == null) {
            singleton.set(new KeeCrack());
        }

        return singleton.get();
    }

    /**
     * Call this to reset the state of the KeeCrack instance. Note that you will need to set the views again after
     * calling this
     */
    public void reset() {
        setDatabaseFile(null);
        setKeyFile(null);
        setWordListFile(null);
        setCrackingView(null);
        setFormView(null);
    }

    public void abort() {
        abort.set(true);
    }

    /**
     * Call this to begin brute-forcing the database file.
     */
    public void attack() {
        if (databaseFile == null || !databaseFile.exists() || !databaseFile.canRead()) {
            sendErrorCode(Code.ERROR_INVALID_DATABASE_FILE);
            return;
        }

        if (wordList == null) {
            sendErrorCode(Code.ERROR_INVALID_WORD_LIST);
            return;
        }

        if (isCracking.get()) {
            sendErrorCode(Code.ERROR_CRACKING_IN_PROGRESS);
            return;
        }
        isCracking.set(true);

        guessCount = 0;
        Instant startTime = Instant.now();

        String line = null;
        boolean haveCorrectPassword = false;
        prepareByteArrays();
        while (!haveCorrectPassword && wordList.hasNext()) {
            if (abort.get()) {
                sendErrorCode(Code.ERROR_CRACKING_INTERRUPTED);
                isCracking.set(false);
                abort.set(false);
                return;
            }
            line = wordList.nextWord();
            try {
                //noinspection ConstantConditions
                crackingView.get().onPasswordGuess(line);
            } catch (NullPointerException ignored) {
            }
            haveCorrectPassword = guessPassword(line);
        }

        Duration duration = Duration.between(startTime, Instant.now());
        String password = null;
        if (haveCorrectPassword) {
            password = line;
        }
        try {
            //noinspection ConstantConditions
            crackingView.get().onResult(password, guessCount, duration);
        } catch (NullPointerException ignored) {
        }

        isCracking.set(false);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void prepareByteArrays() {
        databaseBytes = Utils.getFileBytes(databaseFile);
        synchronized (keyFileLock) {
            if (keyFile == null) {
                return;
            }
            keyBytes = Utils.getFileBytes(keyFile);
        }
    }

    private boolean guessPassword(String password) {
        guessCount++;
        InputStream databaseInput = null;
        InputStream keyFileInput = null;
        try {
            databaseInput = new ByteArrayInputStream(databaseBytes);
            KdbxHeader databaseHeader = new KdbxHeader();
            KdbxCreds credentials;
            if (keyBytes == null || keyBytes.length == 0) {
                credentials = new KdbxCreds(password.getBytes());
            } else {
                keyFileInput = new ByteArrayInputStream(keyBytes);
                credentials = new KdbxCreds(password.getBytes(), keyFileInput);
            }
            KdbxSerializer.createUnencryptedInputStream(credentials, databaseHeader, databaseInput);
            return true;
        } catch (IllegalStateException ignored) {
            // This happens when an incorrect guess occurs. Expected behavior, so we ignore it
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.closeQuietly(databaseInput);
            Utils.closeQuietly(keyFileInput);
        }
        return false;
    }


    void sendErrorCode(Code code) {
        try {
            Objects.requireNonNull(crackingView.get()).onError(code);
        } catch (NullPointerException ignored) {
        }
        try {
            Objects.requireNonNull(formView.get()).onError(code);
        } catch (NullPointerException ignored) {
        }
    }


    public File getDatabaseFile() {
        return databaseFile;
    }

    public void setDatabaseFile(File databaseFile) {
        this.databaseFile = databaseFile;
        try {
            String response = (databaseFile == null) ? null : databaseFile.getName();
            //noinspection ConstantConditions
            formView.get().onDatabaseFileSet(response);
        } catch (NullPointerException ignored) {
        }
    }

    public File getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(File keyFile) {
        synchronized (keyFileLock) {
            this.keyFile = keyFile;
        }
        try {
            String response = (keyFile == null) ? null : keyFile.getName();
            //noinspection ConstantConditions
            formView.get().onKeyFileSet(response);
        } catch (NullPointerException ignored) {
        }
    }

    public void setWordListFile(File wordlistFile) {
        String response = null;
        if (wordlistFile == null) {
            this.wordList = null;
        } else {
            if (!wordlistFile.exists() || !wordlistFile.canRead()) {
                try {
                    //noinspection ConstantConditions
                    crackingView.get().onError(Code.ERROR_INVALID_WORD_LIST);
                } catch (NullPointerException ignored) {
                }
                this.wordList = null;
                return;
            }
            this.wordList = new WordList(wordlistFile);
            response = wordlistFile.getName();
        }
        try {
            //noinspection ConstantConditions
            formView.get().onWordListSet(response);
        } catch (NullPointerException ignored) {
        }
    }

    public void setWordListPattern(String pattern) {
        if (pattern == null) {
            this.wordList = null;
        } else {
            try {
                this.wordList = new WordList(pattern);
            } catch (IllegalArgumentException ignored) {
                // This can be thrown if the user has entered an invalid regular expression
            }
        }
        try {
            //noinspection ConstantConditions
            formView.get().onWordListSet(pattern);
        } catch (NullPointerException ignored) {
        }
    }

    WordList getWordList() {
        return this.wordList;
    }

    public String getWordListName() {
        if (this.wordList == null)
            return null;
        return this.wordList.getName();
    }

    public void setFormView(FormView formView) {
        this.formView.set(formView);
    }

    public void setCrackingView(CrackingView crackingView) {
        this.crackingView.set(crackingView);
    }

    public boolean isCracking() {
        return isCracking.get();
    }
}
