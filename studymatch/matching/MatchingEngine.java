package com.studymatch.matching;

import com.studymatch.model.TimeSlot;
import com.studymatch.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Thuật toán ghép bạn học của StudyMatch.
 *
 * <p>Điểm phù hợp (0–100) gồm 4 tiêu chí:</p>
 * <ul>
 *   <li>Cùng môn học: 40 điểm (có ít nhất 1 môn chung, hoặc cùng môn đang lọc)</li>
 *   <li>Thời gian rảnh trùng nhau: tối đa 30 điểm, tỉ lệ với số phút trùng/tuần,
 *       đạt tối đa khi trùng từ {@link #FULL_OVERLAP_MINUTES} phút trở lên</li>
 *   <li>Cùng mục tiêu học tập: 20 điểm</li>
 *   <li>Trình độ: 10 điểm nếu bằng nhau, 5 điểm nếu lệch 1 bậc, 0 nếu lệch nhiều hơn</li>
 * </ul>
 *
 * <p>Điều kiện bắt buộc trước khi xếp hạng (theo đề xuất): cùng môn và có ít nhất
 * {@link #MIN_OVERLAP_MINUTES} phút rảnh trùng nhau.</p>
 *
 * <p>Lớp này là Java thuần, không phụ thuộc Android/Firebase nên có thể unit test.</p>
 */
public final class MatchingEngine {

    public static final int WEIGHT_SUBJECT = 40;
    public static final int WEIGHT_TIME = 30;
    public static final int WEIGHT_GOAL = 20;
    public static final int WEIGHT_LEVEL = 10;

    /** Số phút trùng/tuần để đạt trọn điểm thời gian (2 giờ ~ một buổi học). */
    public static final int FULL_OVERLAP_MINUTES = 120;
    /** Số phút trùng tối thiểu để được coi là "có lịch trùng". */
    public static final int MIN_OVERLAP_MINUTES = 30;

    private static final Comparator<TimeSlot> SLOT_ORDER = new Comparator<TimeSlot>() {
        @Override
        public int compare(TimeSlot a, TimeSlot b) {
            if (a.dayOfWeek != b.dayOfWeek) return Integer.compare(a.dayOfWeek, b.dayOfWeek);
            if (a.startMinute != b.startMinute) return Integer.compare(a.startMinute, b.startMinute);
            return Integer.compare(a.endMinute, b.endMinute);
        }
    };

    private MatchingEngine() {
    }

    // ------------------------------------------------------------------
    // Xử lý khung giờ
    // ------------------------------------------------------------------

    /** Sắp xếp và gộp các khung giờ chồng lấn/liền kề trong cùng một ngày. */
    public static List<TimeSlot> normalize(List<TimeSlot> slots) {
        List<TimeSlot> sorted = new ArrayList<>();
        if (slots != null) {
            for (TimeSlot s : slots) {
                if (s != null && s.isValid()) sorted.add(s.copy());
            }
        }
        Collections.sort(sorted, SLOT_ORDER);
        List<TimeSlot> merged = new ArrayList<>();
        for (TimeSlot s : sorted) {
            TimeSlot last = merged.isEmpty() ? null : merged.get(merged.size() - 1);
            if (last != null && last.dayOfWeek == s.dayOfWeek && s.startMinute <= last.endMinute) {
                last.endMinute = Math.max(last.endMinute, s.endMinute);
            } else {
                merged.add(s);
            }
        }
        return merged;
    }

    /** Giao của hai lịch rảnh. */
    public static List<TimeSlot> overlap(List<TimeSlot> a, List<TimeSlot> b) {
        List<TimeSlot> na = normalize(a);
        List<TimeSlot> nb = normalize(b);
        List<TimeSlot> result = new ArrayList<>();
        for (TimeSlot x : na) {
            for (TimeSlot y : nb) {
                if (x.dayOfWeek != y.dayOfWeek) continue;
                int start = Math.max(x.startMinute, y.startMinute);
                int end = Math.min(x.endMinute, y.endMinute);
                if (start < end) result.add(new TimeSlot(x.dayOfWeek, start, end));
            }
        }
        Collections.sort(result, SLOT_ORDER);
        return result;
    }

    public static int totalMinutes(List<TimeSlot> slots) {
        int sum = 0;
        if (slots != null) {
            for (TimeSlot s : slots) sum += s.duration();
        }
        return sum;
    }

    public static int minutesOnDay(List<TimeSlot> slots, int dayOfWeek) {
        int sum = 0;
        if (slots != null) {
            for (TimeSlot s : slots) {
                if (s.dayOfWeek == dayOfWeek) sum += s.duration();
            }
        }
        return sum;
    }

    // ------------------------------------------------------------------
    // Chấm điểm hai sinh viên
    // ------------------------------------------------------------------

    public static MatchResult score(User me, User other) {
        return score(me, other, null);
    }

    /**
     * Chấm điểm độ phù hợp giữa me và other.
     *
     * @param subjectId nếu khác null: chỉ xét môn này (cả hai phải cùng học môn đó).
     */
    public static MatchResult score(User me, User other, String subjectId) {
        MatchResult r = new MatchResult(other);
        if (me == null || other == null) return r;

        // 1. Môn học (40)
        if (subjectId != null) {
            if (contains(me.subjectIds, subjectId) && contains(other.subjectIds, subjectId)) {
                r.commonSubjectIds.add(subjectId);
            }
        } else {
            r.commonSubjectIds.addAll(intersect(me.subjectIds, other.subjectIds));
        }
        r.subjectScore = r.commonSubjectIds.isEmpty() ? 0 : WEIGHT_SUBJECT;

        // 2. Thời gian rảnh trùng (tối đa 30, tỉ lệ theo số phút trùng)
        r.overlapSlots = overlap(me.availability, other.availability);
        r.overlapMinutes = totalMinutes(r.overlapSlots);
        r.timeScore = timeScore(r.overlapMinutes);

        // 3. Mục tiêu (20)
        r.commonGoals = intersect(me.goals, other.goals);
        r.goalScore = r.commonGoals.isEmpty() ? 0 : WEIGHT_GOAL;

        // 4. Trình độ (10)
        r.levelDiff = (me.level <= 0 || other.level <= 0) ? -1 : Math.abs(me.level - other.level);
        r.levelScore = levelScore(me.level, other.level);

        r.total = r.subjectScore + r.timeScore + r.goalScore + r.levelScore;
        return r;
    }

    public static int timeScore(int overlapMinutes) {
        int capped = Math.max(0, Math.min(overlapMinutes, FULL_OVERLAP_MINUTES));
        return Math.round(WEIGHT_TIME * capped / (float) FULL_OVERLAP_MINUTES);
    }

    public static int levelScore(int levelA, int levelB) {
        if (levelA <= 0 || levelB <= 0) return 0;
        int diff = Math.abs(levelA - levelB);
        if (diff == 0) return WEIGHT_LEVEL;
        if (diff == 1) return WEIGHT_LEVEL / 2;
        return 0;
    }

    /** Điều kiện bắt buộc: cùng môn và có lịch rảnh trùng. */
    public static boolean isEligible(MatchResult r) {
        return r.subjectScore > 0 && r.overlapMinutes >= MIN_OVERLAP_MINUTES;
    }

    public static String label(int total) {
        if (total >= 80) return "Rất phù hợp";
        if (total >= 60) return "Phù hợp";
        if (total >= 40) return "Tương đối";
        return "Ít phù hợp";
    }

    // ------------------------------------------------------------------
    // Xếp hạng
    // ------------------------------------------------------------------

    public static List<MatchResult> rank(User me, List<User> candidates, MatchFilter filter) {
        if (filter == null) filter = MatchFilter.defaults();
        List<MatchResult> results = new ArrayList<>();
        if (me == null || candidates == null) return results;

        for (User u : candidates) {
            if (u == null || u.id == null || u.id.equals(me.id)) continue;
            if (filter.subjectId != null && !contains(u.subjectIds, filter.subjectId)) continue;

            MatchResult r = score(me, u, filter.subjectId);

            if (filter.requireEligible && !isEligible(r)) continue;
            if (filter.goal != null && !contains(u.goals, filter.goal)) continue;
            if (filter.dayOfWeek > 0
                    && minutesOnDay(r.overlapSlots, filter.dayOfWeek) < MIN_OVERLAP_MINUTES) continue;
            if (r.total < filter.minScore) continue;

            results.add(r);
        }

        Collections.sort(results, new Comparator<MatchResult>() {
            @Override
            public int compare(MatchResult a, MatchResult b) {
                if (a.total != b.total) return Integer.compare(b.total, a.total);
                if (a.overlapMinutes != b.overlapMinutes) {
                    return Integer.compare(b.overlapMinutes, a.overlapMinutes);
                }
                String na = a.user.name == null ? "" : a.user.name;
                String nb = b.user.name == null ? "" : b.user.name;
                return na.compareTo(nb);
            }
        });
        return results;
    }

    /** Độ phù hợp của một sinh viên với một nhóm = trung bình điểm với các thành viên hiện có. */
    public static int groupScore(User me, List<User> members, String subjectId) {
        int sum = 0;
        int count = 0;
        if (me == null || members == null) return 0;
        for (User m : members) {
            if (m == null || m.id == null || m.id.equals(me.id)) continue;
            // Thành viên chính thức của nhóm thuộc môn subjectId đương nhiên đang học môn đó cùng nhóm
            User effectiveMember = m;
            if (subjectId != null && !contains(m.subjectIds, subjectId)) {
                effectiveMember = new User(m.id, m.name, m.email);
                effectiveMember.level = m.level;
                if (m.goals != null) effectiveMember.goals.addAll(m.goals);
                if (m.availability != null) effectiveMember.availability.addAll(m.availability);
                if (m.subjectIds != null) effectiveMember.subjectIds.addAll(m.subjectIds);
                effectiveMember.subjectIds.add(subjectId);
            }
            sum += score(me, effectiveMember, subjectId).total;
            count++;
        }
        return count == 0 ? 0 : Math.round(sum / (float) count);
    }

    // ------------------------------------------------------------------
    // Đề xuất lịch học chung cho nhóm
    // ------------------------------------------------------------------

    /**
     * Tìm các khung giờ cực đại có nhiều thành viên rảnh nhất (quét điểm mốc theo từng ngày).
     *
     * @param members     thành viên nhóm
     * @param minDuration độ dài tối thiểu (phút) của một khung giờ đề xuất
     * @param maxResults  số đề xuất tối đa
     * @return danh sách sắp xếp theo: số người rảnh giảm dần, độ dài giảm dần, thứ trong tuần.
     */
    public static List<SlotSuggestion> suggestCommonSlots(List<User> members, int minDuration, int maxResults) {
        List<SlotSuggestion> filtered = new ArrayList<>();
        if (members == null || members.isEmpty()) return filtered;

        List<User> validMembers = new ArrayList<>();
        List<List<TimeSlot>> normalized = new ArrayList<>();
        for (User m : members) {
            if (m == null || m.id == null) continue;
            validMembers.add(m);
            normalized.add(normalize(m.availability));
        }
        if (validMembers.isEmpty()) return filtered;

        int minPeople = validMembers.size() <= 1 ? 1 : 2;
        int effectiveMin = Math.max(1, minDuration);

        for (int day = 1; day <= 7; day++) {
            TreeSet<Integer> points = new TreeSet<>();
            for (List<TimeSlot> slots : normalized) {
                for (TimeSlot s : slots) {
                    if (s.dayOfWeek == day) {
                        points.add(s.startMinute);
                        points.add(s.endMinute);
                    }
                }
            }
            if (points.size() < 2) continue;

            Integer[] pts = points.toArray(new Integer[0]);
            for (int i = 0; i < pts.length; i++) {
                for (int j = i + 1; j < pts.length; j++) {
                    int start = pts[i];
                    int end = pts[j];
                    if (end - start < effectiveMin) continue;

                    List<String> ids = membersCovering(validMembers, normalized, day, start, end);
                    if (ids.size() < minPeople) continue;

                    // Chỉ giữ đoạn cực đại của tập thành viên ids (không thể mở rộng trái/phải mà giữ nguyên ids)
                    if (i > 0 && containsAllIds(membersCovering(validMembers, normalized, day, pts[i - 1], end), ids)) {
                        continue;
                    }
                    if (j + 1 < pts.length && containsAllIds(membersCovering(validMembers, normalized, day, start, pts[j + 1]), ids)) {
                        continue;
                    }
                    filtered.add(new SlotSuggestion(day, start, end, ids));
                }
            }
        }

        Collections.sort(filtered, new Comparator<SlotSuggestion>() {
            @Override
            public int compare(SlotSuggestion a, SlotSuggestion b) {
                if (a.count() != b.count()) return Integer.compare(b.count(), a.count());
                if (a.duration() != b.duration()) return Integer.compare(b.duration(), a.duration());
                if (a.dayOfWeek != b.dayOfWeek) return Integer.compare(a.dayOfWeek, b.dayOfWeek);
                return Integer.compare(a.startMinute, b.startMinute);
            }
        });
        if (maxResults > 0 && filtered.size() > maxResults) {
            return new ArrayList<>(filtered.subList(0, maxResults));
        }
        return filtered;
    }

    private static List<String> membersCovering(List<User> members, List<List<TimeSlot>> normalized,
                                                int day, int start, int end) {
        List<String> ids = new ArrayList<>();
        for (int k = 0; k < members.size(); k++) {
            if (coversAny(normalized.get(k), day, start, end)) ids.add(members.get(k).id);
        }
        return ids;
    }

    private static boolean containsAllIds(List<String> superset, List<String> subset) {
        return superset.size() >= subset.size() && superset.containsAll(subset);
    }

    /** Danh sách id thành viên rảnh trọn vẹn trong [start, end) của ngày day (kể cả khi qua nửa đêm). */
    public static List<String> availableMembers(List<User> members, int day, int start, int end) {
        List<String> ids = new ArrayList<>();
        if (members == null || start >= end) return ids;
        for (User m : members) {
            if (m == null || m.id == null) continue;
            List<TimeSlot> norm = normalize(m.availability);
            if (end <= 24 * 60) {
                if (coversAny(norm, day, start, end)) ids.add(m.id);
            } else {
                int nextDay = (day % 7) + 1;
                int overflow = end - 24 * 60;
                if (overflow <= 24 * 60
                        && coversAny(norm, day, start, 24 * 60)
                        && coversAny(norm, nextDay, 0, overflow)) {
                    ids.add(m.id);
                }
            }
        }
        return ids;
    }

    // ------------------------------------------------------------------
    // Tiện ích
    // ------------------------------------------------------------------

    private static boolean coversAny(List<TimeSlot> slots, int day, int start, int end) {
        for (TimeSlot s : slots) {
            if (s.covers(day, start, end)) return true;
        }
        return false;
    }

    private static boolean contains(List<String> list, String value) {
        return list != null && value != null && list.contains(value);
    }

    private static List<String> intersect(List<String> a, List<String> b) {
        List<String> result = new ArrayList<>();
        if (a == null || b == null) return result;
        for (String x : a) {
            if (b.contains(x) && !result.contains(x)) result.add(x);
        }
        return result;
    }
}
