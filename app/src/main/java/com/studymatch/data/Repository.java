package com.studymatch.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.studymatch.model.GroupMember;
import com.studymatch.model.SessionFeedback;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.StudySession;
import com.studymatch.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Lớp truy cập dữ liệu duy nhất mà giao diện sử dụng.
 */
public final class Repository {

    private static final String PREFS = "studymatch_store";
    private static final String KEY_DATA = "data";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private static volatile Repository instance;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private DataStore store;

    public static synchronized void init(Context context) {
        if (instance == null) instance = new Repository(context.getApplicationContext());
    }

    public static Repository get() {
        if (instance == null) throw new IllegalStateException("Repository chưa được khởi tạo");
        return instance;
    }

    private Repository(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    private void load() {
        String json = prefs.getString(KEY_DATA, null);
        DataStore loaded = null;
        if (json != null) {
            try {
                loaded = gson.fromJson(json, DataStore.class);
            } catch (Exception ignored) {
                loaded = null;
            }
        }
        if (loaded == null || loaded.users == null || loaded.users.isEmpty()) {
            loaded = MockData.create();
            store = loaded;
            store.ensureNotNull();
            save();
            return;
        }
        store = loaded;
        store.ensureNotNull();
        if (store.version < DataStore.CURRENT_VERSION) {
            for (User u : store.users) {
                if ("u_demo".equals(u.id)) {
                    if (u.name.isEmpty() || "Nguyễn Minh Anh".equals(u.name)) {
                        u.name = MockData.DEMO_NAME;
                    }
                    if (u.major.isEmpty() || "CNTT – K22".equals(u.major)) {
                        u.major = MockData.DEMO_MAJOR;
                    }
                }
            }
            store.version = DataStore.CURRENT_VERSION;
            save();
        }
    }

    private void save() {
        prefs.edit().putString(KEY_DATA, gson.toJson(store)).apply();
    }

    /** Xóa toàn bộ thay đổi, nạp lại dữ liệu mẫu và đăng xuất. */
    public void resetDemoData() {
        store = MockData.create();
        store.ensureNotNull();
        prefs.edit().remove(KEY_CURRENT_USER).apply();
        save();
    }

    private static String newId(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    // ==================================================================
    // Xác thực
    // ==================================================================

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(("studymatch:" + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format(Locale.US, "%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập email");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu");
        }
        User u = findUserByEmail(email);
        if (u == null) throw new IllegalArgumentException("Email chưa được đăng ký");
        String hash = store.passwordHashes.get(u.id);
        if (hash == null || !hash.equals(hashPassword(password))) {
            throw new IllegalArgumentException("Mật khẩu không đúng");
        }
        prefs.edit().putString(KEY_CURRENT_USER, u.id).apply();
        return u;
    }

    public boolean hasDemoPassword(String userId) {
        if (userId == null) return false;
        String hash = store.passwordHashes.get(userId);
        return hash != null && hash.equals(hashPassword(MockData.DEMO_PASSWORD));
    }

    public User register(String name, String email, String password) {
        if (name == null || name.trim().length() < 2) throw new IllegalArgumentException("Vui lòng nhập họ tên");
        if (!isValidEmail(email)) throw new IllegalArgumentException("Email không hợp lệ");
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }
        if (findUserByEmail(email) != null) throw new IllegalArgumentException("Email này đã được sử dụng");

        User u = new User(newId("u_"), name.trim(), email.trim().toLowerCase(Locale.ROOT));
        u.level = User.LEVEL_MEDIUM;
        u.major = "";
        u.bio = "";
        store.users.add(u);
        store.passwordHashes.put(u.id, hashPassword(password));
        save();
        prefs.edit().putString(KEY_CURRENT_USER, u.id).apply();
        return u;
    }

    public void logout() {
        prefs.edit().remove(KEY_CURRENT_USER).apply();
    }

    public String getCurrentUserId() {
        return prefs.getString(KEY_CURRENT_USER, null);
    }

    public User getCurrentUser() {
        String id = getCurrentUserId();
        return id == null ? null : getUser(id);
    }

    private User findUserByEmail(String email) {
        if (email == null) return null;
        String e = email.trim().toLowerCase(Locale.ROOT);
        for (User u : store.users) {
            if (u.email != null && u.email.toLowerCase(Locale.ROOT).equals(e)) return u;
        }
        return null;
    }

    // ==================================================================
    // Người dùng
    // ==================================================================

    public User getUser(String id) {
        if (id == null) return null;
        for (User u : store.users) {
            if (id.equals(u.id)) return u;
        }
        return null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(store.users);
    }

    /** Bản sao sâu để màn hình chỉnh sửa không làm thay đổi dữ liệu khi chưa bấm Lưu. */
    public User copyUser(User u) {
        return gson.fromJson(gson.toJson(u), User.class);
    }

    public void updateUser(User updated) {
        if (updated == null || updated.id == null) {
            throw new IllegalArgumentException("Dữ liệu người dùng không hợp lệ");
        }
        if (updated.name == null || updated.name.trim().length() < 2) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên");
        }
        for (int i = 0; i < store.users.size(); i++) {
            if (Objects.equals(store.users.get(i).id, updated.id)) {
                store.users.set(i, updated);
                save();
                return;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy người dùng");
    }

    // ==================================================================
    // Nhóm học
    // ==================================================================

    public StudyGroup getGroup(String id) {
        if (id == null) return null;
        for (StudyGroup g : store.groups) {
            if (id.equals(g.id)) return g;
        }
        return null;
    }

    public List<StudyGroup> getAllGroups() {
        List<StudyGroup> list = new ArrayList<>(store.groups);
        Collections.sort(list, new Comparator<StudyGroup>() {
            @Override
            public int compare(StudyGroup a, StudyGroup b) {
                return Long.compare(b.createdAt, a.createdAt);
            }
        });
        return list;
    }

    public StudyGroup createGroup(String ownerId, String name, String subjectId, String goal,
                                  int maxMembers, String description) {
        if (ownerId == null || getUser(ownerId) == null) {
            throw new IllegalStateException("Vui lòng đăng nhập lại");
        }
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng nhập tên nhóm");
        if (subjectId == null || subjectId.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng chọn môn học");
        if (maxMembers < 2 || maxMembers > 20) throw new IllegalArgumentException("Số thành viên từ 2 đến 20");

        long now = System.currentTimeMillis();
        StudyGroup g = new StudyGroup();
        g.id = newId("g_");
        g.name = name.trim();
        g.subjectId = subjectId;
        g.goal = goal == null ? "" : goal;
        g.ownerId = ownerId;
        g.maxMembers = maxMembers;
        g.description = description == null ? "" : description.trim();
        g.createdAt = now;
        store.groups.add(g);
        store.members.add(new GroupMember(newId("m_"), g.id, ownerId,
                GroupMember.STATUS_APPROVED, GroupMember.ROLE_OWNER, now));
        save();
        return g;
    }

    public void deleteGroup(String groupId) {
        if (groupId == null) return;
        Iterator<StudyGroup> gi = store.groups.iterator();
        while (gi.hasNext()) if (Objects.equals(gi.next().id, groupId)) gi.remove();
        Iterator<GroupMember> mi = store.members.iterator();
        while (mi.hasNext()) if (Objects.equals(mi.next().groupId, groupId)) mi.remove();
        List<String> sessionIds = new ArrayList<>();
        Iterator<StudySession> si = store.sessions.iterator();
        while (si.hasNext()) {
            StudySession s = si.next();
            if (Objects.equals(s.groupId, groupId)) {
                sessionIds.add(s.id);
                si.remove();
            }
        }
        Iterator<SessionFeedback> fi = store.feedbacks.iterator();
        while (fi.hasNext()) if (sessionIds.contains(fi.next().sessionId)) fi.remove();
        save();
    }

    /** Bản ghi thành viên (mọi trạng thái) của userId trong nhóm, hoặc null. */
    public GroupMember getMembership(String groupId, String userId) {
        if (groupId == null || userId == null) return null;
        for (GroupMember m : store.members) {
            if (Objects.equals(m.groupId, groupId) && Objects.equals(m.userId, userId)) return m;
        }
        return null;
    }

    private GroupMember getMemberRecord(String memberId) {
        if (memberId == null) return null;
        for (GroupMember m : store.members) {
            if (Objects.equals(m.id, memberId)) return m;
        }
        return null;
    }

    public List<GroupMember> getMembersByStatus(String groupId, String status) {
        List<GroupMember> list = new ArrayList<>();
        if (groupId == null || status == null) return list;
        for (GroupMember m : store.members) {
            if (Objects.equals(m.groupId, groupId) && Objects.equals(m.status, status)) list.add(m);
        }
        Collections.sort(list, new Comparator<GroupMember>() {
            @Override
            public int compare(GroupMember a, GroupMember b) {
                if (a.isOwner() != b.isOwner()) return a.isOwner() ? -1 : 1;
                return Long.compare(a.createdAt, b.createdAt);
            }
        });
        return list;
    }

    /** Thành viên chính thức (chủ nhóm đứng đầu). */
    public List<User> getApprovedUsers(String groupId) {
        List<User> users = new ArrayList<>();
        for (GroupMember m : getMembersByStatus(groupId, GroupMember.STATUS_APPROVED)) {
            User u = getUser(m.userId);
            if (u != null) users.add(u);
        }
        return users;
    }

    public int countApproved(String groupId) {
        if (groupId == null) return 0;
        int c = 0;
        for (GroupMember m : store.members) {
            if (Objects.equals(m.groupId, groupId) && m.isApproved()) c++;
        }
        return c;
    }

    public boolean isFull(StudyGroup g) {
        return g != null && countApproved(g.id) >= g.maxMembers;
    }

    /** Tất cả bản ghi thành viên của một người (đã duyệt, chờ duyệt, được mời). */
    public List<GroupMember> getMembershipsOfUser(String userId) {
        List<GroupMember> list = new ArrayList<>();
        if (userId == null) return list;
        for (GroupMember m : store.members) {
            if (Objects.equals(m.userId, userId) && getGroup(m.groupId) != null) list.add(m);
        }
        return list;
    }

    /** Các nhóm mà người dùng là thành viên chính thức. */
    public List<StudyGroup> getGroupsOfUser(String userId) {
        List<StudyGroup> list = new ArrayList<>();
        for (GroupMember m : getMembershipsOfUser(userId)) {
            if (m.isApproved()) {
                StudyGroup g = getGroup(m.groupId);
                if (g != null) list.add(g);
            }
        }
        return list;
    }

    public List<StudyGroup> getOwnedGroups(String userId) {
        List<StudyGroup> list = new ArrayList<>();
        if (userId == null) return list;
        for (StudyGroup g : store.groups) {
            if (Objects.equals(g.ownerId, userId)) list.add(g);
        }
        return list;
    }

    /** Số thông báo chưa xử lý: yêu cầu tham gia nhóm mình quản lý + lời mời mình nhận được. */
    public int countNotifications(String userId) {
        int count = 0;
        for (StudyGroup g : getOwnedGroups(userId)) {
            count += getMembersByStatus(g.id, GroupMember.STATUS_PENDING).size();
        }
        for (GroupMember m : getMembershipsOfUser(userId)) {
            if (m.isInvited()) count++;
        }
        return count;
    }

    public void requestJoin(String groupId, String userId) {
        StudyGroup g = requireGroup(groupId);
        GroupMember existing = getMembership(groupId, userId);
        if (existing != null) {
            if (existing.isApproved()) throw new IllegalStateException("Bạn đã là thành viên nhóm này");
            if (existing.isPending()) throw new IllegalStateException("Bạn đã gửi yêu cầu, vui lòng chờ duyệt");
            if (existing.isInvited()) {
                acceptInvite(groupId, userId);
                return;
            }
        }
        if (isFull(g)) throw new IllegalStateException("Nhóm đã đủ thành viên");
        store.members.add(new GroupMember(newId("m_"), groupId, userId,
                GroupMember.STATUS_PENDING, GroupMember.ROLE_MEMBER, System.currentTimeMillis()));
        save();
    }

    public void cancelRequest(String groupId, String userId) {
        GroupMember m = getMembership(groupId, userId);
        if (m != null && m.isPending()) {
            store.members.remove(m);
            save();
        }
    }

    public void approve(String memberId) {
        GroupMember m = getMemberRecord(memberId);
        if (m == null || !m.isPending()) throw new IllegalStateException("Yêu cầu không còn tồn tại");
        if (isFull(requireGroup(m.groupId))) throw new IllegalStateException("Nhóm đã đủ thành viên");
        m.status = GroupMember.STATUS_APPROVED;
        m.createdAt = System.currentTimeMillis();
        save();
    }

    public void reject(String memberId) {
        GroupMember m = getMemberRecord(memberId);
        if (m != null) {
            if (m.isOwner()) throw new IllegalStateException("Không thể xóa chủ nhóm");
            store.members.remove(m);
            save();
        }
    }

    public void invite(String groupId, String userId) {
        StudyGroup g = requireGroup(groupId);
        if (userId == null || getUser(userId) == null) {
            throw new IllegalArgumentException("Không tìm thấy sinh viên");
        }
        GroupMember existing = getMembership(groupId, userId);
        if (existing != null) {
            if (existing.isApproved()) throw new IllegalStateException("Người này đã là thành viên");
            if (existing.isInvited()) throw new IllegalStateException("Bạn đã mời người này rồi");
            if (existing.isPending()) {
                // Người đó đã xin vào => mời đồng nghĩa với duyệt
                approve(existing.id);
                return;
            }
        }
        if (isFull(g)) throw new IllegalStateException("Nhóm đã đủ thành viên");
        store.members.add(new GroupMember(newId("m_"), groupId, userId,
                GroupMember.STATUS_INVITED, GroupMember.ROLE_MEMBER, System.currentTimeMillis()));
        save();
    }

    public void acceptInvite(String groupId, String userId) {
        GroupMember m = getMembership(groupId, userId);
        if (m == null || !m.isInvited()) throw new IllegalStateException("Lời mời không còn tồn tại");
        if (isFull(requireGroup(groupId))) throw new IllegalStateException("Nhóm đã đủ thành viên");
        m.status = GroupMember.STATUS_APPROVED;
        m.createdAt = System.currentTimeMillis();
        save();
    }

    public void declineInvite(String groupId, String userId) {
        GroupMember m = getMembership(groupId, userId);
        if (m != null && m.isInvited()) {
            store.members.remove(m);
            save();
        }
    }

    public void leaveGroup(String groupId, String userId) {
        GroupMember m = getMembership(groupId, userId);
        if (m == null) return;
        if (m.isOwner()) throw new IllegalStateException("Chủ nhóm không thể rời nhóm");
        store.members.remove(m);
        save();
    }

    public void removeMember(String memberId) {
        GroupMember m = getMemberRecord(memberId);
        if (m == null) return;
        if (m.isOwner()) throw new IllegalStateException("Không thể xóa chủ nhóm");
        store.members.remove(m);
        save();
    }

    private StudyGroup requireGroup(String groupId) {
        StudyGroup g = getGroup(groupId);
        if (g == null) throw new IllegalStateException("Nhóm không tồn tại");
        return g;
    }

    // ==================================================================
    // Buổi học
    // ==================================================================

    public StudySession createSession(String groupId, String createdBy, String title, long startTime,
                                      int durationMinutes, String location, String note) {
        requireGroup(groupId);
        GroupMember membership = getMembership(groupId, createdBy);
        if (membership == null || !membership.isApproved()) {
            throw new IllegalStateException("Bạn không phải thành viên chính thức của nhóm này");
        }
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng nhập chủ đề buổi học");
        if (location == null || location.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng nhập địa điểm");
        if (startTime <= System.currentTimeMillis()) throw new IllegalArgumentException("Thời gian phải ở tương lai");
        if (durationMinutes < 15) throw new IllegalArgumentException("Buổi học tối thiểu 15 phút");

        StudySession s = new StudySession();
        s.id = newId("s_");
        s.groupId = groupId;
        s.createdBy = createdBy;
        s.title = title.trim();
        s.startTime = startTime;
        s.durationMinutes = durationMinutes;
        s.location = location.trim();
        s.note = note == null ? "" : note.trim();
        store.sessions.add(s);
        save();
        return s;
    }

    public StudySession getSession(String id) {
        if (id == null) return null;
        for (StudySession s : store.sessions) {
            if (Objects.equals(s.id, id)) return s;
        }
        return null;
    }

    public void deleteSession(String sessionId) {
        if (sessionId == null) return;
        Iterator<StudySession> it = store.sessions.iterator();
        while (it.hasNext()) if (Objects.equals(it.next().id, sessionId)) it.remove();
        Iterator<SessionFeedback> fi = store.feedbacks.iterator();
        while (fi.hasNext()) if (Objects.equals(fi.next().sessionId, sessionId)) fi.remove();
        save();
    }

    private static final Comparator<StudySession> BY_START = new Comparator<StudySession>() {
        @Override
        public int compare(StudySession a, StudySession b) {
            return Long.compare(a.startTime, b.startTime);
        }
    };

    /** Buổi học của nhóm: sắp diễn ra trước (tăng dần), đã qua sau (mới nhất trước). */
    public List<StudySession> getSessionsOfGroup(String groupId) {
        long now = System.currentTimeMillis();
        List<StudySession> upcoming = new ArrayList<>();
        List<StudySession> past = new ArrayList<>();
        if (groupId == null) return upcoming;
        for (StudySession s : store.sessions) {
            if (!Objects.equals(s.groupId, groupId)) continue;
            if (s.isFinished(now)) past.add(s);
            else upcoming.add(s);
        }
        Collections.sort(upcoming, BY_START);
        Collections.sort(past, Collections.reverseOrder(BY_START));
        upcoming.addAll(past);
        return upcoming;
    }

    /** Buổi học chưa kết thúc của tất cả các nhóm người dùng tham gia. */
    public List<StudySession> getUpcomingSessionsOfUser(String userId) {
        long now = System.currentTimeMillis();
        List<String> groupIds = new ArrayList<>();
        for (StudyGroup g : getGroupsOfUser(userId)) groupIds.add(g.id);
        List<StudySession> list = new ArrayList<>();
        for (StudySession s : store.sessions) {
            if (groupIds.contains(s.groupId) && !s.isFinished(now)) list.add(s);
        }
        Collections.sort(list, BY_START);
        return list;
    }

    // ==================================================================
    // Đánh giá buổi học
    // ==================================================================

    public void rateSession(String sessionId, String userId, int rating, String comment) {
        StudySession session = getSession(sessionId);
        if (session == null) throw new IllegalStateException("Buổi học không tồn tại");
        if (!session.isFinished(System.currentTimeMillis())) {
            throw new IllegalStateException("Chỉ có thể đánh giá sau khi buổi học kết thúc");
        }
        GroupMember membership = getMembership(session.groupId, userId);
        if (membership == null || !membership.isApproved()) {
            throw new IllegalStateException("Bạn không phải thành viên của nhóm học này");
        }
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Vui lòng chọn số sao (1–5)");
        SessionFeedback existing = getFeedback(sessionId, userId);
        if (existing != null) store.feedbacks.remove(existing);
        store.feedbacks.add(new SessionFeedback(sessionId, userId, rating,
                comment == null ? "" : comment.trim(), System.currentTimeMillis()));
        save();
    }

    public SessionFeedback getFeedback(String sessionId, String userId) {
        if (sessionId == null || userId == null) return null;
        for (SessionFeedback f : store.feedbacks) {
            if (Objects.equals(f.sessionId, sessionId) && Objects.equals(f.userId, userId)) return f;
        }
        return null;
    }

    public List<SessionFeedback> getFeedbacks(String sessionId) {
        List<SessionFeedback> list = new ArrayList<>();
        if (sessionId == null) return list;
        for (SessionFeedback f : store.feedbacks) {
            if (Objects.equals(f.sessionId, sessionId)) list.add(f);
        }
        return list;
    }

    public float averageRating(String sessionId) {
        List<SessionFeedback> list = getFeedbacks(sessionId);
        if (list.isEmpty()) return 0f;
        int sum = 0;
        for (SessionFeedback f : list) sum += f.rating;
        return sum / (float) list.size();
    }
}
