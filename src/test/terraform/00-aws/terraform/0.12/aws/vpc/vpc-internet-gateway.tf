resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags = merge({ Name = "${var.vpc_name}-igw" }, var.tags)
}

resource "aws_route_table" "main" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
  tags = merge({ Name = "${var.vpc_name}-public-route" }, var.tags)
  depends_on = [aws_internet_gateway.main]
}

resource "aws_route_table_association" "rt-public" {
  for_each       = aws_subnet.public
  subnet_id      = each.value.id
  route_table_id = aws_route_table.main.id
}
