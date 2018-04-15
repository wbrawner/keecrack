# KeeCrack [![Build Status](https://travis-ci.org/wbrawner/keecrack.svg?branch=master)](https://travis-ci.org/wbrawner/keecrack)

KeeCrack is a Java program used for brute-forcing KeePass database file master passwords. This should go without saying
but use of this application is prohibited without the express consent of the owner of the database file. KeeCrack works
by taking a KeePass database file, an optional key file, and a word list, then attempts to open the database with the
give key file/password pair until it finds a successful password. The word list can either be a newline separated 
file, or a regular expression pattern.

## Usage

You can download a JAR from the releases page. Each build contains both a <abbr title="graphical user
interface">GUI</abbr> and <abbr title="command line interface">CLI</abbr>. For the GUI, download and double-click the
JAR file. For command line usage, run the jar with `java -jar keecrack.jar -h` to get the following output:

```
usage: KeeCrack [-h] [--verbose] [--incremental] --word-list WORD-LIST-FILE [--key-file KEY-FILE] database

Brute force KeePass database files

positional arguments:
  database               the database file to brute force

named arguments:
  -h, --help             show this help message and exit
  --verbose, -v          Increase logging output
  --incremental, -i      Use pattern-based (incremental) guesses instead of a list of words from a file
  --word-list WORD-LIST-FILE, -w WORD-LIST-FILE
                         a file containing newline-separated words to use as the passwords, or the pattern to generate words from if the --incremental flag is set
  --key-file KEY-FILE, -k KEY-FILE
                         the key file to use with the database
```

## Building

KeeCrack makes use of Gradle, so to build it yourself, you can just run

    ./gradlew shadowJar

## Contributing

If you'd like to contribute, please fork the repository, make your changes, squash your commits, and send a pull 
request.

## Attribution

KeeCrack is possible thanks to the following libraries:

[Generex](https://github.com/mifmif/Generex)

[KeePassJava2](https://github.com/jorabin/KeePassJava2)

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
