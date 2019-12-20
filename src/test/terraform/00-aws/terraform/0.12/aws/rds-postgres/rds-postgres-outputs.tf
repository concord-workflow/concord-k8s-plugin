output "db_name" {
  value = aws_db_instance.default.name
}

output "db_username" {
  value = aws_db_instance.default.username
}

output "db_endpoint" {
  value = aws_db_instance.default.endpoint
}

output "db_port" {
  value = aws_db_instance.default.port
}

output "db_arn" {
  value = aws_db_instance.default.arn
}
