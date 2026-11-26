package com.studymatch.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.matching.MatchResult;
import com.studymatch.model.TimeSlot;
import com.studymatch.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/** Hiển thị danh sách bạn học kèm điểm phù hợp của thuật toán MatchingEngine. */
public class PartnerMatchAdapter extends RecyclerView.Adapter<PartnerMatchAdapter.VH> {

    private final List<MatchResult> items = new ArrayList<>();
    private String filterSubjectId;

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<MatchResult> data, String filterSubjectId) {
        this.items.clear();
        if (data != null) this.items.addAll(data);
        this.filterSubjectId = filterSubjectId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_partner_match, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MatchResult r = items.get(position);
        Context ctx = h.itemView.getContext();

        h.tvAvatar.setText(UiHelper.avatarInitial(r.user.name));
        h.tvName.setText(r.user.name);

        String major = (r.user.major == null || r.user.major.isEmpty()) ? "Sinh viên" : r.user.major;
        h.tvMeta.setText(major + " • Trình độ: " + Catalog.levelLabel(r.user.level));

        UiHelper.bindScoreBadge(ctx, h.tvScoreBadge, r.total, r.total + "% • " + r.label());

        if (r.commonSubjectIds.isEmpty()) {
            String otherSubs = r.user.subjectIds == null || r.user.subjectIds.isEmpty()
                    ? "Chưa khai báo"
                    : Formatters.join(Catalog.subjectNames(r.user.subjectIds), ", ");
            h.tvCommonSubjects.setText("Môn học: " + otherSubs);
        } else {
            h.tvCommonSubjects.setText("Cùng môn: " + Formatters.join(Catalog.subjectNames(r.commonSubjectIds), ", "));
        }

        if (r.overlapMinutes <= 0) {
            h.tvOverlapInfo.setText("Chưa có khung giờ rảnh trùng nhau");
        } else if (r.user.hideSchedule) {
            h.tvOverlapInfo.setText("Trùng " + Formatters.duration(r.overlapMinutes)
                    + "/tuần (sinh viên ẩn chi tiết giờ rảnh)");
        } else {
            List<String> slotLabels = new ArrayList<>();
            for (int i = 0; i < Math.min(2, r.overlapSlots.size()); i++) {
                TimeSlot s = r.overlapSlots.get(i);
                slotLabels.add(Formatters.slot(s));
            }
            String extra = r.overlapSlots.size() > 2 ? " (+" + (r.overlapSlots.size() - 2) + ")" : "";
            h.tvOverlapInfo.setText("Trùng lịch: " + Formatters.join(slotLabels, " • ") + extra);
        }

        h.tvBreakdown.setText("Điểm: Môn " + r.subjectScore + "/40 • Lịch " + r.timeScore
                + "/30 • Mục tiêu " + r.goalScore + "/20 • Trình độ " + r.levelScore + "/10");

        View.OnClickListener openDetail = v -> {
            Intent it = new Intent(ctx, UserDetailActivity.class);
            it.putExtra(UserDetailActivity.EXTRA_USER_ID, r.user.id);
            it.putExtra(UserDetailActivity.EXTRA_SUBJECT_ID, filterSubjectId);
            ctx.startActivity(it);
        };
        h.itemView.setOnClickListener(openDetail);
        h.btnDetail.setOnClickListener(openDetail);
        h.btnInvite.setOnClickListener(v -> UiHelper.showInviteToGroupDialog(ctx, r.user));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvAvatar;
        final TextView tvName;
        final TextView tvMeta;
        final TextView tvScoreBadge;
        final TextView tvCommonSubjects;
        final TextView tvOverlapInfo;
        final TextView tvBreakdown;
        final MaterialButton btnDetail;
        final MaterialButton btnInvite;

        VH(@NonNull View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tvItemAvatar);
            tvName = v.findViewById(R.id.tvItemName);
            tvMeta = v.findViewById(R.id.tvItemMeta);
            tvScoreBadge = v.findViewById(R.id.tvItemScoreBadge);
            tvCommonSubjects = v.findViewById(R.id.tvItemCommonSubjects);
            tvOverlapInfo = v.findViewById(R.id.tvItemOverlapInfo);
            tvBreakdown = v.findViewById(R.id.tvItemScoreBreakdown);
            btnDetail = v.findViewById(R.id.btnItemDetail);
            btnInvite = v.findViewById(R.id.btnItemInvite);
        }
    }
}
