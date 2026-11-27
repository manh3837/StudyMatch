package com.studymatch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.Repository;
import com.studymatch.matching.MatchFilter;
import com.studymatch.matching.MatchResult;
import com.studymatch.matching.MatchingEngine;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.StudySession;
import com.studymatch.model.User;
import com.studymatch.util.Formatters;

import java.util.List;

/** Màn hình 2: Trang chủ – Môn học của bạn, buổi học sắp tới và gợi ý bạn học hàng đầu. */
public class HomeFragment extends Fragment {

    private TextView tvGreeting;
    private View cardIncomplete;
    private View cardNotifBanner;
    private TextView tvNotifBanner;
    private ChipGroup chipGroupSubjects;
    private TextView tvNoSubjects;
    private LinearLayout containerUpcoming;
    private TextView tvNoUpcoming;
    private TextView tvNoPartners;
    private PartnerMatchAdapter partnerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = v.findViewById(R.id.tvGreeting);
        cardIncomplete = v.findViewById(R.id.cardIncompleteProfile);
        cardNotifBanner = v.findViewById(R.id.cardNotificationBanner);
        tvNotifBanner = v.findViewById(R.id.tvNotificationBanner);
        chipGroupSubjects = v.findViewById(R.id.chipGroupMySubjects);
        tvNoSubjects = v.findViewById(R.id.tvNoSubjects);
        containerUpcoming = v.findViewById(R.id.containerUpcomingHome);
        tvNoUpcoming = v.findViewById(R.id.tvNoUpcomingHome);
        tvNoPartners = v.findViewById(R.id.tvNoPartnersHome);

        RecyclerView rvPartners = v.findViewById(R.id.rvSuggestedPartners);
        rvPartners.setLayoutManager(new LinearLayoutManager(requireContext()));
        partnerAdapter = new PartnerMatchAdapter();
        rvPartners.setAdapter(partnerAdapter);

        v.findViewById(R.id.btnQuickFind).setOnClickListener(x -> host().openTab(R.id.nav_find));
        v.findViewById(R.id.btnSeeAllPartners).setOnClickListener(x -> host().openTab(R.id.nav_find));
        v.findViewById(R.id.btnSeeAllSchedule).setOnClickListener(x -> host().openTab(R.id.nav_schedule));
        v.findViewById(R.id.btnOpenGroupsFromBanner).setOnClickListener(x -> host().openTab(R.id.nav_groups));

        View.OnClickListener goEdit = x ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class));
        v.findViewById(R.id.btnCompleteProfile).setOnClickListener(goEdit);
        v.findViewById(R.id.btnEditSubjects).setOnClickListener(goEdit);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindData();
    }

    private void bindData() {
        Repository repo = Repository.get();
        User me = repo.getCurrentUser();
        if (me == null) return;

        tvGreeting.setText("Xin chào, " + me.name + "!");
        cardIncomplete.setVisibility(me.isProfileComplete() ? View.GONE : View.VISIBLE);

        int notifCount = repo.countNotifications(me.id);
        if (notifCount > 0) {
            cardNotifBanner.setVisibility(View.VISIBLE);
            tvNotifBanner.setText("Bạn có " + notifCount + " yêu cầu tham gia / lời mời nhóm đang chờ xử lý.");
        } else {
            cardNotifBanner.setVisibility(View.GONE);
        }

        // Môn học của tôi
        chipGroupSubjects.removeAllViews();
        if (me.subjectIds.isEmpty()) {
            tvNoSubjects.setVisibility(View.VISIBLE);
        } else {
            tvNoSubjects.setVisibility(View.GONE);
            for (String subId : me.subjectIds) {
                chipGroupSubjects.addView(UiHelper.makeActionChip(
                        requireContext(),
                        Catalog.subjectName(subId),
                        v -> host().openFindWithSubject(subId)
                ));
            }
        }

        // Tối đa 2 buổi học sắp tới
        containerUpcoming.removeAllViews();
        List<StudySession> sessions = repo.getUpcomingSessionsOfUser(me.id);
        if (sessions.isEmpty()) {
            tvNoUpcoming.setVisibility(View.VISIBLE);
        } else {
            tvNoUpcoming.setVisibility(View.GONE);
            LayoutInflater inf = LayoutInflater.from(requireContext());
            int limit = Math.min(2, sessions.size());
            for (int i = 0; i < limit; i++) {
                StudySession s = sessions.get(i);
                StudyGroup g = repo.getGroup(s.groupId);
                View row = inf.inflate(R.layout.item_session, containerUpcoming, false);
                ((TextView) row.findViewById(R.id.tvSessionDayOfWeek))
                        .setText(Formatters.dayShort(Formatters.dayOfWeek(s.startTime)));
                ((TextView) row.findViewById(R.id.tvSessionDayMonth))
                        .setText(Formatters.dayMonth(s.startTime));
                ((TextView) row.findViewById(R.id.tvSessionGroupName))
                        .setText(g != null ? g.name : "Nhóm học");
                ((TextView) row.findViewById(R.id.tvSessionTitle)).setText(s.title);
                ((TextView) row.findViewById(R.id.tvSessionTimeLocation))
                        .setText(Formatters.sessionTime(s.startTime, s.durationMinutes) + " • " + s.location);
                TextView tvNote = row.findViewById(R.id.tvSessionNote);
                if (s.note != null && !s.note.trim().isEmpty()) {
                    tvNote.setVisibility(View.VISIBLE);
                    tvNote.setText("Ghi chú: " + s.note.trim());
                } else {
                    tvNote.setVisibility(View.GONE);
                }
                row.findViewById(R.id.btnSessionAction).setVisibility(View.GONE);
                row.setOnClickListener(v -> {
                    Intent it = new Intent(requireContext(), GroupDetailActivity.class);
                    it.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, s.groupId);
                    startActivity(it);
                });
                containerUpcoming.addView(row);
            }
        }

        // Top 5 bạn học phù hợp
        List<MatchResult> ranked = MatchingEngine.rank(me, repo.getAllUsers(), MatchFilter.defaults());
        if (ranked.size() > 5) ranked = ranked.subList(0, 5);
        partnerAdapter.submit(ranked, null);
        tvNoPartners.setVisibility(ranked.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private MainActivity host() {
        return (MainActivity) requireActivity();
    }
}
