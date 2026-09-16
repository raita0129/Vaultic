# Vaultic

本地優先(local-first)的 Android 密碼管理器。聚焦在密碼學金鑰衍生、本機資料庫加密,以及 Clean Architecture 分層設計的實作。

## 核心特色

- **整庫加密**:資料庫層級用 SQLCipher 加密,不是只加密單一欄位
- **金鑰衍生**:主密碼透過 Argon2id 衍生出加密金鑰,主密碼與衍生金鑰皆不落地儲存,僅保存隨機 salt
- **本機優先**:所有資料僅存於裝置本機,不上傳任何雲端服務
- **Clean Architecture**:domain / data / presentation 三層分離,domain 層純 Kotlin、不依賴任何 Android SDK
- **Jetpack Compose**:全畫面採用 Compose 撰寫,狀態驅動 UI

## 技術棧

| 分類 | 技術 |
|---|---|
| 語言 | Kotlin |
| UI | Jetpack Compose, Material3, Navigation Compose |
| 資料庫 | Room + SQLCipher |
| 加密 | Argon2Kt(Argon2id 金鑰衍生), Android Keystore(EncryptedSharedPreferences) |
| 非同步 | Kotlin Coroutines, Flow |
| 架構 | MVVM + Clean Architecture(UseCase 分層) |

## 專案結構

```
app/src/main/java/com/raita/vaultic/
├── MainActivity.kt
├── VaulticApp.kt
├── domain/                  純 Kotlin,不依賴任何 Android SDK,可獨立寫單元測試
│   ├── model/                VaultEntry
│   ├── repository/           VaultRepository(interface)
│   └── usecase/               UnlockVaultUseCase, AddEntryUseCase, UpdateEntryUseCase, GeneratePasswordUseCase
├── data/
│   ├── local/                 VaultDatabase(Room + SQLCipher), VaultEntryDao, VaultEntryEntity
│   ├── crypto/                 KeyDerivation(Argon2id), SecureStore(salt 存放)
│   └── repository/            VaultRepositoryImpl
├── presentation/
│   ├── unlock/                 UnlockScreen, UnlockViewModel
│   └── vault/                  VaultListScreen, AddEntryScreen, VaultViewModel
├── navigation/                VaulticNavHost
└── ui/theme/                  VaulticTheme
```

## 架構設計理由

**為什麼分 domain / data / presentation 三層**
三層變動的原因彼此獨立——domain 只在密碼規則調整時變、data 只在換資料庫或加密技術時變、presentation 只在畫面改版時變。分層後 domain 可在不啟動模擬器的情況下獨立跑單元測試,且依賴方向單向(presentation → domain ← data),不會互相牽連。

**為什麼整庫加密而非只加密密碼欄位**
若只加密密碼欄位,標題、帳號等欄位仍是明文,裝置被存取時仍能看出使用者在哪些服務有帳號,已構成隱私外洩。SQLCipher 在 SQLite 檔案格式層級加密,沒有金鑰整個 `.db` 檔案無法解讀。

**為什麼用 Argon2id 而非直接雜湊**
一般雜湊函式(MD5/SHA-256)設計成「快」,適合驗證檔案完整性;金鑰衍生則需要刻意設計成「慢且吃記憶體」,拉高攻擊者離線暴力破解的成本。Argon2id 可調整記憶體用量(本專案設定 64MB)與疊代次數,抵抗 GPU/ASIC 平行化暴力破解,是 OWASP 目前推薦的金鑰衍生演算法。

**為什麼主密碼與衍生金鑰完全不落地儲存**
資料庫加密的意義在於「沒有主密碼就無法解密」。若把金鑰存在裝置上,攻擊者不需要破解密碼,直接竊取金鑰即可解密整個資料庫,加密機制形同虛設。

## 開發環境

- Android Studio(建議使用支援 Kotlin 2.0+ 的版本)
- JDK 17
- minSdk 26 / targetSdk 34
- 建議使用實體機測試(部分純模擬器可能無法正確顯示生物辨識相關 UI)

### 建置步驟

1. `git clone` 這個倉庫
2. 用 Android Studio 開啟專案根目錄(含 `settings.gradle.kts` 的那一層)
3. 等待 Gradle Sync 完成(首次會下載 SQLCipher / Argon2Kt 等依賴)
4. 確認 Settings → Build Tools → Gradle 裡的 JDK 版本為 17
5. Run

## 已知限制與規劃中項目

目前版本聚焦在核心加密與資料流程,以下項目尚未實作,列為後續規劃:

- [ ] 生物辨識解鎖(UI 尚未接上 Android Keystore 的 `KeyGenParameterSpec` 金鑰保護流程)
- [ ] Room 正式 Migration(目前使用 `fallbackToDestructiveMigration()`,僅適合開發階段)
- [ ] 單元測試(`GeneratePasswordUseCase`、`AddEntryUseCase` 為純 Kotlin 邏輯,優先補測試對象)
- [ ] 正式 App Icon(目前為預設 mipmap 圖示)
- [ ] Google Play 上架前置作業(簽名金鑰、隱私權政策、商店素材)

## 安全性設計摘要

| 項目 | 做法 |
|---|---|
| 資料庫加密 | SQLCipher(SQLite 檔案層級加密) |
| 金鑰衍生 | Argon2id(mCost 64MB, tCost 3, parallelism 4) |
| Salt | 每組密碼庫獨立產生,16 bytes,存於 `EncryptedSharedPreferences` |
| 敏感資料記憶體處理 | 主密碼(`CharArray`)與衍生金鑰(`ByteArray`)使用完畢後主動清零 |
| 剪貼簿 | 複製密碼時標記 `IS_SENSITIVE`,避免部分鍵盤/系統預覽明文內容 |
| 備份 | `android:allowBackup="false"`,避免加密資料庫透過系統備份機制外流 |

## License

尚未指定(作品集用途)。