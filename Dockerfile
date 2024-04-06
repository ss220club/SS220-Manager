FROM python:3.11

WORKDIR /app

RUN apt-get update -y
RUN apt-get install -y libmariadb-dev
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY src/ .

ENTRYPOINT ["python", "bot.py"]