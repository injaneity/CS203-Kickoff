# CS203-Kickoff

## Project Overview
Kickoff is a community-led tournament management system for football in Singapore, connecting clubs and players through various features including tournament hosting, player recruitment, and club management.

## Project Structure
- `/backend`: Spring Boot backend
- `/frontend`: React/Vite frontend

## Setup Instructions

### Backend Setup
1. Navigate to the backend directory:
   ```
   cd backend
   ```
2. Make the Maven wrapper executable:
   ```
   chmod +x mvnw
   ```
3. Run the Spring Boot application:
   ```
   ./mvnw spring-boot:run
   ```
4. Verify the backend is running:
   ```
   curl http://localhost:8080
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```
   cd frontend
   ```
2. Install dependencies:
   ```
   npm install
   ```
3. Start the development server:
   ```
   npm run dev
   ```
4. Open your browser and visit `http://localhost:5173` (or the port Vite specifies)

## Current Dependencies

### Backend
1. Spring Web
2. Spring DevTools
3. Lombok
4. Starter Data JPA
5. H2 Database
6. Spring Security
7. Hibernate Validator

### Frontend
1. React
2. Vite
3. TypeScript
4. Redux Toolkit
5. React Router
6. Axios
7. Tailwind CSS

## Updating Dependencies

### Backend
1. Go to the Maven Central Repository
2. Copy the <dependency> block
3. Paste it into pom.xml
4. Run `./mvnw clean install`

### Frontend
1. Run `npm install [package-name]` for production dependencies
2. Run `npm install -D [package-name]` for development dependencies

## Deployment Architecture Overview

![Deployment Architecture Diagram](./assets/deployment-architecure-diagram.png)

### Architecture Components

This deployment architecture is designed for high availability, scalability, and security, managed through Terraform for infrastructure as code (IaC). Below is a breakdown of the main components and their interactions:

- **User Access**:
  - Users access the application through **Route 53**, which routes traffic to **CloudFront** for content delivery.
  - The frontend is hosted on an **S3 bucket**, which is integrated with **CloudFront** for improved distribution and caching.

- **Load Balancing and Networking**:
  - Traffic is managed by an **Application Load Balancer (ALB)**, which forwards requests to various **ECS clusters**.
  - An **Internet Gateway (IGW)** is used to allow resources within the public subnet to connect to the internet.

- **ECS Clusters**:
  - **Users ECS**: Handles user-related services
  - **Clubs ECS**: Manages club-related services
  - **Tournaments ECS**: Hosts tournament services, including integration with **Stripe API** for verification payment
  - **Chatbot ECS**: Supports chatbot functionalities
  - All of our ECS Clusters are desgined to scale based on load.

- **Database Layer**:
  - Each ECS service has a dedicated **MySQL RDS** instance in the private subnet:
    - **Users RDS**
    - **Clubs RDS**
    - **Tournaments RDS**
  - This configuration ensures data security by isolating databases from public access.

- **Storage Services**:
  - **Profile Pictures S3**: Stores user profile pictures.
  - **Verification Pictures S3**: Holds tourament verification images for hosts.

### Security and Compliance

- **Security Services**:
  - **Certificate Manager**: Manages SSL/TLS certificates for secure connections.
  - **WAF (Web Application Firewall)**: Protects against common web exploits.
  - **IAM (Identity and Access Management)**: Controls access to AWS services and resources.

- **Monitoring and Management**:
  - **CloudWatch**: Monitors performance and logs, especially for auto-scaling of the ECS.
  - **Cloud Map**: Provides service discovery for the microservices, allowing for microservice-to-microservice communication to ensure data consistency.

### Infrastructure Management

- **Terraform**: The entire architecture is provisioned and managed using Terraform, ensuring consistency and ease of deployment.

## CI/CD Pipeline Overview

![CI/CD Pipeline Diagram](./assets/ci-cd-pipeline.png)

This CI/CD pipeline automates the complete process of building, analyzing, deploying, and managing the infrastructure of the entire application. It ensures that each part of the app (backend, frontend, and infrastructure) is managed through dedicated workflows triggered by specific events such as merges, pull requests, or manual triggers.

### Key Workflows

- **Merge to Main:**
  - **Backend Changes**:
    - Runs jobs for each microservice to build and push Docker images to Docker Hub.
    - If applicable, downloads data from S3 and deploys to ECS with a forced new deployment if the service is already running.
    - Runs analysis jobs for microservices using `SonarQube` to ensure code quality and security.
  - **Frontend Changes**:
    - Builds the frontend, syncs built files to an S3 bucket, and invalidates the CloudFront cache for updated content delivery.
  - **Infrastructure (Terraform) Changes**:
    - Runs `terraform plan`, and if successful, applies the changes using `terraform apply`.

- **Pull Requests:**
  - **Backend Changes**:
    - Builds and analyzes microservices and posts code quality feedback on the PR.
  - **Terraform Changes**:
    - Runs `terraform plan` and comments the plan results on the PR for review before merging.

- **Manual Trigger:**
  - **Infrastructure Cleanup**:
    - Runs `terraform destroy` to decommission and clean up infrastructure resources when needed.


## Contributing
[Add contribution guidelines here]

## License
[Add license information here]
