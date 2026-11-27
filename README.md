<img width="100" height="100" src="https://github.com/user-attachments/assets/35e39eb6-1f80-4949-b8f9-eba58c9215f0" />

# StudyMatch

StudyMatch là ứng dụng **tìm bạn học và ghép nhóm học tập thông minh** dành cho sinh viên trên nền tảng **Android 7.0+ (API 24+)**.

Khác với việc tìm nhóm thủ công qua các hội nhóm mạng xã hội vốn dễ lệch khung giờ hoặc khác mục tiêu, StudyMatch sử dụng thuật toán định lượng **`MatchingEngine`** để tự động chấm điểm mức độ tương thích (0–100%) giữa các sinh viên dựa trên **4 tiêu chí cốt lõi**: môn học chung, độ giao thoa lịch rảnh trong tuần, mục tiêu học tập và trình độ hiện tại.

Bên cạnh việc **chấm điểm & xếp hạng bạn học theo thời gian thực**[^1], StudyMatch còn tích hợp thuật toán quét mốc thời gian (*sweep-line*) giúp tự động đề xuất khung giờ học chung đông thành viên rảnh nhất cho cả nhóm, quy trình xin vào nhóm – duyệt thành viên – mời bạn học, cùng tính năng bảo vệ quyền riêng tư và đánh giá sao sau mỗi buổi học.

[^1]: Thuật toán tự động chuẩn hóa và gộp các khoảng thời gian rảnh chồng lấn/liền kề trước khi tính giao thoa. Điều này giúp kết quả ghép cặp luôn chính xác ngay cả khi sinh viên nhập nhiều khung giờ lẻ trong cùng một ngày.

<table>
  <tr>
    <td>
      <img width=150 src="https://github.com/user-attachments/assets/0693d4d0-5290-448f-b357-149be613f05f"><br>
    </td>
    <td>
      <img width=150 src="https://github.com/user-attachments/assets/5039452a-15e0-4630-a526-36c44ecac931"><br>
    </td>
    <td>
      <img width=150 src="https://github.com/user-attachments/assets/5d33facc-1f2a-40c0-92ee-6e5ad1070142"><br>
    </td>
    <td>
      <img width=150 src="https://github.com/user-attachments/assets/68acf009-b032-4ca5-a8ff-0dcd953995bf"><br>
    </td>
    <td>
      <img width=150 src="https://github.com/user-attachments/assets/1750876e-2472-4e38-aa37-64dde60ecd49"><br>
    </td>
  </tr>
</table>

## Cài đặt (Installation)

Bản dựng mới nhất luôn có sẵn để tải về tại trang [Releases](https://github.com/manh3837/StudyMatch/releases).

Nếu bạn gặp thắc mắc hoặc cần xem ảnh chụp giao diện trong quá trình sử dụng, vui lòng tham khảo tại [Issues / Thảo luận](https://github.com/manh3837/StudyMatch/issues/1).

StudyMatch hỗ trợ cài đặt trực tiếp qua tệp APK[^2] hoặc chạy trực tiếp từ mã nguồn trên Android Studio[^3]:

<table>
  <thead>
    <tr>
      <th>Phương thức</th>
      <th>Phiên bản Android</th>
      <th>Bản dựng (Tải trực tiếp)</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Cài đặt trực tiếp (Sideload APK)</td>
      <td>Android 7.0+ (API 24 – 35)</td>
      <td><a href="https://github.com/manh3837/StudyMatch/releases/latest/download/StudyMatch.apk"><code>StudyMatch.apk</code></a></td>
    </tr>
    <tr>
      <td>Trình giả lập Android Emulator (AVD)</td>
      <td>Android 7.0+ (x86_64 / arm64)</td>
      <td><a href="https://github.com/manh3837/StudyMatch/releases/latest/download/StudyMatch.apk"><code>StudyMatch.apk</code></a></td>
    </tr>
    <tr>
      <td>Build &amp; Run từ Android Studio</td>
      <td>Android 7.0+ (Debug / Release)</td>
      <td><a href="https://github.com/manh3837/StudyMatch/archive/refs/heads/main.zip"><code>Source Code (.zip)</code></a></td>
    </tr>
  </tbody>
</table>

[^2]: Bản APK đã tích hợp sẵn bộ dữ liệu giả lập gồm **11 hồ sơ sinh viên**, các nhóm học mẫu và lịch sử buổi học để bạn có thể trải nghiệm ngay mọi luồng tính năng mà không cần cấu hình máy chủ.
[^3]: Dữ liệu phát sinh trong quá trình sử dụng (tạo nhóm, duyệt thành viên, đặt lịch học, sửa hồ sơ) được lưu cục bộ bền vững qua `SharedPreferences` + `Gson` và có thể khôi phục về trạng thái demo ban đầu chỉ với 1 chạm trong tab **Hồ sơ**.

> [!NOTE]
> Khi cài đặt qua tệp `StudyMatch.apk` trên thiết bị Android thật, nếu trình duyệt hoặc trình quản lý tệp hiển thị cảnh báo bảo mật, hãy bật **Cho phép cài đặt ứng dụng không rõ nguồn gốc** (*Install unknown apps*). Nếu Google Play Protect hỏi xác nhận đối với ứng dụng đồ án chưa phát hành trên Play Store, hãy chọn **Chi tiết khác (More details) → Vẫn cài đặt (Install anyway)**.

**Tài khoản mẫu có sẵn để trải nghiệm nhanh (mật khẩu chung: `123456`):**

Bạn có thể bấm nút **Vào nhanh tài khoản mẫu** ngay ở màn hình Đăng nhập, hoặc chuyển đổi qua lại giữa 11 sinh viên mẫu để kiểm thử luồng xin vào nhóm – duyệt thành viên – mời nhóm:

- **`demo@studymatch.vn` (Nguyễn Thế Mạnh – CNTT – K29)**: Đang làm chủ nhóm *"Android Tối Thứ Ba"* (có sẵn 2 yêu cầu xin tham gia chờ duyệt từ Bảo Ngọc & Minh Khoa), nhận 1 lời mời vào nhóm *"Ôn thi CTDL & GT"*, kèm 1 buổi học đã kết thúc để thử tính năng đánh giá sao.
- **`binh@studymatch.vn` (Trần Quốc Bình)**: Hồ sơ đạt **80% (Rất phù hợp)** với Thế Mạnh ở môn *Lập trình Android*.
- **`quynh@studymatch.vn` (Mai Như Quỳnh)**: Đang bật chế độ **Ẩn lịch rảnh chi tiết** để minh họa tính năng bảo vệ quyền riêng tư.

> [!IMPORTANT]
> - **Không yêu cầu kết nối Internet hoặc cấu hình Firebase** để chạy bản demo hiện tại.
> - Toàn bộ giao diện gọi dữ liệu thông qua tầng `com.studymatch.data.Repository`, sẵn sàng chuyển đổi sang **Firebase Auth & Cloud Firestore** khi triển khai thực tế.

## Thuật toán & Kiến trúc hệ thống

### 1. Công thức chấm điểm độ phù hợp (`MatchingEngine` — thang 0–100%)

- **Cùng môn học (`40 điểm`)**: Có ít nhất 1 môn học chung (hoặc cùng học môn đang lọc).
- **Thời gian rảnh trùng nhau (`tối đa 30 điểm`)**: Chuẩn hóa và tìm giao của hai lịch rảnh trong tuần; điểm tính tỉ lệ tuyến tính theo tổng số phút trùng và đạt tối đa `30/30` khi trùng từ **120 phút/tuần** trở lên:

$$\text{Điểm thời gian} = \text{round}\left(30 \times \frac{\min(\text{số phút trùng}, 120)}{120}\right)$$

- **Cùng mục tiêu học tập (`20 điểm`)**: Có ít nhất 1 mục tiêu chung (*Ôn thi cuối kỳ, Làm bài tập, Luyện lập trình, Làm đồ án, Học trước bài mới*).
- **Trình độ học tập (`10 điểm`)**: Cùng bậc đạt `10 điểm`, lệch 1 bậc đạt `5 điểm`, lệch 2 bậc đạt `0 điểm`.
- **Điều kiện lọc trước xếp hạng (`isEligible`)**: Cùng môn học và trùng lịch rảnh ít nhất **30 phút/tuần**.

### 2. Tự động đề xuất lịch học chung cho nhóm (`suggestCommonSlots`)

Sử dụng thuật toán quét điểm mốc (*sweep-line*) trên từng ngày từ Thứ 2 đến Chủ nhật:
1. Chuẩn hóa lịch rảnh của từng thành viên chính thức (gộp các đoạn chồng lấn hoặc liền kề).
2. Thu thập tất cả mốc thời gian bắt đầu/kết thúc trong ngày, xét từng khoảng con liên tiếp và đếm tập thành viên rảnh trọn khoảng đó.
3. Gộp các khoảng liền kề có cùng tập thành viên rảnh, lọc các khung giờ từ `60 phút` trở lên và sắp xếp giảm dần theo **(số thành viên rảnh, thời lượng)**.

### 3. Cấu trúc Package & 6 cụm màn hình chính

| Package | Vai trò |
|---|---|
| `com.studymatch.model` | 5 thực thể cốt lõi (`User`, `TimeSlot`, `StudyGroup`, `GroupMember`, `StudySession`) + `Subject`, `SessionFeedback` |
| `com.studymatch.matching` | Thuật toán chấm điểm ghép bạn (`MatchingEngine`), bộ lọc (`MatchFilter`), đề xuất khung giờ chung (`SlotSuggestion`) |
| `com.studymatch.data` | Danh mục môn/mục tiêu (`Catalog`), dữ liệu mẫu (`MockData`), tầng lưu trữ duy nhất (`Repository`, `DataStore`) |
| `com.studymatch.ui` | 6 cụm màn hình giao diện (`Login/Register`, `Home`, `FindPartner`, `Groups`, `Schedule`, `Profile`) & các `RecyclerView.Adapter` |

## Hướng dẫn Build từ mã nguồn (Building)

> [!WARNING]
> Để biên dịch dự án từ mã nguồn, máy tính của bạn cần cài đặt **Android Studio** (khuyến nghị bản Ladybug / Koala trở lên), **JDK 11+** và **Android SDK Platform 35**.

Sao chép kho lưu trữ (Clone repository):

```bash
git clone https://github.com/manh3837/StudyMatch.git
cd StudyMatch
```

Biên dịch tệp cài đặt `app-debug.apk` bằng Gradle Wrapper:

```bash
./gradlew assembleDebug
```

*(Trên Windows PowerShell / CMD, chạy `.\gradlew.bat assembleDebug`. Tệp APK sau khi build sẽ nằm tại `app/build/outputs/apk/debug/app-debug.apk`).*

Chạy bộ kiểm thử đơn vị (JUnit) cho thuật toán ghép nhóm `MatchingEngineTest`:

```bash
./gradlew testDebugUnitTest
```

Hoặc mở trực tiếp thư mục `StudyMatch` trong **Android Studio**, đợi đồng bộ Gradle hoàn tất và nhấn **Run (`Shift + F10`)**.

## Định hướng phát triển & Nâng cấp Firebase (Development)

Dự án được xây dựng nhằm giải quyết bài toán thực tế của sinh viên trong việc tìm kiếm bạn đồng hành học tập cùng môn, hợp lịch rảnh và chung mục tiêu. Khi cần nâng cấp lên hệ thống thời gian thực với **Firebase**:

1. Tạo project trên Firebase Console, tải `google-services.json` đặt vào `app/google-services.json`.
2. Thêm plugin `com.google.gms.google-services` và các thư viện `firebase-auth`, `firebase-firestore` vào `app/build.gradle`.
3. Ánh xạ 5 danh sách trong `DataStore` sang 5 collection trên Cloud Firestore:
   - `users/{userId}`
   - `users/{userId}/availability/{slotId}` (hoặc mảng `availability` trong document `users/{userId}`)
   - `groups/{groupId}`
   - `groupMembers/{memberId}`
   - `studySessions/{sessionId}` (kèm sub-collection `feedbacks/{userId}`)

Mọi đóng góp, báo lỗi hoặc góp ý cải thiện thuật toán ghép nhóm đều được hoan nghênh qua mục [Issues](https://github.com/manh3837/StudyMatch/issues) hoặc [Pull Requests](https://github.com/manh3837/StudyMatch/pulls).

## Công nghệ & Thư viện sử dụng (Acknowledgements)

- [Android Jetpack & AppCompat](https://developer.android.com/jetpack): Nền tảng giao diện tương thích ngược, `Fragment`, `RecyclerView` và kiến trúc thành phần Android.
- [Material Components for Android](https://github.com/material-components/material-components-android): Hệ thống thiết kế Material Design 3 (Cards, Chips, BottomNavigationView, Dialogs).
- [Google Gson](https://github.com/google/gson): Tuần tự hóa và lưu trữ bền vững trạng thái đối tượng cục bộ.
- [JUnit 4](https://junit.org/junit4/): Kiểm thử tự động độ chính xác của thuật toán chấm điểm `MatchingEngine` và đề xuất khung giờ chung.

## Giấy phép (License)

Dự án được phát triển phục vụ mục đích học tập, nghiên cứu và báo cáo đồ án môn học. Xem chi tiết tại kho lưu trữ [StudyMatch](https://github.com/manh3837/StudyMatch).
