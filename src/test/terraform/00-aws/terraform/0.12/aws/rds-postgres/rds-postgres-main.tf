data "aws_vpc" "selected" {
  tags = {
    Name = var.aws_vpc_name
  }
}

data "aws_subnet_ids" "selected" {
  vpc_id = data.aws_vpc.selected.id
}

resource "aws_db_subnet_group" "main" {
  name        = "${var.aws_vpc_name}-concord-db"
  description = "${var.aws_vpc_name}-concord-db"
  subnet_ids = data.aws_subnet_ids.selected.ids
  tags = var.tags
}

resource "aws_security_group" "rds-postgres-main" {
  name        = "var.db_identifier"
  description = "var.db_identifier"
  vpc_id      = data.aws_vpc.selected.id
  tags = var.tags
}

resource "aws_db_instance" "default" {
  allocated_storage       = var.db_storage_size
  storage_type            = var.db_storage_type
  engine                  = var.db_engine
  engine_version          = var.db_engine_version
  instance_class          = var.db_instance_type
  name                    = var.db_name
  username                = var.db_username
  password                = var.db_password
  backup_retention_period = 7
  identifier              = var.db_identifier
  publicly_accessible     = var.db_publicly_accessible
  deletion_protection     = false
  skip_final_snapshot     = true
  db_subnet_group_name    = aws_db_subnet_group.main.id

  vpc_security_group_ids = [
    aws_security_group.main.id
  ]

  tags = var.tags
}

resource "aws_security_group_rule" "allow_db_outbound" {
  description       = "Allow all outbound"
  type              = "egress"
  security_group_id = aws_security_group.main.id
  from_port         = 0
  to_port           = 0
  protocol          = -1
  cidr_blocks       = ["0.0.0.0/0"]
  # Why aren't tags allowed here?
  #tags = var.tags
}

resource "aws_security_group_rule" "allow_db_inbound" {
  description       = "Allow 5432 inbound"
  type              = "ingress"
  security_group_id = aws_security_group.main.id
  from_port         = var.db_port
  to_port           = var.db_port
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  # Why aren't tags allowed here?
  #tags = var.tags
}
