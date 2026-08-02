#!/usr/bin/env sh
set -eu

SEAWEEDFS_BUCKET="${SEAWEEDFS_BUCKET:-footballay-data-quality-raw-local}"
SEAWEEDFS_ENDPOINT="${SEAWEEDFS_ENDPOINT:-http://localhost:8333}"
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-seaweedfsadmin}"
export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY:-seaweedfsadmin}"
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-ap-northeast-2}"
export AWS_EC2_METADATA_DISABLED=true

aws_s3api() {
  aws --endpoint-url "$SEAWEEDFS_ENDPOINT" s3api "$@"
}

case "${1:-}" in
  up) docker compose -f docker-compose.seaweedfs.yml up -d ;;
  down) docker compose -f docker-compose.seaweedfs.yml down ;;
  buckets) aws_s3api list-buckets ;;
  upload) aws_s3api put-object --bucket "$SEAWEEDFS_BUCKET" --key "$3" --body "$2" ;;
  list) aws --endpoint-url "$SEAWEEDFS_ENDPOINT" s3 ls "s3://$SEAWEEDFS_BUCKET" --recursive ;;
  download) aws_s3api get-object --bucket "$SEAWEEDFS_BUCKET" --key "$2" "$3" ;;
  restart) docker compose -f docker-compose.seaweedfs.yml restart seaweedfs ;;
  reset) aws --endpoint-url "$SEAWEEDFS_ENDPOINT" s3 rm "s3://$SEAWEEDFS_BUCKET" --recursive ;;
  seed) aws --endpoint-url "$SEAWEEDFS_ENDPOINT" s3 sync "$2" "s3://$SEAWEEDFS_BUCKET/${3:-}" ;;
  *)
    echo "usage: $0 {up|down|buckets|upload FILE KEY|list|download KEY FILE|restart|reset|seed DIRECTORY [PREFIX]}" >&2
    exit 64
    ;;
esac
