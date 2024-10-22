variable "public_subnet_ids" {
  type        = list(string)
  description = "List of public subnet ids"
}

variable "ecs_tasks_sg_ids" {
  type        = list(string)
  description = "List of ECS task security group ids"
}

variable "lb_sg_ids" {
  type        = set(string)
  description = "Load balancer's security group ids"
}

variable "vpc_id" {
  type        = string
  description = "VPC's id"
}

variable "aws_region" {
  type        = string
  description = "The region to deploy your apps to"
  default     = "ap-southeast-1" # Singapore
}

# variable "users_app_name" {
#   type        = string
#   description = "Name of users container"
#   default     = "kickoff-users"
# }

# variable "users_app_image" {
#   type        = string
#   description = "Docker image to run in the ECS cluster"
#   default     = "vincetyy/kickoff-users:latest"
# }

# variable "users_app_port" {
#   type        = string
#   default     = "8081"
# }

variable "app_count" {
  type        = number
  description = "Number of docker containers to run"
  default     = 1
}

variable "health_check_path" {
  type        = string
  default     = "api/v1/health"
  description = "Health check path of the app"
}

variable "kickoff_db_endpoint" {
  type = string
  description = "Endpoint for DB"
}

variable "fargate_cpu" {
  type        = string
  description = "Fargate instance CPU units to provision (1 vCPU = 1024 CPU units)"
  default     = "1024"
}

variable "fargate_memory" {
  type        = string
  description = "Fargate instance memory to provision (in MiB)"
  default     = "2048"
}

variable "database_name" {
  type        = string
  description = "Name of the database"
}

variable "database_username" {
  type        = string
  description = "Username to connect to the database"
}

variable "database_password" {
  type        = string
  description = "Password to connect to the database"
}

variable "services" {
  type = map(object({
    cluster_name = string
    app_image    = string
    app_port     = number
    path_pattern       = list(string)
  }))
}