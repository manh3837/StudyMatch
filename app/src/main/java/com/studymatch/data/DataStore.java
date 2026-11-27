package com.studymatch.data;

import com.studymatch.model.GroupMember;
import com.studymatch.model.SessionFeedback;
import com.studymatch.model.StudyGroup;
import com.studymatch.model.StudySession;
import com.studymatch.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Toàn bộ dữ liệu của bản demo, được lưu dưới dạng JSON trong SharedPreferences.
 * Mỗi danh sách tương ứng một collection Firestore khi triển khai thật.
 */
public class DataStore {
    public static final int CURRENT_VERSION = 2;

    public int version = CURRENT_VERSION;
    public List<User> users = new ArrayList<>();
    /** userId -> SHA-256(mật khẩu). Firebase Authentication sẽ thay thế phần này. */
    public Map<String, String> passwordHashes = new HashMap<>();
    public List<StudyGroup> groups = new ArrayList<>();
    public List<GroupMember> members = new ArrayList<>();
    public List<StudySession> sessions = new ArrayList<>();
    public List<SessionFeedback> feedbacks = new ArrayList<>();

    void ensureNotNull() {
        if (users == null) users = new ArrayList<>();
        if (passwordHashes == null) passwordHashes = new HashMap<>();
        if (groups == null) groups = new ArrayList<>();
        if (members == null) members = new ArrayList<>();
        if (sessions == null) sessions = new ArrayList<>();
        if (feedbacks == null) feedbacks = new ArrayList<>();

        Iterator<User> ui = users.iterator();
        while (ui.hasNext()) {
            User u = ui.next();
            if (u == null || u.id == null) {
                ui.remove();
                continue;
            }
            if (u.name == null) u.name = "";
            if (u.email == null) u.email = "";
            if (u.major == null) u.major = "";
            if (u.bio == null) u.bio = "";
            if (u.subjectIds == null) u.subjectIds = new ArrayList<>();
            if (u.goals == null) u.goals = new ArrayList<>();
            if (u.availability == null) u.availability = new ArrayList<>();
        }

        Iterator<StudyGroup> gi = groups.iterator();
        while (gi.hasNext()) {
            StudyGroup g = gi.next();
            if (g == null || g.id == null) {
                gi.remove();
                continue;
            }
            if (g.name == null) g.name = "";
            if (g.goal == null) g.goal = "";
            if (g.description == null) g.description = "";
        }

        Iterator<GroupMember> mi = members.iterator();
        while (mi.hasNext()) {
            GroupMember m = mi.next();
            if (m == null || m.id == null || m.groupId == null || m.userId == null || m.status == null) {
                mi.remove();
            }
        }

        Iterator<StudySession> si = sessions.iterator();
        while (si.hasNext()) {
            StudySession s = si.next();
            if (s == null || s.id == null || s.groupId == null) {
                si.remove();
                continue;
            }
            if (s.title == null) s.title = "";
            if (s.location == null) s.location = "";
            if (s.note == null) s.note = "";
        }

        Iterator<SessionFeedback> fi = feedbacks.iterator();
        while (fi.hasNext()) {
            SessionFeedback f = fi.next();
            if (f == null || f.sessionId == null || f.userId == null) {
                fi.remove();
                continue;
            }
            if (f.comment == null) f.comment = "";
        }
    }
}
