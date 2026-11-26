package com.studymatch.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.Repository;
import com.studymatch.matching.MatchFilter;
import com.studymatch.matching.MatchResult;
import com.studymatch.matching.MatchingEngine;
import com.studymatch.model.Subject;
import com.studymatch.model.User;
import com.studymatch.util.Formatters;

import java.util.List;

/** Màn hình 3: Tìm bạn học – Lọc theo môn học, ngày rảnh, mục tiêu và xếp hạng độ phù hợp. */
public class FindPartnerFragment extends Fragment {

    private static final String ARG_SUBJECT_ID = "subject_id";
    private static final String STATE_SUBJECT = "state_subject";
    private static final String STATE_DAY = "state_day";
    private static final String STATE_GOAL = "state_goal";
    private static final String STATE_ELIGIBLE = "state_eligible";

    private ChipGroup chipGroupSubject;
    private ChipGroup chipGroupDay;
    private ChipGroup chipGroupGoal;
    private SwitchCompat swRequireEligible;
    private TextView tvSummary;
    private TextView tvEmpty;
    private PartnerMatchAdapter adapter;

    private final MatchFilter filter = MatchFilter.defaults();

    public static FindPartnerFragment newInstance(@Nullable String preselectedSubjectId) {
        FindPartnerFragment f = new FindPartnerFragment();
        if (preselectedSubjectId != null) {
            Bundle b = new Bundle();
            b.putString(ARG_SUBJECT_ID, preselectedSubjectId);
            f.setArguments(b);
        }
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_find_partner, container, false);

        if (savedInstanceState != null) {
            filter.subjectId = savedInstanceState.getString(STATE_SUBJECT);
            filter.dayOfWeek = savedInstanceState.getInt(STATE_DAY, 0);
            filter.goal = savedInstanceState.getString(STATE_GOAL);
            filter.requireEligible = savedInstanceState.getBoolean(STATE_ELIGIBLE, true);
        } else if (getArguments() != null) {
            filter.subjectId = getArguments().getString(ARG_SUBJECT_ID);
        }

        chipGroupSubject = v.findViewById(R.id.chipGroupSubjectFilter);
        chipGroupDay = v.findViewById(R.id.chipGroupDayFilter);
        chipGroupGoal = v.findViewById(R.id.chipGroupGoalFilter);
        swRequireEligible = v.findViewById(R.id.swRequireEligible);
        tvSummary = v.findViewById(R.id.tvResultSummary);
        tvEmpty = v.findViewById(R.id.tvEmptyPartners);

        RecyclerView rv = v.findViewById(R.id.rvPartners);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PartnerMatchAdapter();
        rv.setAdapter(adapter);

        swRequireEligible.setChecked(filter.requireEligible);
        setupFilterChips();

        swRequireEligible.setOnCheckedChangeListener((btn, checked) -> {
            filter.requireEligible = checked;
            refreshResults();
        });

        return v;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SUBJECT, filter.subjectId);
        outState.putInt(STATE_DAY, filter.dayOfWeek);
        outState.putString(STATE_GOAL, filter.goal);
        outState.putBoolean(STATE_ELIGIBLE, filter.requireEligible);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshResults();
    }

    private void setupFilterChips() {
        // 1. Môn học
        chipGroupSubject.removeAllViews();
        chipGroupSubject.addView(UiHelper.makeSelectableChip(
                requireContext(), "Tất cả môn chung", null, filter.subjectId == null));
        for (Subject s : Catalog.SUBJECTS) {
            boolean checked = s.id.equals(filter.subjectId);
            chipGroupSubject.addView(UiHelper.makeSelectableChip(requireContext(), s.name, s.id, checked));
        }
        chipGroupSubject.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip c = group.findViewById(checkedIds.get(0));
                filter.subjectId = c == null ? null : (String) c.getTag();
                refreshResults();
            }
        });

        // 2. Ngày trong tuần
        chipGroupDay.removeAllViews();
        chipGroupDay.addView(UiHelper.makeSelectableChip(
                requireContext(), "Mọi ngày", 0, filter.dayOfWeek <= 0));
        for (int d = 1; d <= 7; d++) {
            chipGroupDay.addView(UiHelper.makeSelectableChip(
                    requireContext(), Formatters.dayShort(d), d, filter.dayOfWeek == d));
        }
        chipGroupDay.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip c = group.findViewById(checkedIds.get(0));
                filter.dayOfWeek = c != null && c.getTag() instanceof Integer ? (Integer) c.getTag() : 0;
                refreshResults();
            }
        });

        // 3. Mục tiêu
        chipGroupGoal.removeAllViews();
        chipGroupGoal.addView(UiHelper.makeSelectableChip(
                requireContext(), "Mọi mục tiêu", null, filter.goal == null));
        for (String g : Catalog.GOALS) {
            chipGroupGoal.addView(UiHelper.makeSelectableChip(
                    requireContext(), g, g, g.equals(filter.goal)));
        }
        chipGroupGoal.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip c = group.findViewById(checkedIds.get(0));
                filter.goal = c == null ? null : (String) c.getTag();
                refreshResults();
            }
        });
    }

    private void refreshResults() {
        User me = Repository.get().getCurrentUser();
        if (me == null) return;
        List<MatchResult> results = MatchingEngine.rank(me, Repository.get().getAllUsers(), filter);
        adapter.submit(results, filter.subjectId);
        tvSummary.setText("Tìm thấy " + results.size() + " bạn học phù hợp (xếp hạng theo điểm 0–100)");
        tvEmpty.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
