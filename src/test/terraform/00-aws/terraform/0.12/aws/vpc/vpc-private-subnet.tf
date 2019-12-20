resource "aws_subnet" "private" {
  for_each          = var.private_subnet_map
  vpc_id            = aws_vpc.main.id
  availability_zone = each.key
  cidr_block        = cidrsubnet(aws_vpc.main.cidr_block, 3, each.value)
  tags              = merge({ Name = "${var.vpc_name}-private-${each.value}" }, var.tags)                                                             

  lifecycle {
    ignore_changes = [
      # Ignore changes to tags, e.g. because eks adds bunch of tags
      tags,
    ]
  }
}
