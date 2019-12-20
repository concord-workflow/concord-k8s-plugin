terraform {
  required_version = ">= 0.12"
}

provider "aws" {
  region = var.aws_region
  assume_role {
    role_arn     = var.assume_role_arn
    session_name = var.assume_role_session
  }
}
