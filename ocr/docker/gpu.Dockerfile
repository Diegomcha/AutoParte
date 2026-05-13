# Building stage
FROM dhi.io/pytorch:2.10.0-cuda12.8-cudnn9-dev AS builder

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PATH="/app/.venv/bin:$PATH"

ENV UV_PYTHON_DOWNLOADS=0
ENV UV_NO_DEV=1
ENV UV_LINK_MODE=copy
ENV UV_COMPILE_BYTECODE=1

# Install UV
COPY --from=ghcr.io/astral-sh/uv:0.11.14 /uv /uvx /bin/

WORKDIR /app

# Install dependencies
RUN uv venv --system-site-packages
RUN --mount=type=cache,target=/root/.cache/uv \
    --mount=type=bind,source=uv.lock,target=uv.lock \
    --mount=type=bind,source=pyproject.toml,target=pyproject.toml \
    uv sync --locked --extra gpu,deploy --no-install-project

# Copy project files
COPY *.py ./

# Setup & download OCR models
COPY docker/configs/gpu_config.yaml ./rapidocr_config.yaml
RUN rapidocr download_models --config /app/rapidocr_config.yaml

# Running stage
FROM dhi.io/pytorch:2.10.0-cuda12.8-cudnn9

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PATH="/app/.venv/bin:$PATH"

WORKDIR /app

COPY --from=builder /app /app

EXPOSE 80

CMD ["granian", "--interface", "asgi", "api:app", "--host", "0.0.0.0", "--port", "80"]