resource "aws_subnet" "private" {
  for_each          = toset(var.private_subnet_list)
  vpc_id            = aws_vpc.main.id
  availability_zone = each.value
  cidr_block        = cidrsubnet(aws_vpc.main.cidr_block, 3, index(var.private_subnet_list, each.value))
  tags              = merge({ Name = "${var.vpc_name}-private-${index(var.private_subnet_list, each.value)}" }, var.tags)

  lifecycle {
    ignore_changes = [
      # Ignore changes to tags, e.g. because eks adds bunch of tags
      tags,
    ]
  }
}
