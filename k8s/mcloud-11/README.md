# Kubernetes Deployment

## Описание проекта

Проект mcloud-core содержит веб-приложение на Java с микросервисной архитектурой.

## Структура проекта

- `src/` - исходный код Java приложений
- `deploy/` - Docker файлы и конфигурации для развертывания
- `networking/` - микросервисы для обработки данных

## Развертывание в Kubernetes

### Предварительные требования

- Minikube установлен и запущен
- kubectl настроен
- Docker образы собраны и загружены в Minikube

### Шаги развертывания

1. **Запуск Minikube**
   ```bash
   minikube start
   ```

2. **Сборка и загрузка образов**
   ```bash
   # Настройка Docker для Minikube
   eval $(minikube docker-env)
   
   # Сборка образа
   docker build -f deploy/Dockerfile.webapp -t mcloud-webapp:latest .
   ```

3. **Применение манифестов**
   ```bash
   # Применить Deployment
   kubectl apply -f webapp-deployment.yaml
   
   # Применить Service
   kubectl apply -f webapp-service.yaml
   ```

4. **Проверка статуса**
   ```bash
   # Проверить все ресурсы
   kubectl get all
   
   # Проверить логи
   kubectl logs -l app=webapp
   ```

5. **Доступ к приложению**
   ```bash
   # Port-forward
   kubectl port-forward service/webapp-service 8080:8080
   
   # Или через Minikube
   minikube service webapp-service --url
   ```

### Проверка работоспособности

- Главная страница: `http://localhost:8080`
- Health check: `http://localhost:8080/health`

### Масштабирование

```bash
# Увеличить количество реплик
kubectl scale deployment webapp-dep --replicas=5

# Проверить статус
kubectl get pods
```

### Удаление ресурсов

```bash
# Удалить все ресурсы
kubectl delete -f webapp-deployment.yaml
kubectl delete -f webapp-service.yaml

# Или удалить по типу
kubectl delete deployment webapp-dep
kubectl delete service webapp-service
```

## Архитектура

- **WebApp** - основное веб-приложение (порт 8080)
- **GatewayService** - шлюз для входящих запросов
- **ProcessingService** - обработка данных
- **StorageService** - хранение данных

## Мониторинг

```bash
# Посмотреть логи
kubectl logs -l app=webapp -f

# Описание ресурсов
kubectl describe deployment webapp-dep
kubectl describe service webapp-service
```
