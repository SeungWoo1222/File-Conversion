locals {
  lambda_name = "fileconversion-rabbitmq-metric-publisher"
}

# Python 파일을 zip으로 패키징
data "archive_file" "lambda_zip" {
  type        = "zip"
  source_file = "${path.module}/src/rabbitmq_metric_publisher.py"
  output_path = "${path.module}/.build/rabbitmq_metric_publisher.zip"
}

# ── IAM ─────────────────────────────────────────────────────────────────────

resource "aws_iam_role" "lambda" {
  name = "${local.lambda_name}-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_vpc" {
  role       = aws_iam_role.lambda.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

resource "aws_iam_role_policy" "lambda_permissions" {
  name = "${local.lambda_name}-policy"
  role = aws_iam_role.lambda.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = "cloudwatch:PutMetricData"
        Resource = "*"
      },
      {
        Effect   = "Allow"
        Action   = "ssm:GetParameter"
        Resource = "arn:aws:ssm:${var.aws_region}:${var.aws_account_id}:parameter/fileconversion/rabbitmq/*"
      }
    ]
  })
}

# ── Security Group ───────────────────────────────────────────────────────────

resource "aws_security_group" "lambda" {
  name        = "${local.lambda_name}-sg"
  description = "fileconversion rabbitmq metric publisher lambda"
  vpc_id      = var.vpc_id

  egress {
    description     = "RabbitMQ management API"
    from_port       = 15672
    to_port         = 15672
    protocol        = "tcp"
    security_groups = [var.rabbitmq_security_group_id]
  }

  egress {
    description = "AWS API via NAT (CloudWatch, SSM)"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ── Lambda Function ──────────────────────────────────────────────────────────

resource "aws_lambda_function" "metric_publisher" {
  function_name    = local.lambda_name
  role             = aws_iam_role.lambda.arn
  runtime          = "python3.12"
  handler          = "rabbitmq_metric_publisher.handler"
  filename         = data.archive_file.lambda_zip.output_path
  source_code_hash = data.archive_file.lambda_zip.output_base64sha256
  timeout          = 30
  memory_size      = 128

  vpc_config {
    subnet_ids         = var.subnet_ids
    security_group_ids = [aws_security_group.lambda.id]
  }
}

# ── EventBridge (1분마다 Lambda 트리거) ──────────────────────────────────────

resource "aws_cloudwatch_event_rule" "metric_schedule" {
  name                = "${local.lambda_name}-schedule"
  schedule_expression = "rate(1 minute)"
  state               = "ENABLED"
}

resource "aws_cloudwatch_event_target" "metric_schedule_target" {
  rule = aws_cloudwatch_event_rule.metric_schedule.name
  arn  = aws_lambda_function.metric_publisher.arn
}

resource "aws_lambda_permission" "allow_eventbridge" {
  statement_id  = "AllowEventBridgeInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.metric_publisher.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.metric_schedule.arn
}
