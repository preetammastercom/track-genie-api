pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Checkout source code from the version control system (e.g., Git)
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    // Maven build
                    sh 'mvn clean install'
                }
            }
        }

        stage('Copy to /opt/track-genie') {
            steps {
                script {
                    // Create the target directory if it doesn't exist
                    sh 'mkdir -p /opt/track-genie'

                    // Copy artifacts to /opt/track-genie
                    sh 'cp -r target/* /opt/track-genie/'
                }
            }
        }

        stage('Run from /opt/track-genie') {
            steps {
                script {
                    // Change to the target directory and execute the application
                    dir('/opt/track-genie') {
                        sh 'java -jar your-application.jar'
                    }
                }
            }
        }
    }

    post {
        always {
            // Clean up workspace or perform any other cleanup tasks
            cleanWs()
        }
    }
}
