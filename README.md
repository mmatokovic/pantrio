# Pantrio

One-stop comestibles management solution.

## Maven

```shell
mvn -N wrapper:wrapper
```

## Liqudbase

`mvn compile` is needed after each change in the liquibase.properties.

```bash
mvn liquibase:generateChangeLog
```

## Docker

Application is dockerized with Dockerfile and .dockerignore in root directory. Useful commands:

- build image: `docker build --no-cache -t pantrio .`
- build container: `docker run --name pantrio pantrio:latest .`
- start container: `docker start pantrio`
- stop container: `docker stop pantrio`

## Author Information
[Mislav Matoković](https://github.com/mmatokovi)
