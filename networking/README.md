# Networking Microservices Architecture

## Описание архитектуры

Система состоит из трех микросервисов, работающих в изолированных Docker сетях:

### Сервисы

1. **Gateway Service** - Публичный API сервис
   - Порт: 8080 (доступен извне)
   - Сети: public-net, internal-net
   - Функции: Принимает запросы от клиентов, перенаправляет в Processing

2. **Processing Service** - Сервис обработки данных
   - Порт: 8080 (внутренний)
   - Сети: internal-net, storage-net
   - Функции: Обрабатывает данные, получает их от Storage

3. **Storage Service** - Сервис хранения данных
   - Порт: 8080 (внутренний)
   - Сети: storage-net
   - Функции: Хранит и возвращает данные, работает с Redis

4. **Redis** - Кеш-сервер
   - Порт: 6379 (внутренний)
   - Сети: storage-net
   - Функции: Кеширование данных

### Схема сетей

```
Internet → public-net → Gateway → internal-net → Processing → storage-net → Storage
                                                                    ↓
                                                              storage-net → Redis
```

### Изоляция сетей

- **public-net**: Только Gateway (доступен извне)
- **internal-net**: Gateway ↔ Processing
- **storage-net**: Processing ↔ Storage ↔ Redis

### Поток данных

1. Клиент → `http://localhost:8080/process` → Gateway
2. Gateway → `http://processing:8080/process` → Processing
3. Processing → `http://storage:8080/data` → Storage
4. Storage → `redis:6379` → Redis (кеширование)
5. Обратно: Storage → Processing → Gateway → Клиент

## Команды запуска

### Сборка и запуск всех сервисов

```bash
# Перейти в директорию networking
cd networking

# Сборка образов
docker-compose build

# Запуск всех сервисов
docker-compose up

# Запуск в фоне
docker-compose up -d
```

### Остановка сервисов

```bash
# Остановка сервисов
docker-compose down

# Остановка с удалением томов
docker-compose down -v
```

### Просмотр логов

```bash
# Логи всех сервисов
docker-compose logs

# Логи конкретного сервиса
docker-compose logs gateway
docker-compose logs processing
docker-compose logs storage

# Логи в реальном времени
docker-compose logs -f
```

### Выполнение команд в контейнерах

```bash
# Подключиться к контейнеру
docker-compose exec gateway sh
docker-compose exec processing sh
docker-compose exec storage sh

# Выполнить команду
docker-compose exec storage wget -qO- http://localhost:8080/data
```

## Тестирование

### Health Check

```bash
# Проверка здоровья Gateway
curl http://localhost:8080/health

# Проверка здоровья Processing (изнутри контейнера)
docker-compose exec processing wget -qO- http://localhost:8080/health

# Проверка здоровья Storage (изнутри контейнера)
docker-compose exec storage wget -qO- http://localhost:8080/health
```

### Основной функционал

```bash
# Тест основного endpoint
curl -X POST http://localhost:8080/process

# Ожидаемый результат:
# Gateway response: {"processed":"...","status":"success"}
```

## Структура проекта

```
networking/
├── src/
│   ├── GatewayService.java      # Публичный API сервис
│   ├── ProcessingService.java   # Сервис обработки данных
│   └── StorageService.java      # Сервис хранения данных
├── Dockerfile                   # Multi-stage Dockerfile
├── docker-compose.yml          # Конфигурация сервисов
└── README.md                   # Документация
```

## API Endpoints

### Gateway Service (http://localhost:8080)

- `POST /process` - Основной endpoint для обработки данных
- `GET /health` - Проверка здоровья сервиса

### Processing Service (внутренний)

- `POST /process` - Обработка данных от Gateway
- `GET /health` - Проверка здоровья сервиса

### Storage Service (внутренний)

- `GET /data` - Получение всех данных
- `POST /store?key=value` - Сохранение данных
- `GET /health` - Проверка здоровья сервиса

## Требования

- Docker
- Docker Compose
- Java 21+
- Порты: 8080 (Gateway), 6379 (Redis)

## Безопасность

- Gateway - единственный сервис, доступный извне
- Processing и Storage изолированы в внутренних сетях
- Нет прямого доступа к Storage и Redis извне
- Все внутренние коммуникации происходят через Docker сети
