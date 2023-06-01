def mvn(String args, String dir) {
    sh "mvn ${args} -f pom.xml"
}

def dockerBuildPush(String dir) {
    def app_version = sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout -f ${dir}/pom.xml", returnStdout: true).trim()
    def app_name = sh(script: "mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout -f ${dir}/pom.xml", returnStdout: true).trim()
    def image_name = "youssefhilali/${app_name}:${app_version}"
    sh "docker build -t ${image_name} ${dir}"
    sh "docker push ${image_name}"
}

pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    stages {
        stage('Pull code from repository') {
            steps {
                checkout scm
            }
        }

        stage('Build project') {
            steps {
                mvn 'clean install -DskipTests'
            }
        }

        stage('SonarQube analysis') {
            steps {
                script {
                    def scannerHome = tool 'SonarQube';
                        withSonarQubeEnv('SonarServer') {
                            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=pms_original -Dsonar.projectName='pms_original' -Dsonar.exclusions=**/*.java"
                        }
                }
            }
        }

        stage('Wait for SonarQube analysis to complete') {
            steps {
                waitForQualityGate abortPipeline: true
            }
        }

        stage('Build Docker images') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'DOCKER_CREDS', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh "docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}"
                        dockerBuildPush('product_management_system_original')
                    }
                }
            }
        }


        stage('Deploy using Docker Compose') {
            steps {
                sh "docker-compose -f ${env.WORKSPACE}/docker-compose.yml up -d"
            }
        }
    }
}