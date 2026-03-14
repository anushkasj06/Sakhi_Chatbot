```
╔══════════════════════════════════════════════════════════╗
║                                                          ║
║              SUPER 30 - PROJECT MANAGEMENT               ║
║                      SEM-3 PROJECT                       ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

## Team: Mavericks

| Name            | Role                    |
|-----------------|-------------------------|
| Om Panchal      | Team Lead               |
| Anushka Jadhav  | Full Stack Developer    |
| Uday Sapate     | Frontend Developer      |
| Kiran Nandi     | Backend Developer       |
| Yash Bisen      | QA                      |

## Structure
- `frontend/` React (Vite)
- `backend/` Spring Boot API
- `db/` MySQL (Docker Compose)

## Quick start (dev)
1. Start database:
	- `cd db`
	- `docker compose up -d`
2. Start backend:
	- `cd ../backend`
	- Optional: `copy .env.example .env` and edit values
	- `mvnw.cmd spring-boot:run`
3. Start frontend:
	- `cd ../frontend`
	- `npm install`
	- `npm run dev`
