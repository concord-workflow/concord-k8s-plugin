resource "aws_subnet" "public" {
  for_each          = toset(var.public_subnet_list)
  vpc_id            = aws_vpc.main.id
  availability_zone = each.key
  cidr_block        = cidrsubnet(aws_vpc.main.cidr_block, 3, index(var.public_subnet_list, each.value) + length(var.private_subnet_list))
  tags              = merge({ Name = "${var.vpc_name}-public-${index(var.public_subnet_list, each.value)}" }, var.tags)

  lifecycle {
    ignore_changes = [
      # Ignore changes to tags, e.g. because eks adds bunch of tags
      tags,
    ]
  }
}
