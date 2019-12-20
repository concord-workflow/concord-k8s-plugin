resource "aws_subnet" "public" {
  for_each          = var.public_subnet_map
  vpc_id            = aws_vpc.main.id
  availability_zone = each.key
  cidr_block        = cidrsubnet(aws_vpc.main.cidr_block, 3, each.value)
  tags              = merge({ Name = "${var.vpc_name}-public-${each.value}" }, var.tags)                                                                                                               

  lifecycle {
    ignore_changes = [
      # Ignore changes to tags, e.g. because eks adds bunch of tags
      tags,
    ]
  }
}
