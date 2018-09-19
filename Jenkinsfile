#!/usr/bin/env groovy Jenkinsfile

def server = Artifactory.server "Artifactory"
def gradle = Artifactory.newGradleBuild()
def buildInfo = Artifactory.newBuildInfo()

pipeline() {

  agent any

  triggers {
    pollSCM('H/10 * * * *')
  }

  options {
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }

  environment {
    SLACK_AUTOMATION_CHANNEL = "#automation"
    SLACK_AUTOMATION_TOKEN = credentials("jenkins-ci-integration-token")
    JENKINS_HOOKS = credentials("morning-at-lohika-jenkins-ci-hooks")
    GIT_TOKEN = credentials("Jenkins-GitHub-Apps-Personal-access-tokens")
  }

  parameters {
    booleanParam(
        name: 'release',
        description: 'release new version',
        defaultValue: false
    )
  }

  stages {

    stage('Pre configuration') {
      steps {
        script {
          gradle.useWrapper = true
          gradle.deployer.deployMavenDescriptors = true
          gradle.deployer.deployIvyDescriptors = true
          gradle.deployer.mavenCompatible = true

          buildInfo.env.filter.addExclude("*TOKEN*")
          buildInfo.env.filter.addExclude("*HOOK*")
          buildInfo.env.collect()
        }
      }
    }

    stage('Build') {
      steps {
        script {
          info = gradle.run rootDir: "./", buildFile: 'build.gradle', tasks: 'clean build'
          buildInfo.append(info)
        }
      }
    }

    stage('Package and publish release') {
      when {
        branch 'master'
        expression { params.release == false }
      }
      steps {
        script {
          gradle.deployer server: server, repo: 'morning-at-lohika-snapshots'
          info = gradle.run rootDir: "./", buildFile: 'build.gradle', tasks: 'artifactoryPublish'
          buildInfo.append(info)
        }
      }
    }

    stage('Pre Release') {
      when {
        branch 'master'
        expression { params.release == true }
      }
      steps {
        script {
          dir("${env.WORKSPACE}") {
            sh "git config remote.origin.url 'https://${env.GIT_TOKEN}@github.com/morningatlohika/morning-calendar.git'"
            sh 'git clean -fdx'
            sh "git checkout ${env.BRANCH_NAME}"
            sh 'git pull'
          }
        }
      }
    }

    stage('Release') {
      when {
        branch 'master'
        expression { params.release == true }
      }
      steps {
        script {
          info = gradle.run rootDir: "./", buildFile: 'build.gradle', tasks: 'release'
          buildInfo.append(info)
        }
      }
    }

    stage('Publish RELEASE') {
      when {
        branch 'master'
        expression { params.release == true }
      }
      steps {
        script {
          dir("${env.WORKSPACE}") {
            sh 'git log --pretty=format:"%h" -n 2 | sed -n 2p | xargs git checkout'
          }
          gradle.deployer server: server, repo: 'morning-at-lohika'
          info = gradle.run rootDir: "./", buildFile: 'build.gradle', tasks: 'clean build artifactoryPublish'
          buildInfo.append(info)
        }
      }
    }

    stage('Deploy') {
      when {
        buildingTag()
      }
      steps {
        script {
          echo 'Deploying only because this commit is tagged...'
          env.TAG_VERSION = env.GIT_BRANCH.substring(21)
          sh 'printenv'
          sshagent(credentials: ['prod']) {
            sh 'ssh -o StrictHostKeyChecking=no morning@morning ./service.sh morning-calendar status'
            sh 'ssh -o StrictHostKeyChecking=no morning@morning ./service.sh morning-calendar stop'
            sh 'ssh -o StrictHostKeyChecking=no morning@morning ./service.sh morning-calendar install ' + env.TAG_VERSION
            sh 'ssh -o StrictHostKeyChecking=no morning@morning ./service.sh morning-calendar start'
            sh 'ssh -o StrictHostKeyChecking=no morning@morning ./service.sh morning-calendar status'
          }
        }
      }
    }
  }


  post {
    always {
      script {
        publishHTML(target: [
            allowMissing         : true,
            alwaysLinkToLastBuild: false,
            keepAll              : true,
            reportDir            : 'build/reports/tests/test',
            reportFiles          : 'index.html',
            reportName           : "Test Summary"
        ])
        junit testResults: 'build/test-results/test/*.xml', allowEmptyResults: true
        server.publishBuildInfo buildInfo
      }
    }

    success {
      script {
        dir("${env.WORKSPACE}") {
          archiveArtifacts 'build/libs/*.jar'
        }

        slackSend(
            baseUrl: "${env.JENKINS_HOOKS}",
            token: "${env.SLACK_AUTOMATION_TOKEN}",
            channel: "${env.SLACK_AUTOMATION_CHANNEL}",
            botUser: true,
            color: "good",
            message: "BUILD SUCCESS: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]\nCheck console output at: ${env.BUILD_URL}"
        )
      }
    }

    failure {
      script {
        slackSend(
            baseUrl: "${env.JENKINS_HOOKS}",
            token: "${env.SLACK_AUTOMATION_TOKEN}",
            channel: "${env.SLACK_AUTOMATION_CHANNEL}",
            botUser: true,
            color: "danger",
            message: "BUILD FAILURE: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]\nCheck console output at: ${env.BUILD_URL}"
        )
      }
    }
  }
}
