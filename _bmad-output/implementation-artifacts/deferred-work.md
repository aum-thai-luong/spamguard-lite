# Deferred work

- source_spec: `_bmad-output/implementation-artifacts/spec-chan-so-khi-nhan-giu-cuoc-goi.md`
  summary: Thêm test cho Fragment chứa dialog (CallLogFragment, CallDetailFragment, BlocklistFragment), kiểm kết quả dialog đi tới ViewModel kể cả sau khi xoay màn hình.
  evidence: Test dialog chỉ nghe trên FragmentManager của chính dialog. Nếu đổi `childFragmentManager` thành `parentFragmentManager` trong CallLogFragment thì mọi test vẫn xanh. Repo chưa có harness để test Fragment Hilt (`@AndroidEntryPoint` + `by viewModels()`).
- source_spec: `_bmad-output/implementation-artifacts/spec-chan-so-khi-nhan-giu-cuoc-goi.md`
  summary: Ghi vào "Repo gotchas" của CLAUDE.md rằng ViewModel nào gọi `stateIn` trong constructor thì test phải tạo nó sau `MainDispatcherRule` (`by lazy`), và CallLogViewModel dùng `Eagerly`.
  evidence: CallLogViewModelTest đã fail 21 test với lỗi "Module with the Main dispatcher had failed to initialize" cho tới khi đổi sang `by lazy`. Mục này phải sửa CLAUDE.md nên cần người dùng duyệt.
