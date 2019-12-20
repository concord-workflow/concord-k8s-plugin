resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags                  = merge({ Name = "${var.vpc_name}" }, var.tags)                                                             

  lifecycle {
    ignore_changes = [
      # Ignore changes to tags, e.g. because eks adds bunch of tags
      tags,
    ]
  }
}
