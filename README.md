# KeeCrack [![Build Status](https://travis-ci.org/wbrawner/keecrack.svg?branch=master)](https://travis-ci.org/wbrawner/keecrack)
[![Maintainability](https://api.codeclimate.com/v1/badges/a35dff49221e36abf189/maintainability)](https://codeclimate.com/github/wbrawner/keecrack/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/a35dff49221e36abf189/test_coverage)](https://codeclimate.com/github/wbrawner/keecrack/test_coverage)

KeeCrack is a Java program used for brute-forcing KeePass database file master passwords. This should go without saying
but use of this application is prohibited without the express consent of the owner of the database file. KeeCrack works
by taking a KeePass database file, an optional key file, and a word list, then attempts to open the database with the
give key file/password pair until it finds a successful password. KeeCrack does not do incremental word list generation
at this time, though you can

## Usage

You can download a DEB, JAR, or RPM from the releases page. Each build contains both a <abbr title="graphical user
interface">GUI</abbr> and <abbr title="command line interface">CLI</abbr>. For the GUI, download and double-click the
JAR file

## Building

KeeCrack makes use of Gradle, so to build it yourself, you can just run

    ./gradlew jfxJar

This will produce a JAR output, though you can also create platform-specific binaries with the following:

    ./gradlew jfxNative

For more information on building for your OS, please see the README for the
[javafx-gradle-plugin](https://github .com/FibreFoX/javafx-gradle-plugin#requirements)

## Contributing

If you'd like to contribute, please fork the repository, make your changes, squash your commits, and send a pull request
.

## License

KeeCrack is licensed under the Apache 2.0 license

```
Copyright 2018 William Brawner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```