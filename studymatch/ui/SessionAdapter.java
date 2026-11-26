package com.studymatch.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import com.studymatch.data.Repository;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.StudySession;
import com.studymatch.model.User;
import com.studymatch.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Hiển thị danh sách buổi học nhóm kèm đánh giá sau buổi học. */
public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.VH> {

    private final boolean insideGroupDetail;
    private final Runnable onChanged;
    private final List<StudySession> items = new ArrayList<>();

    public SessionAdapter(boolean insideGroupDetail, Runnable onChanged) {
        this.insideGroupDetail = insideGroupDetail;
        this.onChanged = onChanged;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<StudySession> sessions) {
        items.clear();
        if (sessions != null) items.addAll(sessions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        StudySession s = items.get(position);
        Context ctx = h.itemView.getContext();
        Repository repo = Repository.get();
        User me = repo.getCurrentUser();
        StudyGroup g = repo.getGroup(s.groupId);
        long now = System.currentTimeMillis();

        h.tvDayOfWeek.setText(Formatters.dayShort(Formatters.dayOfWeek(s.startTime)));
        h.tvDayMonth.setText(Formatters.dayMonth(s.startTime));
        h.tvGroupName.setText(g != null ? g.name : "Nhóm học");
        h.tvTitle.setText(s.title);
        h.tvTimeLocation.setText(Formatters.sessionTime(s.startTime, s.durationMinutes)
                + " • " + s.location);
        if (s.note != null && !s.note.trim().isEmpty()) {
            h.tvNote.setVisibility(View.VISIBLE);
            h.tvNote.setText("Ghi chú: " + s.note.trim());
        } else {
            h.tvNote.setVisibility(View.GONE);
        }

        float avg = repo.averageRating(s.id);
        int fbCount = repo.getFeedbacks(s.id).size();
        if (fbCount > 0) {
            h.tvRatingInfo.setVisibility(View.VISIBLE);
            h.tvRatingInfo.setText(String.format(Locale.US, "Đánh giá: %.1f★ (%d phản hồi)", avg, fbCount));
        } else if (s.isFinished(now)) {
            h.tvRatingInfo.setVisibility(View.VISIBLE);
            h.tvRatingInfo.setText("Đã kết thúc • Chưa có đánh giá");
        } else if (s.hasStarted(now)) {
            h.tvRatingInfo.setVisibility(View.VISIBLE);
            h.tvRatingInfo.setText("Đang diễn ra");
        } else {
            h.tvRatingInfo.setVisibility(View.GONE);
        }

        boolean isMember = me != null && repo.getMembership(s.groupId, me.id) != null
                && repo.getMembership(s.groupId, me.id).isApproved();
        boolean canDelete = me != null && (me.id.equals(s.createdBy)
                || (g != null && me.id.equals(g.ownerId)));

        if (s.isFinished(now) && isMember) {
            h.btnAction.setVisibility(View.VISIBLE);
            boolean ratedByMe = repo.getFeedback(s.id, me.id) != null;
            h.btnAction.setText(ratedByMe ? "Sửa ĐG" : "Đánh giá");
            h.btnAction.setOnClickListener(v -> UiHelper.showRateSessionDialog(ctx, s, onChanged));
        } else if (insideGroupDetail && canDelete) {
            h.btnAction.setVisibility(View.VISIBLE);
            h.btnAction.setText("Hủy");
            h.btnAction.setOnClickListener(v -> new AlertDialog.Builder(ctx)
                    .setTitle("Hủy buổi học?")
                    .setMessage("Bạn có chắc muốn xóa buổi học \"" + s.title + "\"?")
                    .setPositiveButton("Xóa", (d, w) -> {
                        repo.deleteSession(s.id);
                        Toast.makeText(ctx, "Đã xóa buổi học", Toast.LENGTH_SHORT).show();
                        if (onChanged != null) onChanged.run();
                    })
                    .setNegativeButton("Đóng", null)
                    .show());
        } else if (!insideGroupDetail) {
            h.btnAction.setVisibility(View.VISIBLE);
            h.btnAction.setText("Nhóm");
            h.btnAction.setOnClickListener(v -> openGroup(ctx, s.groupId));
        } else {
            h.btnAction.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            if (s.isFinished(now) && isMember) {
                UiHelper.showRateSessionDialog(ctx, s, onChanged);
            } else if (!insideGroupDetail) {
                openGroup(ctx, s.groupId);
            }
        });
    }

    private void openGroup(Context ctx, String groupId) {
        Intent it = new Intent(ctx, GroupDetailActivity.class);
        it.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, groupId);
        ctx.startActivity(it);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvDayOfWeek;
        final TextView tvDayMonth;
        final TextView tvGroupName;
        final TextView tvTitle;
        final TextView tvTimeLocation;
        final TextView tvNote;
        final TextView tvRatingInfo;
        final MaterialButton btnAction;

        VH(@NonNull View v) {
            super(v);
            tvDayOfWeek = v.findViewById(R.id.tvSessionDayOfWeek);
            tvDayMonth = v.findViewById(R.id.tvSessionDayMonth);
            tvGroupName = v.findViewById(R.id.tvSessionGroupName);
            tvTitle = v.findViewById(R.id.tvSessionTitle);
            tvTimeLocation = v.findViewById(R.id.tvSessionTimeLocation);
            tvNote = v.findViewById(R.id.tvSessionNote);
            tvRatingInfo = v.findViewById(R.id.tvSessionRatingInfo);
            btnAction = v.findViewById(R.id.btnSessionAction);
        }
    }
}
