package com.wbrawner.keecrack.cli;

import com.wbrawner.keecrack.lib.Code;
import com.wbrawner.keecrack.lib.KeeCrack;
import com.wbrawner.keecrack.lib.view.CrackingView;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.time.Duration;
import java.util.Locale;

public class Main {
    private static boolean isVerbose = false;

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("KeeCrack")
                .build()
                .description("Brute force KeePass database files");
        parser.addArgument("--verbose", "-v")
                .help("Increase logging output")
                .action(Arguments.storeTrue())
                .dest("verbose");
        parser.addArgument("--gui")
                .action(Arguments.storeTrue())
                .help("launch the graphical interface (ignores other options)")
                .dest("gui");
        parser.addArgument("--word-list", "-w")
                .help("a file containing newline-separated words to use as the passwords")
                .dest("wordlist")
                .metavar("WORD-LIST-FILE");
        parser.addArgument("--key-file", "-k")
                .help("the key file to use with the database")
                .dest("keyfile")
                .metavar("KEY-FILE");

        parser.addArgument("DATABASE")
                .dest("database")
                .help("the database file to brute force");

        try {
            Namespace res = parser.parseArgs(args);
            isVerbose = res.getBoolean("verbose");
            KeeCrack keeCrack = KeeCrack.getInstance();
            keeCrack.setCrackingView(new CLICrackingView());

            String databasePath = res.getString("database");
            if (databasePath != null) {
                keeCrack.setDatabaseFile(new File(databasePath));
            }

            String keyfilePath = res.getString("keyfile");
            if (keyfilePath != null) {
                keeCrack.setKeyFile(new File(keyfilePath));
            }

            String wordlistPath = res.getString("wordlist");
            if (wordlistPath != null) ;
            keeCrack.setWordlistFile(new File(wordlistPath));

            if (res.getBoolean("gui")) {
                com.wbrawner.keecrack.gui.Main.main(new String[]{});
            } else {
                keeCrack.attack();
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }

    static class CLICrackingView implements CrackingView {
        @Override
        public void onPasswordGuess(String password) {
            if (isVerbose)
                System.out.println("Guessing password: " + password);
        }

        @Override
        public void onResult(String password, int guessCount, Duration timeElapsed) {
            String message;
            if (password == null)
                message = String.format(
                        Locale.US,
                        "Unable to guess password after %d attempts in %s",
                        guessCount,
                        timeElapsed.toString().toLowerCase().substring(2)
                );
            else
                message = String.format(
                        "Successfully guessed password after %d attempts in %s: %s",
                        guessCount,
                        timeElapsed.toString().toLowerCase().substring(2),
                        password
                );
            System.out.println(message);
        }

        @Override
        public void onError(Code code) {
            String message = "";
            switch (code) {
                case ERROR_MISSING_DATABASE_FILE:
                    message = "Please specify a database file that you have read access to";
                    break;
                case ERROR_MISSING_WORD_LIST_FILE:
                    message = "Please specify a word list file that you have read access to";
                    break;
                case ERROR_CRACKING_INTERRUPTED:
                    message = "Aborted.";
                    break;
                case ERROR_FILE_READ:
                    message = "An error occurred while trying to read one of the files";
                    break;
            }
            System.err.println(message);
            System.exit(code.ordinal());
        }
    }
}
