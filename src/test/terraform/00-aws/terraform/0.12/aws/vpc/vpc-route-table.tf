resource "aws_route_table" "private_routes" {
  vpc_id   = aws_vpc.main.id
  for_each = aws_subnet.private
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = lookup(aws_nat_gateway.nat-gateway, each.key).id
  }
}

resource "aws_route_table_association" "private_route_association" {
  for_each       = aws_subnet.private
  subnet_id      = each.value.id
  route_table_id = lookup(aws_route_table.private_routes, each.key).id
}
