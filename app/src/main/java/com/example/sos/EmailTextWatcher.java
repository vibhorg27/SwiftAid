package com.example.sos;

import android.text.Editable;
import android.text.TextWatcher;

public class EmailTextWatcher extends SimpleTextWatcher {
    private final Runnable onTextChangedRunnable;

    public EmailTextWatcher(Runnable onTextChangedRunnable) {
        this.onTextChangedRunnable = onTextChangedRunnable;
    }

    @Override
    public void onTextChanged() {
        onTextChangedRunnable.run();
    }
}