resource "aws_iam_role" "iam_role" {
  name               = var.cluster_name
  description        = "${var.cluster_name} role created by bootstrap"
  assume_role_policy = file("eks-policy-ec2-assume-role.json")
  tags               = var.tags
}

resource "aws_iam_instance_profile" "instance_profile" {
  name = var.cluster_name
  role = aws_iam_role.iam_role.name
}

# EKS master node roles and policies
resource "aws_iam_role" "eks_service_node_role" {
  name               = "${var.cluster_name}-eks-service-node-role"
  description        = "${var.cluster_name} role created by bootstrap for eks master nodes "
  assume_role_policy = file("eks-policy-aws-eks-assume-role.json")
  tags               = var.tags
}


# EKS worker node roles and policies

resource "aws_iam_instance_profile" "eks_worker_node_profile" {
  name = "${var.cluster_name}-eks-worker-node-profile"
  role = aws_iam_role.eks_worker_node_role.name
}

data "aws_iam_policy" "AmazonEKSClusterPolicy" {
  arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

data "aws_iam_policy" "AmazonEKSServicePolicy" {
  arn = "arn:aws:iam::aws:policy/AmazonEKSServicePolicy"
}

data "aws_iam_policy" "AmazonRoute53Policy" {
  arn = "arn:aws:iam::aws:policy/AmazonRoute53FullAccess"
}

data "aws_iam_policy" "ElasticLoadBalancingFullAccess" {
  arn = "arn:aws:iam::aws:policy/ElasticLoadBalancingFullAccess"
}


resource "aws_iam_role_policy_attachment" "eks-service-role-cluster-policy-attach" {
  role       = aws_iam_role.eks_service_node_role.name
  policy_arn = data.aws_iam_policy.AmazonEKSClusterPolicy.arn
}

resource "aws_iam_role_policy_attachment" "eks-service-role-attach" {
  role       = aws_iam_role.eks_service_node_role.name
  policy_arn = data.aws_iam_policy.AmazonEKSServicePolicy.arn
}

resource "aws_iam_role_policy_attachment" "route53-role-attach" {
  role       = aws_iam_role.eks_service_node_role.name
  policy_arn = data.aws_iam_policy.AmazonRoute53Policy.arn
}

resource "aws_iam_role_policy_attachment" "eks-elb-role-attach" {
  role       = aws_iam_role.eks_service_node_role.name
  policy_arn = data.aws_iam_policy.ElasticLoadBalancingFullAccess.arn
}

# EKS worker node roles and policies
resource "aws_iam_role" "eks_worker_node_role" {
  name               = "${var.cluster_name}-eks-worker-node-role"
  description        = "${var.cluster_name} role created by bootstrap for eks worker nodes "
  assume_role_policy = file("eks-policy-ec2-assume-role.json")
  tags               = var.tags
}

data "aws_iam_policy" "AmazonEKSWorkerNodePolicy" {
  arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}

data "aws_iam_policy" "AmazonEKS_CNI_Policy" {
  arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}

data "aws_iam_policy" "AmazonEC2EcrReadPolicy" {
  arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_role_policy_attachment" "eks-role-policy-attach" {
  role       = aws_iam_role.eks_worker_node_role.name
  policy_arn = data.aws_iam_policy.AmazonEKSWorkerNodePolicy.arn
}

resource "aws_iam_role_policy_attachment" "eks-cnipolicy-attachment" {
  role       = aws_iam_role.eks_worker_node_role.name
  policy_arn = data.aws_iam_policy.AmazonEKS_CNI_Policy.arn
}

resource "aws_iam_role_policy_attachment" "eks-ec2ecrread-attachment" {
  role       = aws_iam_role.eks_worker_node_role.name
  policy_arn = data.aws_iam_policy.AmazonEC2EcrReadPolicy.arn
}

resource "aws_iam_role_policy_attachment" "route53-woker-role-attach" {
  role       = aws_iam_role.eks_worker_node_role.name
  policy_arn = data.aws_iam_policy.AmazonRoute53Policy.arn
}

resource "aws_iam_policy" "aws-ebs-csi-driver-policy" {
  name        = "${var.cluster_name}-aws-ebs-csi-driver-policy"
  path        = "/"
  description = "aws-ebs-csi-driver policy "
  policy      = file("eks-policy-aws-ebs-csi-driver.json")
}


resource "aws_iam_role_policy_attachment" "aws-ebs-csi-driver-policy-attachment" {
  role       = aws_iam_role.eks_worker_node_role.name
  policy_arn = aws_iam_policy.aws-ebs-csi-driver-policy.arn
}

resource "aws_iam_policy" "aws-autoscaler-policy" {
  name        = "${var.cluster_name}-aws-autoscaler-policy"
  path        = "/"
  description = "aws-autoscaler-policy policy "
  policy      = file("eks-policy-aws-autoscaler.json")
}

resource "aws_iam_role_policy_attachment" "aws-autoscaler-policy-attachment" {
  role       = aws_iam_role.eks_worker_node_role.name
  policy_arn = aws_iam_policy.aws-autoscaler-policy.arn
}
