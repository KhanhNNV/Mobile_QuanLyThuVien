# Kiến trúc dự án (Project Architecture)

Dự án này được xây dựng theo mô hình **Client-Server**, chia thành hai phần chính:
1.  **Android App (Client)**: Ứng dụng di động dành cho người dùng và nhân viên.
2.  **Spring Boot Backend (Server)**: Cung cấp API và quản lý cơ sở dữ liệu.

---

## 1. Kiến trúc trên Android

Ứng dụng Android tuân thủ kiến trúc **MVVM (Model-View-ViewModel)** kết hợp với **Repository Pattern**. Đây là kiến trúc tiêu chuẩn được Google khuyến nghị nhằm tách biệt rõ ràng giữa giao diện người dùng và logic nghiệp vụ.

### Các thành phần chính:

*   **View (Giao diện)**:
    *   Sử dụng **Fragments** (ví dụ: `LoginFragment`, `BooksFragment`) để hiển thị giao diện.
    *   View chỉ có nhiệm vụ hiển thị dữ liệu và nhận tương tác từ người dùng. Nó không chứa logic xử lý dữ liệu.
    *   Sử dụng **Navigation Component** để quản lý luồng di chuyển giữa các màn hình.

*   **ViewModel**:
    *   Lớp trung gian giữa View và Repository (ví dụ: `LoginViewModel`).
    *   Sử dụng **StateFlow** hoặc **LiveData** để quản lý trạng thái giao diện (UI State).
    *   Xử lý logic liên quan đến UI và gọi các hàm từ Repository trong **viewModelScope** (Coroutines).

*   **Repository (Kho dữ liệu)**:
    *   Đóng vai trò là nguồn dữ liệu duy nhất cho ViewModel (ví dụ: `AuthRepository`, `BookRepository`).
    *   Tách biệt logic truy xuất dữ liệu từ các nguồn khác nhau (Remote API hoặc Local Database).

*   **Model (Dữ liệu)**:
    *   Bao gồm các POJO/Data classes đại diện cho dữ liệu (Request/Response API).

*   **Data Sources (Nguồn dữ liệu)**:
    *   **Remote**: Sử dụng **Retrofit** để gọi API từ Backend.
    *   **Local**: Cấu hình có sẵn cho **Room Database** (mặc dù hiện tại chủ yếu tương tác qua API).

### Công nghệ sử dụng:
*   **Language**: Kotlin.
*   **Networking**: Retrofit 2, OkHttp 3.
*   **Asynchronous**: Kotlin Coroutines & Flow.
*   **DI/Architecture**: ViewModel, ViewModelProvider.Factory, Navigation Component.

---

## 2. Kiến trúc trên Backend (mobileBE)

Phần Backend được viết bằng **Spring Boot** theo kiến trúc **Layered Architecture (Kiến trúc phân lớp)**:

*   **Controller Layer**: Tiếp nhận các yêu cầu HTTP (REST API), điều hướng đến các Service tương ứng (ví dụ: `AuthController`).
*   **Service Layer**: Chứa logic nghiệp vụ (Business Logic) của ứng dụng (ví dụ: `AuthenticationService`).
*   **Repository Layer**: Tương tác trực tiếp với cơ sở dữ liệu thông qua **Spring Data JPA**.
*   **Entity/Model Layer**: Định nghĩa các bảng trong cơ sở dữ liệu (MySQL).
*   **Security**: Sử dụng **Spring Security** và **JWT (JSON Web Token)** để bảo mật và phân quyền.

---

## Luồng dữ liệu (Data Flow)
1.  Người dùng tương tác với **View**.
2.  **View** gọi một hàm trong **ViewModel**.
3.  **ViewModel** gọi **Repository**.
4.  **Repository** thực hiện yêu cầu API thông qua **Retrofit**.
5.  **Backend** xử lý yêu cầu và trả về dữ liệu.
6.  Dữ liệu được đẩy ngược lại qua **Flow/StateFlow** từ **ViewModel** đến **View** để cập nhật giao diện.
