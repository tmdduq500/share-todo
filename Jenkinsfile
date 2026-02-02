pipeline {
  agent any

  environment {
    PROJECT_ID = "project-61ceee4c-ef41-4d6e-a40"
    REGION     = "asia-northeast3"
    REPO       = "share-todo-back"
    IMAGE_NAME = "app"
    IMAGE      = "${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}/${IMAGE_NAME}"

    // 런타임 VM 정보
    RUNTIME_HOST = "35.216.18.47"
    RUNTIME_USER = "osy9907"
    APP_DIR      = "/home/osy9907/deploy/share-todo-back"  // docker-compose.yml 있는 위치
    COMPOSE_FILE = "docker-compose.yml"
  }

  stages {
    stage("Checkout") {
      steps {
        checkout scm
      }
    }

    stage("Build Docker Image") {
      steps {
        sh '''
          set -e
          GIT_SHA=$(git rev-parse --short HEAD)
          echo "GIT_SHA=$GIT_SHA" > .gitsha
          docker build -t ${IMAGE}:${GIT_SHA} -t ${IMAGE}:latest .
        '''
      }
    }

    stage("Push to Artifact Registry") {
      steps {
        sh '''
          set -e
          GIT_SHA=$(cut -d= -f2 .gitsha)
          docker push ${IMAGE}:${GIT_SHA}
          docker push ${IMAGE}:latest
        '''
      }
    }

    stage("Deploy to Runtime VM") {
      steps {
        sshagent(credentials: ['runtime-ssh']) {
          sh '''
            set -e
            ssh -o StrictHostKeyChecking=no ${RUNTIME_USER}@${RUNTIME_HOST} "
              set -e
              cd ${APP_DIR}

              # 1) 기반 서비스(Cloud SQL Proxy / Redis) 항상 보장
              docker compose -f ${COMPOSE_FILE} up -d cloudsql redis

              # 2) 앱만 교체 배포
              docker compose -f ${COMPOSE_FILE} pull app
              docker compose -f ${COMPOSE_FILE} up -d --no-deps --no-build app

              # 3) 사용 안 하는 dangling 이미지 정리(안전)
              docker image prune -f
            "
          '''
        }
      }
    }
  }
}
