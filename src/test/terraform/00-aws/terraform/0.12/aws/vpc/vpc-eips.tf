resource "aws_eip" "eips" {
  for_each   = aws_subnet.public
  vpc        = true
  depends_on = [aws_internet_gateway.main]
  tags = merge({ Name = "${var.vpc_name}" }, var.tags)
}
