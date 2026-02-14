FROM python:3.14-slim

COPY --from=ghcr.io/astral-sh/uv:latest /uv /uvx /bin/

WORKDIR /locust

COPY pyproject.toml .
RUN uv sync --no-dev

COPY . .

ENTRYPOINT ["uv", "run", "locust"]
