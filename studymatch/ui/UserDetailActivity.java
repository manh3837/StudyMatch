package com.studymatch.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.Repository;
import com.studymatch.matching.MatchResult;
import com.studymatch.matching.MatchingEngine;
import com.studymatch.model.TimeSlot;
import com.studymatch.model.User;
import com.studymatch.util.Formatters;

/** Chi tiết hồ sơ bạn học và bảng giải thích điểm phù hợp của MatchingEngine. */
public class UserDetailActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_SUBJECT_ID = "subject_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Repository repo = Repository.get();
        User me = repo.getCurrentUser();
        User other = repo.getUser(getIntent().getStringExtra(EXTRA_USER_ID));
        if (me == null || other == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_user_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hồ sơ bạn học");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String filterSub = getIntent().getStringExtra(EXTRA_SUBJECT_ID);
        MatchResult r = MatchingEngine.score(me, other, filterSub);

        ((TextView) findViewById(R.id.tvDetailAvatar)).setText(UiHelper.avatarInitial(other.name));
        ((TextView) findViewById(R.id.tvDetailName)).setText(other.name);
        String major = (other.major == null || other.major.isEmpty()) ? "Sinh viên" : other.major;
        ((TextView) findViewById(R.id.tvDetailMajor))
                .setText(major + " • Trình độ: " + Catalog.levelLabel(other.level));
        ((TextView) findViewById(R.id.tvDetailBio)).setText(
                (other.bio == null || other.bio.isEmpty()) ? "Chưa có giới thiệu." : other.bio);

        TextView tvTotal = findViewById(R.id.tvDetailTotalScore);
        UiHelper.bindScoreBadge(this, tvTotal, r.total, r.total + "% • " + r.label());

        ((TextView) findViewById(R.id.tvScoreSubject)).setText(
                "1. Cùng môn học: " + r.subjectScore + "/40 điểm"
                        + (r.commonSubjectIds.isEmpty() ? " (chưa trùng môn)"
                        : " (" + Formatters.join(Catalog.subjectNames(r.commonSubjectIds), ", ") + ")"));

        ((TextView) findViewById(R.id.tvScoreTime)).setText(
                "2. Thời gian rảnh trùng: " + r.timeScore + "/30 điểm ("
                        + Formatters.duration(r.overlapMinutes) + "/tuần, tối đa ở 2 giờ)");

        ((TextView) findViewById(R.id.tvScoreGoal)).setText(
                "3. Cùng mục tiêu học tập: " + r.goalScore + "/20 điểm"
                        + (r.commonGoals.isEmpty() ? " (khác mục tiêu)"
                        : " (" + Formatters.join(r.commonGoals, ", ") + ")"));

        ((TextView) findViewById(R.id.tvScoreLevel)).setText(
                "4. Trình độ học tập: " + r.levelScore + "/10 điểm (Bạn: "
                        + Catalog.levelLabel(me.level) + " – Bạn ấy: " + Catalog.levelLabel(other.level) + ")");

        String otherSubs = other.subjectIds == null || other.subjectIds.isEmpty()
                ? "Chưa khai báo"
                : Formatters.join(Catalog.subjectNames(other.subjectIds), ", ");
        ((TextView) findViewById(R.id.tvCommonSubjects)).setText("Môn đang học: " + otherSubs);
        ((TextView) findViewById(R.id.tvCommonGoals)).setText(
                "Mục tiêu: " + (other.goals.isEmpty() ? "Chưa khai báo" : Formatters.join(other.goals, ", ")));

        TextView tvOverlapSummary = findViewById(R.id.tvOverlapSummary);
        ChipGroup chipOverlap = findViewById(R.id.chipGroupOverlapSlots);
        chipOverlap.removeAllViews();
        if (r.overlapMinutes <= 0) {
            tvOverlapSummary.setText("Hai bạn chưa có khung giờ rảnh nào trùng nhau.");
        } else if (other.hideSchedule) {
            tvOverlapSummary.setText("Tổng thời gian rảnh trùng: "
                    + Formatters.duration(r.overlapMinutes)
                    + "/tuần. Sinh viên này bật chế độ ẩn chi tiết khung giờ rảnh.");
        } else {
            tvOverlapSummary.setText("Tổng thời gian rảnh trùng: "
                    + Formatters.duration(r.overlapMinutes) + "/tuần:");
            for (TimeSlot s : r.overlapSlots) {
                chipOverlap.addView(UiHelper.makeDisplayChip(this, Formatters.slot(s)));
            }
        }

        View btnInvite = findViewById(R.id.btnInviteToGroup);
        if (me.id.equals(other.id)) {
            btnInvite.setVisibility(View.GONE);
        } else {
            btnInvite.setVisibility(View.VISIBLE);
            btnInvite.setOnClickListener(v -> UiHelper.showInviteToGroupDialog(this, other));
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
}
