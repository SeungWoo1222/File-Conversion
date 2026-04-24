# ── Scale Out Policy ─────────────────────────────────────────────────────────
# 알람 임계값(100) 기준으로 초과분에 따라 단계적으로 인스턴스 추가

resource "aws_autoscaling_policy" "worker_scale_out" {
  name                   = "fileconversion-worker-scale-out"
  autoscaling_group_name = var.worker_asg_name
  policy_type            = "StepScaling"
  adjustment_type        = "ChangeInCapacity"

  # 큐 100~300개: +1대 (100 초과분이 0~200 사이)
  step_adjustment {
    metric_interval_lower_bound = 0
    metric_interval_upper_bound = 200
    scaling_adjustment          = 1
  }

  # 큐 300~500개: +2대 (100 초과분이 200~400 사이)
  step_adjustment {
    metric_interval_lower_bound = 200
    metric_interval_upper_bound = 400
    scaling_adjustment          = 2
  }

  # 큐 500개 초과: +3대 (100 초과분이 400 이상)
  step_adjustment {
    metric_interval_lower_bound = 400
    scaling_adjustment          = 3
  }
}

# ── Scale In Policy ──────────────────────────────────────────────────────────

resource "aws_autoscaling_policy" "worker_scale_in" {
  name                   = "fileconversion-worker-scale-in"
  autoscaling_group_name = var.worker_asg_name
  policy_type            = "StepScaling"
  adjustment_type        = "ChangeInCapacity"

  # 큐 10개 이하: -1대
  step_adjustment {
    metric_interval_upper_bound = 0
    scaling_adjustment          = -1
  }
}

# ── CloudWatch Alarms ────────────────────────────────────────────────────────

# 큐 깊이 >= 100이 2분 지속 → 스케일 아웃
resource "aws_cloudwatch_metric_alarm" "worker_queue_high" {
  alarm_name          = "fileconversion-worker-queue-high"
  alarm_description   = "큐 적체 지속 - Worker 스케일 아웃"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold           = 100
  evaluation_periods  = 2
  period              = 60
  metric_name         = "RabbitMQQueueDepth"
  namespace           = "FileConversion/Worker"
  statistic           = "Average"

  dimensions = {
    QueueName = "cdc.event.new_conversion"
  }

  alarm_actions = [aws_autoscaling_policy.worker_scale_out.arn]
}

# 큐 깊이 <= 10이 5분 지속 → 스케일 인
resource "aws_cloudwatch_metric_alarm" "worker_queue_low" {
  alarm_name          = "fileconversion-worker-queue-low"
  alarm_description   = "큐 여유 지속 - Worker 스케일 인"
  comparison_operator = "LessThanOrEqualToThreshold"
  threshold           = 10
  evaluation_periods  = 5
  period              = 60
  metric_name         = "RabbitMQQueueDepth"
  namespace           = "FileConversion/Worker"
  statistic           = "Average"

  dimensions = {
    QueueName = "cdc.event.new_conversion"
  }

  alarm_actions = [aws_autoscaling_policy.worker_scale_in.arn]
}
