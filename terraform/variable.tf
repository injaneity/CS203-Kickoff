# Note: This value is hard coded for the purpose of the workshop. 
# In a real-world scenario, you would want to use a data source to look up the VPC ID.
variable "database_name" {
  type        = string
  description = "The name of the database"
  default     = "kickoff"
}

variable "DATABASE_USERNAME" {
  type        = string
  description = "The username for the database"
  sensitive   = true
}

variable "DATABASE_PASSWORD" {
  type        = string
  description = "value of the password for the database"
  sensitive   = true
}

variable "ACM_CERTIFICATE_ARN" {
  type        = string
  description = "arn of the ACM certificate"
  sensitive   = true
}

variable "OPENAI_API_KEY" {
  type        = string
  description = "OpenAI API Key"
  sensitive   = true
}
variable "JWT_SECRET_KEY" {
  type        = string
  description = "JWT Secret Key"
  sensitive   = true
}