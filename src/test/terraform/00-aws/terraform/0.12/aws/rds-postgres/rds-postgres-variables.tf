variable "aws_vpc_name"           {}

# Concord Database
variable "db_password"            {}
variable "db_identifier"          {}
variable "db_name"                {}
variable "db_username"            {}
variable "db_storage_size"        {}
variable "db_storage_type"        {}
variable "db_instance_type"       {}
variable "db_publicly_accessible" {}
variable "db_engine"              { default = "postgres" }
variable "db_engine_version"      { default = "10.6" }
variable "db_port"                { default = 5432 }
