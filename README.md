# Мой Тасбих — обновление интерфейса

В архиве лежит обновлённый метод `createInterface()` для Android-приложения и инструкция по его применению.

## Как применить в существующем GitHub-репозитории

1. Открой файл `app/src/main/java/com/mytasbih/app/MainActivity.java`.
2. Найди метод `private void createInterface() {`.
3. Замени весь метод `createInterface()` на содержимое файла `MainActivity_createInterface_update.txt`.
4. Проверь, что метод `update()`, `button()`, `addAction()`, `params()`, `dp()` и остальные методы остались в `MainActivity.java`.
5. Сделай commit в ветку `main` или `master`.
6. Открой вкладку **Actions** и скачай артефакт `My-Tasbih-APK` после успешной сборки.

## Важно

Этот набор — патч интерфейса, а не полный исходный проект: исходный проект создавался внутри GitHub Actions workflow. Чтобы полностью восстановить проект, нужен исходный workflow из репозитория или полный исходный код `MainActivity.java`, `ReminderReceiver.java`, `BootReceiver.java` и workflow YAML.
