.PHONY: help build up down restart logs clean test

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build Docker images
	docker-compose build

up: ## Start all services
	docker-compose up -d

down: ## Stop all services
	docker-compose down

restart: ## Restart all services
	docker-compose restart

logs: ## Show logs from all services
	docker-compose logs -f

logs-backend: ## Show backend logs
	docker-compose logs -f backend

logs-postgres: ## Show PostgreSQL logs
	docker-compose logs -f postgres

clean: ## Stop and remove containers, volumes, and networks
	docker-compose down -v
	docker system prune -f

test: ## Run tests in Docker
	docker-compose -f docker-compose.test.yml up --build --abort-on-container-exit
	docker-compose -f docker-compose.test.yml down -v

test-up: ## Start test environment
	docker-compose -f docker-compose.test.yml up -d

test-down: ## Stop test environment
	docker-compose -f docker-compose.test.yml down -v

ps: ## Show running containers
	docker-compose ps

shell-backend: ## Open shell in backend container
	docker-compose exec backend sh

shell-postgres: ## Open PostgreSQL shell
	docker-compose exec postgres psql -U postgres -d yazilimdogrulama

