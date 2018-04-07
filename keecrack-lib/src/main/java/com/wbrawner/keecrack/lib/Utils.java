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

import java.io.*;

class Utils {
    static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static byte[] getFileBytes(File file) {
        byte[] fileBytes;
        try (InputStream inputStream = new FileInputStream(file)) {
            fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            return fileBytes;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }
}
