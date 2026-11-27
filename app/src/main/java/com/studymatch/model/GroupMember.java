package com.studymatch.model;

/**
 * Quan hệ thành viên – nhóm (collection "groupMembers").
 * status:
 *  - PENDING : sinh viên đã gửi yêu cầu, chờ chủ nhóm duyệt
 *  - INVITED : chủ nhóm đã mời, chờ sinh viên chấp nhận
 *  - APPROVED: đã là thành viên
 * Khi yêu cầu bị từ chối / lời mời bị từ chối, bản ghi bị xóa để có thể gửi lại sau.
 */
public class GroupMember {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_INVITED = "INVITED";
    public static final String STATUS_APPROVED = "APPROVED";

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_MEMBER = "MEMBER";

    public String id;
    public String groupId;
    public String userId;
    public String status;
    public String role;
    public long createdAt;

    public GroupMember() {
    }

    public GroupMember(String id, String groupId, String userId, String status, String role, long createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.status = status;
        this.role = role;
        this.createdAt = createdAt;
    }

    public boolean isApproved() {
        return STATUS_APPROVED.equals(status);
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isInvited() {
        return STATUS_INVITED.equals(status);
    }

    public boolean isOwner() {
        return ROLE_OWNER.equals(role);
    }
}
