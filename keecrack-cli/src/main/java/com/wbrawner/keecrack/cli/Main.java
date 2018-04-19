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
package com.wbrawner.keecrack.cli;

import com.wbrawner.keecrack.lib.Code;
import com.wbrawner.keecrack.lib.CrackerFactory;
import com.wbrawner.keecrack.lib.KeeCrack;
import com.wbrawner.keecrack.lib.view.CrackingView;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;

public class Main {
    private static final String LOG_SEPARATOR = " - ";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
    private static boolean isVerbose = false;
    private static boolean isIncremental = false;

    public static void main(String[] args) {
        if (args.length == 0) {
            com.wbrawner.keecrack.gui.Main.main(new String[]{});
            return;
        }
        ArgumentParser parser = ArgumentParsers.newFor("KeeCrack")
                .build()
                .description("Brute force KeePass database files");
        parser.addArgument("--verbose", "-v")
                .help("Increase logging output")
                .action(Arguments.storeTrue())
                .dest("verbose");
        parser.addArgument("--incremental", "-i")
                .action(Arguments.storeTrue())
                .help("Use pattern-based (incremental) guesses instead of a list of words from a file")
                .dest("incremental");
        parser.addArgument("--word-list", "-w")
                .help("a file containing newline-separated words to use as the passwords, or the pattern to generate " +
                        "words from if the --incremental flag is set")
                .dest("wordlist")
                .required(true)
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
            isIncremental = res.getBoolean("incremental");
            KeeCrack keeCrack = new CrackerFactory().getCracker(true);
            keeCrack.setCrackingView(new CLICrackingView());

            String databasePath = res.getString("database");
            if (databasePath != null) {
                keeCrack.setDatabaseFile(new File(databasePath));
            }

            String keyfilePath = res.getString("keyfile");
            if (keyfilePath != null) {
                keeCrack.setKeyFile(new File(keyfilePath));
            }

            String wordlist = res.getString("wordlist");
            if (wordlist != null) {
                if (isIncremental) {
                    keeCrack.setWordListPattern(wordlist);
                } else {
                    keeCrack.setWordListFile(new File(wordlist));
                }
            }

            keeCrack.attack();
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }

    private static void print(String message) {
        System.out.print(dateFormat.format(new Date()));
        System.out.print(LOG_SEPARATOR);
        System.out.println(message);
    }

    private static void error(String message) {
        System.err.print(dateFormat.format(new Date()));
        System.err.print(LOG_SEPARATOR);
        System.err.println(message);
    }

    static class CLICrackingView implements CrackingView {
        @Override
        public void onPasswordGuess(String password) {
            if (isVerbose)
                print("Guessing password: " + password);
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
            print(message);
        }

        @Override
        public void onError(Code code) {
            String message = "";
            switch (code) {
                case ERROR_INVALID_DATABASE_FILE:
                    message = "Please specify a database file that you have read access to";
                    break;
                case ERROR_INVALID_WORD_LIST:
                    message = "Please specify a word list file that you have read access to";
                    break;
                case ERROR_CRACKING_INTERRUPTED:
                    message = "Aborted.";
                    break;
                case ERROR_FILE_READ:
                    message = "An error occurred while trying to read one of the files";
                    break;
            }
            error(message);
            System.exit(code.ordinal());
        }
    }
}
