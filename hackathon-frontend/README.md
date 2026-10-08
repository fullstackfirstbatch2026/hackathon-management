# Huddle Hackathon Console

React and Vite frontend for the Spring Boot hackathon management API.

## Run locally

1. Start the backend on port `8098`.
2. From this directory, run `npm install` once and then `npm run dev`.
3. Open the local URL printed by Vite.

Vite proxies `/api` requests to `http://localhost:8098`. To point the frontend at a different API origin, set `VITE_API_BASE_URL` (for example, `https://api.example.com/api`).

## Features

- Create, update, list, search, and delete teams, participants, projects, judges, and evaluations.
- Submit draft projects; new projects start as `DRAFT`, submissions use the backend procedure, and evaluations are reflected as `EVALUATED` by the database lifecycle.
- View project average scores, project/team/participant JOIN details, and above-average projects.
- Review dashboard counts and the `DRAFT` / `SUBMITTED` / `EVALUATED` project breakdown.

## Checks

- `npm run lint`
- `npm run build`
