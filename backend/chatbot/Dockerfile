# Use an official lightweight Python image
FROM python:3.11-slim

# Set environment variables to prevent Python from writing .pyc files and buffering stdout/stderr
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1

# Set the working directory in the container
WORKDIR /app

# Copy only the requirements first to leverage Docker's caching for dependencies
COPY requirements.txt .

# Install dependencies (only necessary ones are included in requirements.txt)
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application files
COPY . .

# Expose a port (if needed for debugging, otherwise optional)
EXPOSE 8000

# Run the chatbot application
CMD ["uvicorn", "kickoff_chatbot_api:app", "--host", "0.0.0.0", "--port", "8000"]