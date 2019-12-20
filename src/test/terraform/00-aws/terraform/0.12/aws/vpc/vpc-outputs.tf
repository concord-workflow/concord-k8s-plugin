# VPC
output "vpc_id" {
  value = aws_vpc.main.id
}

output "vpc" {
  value = aws_vpc.main
}

# Subnets
output "private_subnets" {
  value = aws_subnet.private
}

output "public_subnets" {
  value = aws_subnet.public
}

# NAT
output "nat-eips" {
  value = aws_eip.eips
}

output "nat-gw" {
  value = aws_nat_gateway.nat-gateway
}

output "region" {
  value = var.aws_region
}

output "tags" {
  value = var.tags
}
