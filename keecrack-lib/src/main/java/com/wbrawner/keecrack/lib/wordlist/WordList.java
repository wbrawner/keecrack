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

import com.mifmif.common.regex.Generex;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class WordList {
    private WordListIterator iterator;
    private String name;

    public WordList(File wordFile) {
        java.util.Iterator<String> wordListIterator = null;
        this.name = wordFile.getName();
        try {
            wordListIterator = FileUtils.lineIterator(wordFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        iterator = new WordListIterator(wordListIterator);
    }

    public WordList(String pattern) {
        Generex generex = new Generex(pattern);
        this.name = pattern;
        iterator = new WordListIterator(generex.iterator());
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public String nextWord() {
        return iterator.next();
    }

    public String getName() {
        return this.name;
    }
}
