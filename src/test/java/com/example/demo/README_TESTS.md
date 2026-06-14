# Test Cases Documentation

## Tổng quan

Đã tạo test cases cho 6 APIs quan trọng:
- **Authentication**: 2 APIs (register, login) - 12 test cases
- **Meeting**: 4 APIs (create, join, leave, end) - 16 test cases

**Tổng cộng**: 6 APIs với 28 test cases

## Cấu trúc Test Files

```
src/test/java/com/example/demo/
├── controller/
│   ├── AuthControllerTest.java      (12 test cases)
│   └── MeetingControllerTest.java   (16 test cases)
└── README_TESTS.md                  (file này)
```

## Chi tiết Test Cases

### 1. AuthControllerTest (12 test cases)

#### POST /auth/register
- ✅ **TC-AUTH-001**: Đăng ký thành công với thông tin hợp lệ
- ✅ **TC-AUTH-002**: Đăng ký thất bại - Email đã tồn tại (409 Conflict)
- ✅ **TC-AUTH-003**: Đăng ký thất bại - Email không hợp lệ (400 Bad Request)
- ✅ **TC-AUTH-004**: Đăng ký thất bại - Password quá ngắn (400 Bad Request)
- ✅ **TC-AUTH-005**: Đăng ký thất bại - Fullname trống (400 Bad Request)
- ✅ **TC-AUTH-006**: Đăng ký thất bại - Fullname quá dài (400 Bad Request)

#### POST /auth/login
- ✅ **TC-AUTH-007**: Đăng nhập thành công với thông tin đúng
- ✅ **TC-AUTH-008**: Đăng nhập thất bại - Sai password (401 Unauthorized)
- ✅ **TC-AUTH-009**: Đăng nhập thất bại - Email không tồn tại (401 Unauthorized)
- ✅ **TC-AUTH-010**: Đăng nhập thất bại - Email không hợp lệ (400 Bad Request)
- ✅ **TC-AUTH-011**: Đăng nhập thất bại - Password trống (400 Bad Request)
- ✅ **TC-AUTH-012**: Đăng nhập thất bại - Email trống (400 Bad Request)

### 2. MeetingControllerTest (16 test cases)

#### POST /api/meetings (Create Meeting)
- ✅ **TC-MEET-001**: Tạo meeting thành công (201 Created)
- ✅ **TC-MEET-002**: Tạo meeting thất bại - Group không tồn tại (404 Not Found)
- ✅ **TC-MEET-003**: Tạo meeting thất bại - Không phải member của group (403 Forbidden)
- ✅ **TC-MEET-004**: Tạo meeting thất bại - GroupId null (400 Bad Request)

#### POST /api/meetings/{meetingId}/join
- ✅ **TC-MEET-005**: Join meeting thành công (201 Created)
- ✅ **TC-MEET-006**: Join meeting thất bại - Meeting không tồn tại (404 Not Found)
- ✅ **TC-MEET-007**: Join meeting thất bại - Meeting đã kết thúc (400 Bad Request)
- ✅ **TC-MEET-008**: Join meeting thất bại - Đã tham gia meeting (409 Conflict)
- ✅ **TC-MEET-009**: Join meeting thất bại - Không phải member của group (403 Forbidden)

#### POST /api/meetings/{meetingId}/leave
- ✅ **TC-MEET-010**: Leave meeting thành công (201 Created)
- ✅ **TC-MEET-011**: Leave meeting thất bại - Chưa join meeting (403 Forbidden)
- ✅ **TC-MEET-012**: Leave meeting thất bại - Meeting không tồn tại (404 Not Found)

#### POST /api/meetings/{meetingId}/end
- ✅ **TC-MEET-013**: End meeting thành công bởi host (201 Created)
- ✅ **TC-MEET-014**: End meeting thất bại - Không phải host (403 Forbidden)
- ✅ **TC-MEET-015**: End meeting thất bại - Meeting đã ended (400 Bad Request)
- ✅ **TC-MEET-016**: End meeting thất bại - Meeting không tồn tại (404 Not Found)

## Cách chạy Tests

### 1. Chạy tất cả tests
```bash
mvn test
```

### 2. Chạy test cho một class cụ thể
```bash
# Chạy AuthControllerTest
mvn test -Dtest=AuthControllerTest

# Chạy MeetingControllerTest
mvn test -Dtest=MeetingControllerTest
```

### 3. Chạy một test method cụ thể
```bash
# Chạy test case TC-AUTH-001
mvn test -Dtest=AuthControllerTest#testRegister_Success

# Chạy test case TC-MEET-001
mvn test -Dtest=MeetingControllerTest#testCreateMeeting_Success
```

### 4. Chạy tests với coverage report
```bash
mvn clean test jacoco:report
```

Coverage report sẽ được tạo tại: `target/site/jacoco/index.html`

### 5. Chạy tests trong IDE

**IntelliJ IDEA**:
- Right-click vào test class hoặc test method
- Chọn "Run 'TestName'" hoặc nhấn `Ctrl+Shift+F10`

**Eclipse**:
- Right-click vào test class
- Chọn "Run As" > "JUnit Test"

## Công nghệ sử dụng

- **JUnit 5**: Framework testing
- **Spring Boot Test**: Integration testing support
- **MockMvc**: Test REST endpoints
- **Mockito**: Mock dependencies (services)
- **@WebMvcTest**: Test controller layer riêng biệt
- **@WithMockUser**: Mock authenticated user

## Lưu ý quan trọng

### 1. Mock vs Integration Tests
Các tests hiện tại là **Unit Tests** cho controller layer:
- Sử dụng `@WebMvcTest` để test riêng controller
- Mock service layer với `@MockBean`
- Không kết nối database thực
- Chạy nhanh, phù hợp cho CI/CD

### 2. Security Context
Tất cả tests sử dụng `@WithMockUser` để bypass authentication vì:
- Project có Spring Security enabled
- Cần mock user đã authenticated
- Test focus vào business logic, không test security

### 3. CSRF Protection
Tất cả POST requests cần `.with(csrf())` vì:
- Spring Security bật CSRF protection mặc định
- Test sẽ fail nếu không có CSRF token

### 4. Test Data
Tests sử dụng mock data:
- Không cần setup database
- Không ảnh hưởng data thực
- Có thể control response từ service layer

## Mở rộng Tests

### Thêm Integration Tests
Nếu muốn test với database thực, tạo integration tests:

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testRegister_WithRealDatabase() {
        // Test với database thực
    }
}
```

### Thêm Test Utilities
Tạo helper class cho test data:

```java
public class TestDataBuilder {
    public static RegisterRequest createValidRegisterRequest() {
        return RegisterRequest.builder()
            .email("test@example.com")
            .password("password123")
            .fullname("Test User")
            .build();
    }
}
```

## Troubleshooting

### Lỗi: "No tests found"
- Kiểm tra class có annotation `@WebMvcTest`
- Kiểm tra method có annotation `@Test`
- Rebuild project: `mvn clean compile`

### Lỗi: "403 Forbidden"
- Thêm `@WithMockUser` vào test method
- Thêm `.with(csrf())` vào request

### Lỗi: "Bean not found"
- Kiểm tra `@MockBean` cho tất cả dependencies
- Kiểm tra import đúng class

### Tests chạy chậm
- Đang dùng `@SpringBootTest` thay vì `@WebMvcTest`
- `@WebMvcTest` chỉ load controller layer, nhanh hơn

## Metrics

### Coverage Targets
- **Controllers**: ≥ 80%
- **Services**: ≥ 70%
- **Overall**: ≥ 75%

### Test Execution Time
- Unit tests (hiện tại): < 5 giây
- Integration tests: < 30 giây
- Full test suite: < 1 phút

## Best Practices

1. **Tên test rõ ràng**: Sử dụng `@DisplayName` với mô tả tiếng Việt
2. **AAA Pattern**: Arrange - Act - Assert
3. **Một assertion chính**: Mỗi test focus vào một scenario
4. **Mock chính xác**: Chỉ mock dependencies cần thiết
5. **Clean code**: DRY principle, extract common setup
6. **Fast tests**: Unit tests < 100ms, integration tests < 1s

## Next Steps

Để hoàn thiện test suite, cần thêm:

1. ✅ Auth APIs (2/2) - Hoàn thành
2. ✅ Meeting APIs (4/4) - Hoàn thành
3. ⏳ Group APIs (5 APIs)
4. ⏳ Post APIs (1 API)
5. ⏳ Message APIs (1 API)
6. ⏳ Integration tests
7. ⏳ E2E flow tests
8. ⏳ Performance tests

## Tài liệu tham khảo

- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [MockMvc Testing](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-mvc-test-framework)
