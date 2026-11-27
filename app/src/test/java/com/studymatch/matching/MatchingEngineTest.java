package com.studymatch.matching;

import com.studymatch.data.DataStore;
import com.studymatch.data.MockData;
import com.studymatch.model.TimeSlot;
import com.studymatch.model.User;
import com.studymatch.util.Formatters;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test kiểm chứng thuật toán ghép nhóm (MatchingEngine) và đề xuất lịch học chung.
 */
public class MatchingEngineTest {

    @Test
    public void proposalExample_sameAndroidTuesdaySameLevelDifferentGoal_scores80() {
        User svA = new User("a", "Sinh viên A", "a@test.vn");
        svA.subjectIds = Collections.singletonList("android");
        svA.goals = Collections.singletonList("Ôn thi cuối kỳ");
        svA.level = User.LEVEL_MEDIUM;
        svA.availability = Collections.singletonList(TimeSlot.of(2, 19, 21)); // Tối thứ Ba 19-21h

        User svB = new User("b", "Sinh viên B", "b@test.vn");
        svB.subjectIds = Collections.singletonList("android");
        svB.goals = Collections.singletonList("Làm bài tập");
        svB.level = User.LEVEL_MEDIUM;
        svB.availability = Collections.singletonList(TimeSlot.of(2, 19, 21));

        MatchResult r = MatchingEngine.score(svA, svB);

        assertEquals(40, r.subjectScore);
        assertEquals(30, r.timeScore);
        assertEquals(0, r.goalScore);
        assertEquals(10, r.levelScore);
        assertEquals(80, r.total);
        assertEquals("Rất phù hợp", r.label());
        assertTrue(MatchingEngine.isEligible(r));
    }

    @Test
    public void partialOverlapAndAdjacentLevel_computesProportionalScore() {
        User a = new User("a", "A", "a@test.vn");
        a.subjectIds = Arrays.asList("db", "dsa");
        a.goals = Collections.singletonList("Ôn thi cuối kỳ");
        a.level = User.LEVEL_MEDIUM;
        a.availability = Collections.singletonList(TimeSlot.of(4, 19, 21));

        User b = new User("b", "B", "b@test.vn");
        b.subjectIds = Collections.singletonList("db");
        b.goals = Collections.singletonList("Ôn thi cuối kỳ");
        b.level = User.LEVEL_BASIC; // lệch 1 bậc -> 5 điểm
        b.availability = Collections.singletonList(TimeSlot.of(4, 20, 22)); // trùng 60 phút -> 15/30

        MatchResult r = MatchingEngine.score(a, b);
        assertEquals(40, r.subjectScore);
        assertEquals(60, r.overlapMinutes);
        assertEquals(15, r.timeScore);
        assertEquals(20, r.goalScore);
        assertEquals(5, r.levelScore);
        assertEquals(80, r.total);
    }

    @Test
    public void normalize_mergesOverlappingAndContiguousSlots() {
        List<TimeSlot> raw = Arrays.asList(
                TimeSlot.of(2, 20, 21),
                TimeSlot.of(2, 18, 20),
                new TimeSlot(2, 19 * 60 + 30, 21 * 60 + 30),
                TimeSlot.of(6, 8, 10)
        );

        List<TimeSlot> normalized = MatchingEngine.normalize(raw);
        assertEquals(2, normalized.size());
        assertEquals(new TimeSlot(2, 18 * 60, 21 * 60 + 30), normalized.get(0));
        assertEquals(TimeSlot.of(6, 8, 10), normalized.get(1));
    }

    @Test
    public void rank_enforcesSubjectAndScheduleOverlapByDefault() {
        User me = new User("me", "Me", "me@test.vn");
        me.subjectIds = Collections.singletonList("android");
        me.level = User.LEVEL_MEDIUM;
        me.availability = Collections.singletonList(TimeSlot.of(2, 19, 21));

        // Cùng môn nhưng lệch lịch hoàn toàn -> bị loại khi requireEligible = true
        User noOverlap = new User("u1", "No Overlap", "u1@test.vn");
        noOverlap.subjectIds = Collections.singletonList("android");
        noOverlap.level = User.LEVEL_MEDIUM;
        noOverlap.availability = Collections.singletonList(TimeSlot.of(3, 19, 21));

        // Trùng lịch nhưng khác môn -> bị loại
        User diffSubject = new User("u2", "Diff Subject", "u2@test.vn");
        diffSubject.subjectIds = Collections.singletonList("web");
        diffSubject.level = User.LEVEL_MEDIUM;
        diffSubject.availability = Collections.singletonList(TimeSlot.of(2, 19, 21));

        // Hợp lệ: cùng môn và trùng 90 phút
        User valid = new User("u3", "Valid", "u3@test.vn");
        valid.subjectIds = Collections.singletonList("android");
        valid.level = User.LEVEL_MEDIUM;
        valid.availability = Collections.singletonList(new TimeSlot(2, 19 * 60 + 30, 21 * 60));

        List<MatchResult> ranked = MatchingEngine.rank(
                me, Arrays.asList(noOverlap, diffSubject, valid), MatchFilter.defaults());

        assertEquals(1, ranked.size());
        assertEquals("u3", ranked.get(0).user.id);
        assertFalse(MatchingEngine.isEligible(MatchingEngine.score(me, noOverlap)));
    }

    @Test
    public void suggestCommonSlots_prioritizesMostMembersAvailable() {
        User u1 = new User("u1", "U1", "u1@test.vn");
        u1.availability = Arrays.asList(TimeSlot.of(2, 18, 21), TimeSlot.of(6, 8, 11));

        User u2 = new User("u2", "U2", "u2@test.vn");
        u2.availability = Arrays.asList(TimeSlot.of(2, 19, 21), TimeSlot.of(6, 9, 11));

        User u3 = new User("u3", "U3", "u3@test.vn");
        u3.availability = Collections.singletonList(TimeSlot.of(2, 19, 21));

        List<SlotSuggestion> suggestions = MatchingEngine.suggestCommonSlots(
                Arrays.asList(u1, u2, u3), 60, 5);

        assertFalse(suggestions.isEmpty());
        SlotSuggestion best = suggestions.get(0);
        // Thứ 3 19:00-21:00 có cả 3/3 thành viên rảnh
        assertEquals(2, best.dayOfWeek);
        assertEquals(19 * 60, best.startMinute);
        assertEquals(21 * 60, best.endMinute);
        assertEquals(3, best.count());
    }

    @Test
    public void mockData_demoUserAndBinhMatchProposalScenario() {
        DataStore store = MockData.create();
        User demo = null;
        User binh = null;
        for (User u : store.users) {
            if ("u_demo".equals(u.id)) demo = u;
            if ("u_binh".equals(u.id)) binh = u;
        }
        MatchResult r = MatchingEngine.score(demo, binh, "android");
        assertEquals(80, r.total);
    }

    @Test
    public void suggestCommonSlots_doesNotLoseTwoHourSlotWhenThirdMemberOverlapsBriefly() {
        User u1 = new User("u1", "U1", "u1@test.vn");
        u1.availability = Collections.singletonList(TimeSlot.of(2, 19, 21));

        User u2 = new User("u2", "U2", "u2@test.vn");
        u2.availability = Collections.singletonList(TimeSlot.of(2, 19, 21));

        // Thành viên thứ 3 chỉ trùng 45 phút ở giữa -> không được làm mất khung 2 giờ của u1 & u2
        User u3 = new User("u3", "U3", "u3@test.vn");
        u3.availability = Collections.singletonList(new TimeSlot(2, 19 * 60 + 30, 20 * 60 + 15));

        List<SlotSuggestion> suggestions = MatchingEngine.suggestCommonSlots(
                Arrays.asList(u1, u2, u3), 60, 5);

        assertEquals(1, suggestions.size());
        SlotSuggestion best = suggestions.get(0);
        assertEquals(2, best.dayOfWeek);
        assertEquals(19 * 60, best.startMinute);
        assertEquals(21 * 60, best.endMinute);
        assertEquals(2, best.count());
    }

    @Test
    public void rank_allowsFilteringOtherSubjectWhenRequireEligibleDisabled() {
        User me = new User("me", "Me", "me@test.vn");
        me.subjectIds = Collections.singletonList("android");
        me.level = User.LEVEL_MEDIUM;
        me.availability = Collections.singletonList(TimeSlot.of(2, 19, 21));

        User webStudent = new User("w", "Web Student", "w@test.vn");
        webStudent.subjectIds = Collections.singletonList("web");
        webStudent.level = User.LEVEL_MEDIUM;
        webStudent.availability = Collections.singletonList(TimeSlot.of(2, 19, 21));

        MatchFilter f = MatchFilter.defaults();
        f.subjectId = "web";
        f.requireEligible = false;

        List<MatchResult> results = MatchingEngine.rank(me, Collections.singletonList(webStudent), f);
        assertEquals(1, results.size());
        assertEquals("w", results.get(0).user.id);
        assertEquals(0, results.get(0).subjectScore);
        assertEquals(40, results.get(0).total);
    }

    @Test
    public void groupScore_treatsGroupOwnerAsStudyingGroupSubject() {
        User ownerWithoutSubjectInProfile = new User("owner", "Owner", "o@test.vn");
        ownerWithoutSubjectInProfile.level = User.LEVEL_MEDIUM;
        ownerWithoutSubjectInProfile.availability = Collections.singletonList(TimeSlot.of(2, 19, 21));

        User candidate = new User("cand", "Candidate", "c@test.vn");
        candidate.subjectIds = Collections.singletonList("web");
        candidate.level = User.LEVEL_MEDIUM;
        candidate.availability = Collections.singletonList(TimeSlot.of(2, 19, 21));

        int fit = MatchingEngine.groupScore(
                candidate, Collections.singletonList(ownerWithoutSubjectInProfile), "web");
        assertEquals(80, fit);
    }

    @Test
    public void availableMembers_handlesMidnightCrossingAccurately() {
        User onlyUntilMidnight = new User("u1", "U1", "u1@test.vn");
        onlyUntilMidnight.availability = Collections.singletonList(TimeSlot.of(2, 23, 24));

        User acrossMidnight = new User("u2", "U2", "u2@test.vn");
        acrossMidnight.availability = Arrays.asList(TimeSlot.of(2, 23, 24), TimeSlot.of(3, 0, 2));

        List<String> free = MatchingEngine.availableMembers(
                Arrays.asList(onlyUntilMidnight, acrossMidnight), 2, 23 * 60, 25 * 60);
        assertEquals(Collections.singletonList("u2"), free);
        assertEquals("01:00", Formatters.time((25 * 60) % (24 * 60)));
    }
}
