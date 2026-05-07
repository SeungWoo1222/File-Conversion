# ── Private Hosted Zone ──────────────────────────────────────────────────────

resource "aws_route53_zone" "internal" {
  name = "internal"

  vpc {
    vpc_id = var.vpc_id
  }
}

# ── Static A Records ─────────────────────────────────────────────────────────

# RabbitMQ EC2 (rmq-ec2) - standalone, not in ASG
resource "aws_route53_record" "rmq" {
  zone_id = aws_route53_zone.internal.zone_id
  name    = "rmq.internal"
  type    = "A"
  ttl     = 30

  records = [var.rmq_private_ip]
}

# Redis - rmq-ec2에서 실행 (ASG 밖 고정 EC2, Worker 교체와 무관)
resource "aws_route53_record" "redis" {
  zone_id = aws_route53_zone.internal.zone_id
  name    = "redis.internal"
  type    = "A"
  ttl     = 30

  records = [var.rmq_private_ip]
}
