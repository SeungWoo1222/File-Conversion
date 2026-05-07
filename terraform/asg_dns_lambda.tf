locals {
  dns_updater_name = "fileconversion-asg-dns-updater"
}

# ── Lambda 코드 패키징 ────────────────────────────────────────────────────────

data "archive_file" "asg_dns_updater_zip" {
  type        = "zip"
  source_file = "${path.module}/src/asg_dns_updater.py"
  output_path = "${path.module}/.build/asg_dns_updater.zip"
}

# ── IAM ─────────────────────────────────────────────────────────────────────

resource "aws_iam_role" "asg_dns_updater" {
  name = "${local.dns_updater_name}-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "asg_dns_updater_basic" {
  role       = aws_iam_role.asg_dns_updater.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy" "asg_dns_updater_permissions" {
  name = "${local.dns_updater_name}-policy"
  role = aws_iam_role.asg_dns_updater.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["ec2:DescribeInstances"]
        Resource = "*"
      },
      {
        Effect   = "Allow"
        Action   = ["route53:ChangeResourceRecordSets"]
        Resource = "arn:aws:route53:::hostedzone/${aws_route53_zone.internal.zone_id}"
      }
    ]
  })
}

# ── Lambda Function ──────────────────────────────────────────────────────────

resource "aws_lambda_function" "asg_dns_updater" {
  function_name    = local.dns_updater_name
  role             = aws_iam_role.asg_dns_updater.arn
  runtime          = "python3.12"
  handler          = "asg_dns_updater.handler"
  filename         = data.archive_file.asg_dns_updater_zip.output_path
  source_code_hash = data.archive_file.asg_dns_updater_zip.output_base64sha256
  timeout          = 30
  memory_size      = 128

  environment {
    variables = {
      HOSTED_ZONE_ID = aws_route53_zone.internal.zone_id
    }
  }
}

# ── EventBridge: Worker ASG 인스턴스 시작 성공 시 Lambda 트리거 ────────────────

resource "aws_cloudwatch_event_rule" "worker_launch" {
  name        = "${local.dns_updater_name}-rule"
  description = "Worker ASG 인스턴스 교체 시 redis.internal DNS 자동 업데이트"

  event_pattern = jsonencode({
    source        = ["aws.autoscaling"]
    "detail-type" = ["EC2 Instance Launch Successful"]
    detail = {
      AutoScalingGroupName = [var.worker_asg_name]
    }
  })
}

resource "aws_cloudwatch_event_target" "worker_launch_target" {
  rule = aws_cloudwatch_event_rule.worker_launch.name
  arn  = aws_lambda_function.asg_dns_updater.arn
}

resource "aws_lambda_permission" "allow_eventbridge_asg" {
  statement_id  = "AllowEventBridgeInvokeASG"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.asg_dns_updater.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.worker_launch.arn
}
