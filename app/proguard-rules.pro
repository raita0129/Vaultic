# androidx.security-crypto 底層用到 Google Tink,Tink 本身引用了一些只在編譯期
# 存在的註解類別(errorprone / javax.annotation),執行期不需要,R8 找不到會直接
# 把 missing class 當成錯誤中斷 build,所以要明確告知可以忽略。
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn javax.annotation.concurrent.**
-keep class com.google.crypto.tink.** { *; }
-keep interface com.google.crypto.tink.** { *; }

# Tink 內建「從網路下載金鑰」等我們用不到的選用功能,才會引用到這些函式庫,
# 專案沒有真的依賴它們,忽略即可
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**

# SQLCipher(sqlcipher-android)透過 JNI 呼叫原生函式庫,類別/方法名稱不能被改寫或移除
-keep class net.zetetic.database.** { *; }
-keep interface net.zetetic.database.** { *; }

# Argon2Kt 透過 JNI 呼叫原生函式庫
-keep class com.lambdapioneer.argon2kt.** { *; }
