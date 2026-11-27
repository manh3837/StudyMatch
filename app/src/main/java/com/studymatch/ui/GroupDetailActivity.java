package com.studymatch.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.Repository;
import com.studymatch.matching.MatchFilter;
import com.studymatch.matching.MatchResult;
import com.studymatch.matching.MatchingEngine;
import com.studymatch.matching.SlotSuggestion;
import com.studymatch.model.GroupMember;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.StudySession;
import com.studymatch.model.User;
import com.studymatch.util.Formatters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Màn hình 4b: Quản lý nhóm – Duyệt thành viên, tự đề xuất khung giờ chung và quản lý buổi học. */
public class GroupDetailActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "group_id";

    private String groupId;
    private TextView tvTitle;
    private TextView tvMemberCount;
    private TextView tvSubjectGoal;
    private TextView tvDesc;
    private MaterialButton btnPrimary;
    private MaterialButton btnDeleteOrLeave;
    private View cardPending;
    private TextView tvPendingHeader;
    private LinearLayout containerPending;
    private ChipGroup chipSuggestedSlots;
    private TextView tvNoSuggestedSlots;
    private MaterialButton btnAddSession;
    private TextView tvNoSessions;
    private LinearLayout containerMembers;
    private SessionAdapter sessionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        if (Repository.get().getGroup(groupId) == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_group_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết nhóm học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvTitle = findViewById(R.id.tvGroupTitle);
        tvMemberCount = findViewById(R.id.tvGroupMemberCount);
        tvSubjectGoal = findViewById(R.id.tvGroupSubjectGoal);
        tvDesc = findViewById(R.id.tvGroupDesc);
        btnPrimary = findViewById(R.id.btnGroupPrimaryAction);
        btnDeleteOrLeave = findViewById(R.id.btnDeleteOrLeaveGroup);
        cardPending = findViewById(R.id.cardPendingRequests);
        tvPendingHeader = findViewById(R.id.tvPendingHeader);
        containerPending = findViewById(R.id.containerPendingRequests);
        chipSuggestedSlots = findViewById(R.id.chipGroupGroupSuggestedSlots);
        tvNoSuggestedSlots = findViewById(R.id.tvNoSuggestedSlots);
        btnAddSession = findViewById(R.id.btnAddSessionInGroup);
        tvNoSessions = findViewById(R.id.tvNoGroupSessions);
        containerMembers = findViewById(R.id.containerGroupMembers);

        RecyclerView rvSessions = findViewById(R.id.rvGroupSessions);
        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        sessionAdapter = new SessionAdapter(true, this::bindGroup);
        rvSessions.setAdapter(sessionAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindGroup();
    }

    private void bindGroup() {
        Repository repo = Repository.get();
        StudyGroup g = repo.getGroup(groupId);
        User me = repo.getCurrentUser();
        if (g == null || me == null) {
            finish();
            return;
        }

        List<User> approvedUsers = repo.getApprovedUsers(g.id);
        GroupMember myMembership = repo.getMembership(g.id, me.id);
        boolean isOwner = me.id.equals(g.ownerId);
        boolean isApproved = myMembership != null && myMembership.isApproved();

        tvTitle.setText(g.name);
        tvMemberCount.setText(approvedUsers.size() + "/" + g.maxMembers + " TV");
        String goalText = (g.goal == null || g.goal.isEmpty()) ? "" : " • " + g.goal;
        tvSubjectGoal.setText(Catalog.subjectName(g.subjectId) + goalText);
        tvDesc.setText((g.description == null || g.description.isEmpty())
                ? "Chưa có mô tả." : g.description);

        // Nút hành động chính & rời/xóa nhóm
        setupHeaderButtons(g, me, myMembership, isOwner, isApproved);

        // Yêu cầu chờ duyệt (chỉ chủ nhóm mới thấy)
        List<GroupMember> pendings = repo.getMembersByStatus(g.id, GroupMember.STATUS_PENDING);
        if (isOwner && !pendings.isEmpty()) {
            cardPending.setVisibility(View.VISIBLE);
            tvPendingHeader.setText("Yêu cầu xin tham gia (" + pendings.size() + ")");
            containerPending.removeAllViews();
            int addedPending = 0;
            for (GroupMember pm : pendings) {
                User requester = repo.getUser(pm.userId);
                if (requester != null) {
                    if (addedPending > 0) containerPending.addView(buildDivider());
                    containerPending.addView(buildPendingRow(g, pm, requester, approvedUsers));
                    addedPending++;
                }
            }
        } else {
            cardPending.setVisibility(View.GONE);
        }

        // Đề xuất khung giờ học chung từ lịch rảnh của các thành viên
        chipSuggestedSlots.removeAllViews();
        boolean hideSingleOwnerSlots = approvedUsers.size() == 1
                && approvedUsers.get(0).hideSchedule
                && !me.id.equals(approvedUsers.get(0).id);
        List<SlotSuggestion> suggestions = hideSingleOwnerSlots
                ? Collections.emptyList()
                : MatchingEngine.suggestCommonSlots(approvedUsers, 60, 5);
        if (suggestions.isEmpty()) {
            tvNoSuggestedSlots.setVisibility(View.VISIBLE);
        } else {
            tvNoSuggestedSlots.setVisibility(View.GONE);
            for (SlotSuggestion slot : suggestions) {
                String label = Formatters.day(slot.dayOfWeek) + " "
                        + Formatters.timeRange(slot.startMinute, slot.endMinute)
                        + " (" + slot.count() + "/" + approvedUsers.size() + " rảnh)";
                if (isApproved) {
                    chipSuggestedSlots.addView(UiHelper.makeActionChip(this, label, v -> {
                        Intent it = new Intent(this, CreateSessionActivity.class);
                        it.putExtra(CreateSessionActivity.EXTRA_GROUP_ID, g.id);
                        it.putExtra(CreateSessionActivity.EXTRA_DAY_OF_WEEK, slot.dayOfWeek);
                        it.putExtra(CreateSessionActivity.EXTRA_START_MINUTE, slot.startMinute);
                        it.putExtra(CreateSessionActivity.EXTRA_DURATION, Math.min(120, slot.duration()));
                        startActivity(it);
                    }));
                } else {
                    chipSuggestedSlots.addView(UiHelper.makeDisplayChip(this, label));
                }
            }
        }

        // Buổi học
        btnAddSession.setVisibility(isApproved ? View.VISIBLE : View.GONE);
        btnAddSession.setOnClickListener(v -> {
            Intent it = new Intent(this, CreateSessionActivity.class);
            it.putExtra(CreateSessionActivity.EXTRA_GROUP_ID, g.id);
            startActivity(it);
        });
        List<StudySession> sessions = repo.getSessionsOfGroup(g.id);
        sessionAdapter.submit(sessions);
        tvNoSessions.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);

        // Danh sách thành viên chính thức (+ lời mời đang chờ nếu là chủ nhóm)
        containerMembers.removeAllViews();
        int rowCount = 0;
        for (GroupMember m : repo.getMembersByStatus(g.id, GroupMember.STATUS_APPROVED)) {
            User u = repo.getUser(m.userId);
            if (u != null) {
                if (rowCount > 0) containerMembers.addView(buildDivider());
                containerMembers.addView(buildMemberRow(m, u, isOwner));
                rowCount++;
            }
        }
        if (isOwner) {
            for (GroupMember inv : repo.getMembersByStatus(g.id, GroupMember.STATUS_INVITED)) {
                User u = repo.getUser(inv.userId);
                if (u != null) {
                    if (rowCount > 0) containerMembers.addView(buildDivider());
                    containerMembers.addView(buildInvitedRow(inv, u));
                    rowCount++;
                }
            }
        }
    }

    private void setupHeaderButtons(StudyGroup g, User me, GroupMember myMembership,
                                    boolean isOwner, boolean isApproved) {
        Repository repo = Repository.get();
        if (isOwner) {
            boolean full = repo.isFull(g);
            btnPrimary.setEnabled(!full);
            btnPrimary.setText(full ? "Nhóm đã đủ thành viên" : "Mời bạn phù hợp");
            btnPrimary.setOnClickListener(full ? null : v -> showInviteCandidatePicker(g, me));

            btnDeleteOrLeave.setVisibility(View.VISIBLE);
            btnDeleteOrLeave.setText("Giải tán nhóm");
            btnDeleteOrLeave.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Giải tán nhóm?")
                    .setMessage("Nhóm \"" + g.name + "\" và lịch buổi học của nhóm sẽ bị xóa.")
                    .setPositiveButton("Xóa nhóm", (d, w) -> {
                        repo.deleteGroup(g.id);
                        Toast.makeText(this, "Đã giải tán nhóm", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show());
        } else if (isApproved) {
            btnPrimary.setEnabled(true);
            btnPrimary.setText("+ Lên lịch buổi học");
            btnPrimary.setOnClickListener(v -> {
                Intent it = new Intent(this, CreateSessionActivity.class);
                it.putExtra(CreateSessionActivity.EXTRA_GROUP_ID, g.id);
                startActivity(it);
            });

            btnDeleteOrLeave.setVisibility(View.VISIBLE);
            btnDeleteOrLeave.setText("Rời nhóm");
            btnDeleteOrLeave.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Rời khỏi nhóm?")
                    .setMessage("Bạn muốn rời khỏi nhóm \"" + g.name + "\"?")
                    .setPositiveButton("Rời nhóm", (d, w) -> {
                        repo.leaveGroup(g.id, me.id);
                        finish();
                    })
                    .setNegativeButton("Ở lại", null)
                    .show());
        } else if (myMembership != null && myMembership.isInvited()) {
            btnPrimary.setEnabled(true);
            btnPrimary.setText("Chấp nhận lời mời");
            btnPrimary.setOnClickListener(v -> {
                try {
                    repo.acceptInvite(g.id, me.id);
                    bindGroup();
                } catch (RuntimeException ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            btnDeleteOrLeave.setVisibility(View.VISIBLE);
            btnDeleteOrLeave.setText("Từ chối");
            btnDeleteOrLeave.setOnClickListener(v -> {
                repo.declineInvite(g.id, me.id);
                bindGroup();
            });
        } else if (myMembership != null && myMembership.isPending()) {
            btnPrimary.setEnabled(true);
            btnPrimary.setText("Đang chờ duyệt (chạm để hủy)");
            btnPrimary.setOnClickListener(v -> {
                repo.cancelRequest(g.id, me.id);
                bindGroup();
            });
            btnDeleteOrLeave.setVisibility(View.GONE);
        } else {
            boolean full = repo.isFull(g);
            btnPrimary.setEnabled(!full);
            btnPrimary.setText(full ? "Nhóm đã đủ thành viên" : "Gửi yêu cầu tham gia");
            btnPrimary.setOnClickListener(v -> {
                try {
                    repo.requestJoin(g.id, me.id);
                    Toast.makeText(this, "Đã gửi yêu cầu tham gia!", Toast.LENGTH_SHORT).show();
                    bindGroup();
                } catch (RuntimeException ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            btnDeleteOrLeave.setVisibility(View.GONE);
        }
    }

    private void showInviteCandidatePicker(StudyGroup g, User me) {
        Repository repo = Repository.get();
        if (repo.isFull(g)) {
            Toast.makeText(this, "Nhóm đã đủ thành viên.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<User> approvedUsers = repo.getApprovedUsers(g.id);
        MatchFilter f = MatchFilter.defaults();
        f.subjectId = g.subjectId;
        f.requireEligible = false;
        List<MatchResult> ranked = MatchingEngine.rank(me, repo.getAllUsers(), f);

        List<MatchResult> candidates = new ArrayList<>();
        for (MatchResult r : ranked) {
            if (repo.getMembership(g.id, r.user.id) == null) {
                r.total = MatchingEngine.groupScore(r.user, approvedUsers, g.subjectId);
                candidates.add(r);
            }
        }
        Collections.sort(candidates, (a, b) -> {
            if (a.total != b.total) return Integer.compare(b.total, a.total);
            if (a.overlapMinutes != b.overlapMinutes) return Integer.compare(b.overlapMinutes, a.overlapMinutes);
            String na = a.user.name == null ? "" : a.user.name;
            String nb = b.user.name == null ? "" : b.user.name;
            return na.compareTo(nb);
        });
        if (candidates.isEmpty()) {
            Toast.makeText(this, "Không còn sinh viên nào cùng môn để mời.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            MatchResult r = candidates.get(i);
            labels[i] = r.user.name + " • Phù hợp nhóm " + r.total + "% ("
                    + Formatters.duration(r.overlapMinutes) + " trùng lịch)";
        }
        new AlertDialog.Builder(this)
                .setTitle("Mời bạn cùng môn " + Catalog.subjectName(g.subjectId))
                .setItems(labels, (d, which) -> {
                    User target = candidates.get(which).user;
                    try {
                        repo.invite(g.id, target.id);
                        GroupMember updated = repo.getMembership(g.id, target.id);
                        if (updated != null && updated.isApproved()) {
                            Toast.makeText(this, "Đã duyệt " + target.name + " vào nhóm!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Đã mời " + target.name + "!", Toast.LENGTH_SHORT).show();
                        }
                        bindGroup();
                    } catch (RuntimeException ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private View buildPendingRow(StudyGroup g, GroupMember pm, User requester, List<User> members) {
        int fit = MatchingEngine.groupScore(requester, members, g.subjectId);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(this);
        name.setText(requester.name + " • Phù hợp " + fit + "%");
        name.setTypeface(null, Typeface.BOLD);
        name.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        info.addView(name);

        TextView meta = new TextView(this);
        meta.setText(Catalog.levelLabel(requester.level) + " • "
                + (requester.goals.isEmpty() ? "Chưa rõ mục tiêu" : Formatters.join(requester.goals, ", ")));
        meta.setTextSize(12f);
        meta.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        info.addView(meta);
        info.setOnClickListener(v -> {
            Intent it = new Intent(this, UserDetailActivity.class);
            it.putExtra(UserDetailActivity.EXTRA_USER_ID, requester.id);
            it.putExtra(UserDetailActivity.EXTRA_SUBJECT_ID, g.subjectId);
            startActivity(it);
        });

        MaterialButton btnReject = UiHelper.makeTextButton(this, "Từ chối", R.color.danger);
        btnReject.setOnClickListener(v -> {
            Repository.get().reject(pm.id);
            bindGroup();
        });

        MaterialButton btnApprove = new MaterialButton(this);
        btnApprove.setText("Duyệt");
        btnApprove.setAllCaps(false);
        btnApprove.setOnClickListener(v -> {
            try {
                Repository.get().approve(pm.id);
                Toast.makeText(this, "Đã duyệt " + requester.name + " vào nhóm!", Toast.LENGTH_SHORT).show();
                bindGroup();
            } catch (RuntimeException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        row.addView(info);
        row.addView(btnReject);
        row.addView(btnApprove);
        return row;
    }

    private View buildMemberRow(GroupMember m, User u, boolean viewerIsOwner) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(8), dp(4), dp(8));

        TextView avatar = new TextView(this);
        LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(dp(38), dp(38));
        avatar.setLayoutParams(avLp);
        avatar.setBackgroundResource(R.drawable.bg_avatar_circle);
        avatar.setGravity(Gravity.CENTER);
        avatar.setTypeface(null, Typeface.BOLD);
        avatar.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        avatar.setText(UiHelper.avatarInitial(u.name));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        infoLp.leftMargin = dp(10);
        info.setLayoutParams(infoLp);

        TextView tvName = new TextView(this);
        tvName.setText(u.name + (m.isOwner() ? " (Chủ nhóm)" : ""));
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        info.addView(tvName);

        TextView tvSub = new TextView(this);
        tvSub.setText((u.major == null || u.major.isEmpty() ? "Sinh viên" : u.major)
                + " • " + Catalog.levelLabel(u.level));
        tvSub.setTextSize(12f);
        tvSub.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        info.addView(tvSub);

        row.addView(avatar);
        row.addView(info);

        User me = Repository.get().getCurrentUser();
        if (me != null && !me.id.equals(u.id)) {
            row.setOnClickListener(v -> {
                Intent it = new Intent(this, UserDetailActivity.class);
                it.putExtra(UserDetailActivity.EXTRA_USER_ID, u.id);
                startActivity(it);
            });
        }

        if (viewerIsOwner && !m.isOwner()) {
            MaterialButton btnRemove = UiHelper.makeTextButton(this, "Xóa", R.color.danger);
            btnRemove.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Xóa thành viên?")
                    .setMessage("Mời " + u.name + " ra khỏi nhóm?")
                    .setPositiveButton("Xóa", (d, w) -> {
                        Repository.get().removeMember(m.id);
                        bindGroup();
                    })
                    .setNegativeButton("Hủy", null)
                    .show());
            row.addView(btnRemove);
        }
        return row;
    }

    private View buildInvitedRow(GroupMember inv, User u) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(8), dp(4), dp(8));

        TextView avatar = new TextView(this);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(38), dp(38)));
        avatar.setBackgroundResource(R.drawable.bg_avatar_circle);
        avatar.setGravity(Gravity.CENTER);
        avatar.setTypeface(null, Typeface.BOLD);
        avatar.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        avatar.setText(UiHelper.avatarInitial(u.name));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        infoLp.leftMargin = dp(10);
        info.setLayoutParams(infoLp);

        TextView tvName = new TextView(this);
        tvName.setText(u.name + " (Đã mời)");
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        info.addView(tvName);

        TextView tvSub = new TextView(this);
        tvSub.setText("Đang chờ sinh viên xác nhận lời mời");
        tvSub.setTextSize(12f);
        tvSub.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        info.addView(tvSub);

        MaterialButton btnCancel = UiHelper.makeTextButton(this, "Hủy mời", R.color.text_secondary);
        btnCancel.setOnClickListener(v -> {
            Repository.get().reject(inv.id);
            Toast.makeText(this, "Đã hủy lời mời gửi tới " + u.name, Toast.LENGTH_SHORT).show();
            bindGroup();
        });

        row.addView(avatar);
        row.addView(info);
        row.addView(btnCancel);
        return row;
    }

    private View buildDivider() {
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, dp(1))));
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider));
        return divider;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
