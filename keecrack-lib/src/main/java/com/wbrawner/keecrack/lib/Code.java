package com.wbrawner.keecrack.lib;

/**
 * Rather than simple string error messages, an enum is used so that the error messages can be translated by any UI
 */
public enum Code {
    ERROR_FILE_READ,
    ERROR_CRACKING_INTERRUPTED,
    ERROR_CRACKING_IN_PROGRESS,
    ERROR_MISSING_DATABASE_FILE,
    ERROR_MISSING_WORD_LIST_FILE
}
