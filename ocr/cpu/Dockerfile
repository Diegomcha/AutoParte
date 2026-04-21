# Building stage
FROM dhi.io/python:3.11.15-dev AS builder

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PATH="/app/venv/bin:$PATH"

WORKDIR /app

RUN python -m venv /app/venv
RUN pip install --no-cache-dir rapidocr_api==0.2.0 onnxruntime==1.24.4
RUN pip uninstall -y opencv-python
RUN pip install --no-cache-dir opencv-python-headless==4.13.0.92

COPY rapidocr_config.yaml /app/venv/lib/python3.11/site-packages/rapidocr/config.yaml
RUN rapidocr download_models --config /app/venv/lib/python3.11/site-packages/rapidocr/config.yaml

# Running stage
FROM dhi.io/python:3.11.15

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PATH="/app/venv/bin:$PATH"

WORKDIR /app

COPY --from=builder /app/venv /app/venv

EXPOSE 80

CMD ["rapidocr_api", "-ip", "0.0.0.0", "-p", "80"]