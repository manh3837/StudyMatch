package com.studymatch.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.Repository;
import com.studymatch.model.GroupMember;
import com.studymatch.model.SessionFeedback;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.StudySession;
import com.studymatch.model.User;

import java.util.List;

/** Tiện ích dùng chung cho các màn hình giao diện. */
public final class UiHelper {

    private UiHelper() {
    }

    public static String avatarInitial(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        String last = parts[parts.length - 1];
        int cp = last.codePointAt(0);
        return new String(Character.toChars(Character.toUpperCase(cp)));
    }

    /** Đổi màu huy hiệu điểm số theo ngưỡng (≥80 xanh lá, ≥60 xanh dương, ≥40 vàng, <40 xám). */
    public static void bindScoreBadge(Context ctx, TextView tv, int score, String text) {
        int bgColor;
        int textColor;
        if (score >= 80) {
            bgColor = ContextCompat.getColor(ctx, R.color.score_high_bg);
            textColor = ContextCompat.getColor(ctx, R.color.score_high_text);
        } else if (score >= 60) {
            bgColor = ContextCompat.getColor(ctx, R.color.score_medium_bg);
            textColor = ContextCompat.getColor(ctx, R.color.score_medium_text);
        } else if (score >= 40) {
            bgColor = ContextCompat.getColor(ctx, R.color.score_fair_bg);
            textColor = ContextCompat.getColor(ctx, R.color.score_fair_text);
        } else {
            bgColor = ContextCompat.getColor(ctx, R.color.score_low_bg);
            textColor = ContextCompat.getColor(ctx, R.color.score_low_text);
        }
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(999f);
        shape.setColor(bgColor);
        tv.setBackground(shape);
        tv.setTextColor(textColor);
        tv.setText(text);
    }

    public static Chip makeSelectableChip(Context ctx, String label, Object tag, boolean checked) {
        Chip chip = new Chip(ctx);
        chip.setId(View.generateViewId());
        chip.setText(label);
        chip.setTag(tag);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setCheckedIconVisible(true);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        int[] bgColors = new int[]{
                ContextCompat.getColor(ctx, R.color.primary_light),
                ContextCompat.getColor(ctx, R.color.bg_subtle)
        };
        int[] textColors = new int[]{
                ContextCompat.getColor(ctx, R.color.primary_dark),
                ContextCompat.getColor(ctx, R.color.text_primary)
        };
        chip.setChipBackgroundColor(new ColorStateList(states, bgColors));
        chip.setTextColor(new ColorStateList(states, textColors));
        chip.setChecked(checked);
        return chip;
    }

    public static Chip makeDisplayChip(Context ctx, String label) {
        Chip chip = new Chip(ctx);
        chip.setText(label);
        chip.setCheckable(false);
        chip.setClickable(false);
        chip.setChipBackgroundColorResource(R.color.bg_subtle);
        chip.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary));
        return chip;
    }

    public static Chip makeActionChip(Context ctx, String label, View.OnClickListener listener) {
        Chip chip = new Chip(ctx);
        chip.setText(label);
        chip.setCheckable(false);
        chip.setClickable(true);
        chip.setChipBackgroundColorResource(R.color.primary_light);
        chip.setTextColor(ContextCompat.getColor(ctx, R.color.primary_dark));
        chip.setOnClickListener(listener);
        return chip;
    }

    public static MaterialButton makeTextButton(Context ctx, String label, int textColorRes) {
        MaterialButton btn = new MaterialButton(ctx);
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setElevation(0f);
        btn.setStateListAnimator(null);
        btn.setAllCaps(false);
        btn.setText(label);
        btn.setTextColor(ContextCompat.getColor(ctx, textColorRes));
        return btn;
    }

    /** Hộp thoại chọn nhóm do người dùng hiện tại làm chủ để mời bạn học. */
    public static void showInviteToGroupDialog(Context ctx, User target) {
        User me = Repository.get().getCurrentUser();
        if (me == null || target == null) return;
        List<StudyGroup> owned = Repository.get().getOwnedGroups(me.id);
        if (owned.isEmpty()) {
            new AlertDialog.Builder(ctx)
                    .setTitle("Chưa có nhóm để mời")
                    .setMessage("Bạn cần tạo một nhóm học (với vai trò chủ nhóm) trước khi mời " + target.name + ".")
                    .setPositiveButton("Tạo nhóm ngay", (d, w) ->
                            ctx.startActivity(new Intent(ctx, CreateGroupActivity.class)))
                    .setNegativeButton("Đóng", null)
                    .show();
            return;
        }

        Repository repo = Repository.get();
        String[] items = new String[owned.size()];
        for (int i = 0; i < owned.size(); i++) {
            StudyGroup g = owned.get(i);
            int c = repo.countApproved(g.id);
            GroupMember existing = repo.getMembership(g.id, target.id);
            String state = "";
            if (existing != null) {
                if (existing.isApproved()) state = " • Đã là TV";
                else if (existing.isInvited()) state = " • Đã mời";
                else if (existing.isPending()) state = " • Đang xin vào";
            } else if (c >= g.maxMembers) {
                state = " • Đã đủ TV";
            }
            items[i] = g.name + " (" + Catalog.subjectName(g.subjectId) + " • " + c + "/" + g.maxMembers + state + ")";
        }

        new AlertDialog.Builder(ctx)
                .setTitle("Mời " + target.name + " vào nhóm")
                .setItems(items, (d, which) -> {
                    StudyGroup chosen = owned.get(which);
                    try {
                        repo.invite(chosen.id, target.id);
                        GroupMember updated = repo.getMembership(chosen.id, target.id);
                        if (updated != null && updated.isApproved()) {
                            Toast.makeText(ctx, "Đã duyệt " + target.name + " vào nhóm \"" + chosen.name + "\"!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ctx, "Đã gửi lời mời vào nhóm \"" + chosen.name + "\"", Toast.LENGTH_SHORT).show();
                        }
                        if (ctx instanceof MainActivity) {
                            ((MainActivity) ctx).refreshNotificationBadge();
                        }
                    } catch (RuntimeException ex) {
                        Toast.makeText(ctx, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /** Hộp thoại đánh giá buổi học đã kết thúc (1–5 sao + nhận xét). */
    public static void showRateSessionDialog(Context ctx, StudySession session, Runnable onSaved) {
        User me = Repository.get().getCurrentUser();
        if (me == null || session == null) return;

        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_rate_session, null);
        RatingBar ratingBar = view.findViewById(R.id.ratingBarSession);
        TextInputEditText etComment = view.findViewById(R.id.etFeedbackComment);
        TextView tvExisting = view.findViewById(R.id.tvExistingFeedbacks);

        SessionFeedback mine = Repository.get().getFeedback(session.id, me.id);
        if (mine != null) {
            ratingBar.setRating(mine.rating);
            etComment.setText(mine.comment);
        }

        List<SessionFeedback> all = Repository.get().getFeedbacks(session.id);
        if (all.isEmpty()) {
            tvExisting.setText("Chưa có đánh giá nào từ thành viên nhóm.");
        } else {
            StringBuilder sb = new StringBuilder("Phản hồi của các thành viên:\n");
            for (SessionFeedback f : all) {
                User u = Repository.get().getUser(f.userId);
                String uName = u != null ? u.name : "Thành viên";
                sb.append("• ").append(uName).append(" (").append(f.rating).append("★)");
                if (f.comment != null && !f.comment.isEmpty()) {
                    sb.append(": ").append(f.comment);
                }
                sb.append("\n");
            }
            tvExisting.setText(sb.toString().trim());
        }

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle("Đánh giá: " + session.title)
                .setView(view)
                .setPositiveButton("Lưu đánh giá", null)
                .setNegativeButton("Đóng", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int stars = Math.round(ratingBar.getRating());
            String comment = etComment.getText() == null ? "" : etComment.getText().toString();
            try {
                Repository.get().rateSession(session.id, me.id, stars, comment);
                Toast.makeText(ctx, "Cảm ơn bạn đã đánh giá buổi học!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                if (onSaved != null) onSaved.run();
            } catch (RuntimeException ex) {
                Toast.makeText(ctx, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
