package com.studymatch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.studymatch.R;
import com.studymatch.data.Repository;

/** Màn hình 1b: Đăng ký tài khoản sinh viên mới. */
public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextInputEditText etName = findViewById(R.id.etName);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etConfirm = findViewById(R.id.etConfirmPassword);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        MaterialButton btnBack = findViewById(R.id.btnBackLogin);

        btnRegister.setOnClickListener(v -> {
            String name = text(etName);
            String email = text(etEmail);
            String pw = rawText(etPassword);
            String cf = rawText(etConfirm);

            if (!pw.equals(cf)) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Repository.get().register(name, email, pw);
                Toast.makeText(this, "Đăng ký thành công! Hãy cập nhật môn học & lịch rảnh.", Toast.LENGTH_LONG).show();
                Intent main = new Intent(this, MainActivity.class);
                main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                Intent edit = new Intent(this, EditProfileActivity.class);
                startActivities(new Intent[]{main, edit});
                finish();
            } catch (RuntimeException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private static String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static String rawText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString();
    }
}
