# Run the management app with Docker

This Compose setup runs the React frontend, Spring Boot API, and a dedicated MySQL 8.4 database. Docker stores database files in the `management_mysql_data` volume.

## Start on Windows PowerShell

Open PowerShell in this project folder, create a local environment file, and replace the sample password:

```powershell
Copy-Item .env.example .env
notepad .env
```

Then build and start the services:

```powershell
docker compose up --build
```

Open the frontend at `http://localhost:8080`. The API is also available directly at `http://localhost:8098`. Docker MySQL is exposed on host port `3307`, leaving a local MySQL on port `3306` undisturbed.

Stop with `Ctrl+C`, or use `docker compose down`. Data remains in the Docker volume. `docker compose down -v` also deletes that volume and all database data.

This creates a separate, initially empty `hackathon_db`; it does not import data from an existing MySQL installation. The project currently has no SQL initialization scripts, so any stored procedures, functions, triggers, or existing records must be migrated/imported separately before using endpoints that depend on them.

Keep `.env` private; it is excluded from Git. Do not publish the image with a real database password embedded in image settings.
