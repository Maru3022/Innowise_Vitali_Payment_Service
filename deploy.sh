#!/bin/bash

set -e

NAMESPACE="online-store"

echo "Building payment-service image with minikube..."
minikube image build -t payment-service:latest .

echo "Applying Kubernetes manifests to namespace $NAMESPACE..."
kubectl apply -f k8s/ -n $NAMESPACE

echo "Waiting for MongoDB to be ready..."
kubectl wait --for=condition=ready pod -l app=mongodb -n $NAMESPACE --timeout=120s

echo "Waiting for Kafka (external dependency) to be available..."
# Kafka is managed by the orchestrator, just wait a bit
sleep 10

echo "Applying payment-service deployment and service..."
kubectl apply -f k8s/payment-service-deployment.yaml -n $NAMESPACE
kubectl apply -f k8s/payment-service-svc.yaml -n $NAMESPACE

echo "Waiting for payment-service rollout to complete..."
kubectl rollout status deployment/payment-service -n $NAMESPACE --timeout=120s

echo "Deployment completed successfully!"
echo "Payment service is available at http://payment-service:8084 in the cluster"
