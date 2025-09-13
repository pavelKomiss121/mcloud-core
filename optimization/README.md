# 🚀 MCLOUD-7: Оптимизация Docker образов

Этот репозиторий содержит примеры оптимизации Docker образов для Java приложений.

## 📁 Структура проекта

```
mcloud-core/
└── optimization/
    └── simple/                    # Простое Java веб-приложение
        ├── src/
        │   └── WebApp.java        # HTTP сервер на Java
        ├── Dockerfile.original    # Неоптимизированная версия
        ├── Dockerfile.optimized   # Оптимизированная версия
        └── .dockerignore          # Исключения для Docker
```

## 🎯 Цель урока

Научиться оптимизировать Docker образы для:
- Уменьшения размера образа
- Ускорения сборки
- Повышения безопасности
- Улучшения производительности

## 📊 Результаты оптимизации

*Результаты будут добавлены после выполнения практических заданий*

## 🚀 Как использовать

### Сборка оригинального образа
```bash
cd simple
docker build -f Dockerfile.original -t webapp:original .
```

### Сборка оптимизированного образа
```bash
cd simple
docker build -f Dockerfile.optimized -t webapp:optimized .
```

### Сравнение размеров
```bash
docker images | grep webapp
```

### Запуск приложения
```bash
# Оригинальная версия
docker run -p 8080:8080 webapp:original

# Оптимизированная версия
docker run -p 8080:8080 webapp:optimized
```

## 🔍 Тестирование

Откройте в браузере:
- http://localhost:8080 - главная страница
- http://localhost:8080/health - проверка состояния

## 📚 Техники оптимизации

1. **Multi-stage build** - разделение сборки и runtime
2. **Alpine Linux** - минимальная базовая ОС
3. **Distroless образы** - только приложение без ОС
4. **Layer caching** - оптимизация порядка инструкций
5. **Minimal dependencies** - только необходимые пакеты
