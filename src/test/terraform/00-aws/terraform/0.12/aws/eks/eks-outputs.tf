output "iam-role" {
  value = aws_iam_role.iam_role
}

output "instance-profile" {
  value = aws_iam_instance_profile.instance_profile
}

output "eks-worker-node-instance-profile" {
  value = aws_iam_instance_profile.eks_worker_node_profile
}

output "eks-worker-node-role" {
  value = aws_iam_role.eks_worker_node_role
}
output "eks-service-role" {
  value = aws_iam_role.eks_service_node_role
}
