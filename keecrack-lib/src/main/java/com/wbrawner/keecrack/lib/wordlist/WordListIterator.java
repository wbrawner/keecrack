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
package com.wbrawner.keecrack.lib.wordlist;

/**
 * Since Generex uses their own Iterator interface, that complicates things a bit for us, so this class serves as
 * nothing more than a wrapper around both interfaces to prevent complication in other areas of the code;
 */
public class WordListIterator {
    private com.mifmif.common.regex.util.Iterator generexIterator;
    private java.util.Iterator<String> standardIterator;

    WordListIterator(com.mifmif.common.regex.util.Iterator generexIterator) {
        this.generexIterator = generexIterator;
    }

    WordListIterator(java.util.Iterator<String> standardIterator) {
        this.standardIterator = standardIterator;
    }

    public boolean hasNext() {
        if (generexIterator != null) {
            return generexIterator.hasNext();
        }

        return standardIterator != null && standardIterator.hasNext();

    }

    public String next() {
        if (generexIterator != null) {
            return generexIterator.next();
        }

        if (standardIterator != null) {
            return standardIterator.next();
        }

        return null;
    }
}
