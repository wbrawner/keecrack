package com.wbrawner.keecrack.lib.view;

import java.time.Duration;

public interface CrackingView extends BaseView {
    /**
     * Called prior to each guess
     * @param password The password that will be guessed
     */
    void onPasswordGuess(final String password);

    /**
     * Called when the password has been successfully guessed, or there are no more passwords to guess
     * @param password The password, if successfully guessed, or null if no passwords were successful
     * @param guessCount The number of passwords guessed
     * @param timeElapsed The time taken to guess the password
     */
    void onResult(final String password, final int guessCount, final Duration timeElapsed);
}
