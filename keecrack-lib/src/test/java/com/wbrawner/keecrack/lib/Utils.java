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

@SuppressWarnings("ResultOfMethodCallIgnored")
class Utils {
    private static final File tmpDir = new File(
            System.getProperty("java.io.tmpdir"),
            "KeeCrackTests-" + System.currentTimeMillis()
    );

    static File getDatabase(String name) throws IOException {
        return copyResourceToFile("/databases/" + name);
    }

    static File getWordList(String name) throws IOException {
        return copyResourceToFile("/wordlists/" + name);
    }

    static File getKeyFile() throws IOException {
        return copyResourceToFile("/keyfiles/key.xml");
    }

    static File getInvalidKeyFile() throws IOException {
        return copyResourceToFile("/keyfiles/invalid-key.xml");
    }

    private static File copyResourceToFile(String resourceName) throws IOException {
        if (!resourceName.startsWith("/")) {
            resourceName = "/" + resourceName;
        }

        InputStream input = Utils.class.getResource(resourceName).openStream();
        byte[] bytes = new byte[input.available()];
        input.read(bytes);
        String fileName = resourceName.substring(resourceName.lastIndexOf('/'));
        File outputFile = new File(getTmpDir(), fileName);
        OutputStream output = new FileOutputStream(outputFile);
        output.write(bytes);
        return outputFile;
    }

    static File getTmpDir() {
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        return tmpDir;
    }

    static void rmdir(File dir) {
        if (dir == null) return;

        if (dir.isDirectory()) {
            //noinspection ConstantConditions
            for (File file : dir.listFiles()) {
                rmdir(file);
            }
        }

        dir.delete();
    }
}
