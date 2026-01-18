# Pantrio

One-stop comestibles management solution.

Pantrio helps you manage pantry inventory with modern practices, using CQRS at the API level to separate commands (actions) from queries (read operations), combined with a responsive UI powered by HTMX.

## Docker

Application is dockerized with `Dockerfile`. To run Pantrio using Docker, use the following command:

- build image: `docker build -t pantrio .`
- build container: `docker run --env-file .env -network pantrio_default pantrio:latest .`
- start container: `docker start pantrio`
- stop container: `docker stop pantrio`
- clear cache: `docker builder prune -f`

## Author Information
[Mislav Matoković](https://github.com/mmatokovi)
