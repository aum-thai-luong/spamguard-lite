---
name: room-migration
description: Thay đổi schema Room an toàn — thêm/sửa/xoá bảng, cột, index — kèm Migration, export schema và migration test. Dùng khi được yêu cầu sửa Entity Room, tăng version database, viết migration, hoặc khi gặp lỗi "Room cannot verify the data integrity" / "Migration didn't properly handle".
---

# room-migration

Mục tiêu: người dùng đang có dữ liệu trên máy **không bao giờ mất dữ liệu** khi cập nhật app.

## 1. Kiểm tra hiện trạng trước khi sửa

1. Tìm class `@Database`: ghi lại `version` hiện tại, danh sách entities, các `Migration`/`AutoMigration` đã có.
2. Kiểm tra schema có được export không:
   - `@Database(exportSchema = true)`
   - Cấu hình `room { schemaDirectory("$projectDir/schemas") }` (Room Gradle plugin) hoặc tham số KSP `room.schemaLocation`
   - Thư mục `schemas/` có file JSON cho version hiện tại và đã commit vào Git
3. Nếu chưa export schema → **dừng lại, báo người dùng**. Không có schema của version cũ thì không viết được migration test.
4. Tìm `fallbackToDestructiveMigration` trong builder. Nếu có ở build release → cảnh báo: mọi lần đổi schema sẽ xoá dữ liệu người dùng.

## 2. Xác định loại thay đổi

| Thay đổi | Cách làm |
| --- | --- |
| Thêm bảng, thêm cột có default/nullable, thêm index | `AutoMigration(from = N, to = N + 1)` là đủ |
| Đổi tên cột/bảng | `AutoMigration` + `AutoMigrationSpec` với `@RenameColumn` / `@RenameTable` |
| Xoá cột/bảng | `AutoMigration` + `@DeleteColumn` / `@DeleteTable` — hỏi người dùng xác nhận vì mất dữ liệu cột đó |
| Đổi kiểu dữ liệu, tách/gộp bảng, chuyển dữ liệu | `Migration` viết tay (tạo bảng mới → copy dữ liệu → xoá bảng cũ → đổi tên) |
| Thêm cột NOT NULL không có default | Bắt buộc có `DEFAULT` trong SQL migration |

Trình bày kế hoạch (version cũ → mới, loại migration, SQL dự kiến) và chờ người dùng duyệt nếu có xoá dữ liệu hoặc migration viết tay.

## 3. Thực hiện

1. Sửa Entity (và DAO nếu cần).
2. Tăng `version` lên đúng 1.
3. Thêm migration:

```kotlin
// AutoMigration đơn giản
@Database(
    entities = [BlockedNumberEntity::class, CallLogEntity::class],
    version = 4,
    autoMigrations = [AutoMigration(from = 3, to = 4)],
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase()

// Migration viết tay: thêm cột NOT NULL có default + chuẩn hoá dữ liệu cũ
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE blocked_number ADD COLUMN source TEXT NOT NULL DEFAULT 'manual'")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_blocked_number_prefix ON blocked_number(prefix)")
    }
}
```

4. Đăng ký `Migration` viết tay trong builder: `.addMigrations(MIGRATION_4_5)`.
5. Build để Room sinh file schema mới trong `schemas/` và thêm file đó vào commit.

Lưu ý khi viết SQL:
- SQL phải khớp chính xác tên bảng, tên cột, kiểu và index mà Room sinh ra — đối chiếu với file JSON schema của version mới.
- SQLite cũ không hỗ trợ `DROP COLUMN`/đổi kiểu cột: dùng cách tạo bảng mới + copy dữ liệu.
- Không chạy migration nặng (hàng trăm nghìn dòng) mà không cảnh báo về thời gian mở database lần đầu.

## 4. Migration test — bắt buộc

```kotlin
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate4To5_keepsExistingRows() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL("INSERT INTO blocked_number (number, prefix) VALUES ('0900000000', '0900')")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)

        db.query("SELECT source FROM blocked_number").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("manual", cursor.getString(0))
        }
    }

    @Test
    fun migrateAll_fromFirstVersionToLatest() {
        helper.createDatabase(TEST_DB, 1).close()
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            TEST_DB,
        ).addMigrations(*ALL_MIGRATIONS).build().apply {
            openHelper.writableDatabase.close()
        }
    }

    private companion object { const val TEST_DB = "migration-test" }
}
```

Dữ liệu trong test là dữ liệu giả; không dùng số điện thoại thật.

## 5. Kiểm chứng

```bash
./gradlew <module>:compileDebugKotlin       # sinh schema mới
git status schemas/                          # phải có file <version mới>.json
./gradlew <module>:connectedDebugAndroidTest --tests "*MigrationTest*"   # cần emulator/thiết bị
```

Không có thiết bị: báo rõ migration test **chưa được chạy**, không được nói là đã qua.

## 6. Báo cáo

- Version cũ → mới, loại migration, SQL đã dùng
- File schema mới đã sinh
- Kết quả migration test (hoặc lý do chưa chạy)
- Rủi ro: dữ liệu bị xoá (nếu có), thời gian migration với dữ liệu lớn
