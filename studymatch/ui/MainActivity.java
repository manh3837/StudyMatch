package com.studymatch.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.studymatch.R;
import com.studymatch.data.Repository;
import com.studymatch.model.User;

/** Khung điều hướng chính gồm 5 tab tương ứng các màn hình cốt lõi của StudyMatch. */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private String pendingFilterSubjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Repository.get().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            updateTitleForTab(id);

            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            Fragment fragment;
            if (id == R.id.nav_find) {
                if (pendingFilterSubjectId == null && current instanceof FindPartnerFragment) {
                    return true;
                }
                FindPartnerFragment f = FindPartnerFragment.newInstance(pendingFilterSubjectId);
                pendingFilterSubjectId = null;
                fragment = f;
            } else if (id == R.id.nav_groups) {
                if (current instanceof GroupsFragment) return true;
                fragment = new GroupsFragment();
            } else if (id == R.id.nav_schedule) {
                if (current instanceof ScheduleFragment) return true;
                fragment = new ScheduleFragment();
            } else if (id == R.id.nav_profile) {
                if (current instanceof ProfileFragment) return true;
                fragment = new ProfileFragment();
            } else {
                if (current instanceof HomeFragment) return true;
                fragment = new HomeFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            bottomNav.post(() -> updateTitleForTab(bottomNav.getSelectedItemId()));
        }
    }

    private void updateTitleForTab(int id) {
        if (getSupportActionBar() == null) return;
        String title;
        if (id == R.id.nav_find) title = "Tìm bạn học";
        else if (id == R.id.nav_groups) title = "Quản lý nhóm học";
        else if (id == R.id.nav_schedule) title = "Lịch học nhóm";
        else if (id == R.id.nav_profile) title = "Hồ sơ cá nhân";
        else title = "StudyMatch";
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Repository.get().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        refreshNotificationBadge();
    }

    public void refreshNotificationBadge() {
        User me = Repository.get().getCurrentUser();
        if (me == null || bottomNav == null) return;
        int count = Repository.get().countNotifications(me.id);
        if (count > 0) {
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_groups);
            badge.setVisible(true);
            badge.setNumber(count);
        } else {
            bottomNav.removeBadge(R.id.nav_groups);
        }
    }

    public void openTab(int menuItemId) {
        if (bottomNav != null) bottomNav.setSelectedItemId(menuItemId);
    }

    public void openFindWithSubject(String subjectId) {
        this.pendingFilterSubjectId = subjectId;
        openTab(R.id.nav_find);
    }

    public void redirectToLogin() {
        Intent it = new Intent(this, LoginActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(it);
        finish();
    }
}
