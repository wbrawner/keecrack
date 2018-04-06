package com.wbrawner.keecrack.lib;

import com.wbrawner.keecrack.lib.view.CrackingView;
import com.wbrawner.keecrack.lib.view.FormView;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.stream_3_1.KdbxHeader;
import org.linguafranca.pwdb.kdbx.stream_3_1.KdbxSerializer;

import java.io.*;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class KeeCrack {
    private static final AtomicReference<KeeCrack> singleton = new AtomicReference<>(null);
    private final Object keyFileLock = new Object();
    private WeakReference<FormView> formView = new WeakReference<>(null);
    private WeakReference<CrackingView> crackingView = new WeakReference<>(null);
    private final AtomicBoolean isCracking = new AtomicBoolean(false);
    private File databaseFile;
    private File keyFile;
    private File wordlistFile;

    /**
     * This is used to abort cracking
     */
    private final AtomicBoolean abort = new AtomicBoolean(false);

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
        setWordlistFile(null);
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
            sendErrorCode(Code.ERROR_MISSING_DATABASE_FILE);
            return;
        }

        if (wordlistFile == null || !wordlistFile.exists() || !wordlistFile.canRead()) {
            sendErrorCode(Code.ERROR_MISSING_WORD_LIST_FILE);
            return;
        }

        if (isCracking.get()) {
            sendErrorCode(Code.ERROR_CRACKING_IN_PROGRESS);
            return;
        }
        isCracking.set(true);

        guessCount = 0;
        Instant startTime = Instant.now();

        try (BufferedReader wordReader = new BufferedReader(new FileReader(wordlistFile))) {
            String line = null;
            boolean haveCorrectPassword = false;
            while (!haveCorrectPassword && (line = wordReader.readLine()) != null) {
                if (abort.get()) {
                    sendErrorCode(Code.ERROR_CRACKING_INTERRUPTED);
                    abort.set(false);
                    return;
                }
                CrackingView view = crackingView.get();
                if (view != null)
                    view.onPasswordGuess(line);
                haveCorrectPassword = guessPassword(line);
            }

            CrackingView view = crackingView.get();
            if (view != null) {
                Duration duration = Duration.between(startTime, Instant.now());
                String password = null;
                if (haveCorrectPassword) {
                    password = line;
                }
                view.onResult(password, guessCount, duration);
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendErrorCode(Code.ERROR_FILE_READ);
        } finally {
            isCracking.set(false);
        }
    }

    private boolean guessPassword(String password) {
        guessCount++;
        try (InputStream inputStream = new FileInputStream(databaseFile)) {
            KdbxHeader databaseHeader = new KdbxHeader();
            KdbxCreds credentials;
            synchronized (keyFileLock) {
                if (keyFile == null) {
                    credentials = new KdbxCreds(password.getBytes());
                } else {
                    credentials = new KdbxCreds(password.getBytes(), new FileInputStream(keyFile));
                }
            }
            KdbxSerializer.createUnencryptedInputStream(credentials, databaseHeader, inputStream);
            return true;
        } catch (IllegalStateException ignored) {
            // This happens when an incorrect guess occurs. Expected behavior, so we ignore it
        } catch (Exception e) {
            e.printStackTrace();
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

    public File getWordlistFile() {
        return wordlistFile;
    }

    public void setWordlistFile(File wordlistFile) {
        this.wordlistFile = wordlistFile;
        try {
            String response = (wordlistFile == null) ? null : wordlistFile.getName();
            //noinspection ConstantConditions
            formView.get().onWordListFileSet(response);
        } catch (NullPointerException ignored) {
        }
    }

    public void setFormView(FormView formView) {
        this.formView = new WeakReference<>(formView);
    }

    public void setCrackingView(CrackingView crackingView) {
        this.crackingView = new WeakReference<>(crackingView);
    }

    public boolean isCracking() {
        return isCracking.get();
    }
}
