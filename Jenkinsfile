pipeline {
  agent { label 'linux' }

  parameters {
    booleanParam(name: 'executeAnalysis', defaultValue: true, description: 'Execute SonarQube Analysis')
    booleanParam(name: 'executeTests', defaultValue: true, description: 'Execute Tests')
  }

  environment {
    APP_NAME = "einvoice-api"
    REGISTRY_CREDS = credentials("regcred")
    REGISTRY_URL = "http://crsdigcicd.crosig.group:8081/repository/helm-internal/"
    DOCKER_REGISTRY_URL = "crsdigcicd.crosig.group:8082/integration"
  }

  stages {
    stage('SCM Checkout') {
      steps {
        scmSkip(deleteBuild: true, skipPattern:'.*\\[skip ci\\].*')
        script {
          sh 'env'
          checkout([
            $class: 'GitSCM', gitTool: 'git-linux', branches: scm.branches, doGenerateSubmoduleConfigurations: false, extensions: scm.extensions + [[$class: 'CleanCheckout'], [$class: 'LocalBranch']], userRemoteConfigs: scm.userRemoteConfigs
          ])
        }
      }
    }

    stage ('Build Snapshot') {
      when {
        branch 'develop'
      }
      steps {
        script {
          def pom = readMavenPom file: 'pom.xml'
          env.version = pom.version
          def skipTestsArg = params.executeTests ? '' : '-DskipTests -DskipITs'
          sh './mvnw -v'
          sh "./mvnw -B deploy ${skipTestsArg}"
        }
      }
    }


    stage ('Build') {
      when {
        branch 'main'
      }
      steps {
        sh './mvnw -v'
        sh './mvnw -B build-helper:parse-version versions:set -DnewVersion=\'${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.0\' versions:commit'
        sh 'git add pom.xml'
        script {
          def pom = readMavenPom file: 'pom.xml'
          env.version = pom.version
        }
        sh "git commit -m 'Release $version'"
        script {
          def skipTestsArg = params.executeTests ? '' : '-DskipTests -DskipITs'
          sh "./mvnw -B deploy scm:tag ${skipTestsArg}"
        }
        sh 'git fetch origin develop'
        sh 'git checkout develop'
        sh 'git reset --hard origin/develop'
        sh './mvnw -B build-helper:parse-version versions:set -DnewVersion=\'${parsedVersion.majorVersion}.${parsedVersion.nextMinorVersion}.0-SNAPSHOT\' versions:commit'
        sh 'git add pom.xml'
        sh 'git commit -m "[skip ci] Bump up version for next development iteration"'
        sh 'git push origin HEAD:develop'
      }
    }

    stage('Analysis') {
      when {
        expression { return params.executeAnalysis }
        branch 'develop'
      }
      steps {
        withSonarQubeEnv('On-Prem SonarQube server') {
          sh './mvnw -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.5.0.6356:sonar -Dsonar.scanner.skipJreProvisioning=true'
        }
      }
    }

    stage('Publish') {
      steps {
        script {
          sh '''
          docker build \
            --label org.opencontainers.image.version=$version \
            --label org.opencontainers.image.revision=$GIT_COMMIT \
            --label org.opencontainers.image.source=$GIT_URL \
            -t $DOCKER_REGISTRY_URL/$APP_NAME:$version .
          '''
          sh 'docker push $DOCKER_REGISTRY_URL/$APP_NAME:$version'
        }
      }
    }

    stage ('Package Registry') {
      steps {
        script {
          sh 'helm version'
          sh 'helm package helm/$APP_NAME --version 1.0.0 --app-version $version'
          sh 'curl -sS -u $REGISTRY_CREDS_USR:$REGISTRY_CREDS_PSW -X DELETE $REGISTRY_URL$APP_NAME-1.0.0.tgz'
          sh 'curl -f -sS -u $REGISTRY_CREDS_USR:$REGISTRY_CREDS_PSW -T $APP_NAME-1.0.0.tgz -X PUT $REGISTRY_URL'
        }
      }
    }
  }

  options {
    skipDefaultCheckout()
    disableConcurrentBuilds()
    timestamps()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    buildDiscarder(
      logRotator(daysToKeepStr: '182', numToKeepStr: '10', artifactDaysToKeepStr: '365', artifactNumToKeepStr: '10')
    )
  }

  post {
    success {
      archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
      script {
        if (params.executeTests) {
            junit '**/target/surefire-reports/*.xml'
        }
        if (env.BRANCH_NAME != 'main') {
            build job: 'openshift-release', parameters: [
                [$class: 'StringParameterValue', name: 'APP_NAME', value: "${env.APP_NAME}"],
                [$class: 'StringParameterValue', name: 'VERSION', value: "${env.version}"]
            ], propagate: true
        }
      }
    }
    failure {
      mail to: 'mislav.matokovic@crosig.hr', subject: "❌ Jenkins Build Failed: ${env.JOB_NAME}-#${env.BUILD_NUMBER}", body: """
        The Jenkins build has failed.
        Project: ${env.JOB_NAME} with build number: ${env.BUILD_ID}.
        Check console output at ${env.BUILD_URL} to view the results.
       """
    }
    cleanup { cleanWs() }
  }
}
