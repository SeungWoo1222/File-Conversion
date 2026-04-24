variable "aws_region" {
  type    = string
  default = "ap-northeast-2"
}

variable "aws_account_id" {
  type    = string
  default = "188615717003"
}

variable "vpc_id" {
  type        = string
  description = "Worker와 RabbitMQ가 속한 VPC ID"
}

variable "subnet_ids" {
  type        = list(string)
  description = "NAT Gateway가 있는 프라이빗 서브넷 ID 목록 (Lambda용)"
}

variable "rabbitmq_security_group_id" {
  type        = string
  description = "RabbitMQ 인스턴스에 붙어있는 Security Group ID"
}

variable "worker_asg_name" {
  type    = string
  default = "fileconversion-worker-asg"
}
