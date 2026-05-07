import os
import boto3

HOSTED_ZONE_ID = os.environ["HOSTED_ZONE_ID"]
RECORD_NAME = "redis.internal"
TTL = 30

ec2 = boto3.client("ec2")
route53 = boto3.client("route53")


def _get_private_ip(instance_id: str) -> str:
    resp = ec2.describe_instances(InstanceIds=[instance_id])
    return resp["Reservations"][0]["Instances"][0]["PrivateIpAddress"]


def _upsert_record(ip: str) -> None:
    route53.change_resource_record_sets(
        HostedZoneId=HOSTED_ZONE_ID,
        ChangeBatch={
            "Comment": f"Auto-update by ASG lifecycle event",
            "Changes": [{
                "Action": "UPSERT",
                "ResourceRecordSet": {
                    "Name": RECORD_NAME,
                    "Type": "A",
                    "TTL": TTL,
                    "ResourceRecords": [{"Value": ip}],
                },
            }],
        },
    )


def handler(event, context):
    detail = event.get("detail", {})
    instance_id = detail.get("EC2InstanceId")
    lifecycle = detail.get("LifecycleTransition", "")

    if not instance_id:
        print("No EC2InstanceId in event, skipping")
        return

    if "LAUNCHING" in lifecycle or event.get("detail-type") == "EC2 Instance Launch Successful":
        ip = _get_private_ip(instance_id)
        _upsert_record(ip)
        print(f"Updated {RECORD_NAME} -> {ip} (instance: {instance_id})")
    else:
        print(f"Lifecycle '{lifecycle}' - no action needed")
