# StudyMatch – Ứng dụng tìm bạn học và ghép nhóm học tập thông minh

> **Tên đề tài:** Xây dựng ứng dụng Android hỗ trợ tìm kiếm và ghép nhóm học tập cho sinh viên dựa trên môn học và lịch rảnh.  
> **Công nghệ:** Android Studio • Java • XML + Material Design • Dữ liệu giả lập lưu cục bộ (sẵn sàng chuyển sang Firebase Auth & Cloud Firestore).

---

## 1. Kiến trúc & Danh sách 6 cụm màn hình chính

Dự án được chia thành 4 package rõ ràng để dễ bảo trì, viết báo cáo và mở rộng sang Firebase:

| Package | Vai trò |
|---|---|
| `com.studymatch.model` | 5 thực thể cốt lõi (`User`, `TimeSlot`, `StudyGroup`, `GroupMember`, `StudySession`) + `Subject`, `SessionFeedback` |
| `com.studymatch.matching` | Thuật toán chấm điểm ghép bạn (`MatchingEngine`), bộ lọc (`MatchFilter`), đề xuất khung giờ chung (`SlotSuggestion`) |
| `com.studymatch.data` | Danh mục môn/mục tiêu (`Catalog`), dữ liệu mẫu (`MockData`), tầng lưu trữ duy nhất (`Repository`, `DataStore`) |
| `com.studymatch.ui` | 6 cụm màn hình giao diện & các `RecyclerView.Adapter` |

### Ánh xạ 6 màn hình theo đề xuất đồ án

1. **Đăng nhập / Đăng ký (`LoginActivity`, `RegisterActivity`)**: Hỗ trợ vào nhanh bằng tài khoản mẫu hoặc đổi qua lại giữa 11 sinh viên mẫu để demo luồng xin vào nhóm – duyệt thành viên – mời nhóm.
2. **Trang chủ (`HomeFragment`)**: Chào sinh viên, nhắc hoàn thiện hồ sơ, thông báo yêu cầu/lời mời nhóm, danh sách môn học (chạm để lọc), buổi học sắp tới và top 5 bạn học phù hợp nhất.
3. **Tìm bạn học (`FindPartnerFragment`, `UserDetailActivity`)**: Bộ lọc theo môn học, thứ trong tuần, mục tiêu học tập và công tắc bắt buộc trùng lịch; hiển thị bảng phân tích điểm chi tiết 4 tiêu chí.
4. **Quản lý nhóm (`GroupsFragment`, `CreateGroupActivity`, `GroupDetailActivity`)**: Tạo nhóm, giới hạn số lượng thành viên, gửi/hủy yêu cầu tham gia, chủ nhóm duyệt/từ chối yêu cầu, mời bạn phù hợp và xem khung giờ học chung được đề xuất.
5. **Lịch học nhóm (`ScheduleFragment`, `CreateSessionActivity`)**: Xem lịch tổng hợp các buổi học sắp diễn ra, chọn 1 chạm khung giờ chung đông thành viên rảnh nhất để tạo buổi học, kiểm tra trực tiếp ai rảnh và **đánh giá sao sau buổi học**.
6. **Hồ sơ cá nhân (`ProfileFragment`, `EditProfileActivity`)**: Quản lý môn học, mục tiêu, trình độ, các khung giờ rảnh trong tuần, công tắc **Ẩn lịch rảnh chi tiết** (quyền riêng tư) và nút khôi phục dữ liệu demo.

---

## 2. Thuật toán ghép nhóm (`MatchingEngine`)

### 2.1. Công thức chấm điểm độ phù hợp (0–100%)
- **Cùng môn học (`40 điểm`)**: Có ít nhất 1 môn học chung (hoặc cùng học môn đang lọc).
- **Thời gian rảnh trùng nhau (`tối đa 30 điểm`)**: Chuẩn hóa và tìm giao của hai lịch rảnh trong tuần; điểm tính tỉ lệ tuyến tính theo tổng số phút trùng và đạt tối đa `30/30` khi trùng từ **120 phút/tuần** trở lên:
  $$\text{Điểm thời gian} = \text{round}\left(30 \times \frac{\min(\text{số phút trùng}, 120)}{120}\right)$$
- **Cùng mục tiêu học tập (`20 điểm`)**: Có ít nhất 1 mục tiêu chung (*Ôn thi cuối kỳ, Làm bài tập, Luyện lập trình, Làm đồ án, Học trước bài mới*).
- **Trình độ học tập (`10 điểm`)**: Cùng bậc đạt `10 điểm`, lệch 1 bậc đạt `5 điểm`, lệch 2 bậc đạt `0 điểm`.
- **Điều kiện lọc trước xếp hạng (`isEligible`)**: Cùng môn học và trùng lịch rảnh ít nhất **30 phút/tuần**.

### 2.2. Tự động đề xuất lịch học chung cho nhóm (`suggestCommonSlots`)
Sử dụng thuật toán quét điểm mốc (*sweep-line*) trên từng ngày từ Thứ 2 đến Chủ nhật:
1. Chuẩn hóa lịch rảnh của từng thành viên chính thức (gộp các đoạn chồng lấn/liền kề).
2. Thu thập tất cả mốc thời gian bắt đầu/kết thúc trong ngày, xét từng khoảng con liên tiếp và đếm tập thành viên rảnh trọn khoảng đó.
3. Gộp các khoảng liền kề có cùng tập thành viên rảnh, lọc các khung giờ từ `60 phút` trở lên và sắp xếp giảm dần theo **(số thành viên rảnh, thời lượng)**.

---

## 3. Hướng dẫn chạy Demo trên Android Studio

1. Mở **Android Studio** -> chọn **Open** -> trỏ tới thư mục `StudyMatch`.
2. Đợi Android Studio đồng bộ Gradle và tự tạo `local.properties` theo Android SDK trên máy.
3. Chọn thiết bị ảo (API 24+) hoặc điện thoại thật -> bấm **Run (`Shift + F10`)**.
4. Chạy kiểm thử thuật toán bằng JUnit: mở file `app/src/test/java/com/studymatch/matching/MatchingEngineTest.java` và bấm nút **Run Test**.

### Tài khoản mẫu có sẵn (mật khẩu chung: `123456`)
- **`demo@studymatch.vn` (Nguyễn Thế Mạnh – CNTT – K29)**: Đang làm chủ nhóm *"Android Tối Thứ Ba"* (có sẵn 2 yêu cầu xin tham gia chờ duyệt từ Bảo Ngọc & Minh Khoa) và nhận 1 lời mời vào nhóm *"Ôn thi CTDL & GT"*, kèm 1 buổi học đã kết thúc để thử tính năng đánh giá sao.
- **`binh@studymatch.vn` (Trần Quốc Bình)**: Minh họa đúng ví dụ trong đề xuất — đạt **80% (Rất phù hợp)** với Thế Mạnh ở môn Lập trình Android.
- **`quynh@studymatch.vn` (Mai Như Quỳnh)**: Đang bật chế độ **Ẩn lịch rảnh chi tiết** để minh họa tính năng quyền riêng tư.

---

## 4. Hướng dẫn nâng cấp sang Firebase (khi cần triển khai thật)

Toàn bộ giao diện chỉ gọi dữ liệu thông qua lớp `com.studymatch.data.Repository`. Khi muốn kết nối Firebase:
1. Tạo project trên Firebase Console, tải `google-services.json` đặt vào `app/google-services.json`.
2. Thêm plugin `com.google.gms.google-services` và các thư viện `firebase-auth`, `firebase-firestore` vào `app/build.gradle`.
3. Ánh xạ 5 danh sách trong `DataStore` sang 5 collection trên Cloud Firestore:
   - `users/{userId}`
   - `users/{userId}/availability/{slotId}` (hoặc mảng `availability` trong document `users/{userId}`)
   - `groups/{groupId}`
   - `groupMembers/{memberId}`
   - `studySessions/{sessionId}` (kèm sub-collection `feedbacks/{userId}`)
