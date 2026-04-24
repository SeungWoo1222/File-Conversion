output "lambda_function_arn" {
  value = aws_lambda_function.metric_publisher.arn
}

output "lambda_security_group_id" {
  description = "이 SG를 RabbitMQ 인스턴스의 인바운드 규칙에 추가 (포트 15672)"
  value       = aws_security_group.lambda.id
}
