package com.studymatch.data;

import com.studymatch.model.GroupMember;
import com.studymatch.model.SessionFeedback;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.StudySession;
import com.studymatch.model.TimeSlot;
import com.studymatch.model.User;
import com.studymatch.util.DateUtils;

import java.util.Arrays;

/**
 * Dữ liệu giả lập để demo. Tất cả tài khoản dùng mật khẩu "123456".
 * Tài khoản chính: demo@studymatch.vn
 *
 * Thứ trong tuần: 1 = Thứ 2, 2 = Thứ 3, ..., 6 = Thứ 7, 7 = Chủ nhật.
 */
public final class MockData {

    public static final String DEMO_NAME = "Nguyễn Thế Mạnh";
    public static final String DEMO_MAJOR = "CNTT – K29";
    public static final String DEMO_EMAIL = "demo@studymatch.vn";
    public static final String DEMO_PASSWORD = "123456";

    private MockData() {
    }

    public static DataStore create() {
        DataStore s = new DataStore();
        long now = System.currentTimeMillis();

        // ---------------- Sinh viên ----------------
        User demo = user(s, "u_demo", DEMO_NAME, DEMO_EMAIL, DEMO_MAJOR, User.LEVEL_MEDIUM,
                "Mình đang học Android và muốn tìm bạn cùng luyện code buổi tối.",
                new String[]{"android", "dsa", "db"},
                new String[]{"Ôn thi cuối kỳ", "Luyện lập trình"},
                TimeSlot.of(2, 19, 21), TimeSlot.of(4, 19, 22), TimeSlot.of(6, 8, 11), TimeSlot.of(7, 14, 17));

        // Ví dụ trong đề xuất: cùng Android, cùng rảnh tối thứ Ba, cùng trình độ, khác mục tiêu => 80 điểm
        User binh = user(s, "u_binh", "Trần Quốc Bình", "binh@studymatch.vn", "CNTT – K22", User.LEVEL_MEDIUM,
                "Thích UI/UX, đang làm bài tập Android và Web.",
                new String[]{"android", "web"},
                new String[]{"Làm bài tập"},
                TimeSlot.of(2, 19, 21), TimeSlot.of(6, 9, 11));

        User chi = user(s, "u_chi", "Lê Thảo Chi", "chi@studymatch.vn", "Hệ thống thông tin – K22", User.LEVEL_MEDIUM,
                "Cần bạn ôn thi Android & CSDL, học nghiêm túc nha!",
                new String[]{"android", "db"},
                new String[]{"Ôn thi cuối kỳ"},
                TimeSlot.of(2, 19, 21), TimeSlot.of(4, 19, 21), TimeSlot.of(7, 14, 16));

        User dung = user(s, "u_dung", "Phạm Tiến Dũng", "dung@studymatch.vn", "Khoa học máy tính – K21", User.LEVEL_GOOD,
                "Hay luyện LeetCode, sẵn sàng giải thích thuật toán.",
                new String[]{"dsa", "discrete"},
                new String[]{"Luyện lập trình"},
                TimeSlot.of(6, 8, 10), TimeSlot.of(3, 20, 22));

        User ha = user(s, "u_ha", "Võ Thu Hà", "ha@studymatch.vn", "Kỹ thuật phần mềm – K23", User.LEVEL_BASIC,
                "Mới học CSDL, cần người kèm thêm phần SQL.",
                new String[]{"db", "prob"},
                new String[]{"Ôn thi cuối kỳ"},
                TimeSlot.of(4, 20, 21), TimeSlot.of(5, 14, 16));

        User khoa = user(s, "u_khoa", "Đặng Minh Khoa", "khoa@studymatch.vn", "CNTT – K22", User.LEVEL_MEDIUM,
                "Đang làm đồ án Android dùng Firebase.",
                new String[]{"android"},
                new String[]{"Làm đồ án"},
                TimeSlot.of(1, 18, 20), TimeSlot.of(3, 18, 20));

        User linh = user(s, "u_linh", "Bùi Khánh Linh", "linh@studymatch.vn", "Ngôn ngữ Anh – K22", User.LEVEL_GOOD,
                "Frontend + tiếng Anh, có thể giúp đọc tài liệu.",
                new String[]{"web", "english"},
                new String[]{"Làm bài tập"},
                TimeSlot.of(2, 19, 21));

        User minh = user(s, "u_minh", "Hoàng Đức Minh", "minh@studymatch.vn", "CNTT – K22", User.LEVEL_MEDIUM,
                "Ôn thi cuối kỳ CTDL, CSDL và Mạng máy tính.",
                new String[]{"dsa", "db", "network"},
                new String[]{"Ôn thi cuối kỳ", "Làm bài tập"},
                TimeSlot.of(7, 15, 17), TimeSlot.of(3, 19, 21));

        User ngoc = user(s, "u_ngoc", "Ngô Bảo Ngọc", "ngoc@studymatch.vn", "CNTT – K23", User.LEVEL_BASIC,
                "Muốn học trước Android để kịp tiến độ lớp.",
                new String[]{"android", "dsa"},
                new String[]{"Học trước bài mới"},
                TimeSlot.of(2, 20, 21), TimeSlot.of(6, 10, 11));

        user(s, "u_phuc", "Trịnh Hữu Phúc", "phuc@studymatch.vn", "Mạng máy tính – K21", User.LEVEL_GOOD,
                "Ôn CCNA và Toán rời rạc.",
                new String[]{"network", "discrete"},
                new String[]{"Ôn thi cuối kỳ"},
                TimeSlot.of(1, 19, 21));

        User quynh = user(s, "u_quynh", "Mai Như Quỳnh", "quynh@studymatch.vn", "Kỹ thuật phần mềm – K22", User.LEVEL_MEDIUM,
                "Leader đồ án Android, cần thêm bạn làm backend.",
                new String[]{"android", "web", "db"},
                new String[]{"Làm đồ án", "Ôn thi cuối kỳ"},
                new TimeSlot(4, 19 * 60 + 30, 21 * 60 + 30), TimeSlot.of(7, 9, 11));
        quynh.hideSchedule = true;

        // ---------------- Nhóm học ----------------
        StudyGroup g1 = group(s, "g_android_t3", "Android Tối Thứ Ba", "android", demo.id, 5,
                "Luyện lập trình", "Cùng code Android mỗi tối thứ Ba: RecyclerView, Room, Firebase.", now - days(20));
        member(s, g1, demo, GroupMember.STATUS_APPROVED, GroupMember.ROLE_OWNER, now - days(20));
        member(s, g1, binh, GroupMember.STATUS_APPROVED, GroupMember.ROLE_MEMBER, now - days(18));
        member(s, g1, chi, GroupMember.STATUS_APPROVED, GroupMember.ROLE_MEMBER, now - days(15));
        member(s, g1, ngoc, GroupMember.STATUS_PENDING, GroupMember.ROLE_MEMBER, now - days(1));
        member(s, g1, khoa, GroupMember.STATUS_PENDING, GroupMember.ROLE_MEMBER, now - hours(5));

        StudyGroup g2 = group(s, "g_dsa", "Ôn thi CTDL & GT", "dsa", dung.id, 6,
                "Ôn thi cuối kỳ", "Giải đề cũ, luyện cây – đồ thị – quy hoạch động.", now - days(12));
        member(s, g2, dung, GroupMember.STATUS_APPROVED, GroupMember.ROLE_OWNER, now - days(12));
        member(s, g2, minh, GroupMember.STATUS_APPROVED, GroupMember.ROLE_MEMBER, now - days(10));
        member(s, g2, demo, GroupMember.STATUS_INVITED, GroupMember.ROLE_MEMBER, now - hours(10));

        StudyGroup g3 = group(s, "g_db", "CSDL – Cùng làm bài tập", "db", minh.id, 4,
                "Làm bài tập", "Làm bài tập SQL hằng tuần, chuẩn hóa dữ liệu, ERD.", now - days(9));
        member(s, g3, minh, GroupMember.STATUS_APPROVED, GroupMember.ROLE_OWNER, now - days(9));
        member(s, g3, ha, GroupMember.STATUS_APPROVED, GroupMember.ROLE_MEMBER, now - days(8));
        member(s, g3, quynh, GroupMember.STATUS_APPROVED, GroupMember.ROLE_MEMBER, now - days(7));

        StudyGroup g4 = group(s, "g_web", "Web Project Squad", "web", linh.id, 3,
                "Làm đồ án", "Nhóm làm đồ án web, đã đủ người.", now - days(30));
        member(s, g4, linh, GroupMember.STATUS_APPROVED, GroupMember.ROLE_OWNER, now - days(30));
        member(s, g4, binh, GroupMember.STATUS_APPROVED, GroupMember.ROLE_MEMBER, now - days(29));
        member(s, g4, quynh, GroupMember.STATUS_APPROVED, GroupMember.ROLE_MEMBER, now - days(28));

        StudyGroup g5 = group(s, "g_android_doan", "Đồ án Android cuối kỳ", "android", quynh.id, 4,
                "Làm đồ án", "Hỗ trợ nhau hoàn thiện đồ án Android: kiến trúc, Firebase, demo.", now - days(5));
        member(s, g5, quynh, GroupMember.STATUS_APPROVED, GroupMember.ROLE_OWNER, now - days(5));
        member(s, g5, khoa, GroupMember.STATUS_APPROVED, GroupMember.ROLE_MEMBER, now - days(4));

        // ---------------- Buổi học ----------------
        StudySession past = session(s, "s_past", g1, "Ôn RecyclerView & Adapter",
                DateUtils.daysAgoAt(3, 19 * 60, now), 120, "Thư viện tầng 3", demo.id,
                "Mang laptop, chuẩn bị bài tập tuần 4.");
        s.feedbacks.add(new SessionFeedback(past.id, binh.id, 5, "Rất hữu ích, giải thích dễ hiểu!", now - days(2)));
        s.feedbacks.add(new SessionFeedback(past.id, chi.id, 4, "Buổi học hiệu quả.", now - days(2)));

        session(s, "s_firebase", g1, "Firebase Authentication",
                DateUtils.nextOccurrence(2, 19 * 60, now), 120, "Google Meet", demo.id,
                "Mỗi người tự tạo Firebase project trước buổi học.");
        session(s, "s_room", g1, "Bài tập Room Database",
                DateUtils.nextOccurrence(6, 9 * 60, now), 90, "Phòng tự học B2", binh.id, "");
        session(s, "s_dsa", g2, "Giải đề thi CTDL 2025",
                DateUtils.nextOccurrence(6, 8 * 60, now), 120, "Phòng A1-204", dung.id, "");
        session(s, "s_db", g3, "Chuẩn hóa 3NF & BCNF",
                DateUtils.nextOccurrence(7, 15 * 60, now), 120, "Discord: CSDL-K22", minh.id, "");

        return s;
    }

    // ------------------------------------------------------------------

    private static User user(DataStore s, String id, String name, String email, String major, int level,
                             String bio, String[] subjects, String[] goals, TimeSlot... slots) {
        User u = new User(id, name, email);
        u.major = major;
        u.level = level;
        u.bio = bio;
        u.subjectIds.addAll(Arrays.asList(subjects));
        u.goals.addAll(Arrays.asList(goals));
        u.availability.addAll(Arrays.asList(slots));
        s.users.add(u);
        s.passwordHashes.put(id, Repository.hashPassword(DEMO_PASSWORD));
        return u;
    }

    private static StudyGroup group(DataStore s, String id, String name, String subjectId, String ownerId,
                                    int max, String goal, String desc, long createdAt) {
        StudyGroup g = new StudyGroup();
        g.id = id;
        g.name = name;
        g.subjectId = subjectId;
        g.ownerId = ownerId;
        g.maxMembers = max;
        g.goal = goal;
        g.description = desc;
        g.createdAt = createdAt;
        s.groups.add(g);
        return g;
    }

    private static void member(DataStore s, StudyGroup g, User u, String status, String role, long createdAt) {
        s.members.add(new GroupMember("m_" + g.id + "_" + u.id, g.id, u.id, status, role, createdAt));
    }

    private static StudySession session(DataStore s, String id, StudyGroup g, String title, long start,
                                        int duration, String location, String createdBy, String note) {
        StudySession ss = new StudySession();
        ss.id = id;
        ss.groupId = g.id;
        ss.title = title;
        ss.startTime = start;
        ss.durationMinutes = duration;
        ss.location = location;
        ss.createdBy = createdBy;
        ss.note = note;
        s.sessions.add(ss);
        return ss;
    }

    private static long days(int n) {
        return n * 24L * 60 * 60 * 1000;
    }

    private static long hours(int n) {
        return n * 60L * 60 * 1000;
    }
}
