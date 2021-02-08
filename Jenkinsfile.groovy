#!/usr/bin/env groovy
node{
    
        stage('Get Latest Code'){
            deleteDir()
            checkout scm
        }
        stage('User Input'){
            input('Do you want to proceed?')
        }
        stage('Build'){
                echo "Building..."
                docker.image('python:3.5.1').inside{
                    sh 'python --version'
                    sh 'python -m py_compile src/library.py'
                stash(name: 'compiled-results', includes: 'src/*.py*')
                }
                
                echo "Build Successful"
        }
        stage('Test'){
            echo "Testing..."
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
            
            echo "Test Successful"
        }
        stage('Deliver'){
            
            environment {
                VOLUME = '$(pwd)/sources:/src'
                IMAGE = 'cdrx/pyinstaller-linux:python3'
            }
                dir(path: env.BUILD_ID){
                    unstash(name:'compiled-results')
                    //sh 'docker run --rm -v ${VOLUME} ${IMAGE} 'pyinstaller -F library.py' '
                }
                archiveArtifacts "${env.BUILD_ID}/src/dist/library"
                //sh 'docker run --rm -v ${VOLUME} ${IMAGE} ''rm -rf build dist'
        }
}