package com.studymatch.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.Repository;
import com.studymatch.model.GroupMember;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.Subject;
import com.studymatch.model.User;

import java.util.ArrayList;
import java.util.List;

/** Màn hình 4a: Quản lý nhóm của tôi, xử lý lời mời và khám phá các nhóm đang mở. */
public class GroupsFragment extends Fragment {

    private View sectionInvites;
    private LinearLayout containerInvites;
    private TextView tvEmptyMyGroups;
    private TextView tvEmptyDiscover;
    private ChipGroup chipGroupSubject;
    private GroupAdapter myGroupsAdapter;
    private GroupAdapter discoverAdapter;

    private static final String STATE_SUBJECT = "state_subject";

    private String selectedSubjectId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_groups, container, false);

        if (savedInstanceState != null) {
            selectedSubjectId = savedInstanceState.getString(STATE_SUBJECT);
        }

        sectionInvites = v.findViewById(R.id.sectionInvites);
        containerInvites = v.findViewById(R.id.containerInvites);
        tvEmptyMyGroups = v.findViewById(R.id.tvEmptyMyGroups);
        tvEmptyDiscover = v.findViewById(R.id.tvEmptyDiscoverGroups);
        chipGroupSubject = v.findViewById(R.id.chipGroupFilterGroupSubject);

        RecyclerView rvMy = v.findViewById(R.id.rvMyGroups);
        rvMy.setLayoutManager(new LinearLayoutManager(requireContext()));
        myGroupsAdapter = new GroupAdapter(true, this::refreshAll);
        rvMy.setAdapter(myGroupsAdapter);

        RecyclerView rvDiscover = v.findViewById(R.id.rvDiscoverGroups);
        rvDiscover.setLayoutManager(new LinearLayoutManager(requireContext()));
        discoverAdapter = new GroupAdapter(false, this::refreshAll);
        rvDiscover.setAdapter(discoverAdapter);

        setupSubjectFilter();

        v.findViewById(R.id.fabCreateGroup).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), CreateGroupActivity.class)));

        return v;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SUBJECT, selectedSubjectId);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAll();
    }

    private void setupSubjectFilter() {
        chipGroupSubject.removeAllViews();
        chipGroupSubject.addView(UiHelper.makeSelectableChip(
                requireContext(), "Tất cả môn", null, selectedSubjectId == null));
        for (Subject s : Catalog.SUBJECTS) {
            chipGroupSubject.addView(UiHelper.makeSelectableChip(
                    requireContext(), s.name, s.id, s.id.equals(selectedSubjectId)));
        }
        chipGroupSubject.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip c = group.findViewById(checkedIds.get(0));
                selectedSubjectId = c == null ? null : (String) c.getTag();
                refreshAll();
            }
        });
    }

    private void refreshAll() {
        Repository repo = Repository.get();
        User me = repo.getCurrentUser();
        if (me == null) return;

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshNotificationBadge();
        }

        // 1. Lời mời tham gia nhóm
        containerInvites.removeAllViews();
        List<GroupMember> invites = new ArrayList<>();
        for (GroupMember m : repo.getMembershipsOfUser(me.id)) {
            if (m.isInvited()) invites.add(m);
        }
        if (invites.isEmpty()) {
            sectionInvites.setVisibility(View.GONE);
        } else {
            sectionInvites.setVisibility(View.VISIBLE);
            for (GroupMember inv : invites) {
                StudyGroup g = repo.getGroup(inv.groupId);
                if (g == null) continue;
                containerInvites.addView(buildInviteCard(g));
            }
        }

        // 2. Nhóm của tôi
        List<StudyGroup> mine = repo.getGroupsOfUser(me.id);
        myGroupsAdapter.submit(mine);
        tvEmptyMyGroups.setVisibility(mine.isEmpty() ? View.VISIBLE : View.GONE);

        // 3. Khám phá nhóm khác
        List<StudyGroup> discover = new ArrayList<>();
        for (StudyGroup g : repo.getAllGroups()) {
            GroupMember m = repo.getMembership(g.id, me.id);
            if (m != null && (m.isApproved() || m.isInvited())) continue;
            if (selectedSubjectId != null && !selectedSubjectId.equals(g.subjectId)) continue;
            discover.add(g);
        }
        discoverAdapter.submit(discover);
        tvEmptyDiscover.setVisibility(discover.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private View buildInviteCard(StudyGroup g) {
        Repository repo = Repository.get();
        User me = repo.getCurrentUser();
        User owner = repo.getUser(g.ownerId);

        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = dp(8);
        card.setLayoutParams(cardLp);
        card.setRadius(dp(12));
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary_light));

        LinearLayout box = new LinearLayout(requireContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));

        TextView title = new TextView(requireContext());
        title.setText(g.name + " (" + Catalog.subjectName(g.subjectId) + ")");
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        box.addView(title);

        TextView sub = new TextView(requireContext());
        sub.setText("Được mời bởi " + (owner != null ? owner.name : "chủ nhóm"));
        sub.setTextSize(12f);
        sub.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        box.addView(sub);

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.END);

        MaterialButton btnDecline = UiHelper.makeTextButton(
                requireContext(), "Từ chối", R.color.text_secondary);
        btnDecline.setOnClickListener(v -> {
            if (me == null) return;
            repo.declineInvite(g.id, me.id);
            refreshAll();
        });

        MaterialButton btnAccept = new MaterialButton(requireContext());
        btnAccept.setText("Chấp nhận");
        btnAccept.setAllCaps(false);
        btnAccept.setOnClickListener(v -> {
            if (me == null) return;
            try {
                repo.acceptInvite(g.id, me.id);
                Toast.makeText(requireContext(), "Đã tham gia nhóm \"" + g.name + "\"!", Toast.LENGTH_SHORT).show();
                refreshAll();
            } catch (RuntimeException ex) {
                Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        actions.addView(btnDecline);
        actions.addView(btnAccept);
        box.addView(actions);
        card.addView(box);
        return card;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
