package com.wbrawner.keecrack.lib.view;

public interface FormView extends BaseView {
    void onDatabaseFileSet(String name);
    void onKeyFileSet(String name);
    void onWordListFileSet(String name);
}
