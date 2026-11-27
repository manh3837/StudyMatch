package com.studymatch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.Repository;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.Subject;
import com.studymatch.model.User;

/** Tạo nhóm học mới (chủ nhóm chọn môn học, mục tiêu và giới hạn thành viên). */
public class CreateGroupActivity extends AppCompatActivity {

    private static final String STATE_SUBJECT = "state_subject";
    private static final String STATE_GOAL = "state_goal";

    private ChipGroup chipSubject;
    private ChipGroup chipGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User me = Repository.get().getCurrentUser();
        if (me == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_create_group);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tạo nhóm học mới");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextInputEditText etName = findViewById(R.id.etGroupName);
        chipSubject = findViewById(R.id.chipGroupCreateSubject);
        chipGoal = findViewById(R.id.chipGroupCreateGoal);
        TextInputEditText etMax = findViewById(R.id.etMaxMembers);
        TextInputEditText etDesc = findViewById(R.id.etGroupDesc);

        String defaultSub = me.subjectIds.isEmpty() ? Catalog.SUBJECTS.get(0).id : me.subjectIds.get(0);
        if (savedInstanceState != null && savedInstanceState.getString(STATE_SUBJECT) != null) {
            defaultSub = savedInstanceState.getString(STATE_SUBJECT);
        }
        for (Subject s : Catalog.SUBJECTS) {
            chipSubject.addView(UiHelper.makeSelectableChip(this, s.name, s.id, s.id.equals(defaultSub)));
        }

        String defaultGoal = me.goals.isEmpty() ? Catalog.GOALS.get(0) : me.goals.get(0);
        if (savedInstanceState != null && savedInstanceState.getString(STATE_GOAL) != null) {
            defaultGoal = savedInstanceState.getString(STATE_GOAL);
        }
        for (String g : Catalog.GOALS) {
            chipGoal.addView(UiHelper.makeSelectableChip(this, g, g, g.equals(defaultGoal)));
        }

        findViewById(R.id.btnSubmitCreateGroup).setOnClickListener(v -> {
            Chip subChip = chipSubject.findViewById(chipSubject.getCheckedChipId());
            Chip goalChip = chipGoal.findViewById(chipGoal.getCheckedChipId());
            String subjectId = subChip != null ? (String) subChip.getTag() : null;
            String goal = goalChip != null ? (String) goalChip.getTag() : "";

            int maxMembers;
            try {
                maxMembers = Integer.parseInt(text(etMax));
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Số thành viên tối đa không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                StudyGroup created = Repository.get().createGroup(
                        me.id, text(etName), subjectId, goal, maxMembers, text(etDesc));
                Toast.makeText(this, "Đã tạo nhóm \"" + created.name + "\"!", Toast.LENGTH_SHORT).show();
                Intent detail = new Intent(this, GroupDetailActivity.class);
                detail.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, created.id);
                startActivity(detail);
                finish();
            } catch (RuntimeException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (chipSubject != null) {
            Chip subChip = chipSubject.findViewById(chipSubject.getCheckedChipId());
            if (subChip != null && subChip.getTag() instanceof String) {
                outState.putString(STATE_SUBJECT, (String) subChip.getTag());
            }
        }
        if (chipGoal != null) {
            Chip goalChip = chipGoal.findViewById(chipGoal.getCheckedChipId());
            if (goalChip != null && goalChip.getTag() instanceof String) {
                outState.putString(STATE_GOAL, (String) goalChip.getTag());
            }
        }
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
}
