# Kaiburr Assessment 2025 — Task 2: Kubernetes Deployment

## Overview

This project demonstrates **cloud-native deployment** of the Task Manager backend (Java/Spring Boot app) on a Kubernetes cluster. The application manages tasks, executes commands, and stores all data in MongoDB — now orchestrated entirely via containers and Kubernetes manifests. The solution is built for high portability, reliability, and scalability.

---

## Features & Achievements

- **Production-ready Docker images** for backend and MongoDB
- **Kubernetes manifests** for deployment, service, persistent volume, and RBAC
- **Automatic provisioning** of a MongoDB pod with persistent storage (data survives restarts)
- **Dynamic task execution:** Backend now creates a throwaway pod per shell command, runs task inside, and saves the result to MongoDB
- **Environment variables** used for configuration (DB host, port…)
- **Service exposure** (NodePort/LoadBalancer) for API access from outside the cluster
- **Screenshots** and CLI proofs of deployment

---

## Project Structure
```
task-manager/
├── Dockerfile
├── Kubernetes_YAML/
│ ├── app-deployment.yaml # Task Manager app Deployment & Service
│ ├── mongodb-deployment.yaml # MongoDB StatefulSet/Deployment & Persistent Volume
│ └── rbac.yaml # Role-Based Access Control (if applicable)
└── (additional source files)
```
---

## Prerequisites

- [Docker](https://www.docker.com/)
- [Minikube](https://minikube.sigs.k8s.io/docs/) or any Kubernetes cluster (local/managed)
- [kubectl](https://kubernetes.io/docs/tasks/tools/) (configured for your cluster)
- Backend built and Dockerized (Task 1 prerequisites)

---

## Setup & Deployment

### 1. **Build Docker Images**

Build and tag your backend Docker image:
docker build -t yourusername/task-manager-backend:latest .

### 2. **Start Minikube**

minikube start

### 3. **Apply Kubernetes Manifests**

Deploy MongoDB with persistent storage
kubectl apply -f Kubernetes_YAML/mongodb-deployment.yaml

Deploy backend service (Task Manager)
kubectl apply -f Kubernetes_YAML/app-deployment.yaml

(Optional) Apply RBAC rules if required
kubectl apply -f Kubernetes_YAML/rbac.yaml

### 4. **Get Service URL**

Expose your backend with NodePort:
minikube service task-manager-service --url

This outputs an address (e.g., http://127.0.0.1:31234) for Postman/UI frontend to use.

---

## API Endpoints (same as Task 1)

| Method | Endpoint                | Description                        |
|--------|-------------------------|------------------------------------|
| GET    | `/tasks`                | Get all tasks                      |
| POST   | `/tasks`                | Create a new task                  |
| DELETE | `/tasks/{id}`           | Delete task by ID                  |
| PUT    | `/tasks/{id}/execute`   | **Creates a new pod**, runs command|
| GET    | `/tasks/search?name=...`| Search tasks by name               |

_Backend takes MongoDB connection variables from environment (`MONGO_HOST`, `MONGO_PORT`, etc.)_

---

## Key Design Notes

- **Shell Command Execution:** Instead of running in the main container, each command request spins up a separate pod (busybox/utility image) for execution, enhancing security and isolation.
- **MongoDB Persistence:** Data is stored in a persistent volume — safe from pod restarts/deletions.
- **Deployment Proof:** All deployment/configuration is via declarative `.yaml` manifests — ready for CI/CD on any cloud.
- **Portability:** Solution runs equally on local (Minikube), cloud-managed, or bare-metal Kubernetes clusters.

---

## Troubleshooting

- **Pod CrashLoopBackOff:** Check `kubectl logs <pod-name>` for errors (DB env vars, network, etc.)
- **Service Unreachable:** Ensure service type is NodePort and use the correct minikube service url.
- **Data lost on MongoDB restart:** Ensure PVC and PV are configured and bound successfully (`kubectl get pvc`).

---

## Screenshots

Below are the required screenshots to demonstrate the Kubernetes deployment and dynamic task execution.  

### 1. Kubernetes Pods Running

A screenshot of the command:
kubectl get pods
<img width="1918" height="1078" alt="Task-2-GETPODS" src="https://github.com/user-attachments/assets/ecc4158e-9fcd-4370-bfd7-5d6871c6a1ea" />

---

### 2. Kubernetes Services Exposed

A screenshot of:
kubectl get svc
<img width="1918" height="1078" alt="Task-2-DEPLOYMENTS" src="https://github.com/user-attachments/assets/c1ac2d09-1585-42ad-9585-fc7aeb87b02d" />

---

### 3. API Endpoint Access Proof

Use `curl` or Postman to call at least one backend endpoint:
<img width="1918" height="1078" alt="Task-2-GET" src="https://github.com/user-attachments/assets/ff370ec7-dfbc-449b-96da-42726d74823e" />

---

### 4. Dynamic Pod Creation for Command Execution
<img width="1918" height="1078" alt="Task-2-KUBERNETES Pods" src="https://github.com/user-attachments/assets/a93da386-a805-425f-bc4e-7dec8426fe40" />

---

## Author

Final Year CCE, Amrita Vishwa Vidyapeetham  
**Shyam Anand**  
October 2025
