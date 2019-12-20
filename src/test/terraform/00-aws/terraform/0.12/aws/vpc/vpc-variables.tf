variable "vpc_cidr"       {}

variable "public_subnet_map" {
  description = "Map from availability zone to the number that should be used for each availability zone's subnet"
  type        = map
}

variable "private_subnet_map" {
  description = "Map from availability zone to the number that should be used for each availability zone's subnet"
  type        = map
}
