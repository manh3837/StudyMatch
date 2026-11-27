package com.studymatch.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.Repository;
import com.studymatch.matching.MatchingEngine;
import com.studymatch.model.GroupMember;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.User;

import java.util.ArrayList;
import java.util.List;

/** Hiển thị danh sách nhóm học (Nhóm của tôi hoặc Khám phá nhóm). */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.VH> {

    private final boolean myGroupsMode;
    private final Runnable onDataChanged;
    private final List<StudyGroup> items = new ArrayList<>();

    public GroupAdapter(boolean myGroupsMode, Runnable onDataChanged) {
        this.myGroupsMode = myGroupsMode;
        this.onDataChanged = onDataChanged;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<StudyGroup> groups) {
        items.clear();
        if (groups != null) items.addAll(groups);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        StudyGroup g = items.get(position);
        Context ctx = h.itemView.getContext();
        Repository repo = Repository.get();
        User me = repo.getCurrentUser();

        h.tvName.setText(g.name);
        String goalText = (g.goal == null || g.goal.isEmpty()) ? "" : " • " + g.goal;
        h.tvSubjectGoal.setText(Catalog.subjectName(g.subjectId) + goalText);
        h.tvDesc.setText(g.description == null || g.description.isEmpty()
                ? "Chưa có mô tả chi tiết." : g.description);

        int count = repo.countApproved(g.id);
        User owner = repo.getUser(g.ownerId);
        String ownerName = owner != null ? owner.name : "Chủ nhóm";

        View.OnClickListener openDetail = v -> {
            Intent it = new Intent(ctx, GroupDetailActivity.class);
            it.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, g.id);
            ctx.startActivity(it);
        };
        h.itemView.setOnClickListener(openDetail);

        if (myGroupsMode) {
            boolean isOwner = me != null && me.id.equals(g.ownerId);
            int pendingCount = isOwner
                    ? repo.getMembersByStatus(g.id, GroupMember.STATUS_PENDING).size() : 0;
            if (pendingCount > 0) {
                UiHelper.bindScoreBadge(ctx, h.tvBadge, 50, "Chờ duyệt: " + pendingCount);
            } else {
                UiHelper.bindScoreBadge(ctx, h.tvBadge, 70, count + "/" + g.maxMembers + " TV");
            }
            h.tvStatus.setText((isOwner ? "Bạn là chủ nhóm" : "Chủ nhóm: " + ownerName)
                    + " • " + count + "/" + g.maxMembers + " thành viên");
            h.btnAction.setEnabled(true);
            h.btnAction.setText(isOwner ? "Quản lý" : "Mở nhóm");
            h.btnAction.setOnClickListener(openDetail);
        } else {
            List<User> members = repo.getApprovedUsers(g.id);
            int fit = me == null ? 0 : MatchingEngine.groupScore(me, members, g.subjectId);
            UiHelper.bindScoreBadge(ctx, h.tvBadge, fit, "Phù hợp " + fit + "%");

            boolean full = count >= g.maxMembers;
            h.tvStatus.setText("Chủ nhóm: " + ownerName + " • " + count + "/" + g.maxMembers
                    + (full ? " (Đã đủ)" : " (Còn chỗ)"));

            GroupMember m = me == null ? null : repo.getMembership(g.id, me.id);
            if (m != null && m.isPending()) {
                h.btnAction.setEnabled(true);
                h.btnAction.setText("Hủy yêu cầu");
                h.btnAction.setOnClickListener(v -> {
                    repo.cancelRequest(g.id, me.id);
                    Toast.makeText(ctx, "Đã hủy yêu cầu tham gia", Toast.LENGTH_SHORT).show();
                    if (onDataChanged != null) onDataChanged.run();
                });
            } else if (full) {
                h.btnAction.setEnabled(false);
                h.btnAction.setText("Đã đủ TV");
                h.btnAction.setOnClickListener(null);
            } else {
                h.btnAction.setEnabled(true);
                h.btnAction.setText("Xin tham gia");
                h.btnAction.setOnClickListener(v -> {
                    if (me == null) return;
                    try {
                        repo.requestJoin(g.id, me.id);
                        Toast.makeText(ctx, "Đã gửi yêu cầu tới chủ nhóm!", Toast.LENGTH_SHORT).show();
                        if (onDataChanged != null) onDataChanged.run();
                    } catch (RuntimeException ex) {
                        Toast.makeText(ctx, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvBadge;
        final TextView tvSubjectGoal;
        final TextView tvDesc;
        final TextView tvStatus;
        final MaterialButton btnAction;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvGroupItemName);
            tvBadge = v.findViewById(R.id.tvGroupItemBadge);
            tvSubjectGoal = v.findViewById(R.id.tvGroupItemSubjectGoal);
            tvDesc = v.findViewById(R.id.tvGroupItemDesc);
            tvStatus = v.findViewById(R.id.tvGroupItemStatus);
            btnAction = v.findViewById(R.id.btnGroupItemAction);
        }
    }
}
