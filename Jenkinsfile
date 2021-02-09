#!/usr/bin/env groovy
pipeline{
    agent any
    stages{
        stage('Get Latest Code'){
            steps{
            deleteDir()
            checkout scm
            }
        }
        stage('User Input'){
            steps{
            input('Do you want to proceed?')
            }
        }
        stage('Build'){
            steps{
                echo "Building..."
                    docker.image('python:3.5.1').inside{
                    sh 'python --version'
                    sh 'python -m py_compile src/library.py'
                    stash(name: 'compiled-results', includes: 'src/*.py*')
                    }
                echo "Build Successful"
            }
        }
        stage('Test'){
            steps{
            
            echo "Testing..."
            script{
            def testError = null
            try{
                docker.image('python:3.5.1').inside{
                sh ' python src/library_test.py '
            }
            }
            catch(err){
                testError = err
                currentBuild.result = 'FAILURE'
            }
            }
            
            echo "Test Successful"
            }
        }
        stage('Deliver'){
            agent any
            
            environment {
                VOLUME = '$(pwd)/sources:/src'
                IMAGE = 'cdrx/pyinstaller-linux:python3'
            }
            steps{
                dir(path: env.BUILD_ID){
                    unstash(name:'compiled-results')
                    sh "docker run --rm -v ${VOLUME} ${IMAGE} 'pyinstaller -F library.py' "
                }
            }
            post{
                success{
                archiveArtifacts "src/library.py"
                sh "docker run --rm -v ${VOLUME} ${IMAGE} 'rm -rf build dist' "
            }
            }
        }
    }
}