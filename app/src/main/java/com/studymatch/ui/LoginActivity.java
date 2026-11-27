package com.studymatch.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.MockData;
import com.studymatch.data.Repository;
import com.studymatch.model.User;
import com.studymatch.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/** Màn hình 1a: Đăng nhập tài khoản sinh viên (kèm chọn nhanh tài khoản mẫu để demo). */
@SuppressWarnings("SpellCheckingInspection")
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Repository.get().getCurrentUser() != null) {
            goMain();
            return;
        }
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnDemoQuick = findViewById(R.id.btnDemoQuick);
        MaterialButton btnPickMock = findViewById(R.id.btnPickMockAccount);
        MaterialButton btnGoRegister = findViewById(R.id.btnGoRegister);

        if (savedInstanceState == null) {
            etEmail.setText(MockData.DEMO_EMAIL);
            etPassword.setText(MockData.DEMO_PASSWORD);
        }

        btnLogin.setOnClickListener(v -> doLogin(
                text(etEmail),
                rawText(etPassword)
        ));

        btnDemoQuick.setOnClickListener(v -> doLogin(MockData.DEMO_EMAIL, MockData.DEMO_PASSWORD));

        btnPickMock.setOnClickListener(v -> showMockAccountPicker());

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void doLogin(String email, String password) {
        try {
            User u = Repository.get().login(email, password);
            Toast.makeText(this, "Xin chào " + u.name + "!", Toast.LENGTH_SHORT).show();
            goMain();
        } catch (RuntimeException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showMockAccountPicker() {
        Repository repo = Repository.get();
        List<User> mockUsers = new ArrayList<>();
        for (User u : repo.getAllUsers()) {
            if (repo.hasDemoPassword(u.id)) mockUsers.add(u);
        }
        if (mockUsers.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy tài khoản mẫu.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[mockUsers.size()];
        for (int i = 0; i < mockUsers.size(); i++) {
            User u = mockUsers.get(i);
            String subs = u.subjectIds.isEmpty()
                    ? "Chưa chọn môn học"
                    : Formatters.join(Catalog.subjectNames(u.subjectIds), ", ");
            labels[i] = u.name + " (" + u.email + ")\n" + subs;
        }
        new AlertDialog.Builder(this)
                .setTitle("Chọn sinh viên mẫu (mật khẩu 123456)")
                .setItems(labels, (d, which) -> {
                    User chosen = mockUsers.get(which);
                    doLogin(chosen.email, MockData.DEMO_PASSWORD);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void goMain() {
        Intent it = new Intent(this, MainActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(it);
        finish();
    }

    private static String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static String rawText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString();
    }
}
