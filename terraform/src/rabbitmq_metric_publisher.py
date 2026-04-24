import base64
import json
import urllib.parse
import urllib.request

import boto3

QUEUE_NAME = "cdc.event.new_conversion"
NAMESPACE = "FileConversion/Worker"

ssm = boto3.client("ssm")
cloudwatch = boto3.client("cloudwatch")


def _get_param(name: str) -> str:
    return ssm.get_parameter(Name=name, WithDecryption=True)["Parameter"]["Value"]


def handler(event, context):
    host = _get_param("/fileconversion/rabbitmq/host")
    port = _get_param("/fileconversion/rabbitmq/mgmt-port")
    username = _get_param("/fileconversion/rabbitmq/username")
    password = _get_param("/fileconversion/rabbitmq/password")

    encoded_queue = urllib.parse.quote(QUEUE_NAME, safe="")
    url = f"http://{host}:{port}/api/queues/%2F/{encoded_queue}"

    credentials = base64.b64encode(f"{username}:{password}".encode()).decode()
    req = urllib.request.Request(url, headers={"Authorization": f"Basic {credentials}"})

    with urllib.request.urlopen(req, timeout=10) as response:
        data = json.loads(response.read())

    messages_ready = float(data.get("messages_ready", 0))

    cloudwatch.put_metric_data(
        Namespace=NAMESPACE,
        MetricData=[
            {
                "MetricName": "RabbitMQQueueDepth",
                "Dimensions": [{"Name": "QueueName", "Value": QUEUE_NAME}],
                "Value": messages_ready,
                "Unit": "Count",
            }
        ],
    )

    print(f"Published RabbitMQQueueDepth={messages_ready} for queue={QUEUE_NAME}")
    return {"statusCode": 200, "messages_ready": messages_ready}
