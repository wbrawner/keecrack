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
package com.wbrawner.keecrack.lib.view;

import java.time.Duration;

public interface CrackingView extends BaseView {
    /**
     * Called prior to each guess
     *
     * @param password The password that will be guessed
     */
    void onPasswordGuess(final String password);

    /**
     * Called when the password has been successfully guessed, or there are no more passwords to guess
     *
     * @param password    The password, if successfully guessed, or null if no passwords were successful
     * @param guessCount  The number of passwords guessed
     * @param timeElapsed The time taken to guess the password
     */
    void onResult(final String password, final int guessCount, final Duration timeElapsed);
}
