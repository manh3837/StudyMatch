package com.studymatch.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.studymatch.R;
import com.studymatch.data.Repository;
import com.studymatch.matching.MatchingEngine;
import com.studymatch.matching.SlotSuggestion;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.User;
import com.studymatch.util.DateUtils;
import com.studymatch.util.Formatters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeSet;

/** Màn hình 5b: Tạo buổi học nhóm với gợi ý khung giờ chung từ lịch rảnh của thành viên. */
public class CreateSessionActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_DAY_OF_WEEK = "day_of_week";
    public static final String EXTRA_START_MINUTE = "start_minute";
    public static final String EXTRA_DURATION = "duration";

    private static final String STATE_GROUP_ID = "state_group_id";
    private static final String STATE_START_MILLIS = "state_start_millis";
    private static final String STATE_DURATION = "state_duration";

    private static final int[] DURATIONS = {60, 90, 120, 180};

    private ChipGroup chipGroupPicker;
    private ChipGroup chipSuggested;
    private TextView tvNoSuggestions;
    private TextInputEditText etTitle;
    private MaterialButton btnDate;
    private MaterialButton btnStart;
    private ChipGroup chipDuration;
    private TextView tvAvailabilityPreview;
    private TextInputEditText etLocation;
    private TextInputEditText etNote;

    private String selectedGroupId;
    private long selectedStartMillis;
    private int selectedDuration = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User me = Repository.get().getCurrentUser();
        if (me == null) {
            finish();
            return;
        }
        List<StudyGroup> myGroups = Repository.get().getGroupsOfUser(me.id);
        if (myGroups.isEmpty()) {
            Toast.makeText(this, "Bạn chưa tham gia nhóm nào", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_create_session);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lên lịch buổi học nhóm");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chipGroupPicker = findViewById(R.id.chipGroupSessionGroup);
        chipSuggested = findViewById(R.id.chipGroupSuggestedSlots);
        tvNoSuggestions = findViewById(R.id.tvNoSlotSuggestions);
        etTitle = findViewById(R.id.etSessionTitle);
        btnDate = findViewById(R.id.btnPickDate);
        btnStart = findViewById(R.id.btnPickStartTime);
        chipDuration = findViewById(R.id.chipGroupDuration);
        tvAvailabilityPreview = findViewById(R.id.tvAvailabilityPreview);
        etLocation = findViewById(R.id.etSessionLocation);
        etNote = findViewById(R.id.etSessionNote);

        if (savedInstanceState != null) {
            selectedGroupId = savedInstanceState.getString(STATE_GROUP_ID);
            selectedStartMillis = savedInstanceState.getLong(STATE_START_MILLIS, 0L);
            selectedDuration = savedInstanceState.getInt(STATE_DURATION, 120);
        } else {
            selectedGroupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
            selectedDuration = getIntent().getIntExtra(EXTRA_DURATION, 120);
        }

        if (!containsGroup(myGroups, selectedGroupId)) {
            selectedGroupId = myGroups.get(0).id;
        }

        if (savedInstanceState == null) {
            int passedDay = getIntent().getIntExtra(EXTRA_DAY_OF_WEEK, 0);
            int passedStart = getIntent().getIntExtra(EXTRA_START_MINUTE, 19 * 60);
            long now = System.currentTimeMillis();
            if (passedDay >= 1 && passedDay <= 7) {
                selectedStartMillis = DateUtils.nextOccurrence(passedDay, passedStart, now);
            } else {
                applyBestSuggestionOrDefault(now);
            }
        }

        // Chip chọn nhóm
        for (StudyGroup g : myGroups) {
            chipGroupPicker.addView(UiHelper.makeSelectableChip(
                    this, g.name, g.id, g.id.equals(selectedGroupId)));
        }
        chipGroupPicker.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip c = group.findViewById(checkedIds.get(0));
                if (c != null && c.getTag() instanceof String) {
                    String newGroupId = (String) c.getTag();
                    if (!newGroupId.equals(selectedGroupId)) {
                        selectedGroupId = newGroupId;
                        applyBestSuggestionOrDefault(System.currentTimeMillis());
                        rebuildDurationChips();
                        refreshSuggestionsAndPreview();
                    }
                }
            }
        });

        // Chip thời lượng
        rebuildDurationChips();
        chipDuration.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip c = group.findViewById(checkedIds.get(0));
                if (c != null && c.getTag() instanceof Integer) {
                    selectedDuration = (Integer) c.getTag();
                    updateDateTimeButtonsAndPreview();
                }
            }
        });

        btnDate.setOnClickListener(v -> pickDate());
        btnStart.setOnClickListener(v -> pickTime());
        findViewById(R.id.btnSubmitSession).setOnClickListener(v -> submit(me.id));

        refreshSuggestionsAndPreview();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_GROUP_ID, selectedGroupId);
        outState.putLong(STATE_START_MILLIS, selectedStartMillis);
        outState.putInt(STATE_DURATION, selectedDuration);
    }

    private static boolean containsGroup(List<StudyGroup> groups, String groupId) {
        if (groupId == null) return false;
        for (StudyGroup g : groups) {
            if (groupId.equals(g.id)) return true;
        }
        return false;
    }

    private void applyBestSuggestionOrDefault(long now) {
        List<User> members = Repository.get().getApprovedUsers(selectedGroupId);
        List<SlotSuggestion> suggestions = MatchingEngine.suggestCommonSlots(members, 60, 5);
        if (!suggestions.isEmpty()) {
            SlotSuggestion best = suggestions.get(0);
            selectedStartMillis = DateUtils.nextOccurrence(best.dayOfWeek, best.startMinute, now);
            selectedDuration = Math.min(120, best.duration());
        } else {
            selectedStartMillis = DateUtils.nextOccurrence(2, 19 * 60, now);
            selectedDuration = 120;
        }
    }

    private void rebuildDurationChips() {
        TreeSet<Integer> durations = new TreeSet<>();
        for (int d : DURATIONS) durations.add(d);
        if (selectedDuration >= 15) durations.add(selectedDuration);

        chipDuration.removeAllViews();
        for (int dur : durations) {
            chipDuration.addView(UiHelper.makeSelectableChip(
                    this, Formatters.duration(dur), dur, dur == selectedDuration));
        }
    }

    private void refreshSuggestionsAndPreview() {
        List<User> members = Repository.get().getApprovedUsers(selectedGroupId);
        List<SlotSuggestion> suggestions = MatchingEngine.suggestCommonSlots(members, 60, 5);

        chipSuggested.removeAllViews();
        if (suggestions.isEmpty()) {
            tvNoSuggestions.setVisibility(View.VISIBLE);
        } else {
            tvNoSuggestions.setVisibility(View.GONE);
            for (SlotSuggestion s : suggestions) {
                String label = Formatters.day(s.dayOfWeek) + " "
                        + Formatters.timeRange(s.startMinute, s.endMinute)
                        + " (" + s.count() + "/" + members.size() + ")";
                chipSuggested.addView(UiHelper.makeActionChip(this, label, v -> {
                    selectedStartMillis = DateUtils.nextOccurrence(
                            s.dayOfWeek, s.startMinute, System.currentTimeMillis());
                    if (selectedDuration > s.duration()) {
                        selectedDuration = Math.min(120, s.duration());
                        rebuildDurationChips();
                    }
                    updateDateTimeButtonsAndPreview();
                }));
            }
        }
        updateDateTimeButtonsAndPreview();
    }

    private void updateDateTimeButtonsAndPreview() {
        btnDate.setText("Ngày: " + Formatters.date(selectedStartMillis));
        btnStart.setText("Bắt đầu: " + Formatters.time(Formatters.minuteOfDay(selectedStartMillis)));

        User me = Repository.get().getCurrentUser();
        List<User> members = Repository.get().getApprovedUsers(selectedGroupId);
        int day = Formatters.dayOfWeek(selectedStartMillis);
        int start = Formatters.minuteOfDay(selectedStartMillis);
        int end = start + selectedDuration;

        List<String> freeIds = MatchingEngine.availableMembers(members, day, start, end);
        List<String> visibleFreeNames = new ArrayList<>();
        int hiddenCount = 0;
        for (User m : members) {
            if (!freeIds.contains(m.id)) continue;
            if (m.hideSchedule && (me == null || !m.id.equals(me.id))) {
                hiddenCount++;
            } else {
                visibleFreeNames.add(m.firstName());
            }
        }
        if (freeIds.isEmpty()) {
            tvAvailabilityPreview.setText("Lưu ý: Chưa có thành viên nào khai báo rảnh trọn khung giờ này.");
        } else {
            String detail;
            if (!visibleFreeNames.isEmpty() && hiddenCount > 0) {
                detail = ": " + Formatters.join(visibleFreeNames, ", ") + " (+" + hiddenCount + " TV ẩn lịch)";
            } else if (!visibleFreeNames.isEmpty()) {
                detail = ": " + Formatters.join(visibleFreeNames, ", ");
            } else {
                detail = " (ẩn chi tiết lịch rảnh)";
            }
            tvAvailabilityPreview.setText("Có " + freeIds.size() + "/" + members.size()
                    + " thành viên rảnh khung giờ này" + detail);
        }
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(selectedStartMillis);
        new DatePickerDialog(this, (dp, year, month, dayOfMonth) -> {
            Calendar updated = Calendar.getInstance();
            updated.setTimeInMillis(selectedStartMillis);
            updated.set(Calendar.YEAR, year);
            updated.set(Calendar.MONTH, month);
            updated.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedStartMillis = updated.getTimeInMillis();
            updateDateTimeButtonsAndPreview();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickTime() {
        int curMin = Formatters.minuteOfDay(selectedStartMillis);
        new TimePickerDialog(this, (tp, hour, minute) -> {
            Calendar updated = Calendar.getInstance();
            updated.setTimeInMillis(selectedStartMillis);
            updated.set(Calendar.HOUR_OF_DAY, hour);
            updated.set(Calendar.MINUTE, minute);
            updated.set(Calendar.SECOND, 0);
            updated.set(Calendar.MILLISECOND, 0);
            selectedStartMillis = updated.getTimeInMillis();
            updateDateTimeButtonsAndPreview();
        }, curMin / 60, curMin % 60, true).show();
    }

    private void submit(String creatorId) {
        try {
            Repository.get().createSession(
                    selectedGroupId,
                    creatorId,
                    text(etTitle),
                    selectedStartMillis,
                    selectedDuration,
                    text(etLocation),
                    text(etNote)
            );
            Toast.makeText(this, "Đã tạo buổi học nhóm!", Toast.LENGTH_SHORT).show();
            finish();
        } catch (RuntimeException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
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
