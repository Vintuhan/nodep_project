# Как получить APK через браузер

Android Studio не нужен.

1. Открой GitHub и войди в аккаунт.
2. Нажми **New repository**.
3. Назови его, например, `nodep-android`. Можно сделать **Private**.
4. Открой созданный репозиторий → **Add file → Upload files**.
5. Распакуй этот ZIP и загрузи **все файлы и папки внутри `nodep-android-source`**, чтобы `.github` тоже оказался в корне репозитория.
6. Нажми **Commit changes**.
7. Открой вкладку **Actions**.
8. Слева выбери **Build Android APK** → **Run workflow** → **Run workflow**.
9. Когда сборка станет зелёной, открой её → внизу **Artifacts** → скачай `nodep-android-apk`.
10. Внутри архива будет `app-debug.apk`. Перекинь APK на телефон и установи.

### Важно
APK из этой сборки — debug APK для личной установки. Для публикации в Google Play понадобится отдельная release-сборка с подписью.
