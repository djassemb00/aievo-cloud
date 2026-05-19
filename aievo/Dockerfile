FROM python:3.10-slim

WORKDIR /app

# تثبيت التبعات
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
RUN pip install fastapi uvicorn pydantic

# نسخ الكود
COPY . .

# تشغيل الخادم
CMD ["uvicorn", "cloud_server:app", "--host", "0.0.0.0", "--port", "8000"]
