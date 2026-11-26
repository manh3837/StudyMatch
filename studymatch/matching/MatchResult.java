package com.studymatch.matching;

import com.studymatch.model.TimeSlot;
import com.studymatch.model.User;

import java.util.ArrayList;
import java.util.List;

/** Kết quả chấm điểm độ phù hợp giữa người dùng hiện tại và một sinh viên khác. */
public class MatchResult {
    public final User user;

    public int subjectScore;
    public int timeScore;
    public int goalScore;
    public int levelScore;
    public int total;

    public List<String> commonSubjectIds = new ArrayList<>();
    public List<String> commonGoals = new ArrayList<>();
    public List<TimeSlot> overlapSlots = new ArrayList<>();
    public int overlapMinutes;
    public int levelDiff;

    public MatchResult(User user) {
        this.user = user;
    }

    public String label() {
        return MatchingEngine.label(total);
    }
}
