package com.studymatch.ui;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.Repository;
import com.studymatch.matching.MatchingEngine;
import com.studymatch.model.Subject;
import com.studymatch.model.TimeSlot;
import com.studymatch.model.User;
import com.studymatch.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/** Màn hình 6b: Chỉnh sửa hồ sơ học tập (môn học, mục tiêu, trình độ, lịch rảnh, quyền riêng tư). */
public class EditProfileActivity extends AppCompatActivity {

    private static final String STATE_DRAFT_JSON = "state_draft_json";

    private User draft;
    private TextInputEditText etName;
    private TextInputEditText etMajor;
    private TextInputEditText etBio;
    private ChipGroup chipLevel;
    private ChipGroup chipSubjects;
    private ChipGroup chipGoals;
    private SwitchCompat swHideSchedule;
    private LinearLayout containerSlots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User current = Repository.get().getCurrentUser();
        if (current == null) {
            finish();
            return;
        }
        if (savedInstanceState != null) {
            String json = savedInstanceState.getString(STATE_DRAFT_JSON);
            if (json != null) {
                try {
                    draft = new Gson().fromJson(json, User.class);
                } catch (Exception ignored) {
                    draft = null;
                }
            }
        }
        if (draft == null) {
            draft = Repository.get().copyUser(current);
        }
        if (draft.subjectIds == null) draft.subjectIds = new ArrayList<>();
        if (draft.goals == null) draft.goals = new ArrayList<>();
        if (draft.availability == null) draft.availability = new ArrayList<>();

        setContentView(R.layout.activity_edit_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chỉnh sửa hồ sơ học tập");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etName = findViewById(R.id.etEditName);
        etMajor = findViewById(R.id.etEditMajor);
        etBio = findViewById(R.id.etEditBio);
        chipLevel = findViewById(R.id.chipGroupEditLevel);
        chipSubjects = findViewById(R.id.chipGroupEditSubjects);
        chipGoals = findViewById(R.id.chipGroupEditGoals);
        swHideSchedule = findViewById(R.id.swHideSchedule);
        containerSlots = findViewById(R.id.containerEditSlots);

        if (savedInstanceState == null) {
            etName.setText(draft.name);
            etMajor.setText(draft.major);
            etBio.setText(draft.bio);
            swHideSchedule.setChecked(draft.hideSchedule);
        }

        // Trình độ (1..3)
        int initialLevel = draft.level >= 1 && draft.level <= 3 ? draft.level : User.LEVEL_MEDIUM;
        for (int lv = 1; lv <= 3; lv++) {
            chipLevel.addView(UiHelper.makeSelectableChip(
                    this, Catalog.levelLabel(lv), lv, lv == initialLevel));
        }

        // Môn học (chọn nhiều)
        for (Subject s : Catalog.SUBJECTS) {
            chipSubjects.addView(UiHelper.makeSelectableChip(
                    this, s.name, s.id, draft.subjectIds.contains(s.id)));
        }

        // Mục tiêu (chọn nhiều)
        for (String g : Catalog.GOALS) {
            chipGoals.addView(UiHelper.makeSelectableChip(
                    this, g, g, draft.goals.contains(g)));
        }

        renderSlots();

        findViewById(R.id.btnAddSlot).setOnClickListener(v -> showAddSlotDialog());
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());
    }

    private void renderSlots() {
        containerSlots.removeAllViews();
        draft.availability = MatchingEngine.normalize(draft.availability);
        if (draft.availability.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Chưa có khung giờ rảnh nào. Hãy thêm ít nhất 1 khung giờ!");
            empty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            containerSlots.addView(empty);
            return;
        }

        for (int i = 0; i < draft.availability.size(); i++) {
            final int index = i;
            TimeSlot slot = draft.availability.get(i);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(4), 0, dp(4));

            TextView tv = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tv.setLayoutParams(lp);
            tv.setText("• " + Formatters.slot(slot) + " (" + Formatters.duration(slot.duration()) + ")");
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

            MaterialButton btnDel = UiHelper.makeTextButton(this, "Xóa", R.color.danger);
            btnDel.setOnClickListener(v -> {
                draft.availability.remove(index);
                renderSlots();
            });

            row.addView(tv);
            row.addView(btnDel);
            containerSlots.addView(row);
        }
    }

    private void showAddSlotDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_slot, null);
        ChipGroup dayGroup = view.findViewById(R.id.chipGroupSlotDay);
        MaterialButton btnStart = view.findViewById(R.id.btnSlotStart);
        MaterialButton btnEnd = view.findViewById(R.id.btnSlotEnd);

        for (int d = 1; d <= 7; d++) {
            dayGroup.addView(UiHelper.makeSelectableChip(this, Formatters.dayShort(d), d, d == 2));
        }

        final int[] startMin = {19 * 60};
        final int[] endMin = {21 * 60};
        btnStart.setText("Từ " + Formatters.time(startMin[0]));
        btnEnd.setText("Đến " + Formatters.time(endMin[0]));

        btnStart.setOnClickListener(v -> new TimePickerDialog(this, (tp, h, m) -> {
            startMin[0] = h * 60 + m;
            btnStart.setText("Từ " + Formatters.time(startMin[0]));
        }, startMin[0] / 60, startMin[0] % 60, true).show());

        btnEnd.setOnClickListener(v -> new TimePickerDialog(this, (tp, h, m) -> {
            endMin[0] = (h == 0 && m == 0) ? 24 * 60 : (h * 60 + m);
            btnEnd.setText("Đến " + Formatters.time(endMin[0]));
        }, (endMin[0] / 60) % 24, endMin[0] % 60, true).show());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm khung giờ rảnh")
                .setView(view)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int checkedId = dayGroup.getCheckedChipId();
            Chip chip = dayGroup.findViewById(checkedId);
            int day = chip != null && chip.getTag() instanceof Integer ? (Integer) chip.getTag() : 2;
            TimeSlot ts = new TimeSlot(day, startMin[0], endMin[0]);
            if (!ts.isValid()) {
                Toast.makeText(this, "Giờ kết thúc phải sau giờ bắt đầu", Toast.LENGTH_SHORT).show();
                return;
            }
            draft.availability.add(ts);
            renderSlots();
            dialog.dismiss();
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (draft != null && etName != null) {
            syncFormIntoDraft();
            outState.putString(STATE_DRAFT_JSON, new Gson().toJson(draft));
        }
    }

    private void syncFormIntoDraft() {
        draft.name = text(etName);
        draft.major = text(etMajor);
        draft.bio = text(etBio);
        draft.hideSchedule = swHideSchedule.isChecked();

        int levelChipId = chipLevel.getCheckedChipId();
        Chip lvChip = chipLevel.findViewById(levelChipId);
        draft.level = lvChip != null && lvChip.getTag() instanceof Integer
                ? (Integer) lvChip.getTag() : User.LEVEL_MEDIUM;

        List<String> subs = new ArrayList<>();
        for (int id : chipSubjects.getCheckedChipIds()) {
            Chip c = chipSubjects.findViewById(id);
            if (c != null && c.getTag() instanceof String) subs.add((String) c.getTag());
        }
        draft.subjectIds = subs;

        List<String> goals = new ArrayList<>();
        for (int id : chipGoals.getCheckedChipIds()) {
            Chip c = chipGoals.findViewById(id);
            if (c != null && c.getTag() instanceof String) goals.add((String) c.getTag());
        }
        draft.goals = goals;
        draft.availability = MatchingEngine.normalize(draft.availability);
    }

    private void saveProfile() {
        String name = text(etName);
        if (name.length() < 2) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }
        syncFormIntoDraft();
        Repository.get().updateUser(draft);
        Toast.makeText(this, "Đã lưu hồ sơ học tập!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
