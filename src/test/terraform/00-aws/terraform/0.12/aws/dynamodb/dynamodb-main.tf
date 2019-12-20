resource "aws_dynamodb_table" "main" {
  name           = var.aws_dynamodb_table_name
  hash_key       = "LockID"
  read_capacity  = 20
  write_capacity = 20

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = var.tags
}
