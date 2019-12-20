resource "aws_nat_gateway" "nat-gateway" {
  for_each      = aws_eip.eips
  allocation_id = lookup(aws_eip.eips, each.key).id
  subnet_id     = lookup(aws_subnet.public, each.key).id
  depends_on    = [aws_internet_gateway.main, aws_subnet.public, aws_subnet.private, aws_eip.eips]
  tags          = merge({ Name = "nat-${lookup(aws_subnet.public, each.key).tags["Name"]}" }, var.tags)                                                                                        
}
