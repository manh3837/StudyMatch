package com.studymatch.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.ChipGroup;
import com.studymatch.R;
import com.studymatch.data.Catalog;
import com.studymatch.data.Repository;
import com.studymatch.matching.MatchingEngine;
import com.studymatch.model.TimeSlot;
import com.studymatch.model.User;
import com.studymatch.util.Formatters;

import java.util.List;

/** Màn hình 6a: Hồ sơ cá nhân, quyền riêng tư và khôi phục dữ liệu mẫu. */
public class ProfileFragment extends Fragment {

    private TextView tvAvatar;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvMajor;
    private TextView tvBio;
    private ChipGroup chipSubjects;
    private TextView tvNoSubjects;
    private ChipGroup chipGoals;
    private TextView tvNoGoals;
    private TextView tvPrivacy;
    private ChipGroup chipSlots;
    private TextView tvNoSlots;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        tvAvatar = v.findViewById(R.id.tvAvatarInitial);
        tvName = v.findViewById(R.id.tvProfileName);
        tvEmail = v.findViewById(R.id.tvProfileEmail);
        tvMajor = v.findViewById(R.id.tvProfileMajor);
        tvBio = v.findViewById(R.id.tvProfileBio);
        chipSubjects = v.findViewById(R.id.chipGroupProfileSubjects);
        tvNoSubjects = v.findViewById(R.id.tvProfileNoSubjects);
        chipGoals = v.findViewById(R.id.chipGroupProfileGoals);
        tvNoGoals = v.findViewById(R.id.tvProfileNoGoals);
        tvPrivacy = v.findViewById(R.id.tvPrivacyStatus);
        chipSlots = v.findViewById(R.id.chipGroupProfileSlots);
        tvNoSlots = v.findViewById(R.id.tvProfileNoSlots);

        v.findViewById(R.id.btnEditProfile).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        v.findViewById(R.id.btnResetDemo).setOnClickListener(x ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Khôi phục dữ liệu mẫu?")
                        .setMessage("Mọi thay đổi bạn đã thử nghiệm sẽ được đặt lại về dữ liệu demo ban đầu.")
                        .setPositiveButton("Khôi phục", (d, w) -> {
                            Repository.get().resetDemoData();
                            Toast.makeText(requireContext(), "Đã khôi phục dữ liệu mẫu!", Toast.LENGTH_SHORT).show();
                            ((MainActivity) requireActivity()).redirectToLogin();
                        })
                        .setNegativeButton("Hủy", null)
                        .show());

        v.findViewById(R.id.btnLogout).setOnClickListener(x -> {
            Repository.get().logout();
            ((MainActivity) requireActivity()).redirectToLogin();
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindProfile();
    }

    private void bindProfile() {
        User me = Repository.get().getCurrentUser();
        if (me == null) return;

        tvAvatar.setText(UiHelper.avatarInitial(me.name));
        tvName.setText(me.name);
        tvEmail.setText(me.email);

        String major = (me.major == null || me.major.isEmpty()) ? "Chưa cập nhật ngành" : me.major;
        tvMajor.setText(major + " • Trình độ: " + Catalog.levelLabel(me.level));
        tvBio.setText((me.bio == null || me.bio.isEmpty())
                ? "Chưa có giới thiệu bản thân." : me.bio);

        chipSubjects.removeAllViews();
        if (me.subjectIds.isEmpty()) {
            tvNoSubjects.setVisibility(View.VISIBLE);
        } else {
            tvNoSubjects.setVisibility(View.GONE);
            for (String s : me.subjectIds) {
                chipSubjects.addView(UiHelper.makeDisplayChip(requireContext(), Catalog.subjectName(s)));
            }
        }

        chipGoals.removeAllViews();
        if (me.goals.isEmpty()) {
            tvNoGoals.setVisibility(View.VISIBLE);
        } else {
            tvNoGoals.setVisibility(View.GONE);
            for (String g : me.goals) {
                chipGoals.addView(UiHelper.makeDisplayChip(requireContext(), g));
            }
        }

        tvPrivacy.setText(me.hideSchedule
                ? "Đang bật Ẩn lịch chi tiết: sinh viên khác chỉ thấy tổng số giờ trùng."
                : "Công khai khung giờ rảnh để bạn học dễ sắp xếp lịch chung.");

        chipSlots.removeAllViews();
        List<TimeSlot> slots = MatchingEngine.normalize(me.availability);
        if (slots.isEmpty()) {
            tvNoSlots.setVisibility(View.VISIBLE);
        } else {
            tvNoSlots.setVisibility(View.GONE);
            for (TimeSlot t : slots) {
                chipSlots.addView(UiHelper.makeDisplayChip(requireContext(), Formatters.slot(t)));
            }
        }
    }
}
