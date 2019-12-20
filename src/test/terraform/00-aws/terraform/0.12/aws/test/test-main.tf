# A Terraform setup that tests the credentials you intend to use

data "aws_caller_identity" "selected" {}

output "account_id" {
  value = "data.aws_caller_identity.selected.account_id"
}

output "caller_arn" {
  value = "data.aws_caller_identity.selected.arn"
}

output "caller_user" {
  value = "data.aws_caller_identity.selected.user_id"
}
