pipeline {
  agent any
  tools {
    maven 'Maven 3.9.11'
    jdk 'jdk21'
  }
  options {
    disableConcurrentBuilds()
    buildDiscarder(
      logRotator(daysToKeepStr: '180', numToKeepStr: '10', artifactDaysToKeepStr: '365', artifactNumToKeepStr: '10')
    )
    timestamps()
  }
  environment {
    REGISTRY_CREDS = credentials('registry_creds')
    REGISTRY_DOCKER_REPO = 'registry.local'
  }
  stages {
    stage('Verify') {
      steps {
        bat 'mvn clean verify'
        junit '**/target/surefire-reports/*.xml'
      }
    }
    stage('Setup')  {
      steps {
        script {
          bat 'mvn -B -Darguments=-DskipTests release:prepare release:clean'
          bat 'mvn release:perform'

          bat '''
              mvn build-helper:parse-version \
                    versions:set -DgenerateBackupPoms=false -DnewVersion=${parsedVersion.majorVersion}.${parsedVersion.nextMinorVersion}.0-SNAPSHOT versions:commit'
            '''
          env.IMAGE_TAG = bat(script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true).trim()
        }
      }
    }
    stage('Code Analysis') {
      steps {
        withSonarQubeEnv("On-Prem SonarQube server") {
          bat 'mvn sonar:sonar'
        }
      }
    }
    stage('Publish') {
      steps {
        bat "docker build -t %REGISTRY_DOCKER_REPO%/einvoice-api:%IMAGE_TAG% ."

        withCredentials([usernamePassword(credentialsId: 'registry_creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
          bat 'echo %PASS% | docker login -u %USER% --password-stdin %REGISTRY_DOCKER_REPO%'
        }

        bat "docker push %REGISTRY_DOCKER_REPO%/einvoice-api:%IMAGE_TAG%"
      }
    }
  }
  post {
    cleanup {
        cleanWs()
    }
  }
}
