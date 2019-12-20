resource "aws_vpc_endpoint" "private-s3" {
  vpc_id       = aws_vpc.main.id
  service_name = "com.amazonaws.${var.aws_region}.s3"
  policy       = file("vpc-policy-aws-s3.json")
  tags         = merge({ Name = "${var.vpc_name}-private-s3" }, var.tags)
}

resource "aws_vpc_endpoint_route_table_association" "private-s3-rt" {
  for_each        = aws_route_table.private_routes
  vpc_endpoint_id = aws_vpc_endpoint.private-s3.id
  route_table_id  = lookup(aws_route_table.private_routes, each.key).id
}

resource "aws_vpc_endpoint_route_table_association" "public-s3-rt" {
  vpc_endpoint_id = aws_vpc_endpoint.private-s3.id
  route_table_id  = aws_route_table.main.id
}
