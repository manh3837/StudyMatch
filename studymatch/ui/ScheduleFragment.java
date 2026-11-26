package com.studymatch.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.studymatch.R;
import com.studymatch.data.Repository;
import com.studymatch.matching.MatchingEngine;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.StudySession;
import com.studymatch.model.TimeSlot;
import com.studymatch.model.User;
import com.studymatch.util.Formatters;

import java.util.List;

/** Màn hình 5a: Lịch học nhóm tổng hợp và thời gian rảnh cá nhân hằng tuần. */
public class ScheduleFragment extends Fragment {

    private TextView tvEmptySessions;
    private ChipGroup chipGroupSlots;
    private TextView tvEmptySlots;
    private SessionAdapter sessionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);

        tvEmptySessions = v.findViewById(R.id.tvEmptySessions);
        chipGroupSlots = v.findViewById(R.id.chipGroupMySlots);
        tvEmptySlots = v.findViewById(R.id.tvEmptyMySlots);

        RecyclerView rv = v.findViewById(R.id.rvUpcomingSessions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        sessionAdapter = new SessionAdapter(false, this::bindData);
        rv.setAdapter(sessionAdapter);

        v.findViewById(R.id.btnCreateSessionTop).setOnClickListener(x -> handleCreateSession());
        v.findViewById(R.id.btnManageAvailability).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindData();
    }

    private void handleCreateSession() {
        User me = Repository.get().getCurrentUser();
        if (me == null) return;
        List<StudyGroup> myGroups = Repository.get().getGroupsOfUser(me.id);
        if (myGroups.isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chưa có nhóm học")
                    .setMessage("Bạn cần tạo hoặc tham gia ít nhất một nhóm học trước khi lên lịch buổi học.")
                    .setPositiveButton("Tạo nhóm", (d, w) ->
                            startActivity(new Intent(requireContext(), CreateGroupActivity.class)))
                    .setNegativeButton("Đóng", null)
                    .show();
            return;
        }
        Intent it = new Intent(requireContext(), CreateSessionActivity.class);
        it.putExtra(CreateSessionActivity.EXTRA_GROUP_ID, myGroups.get(0).id);
        startActivity(it);
    }

    private void bindData() {
        Repository repo = Repository.get();
        User me = repo.getCurrentUser();
        if (me == null) return;

        List<StudySession> upcoming = repo.getUpcomingSessionsOfUser(me.id);
        sessionAdapter.submit(upcoming);
        tvEmptySessions.setVisibility(upcoming.isEmpty() ? View.VISIBLE : View.GONE);

        chipGroupSlots.removeAllViews();
        List<TimeSlot> normalized = MatchingEngine.normalize(me.availability);
        if (normalized.isEmpty()) {
            tvEmptySlots.setVisibility(View.VISIBLE);
        } else {
            tvEmptySlots.setVisibility(View.GONE);
            for (TimeSlot s : normalized) {
                chipGroupSlots.addView(UiHelper.makeDisplayChip(requireContext(), Formatters.slot(s)));
            }
        }
    }
}
