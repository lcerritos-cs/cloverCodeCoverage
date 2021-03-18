node {
    git 'https://github.com/eramirez-cs/cloverCodeCoverage'
    def mvn = tool 'Maven 3.6.3'
    stage('SCM') {
        checkout scm
    }
    stage('Master Coverage') {
        if (env.CHANGE_ID) {
            checkout([
              $class: 'GitSCM',
              branches: [[name: '*/master']],
              userRemoteConfigs: [[url: 'https://github.com/eramirez-cs/cloverCodeCoverage']]
            ])
            try {
                // Checkout to develop and run mvn test
                sh "${mvn}/bin/mvn clean install clover:setup test clover:aggregate clover:clover"
                step([
                    $class: 'CloverPublisher',
                    cloverReportDir: 'target/site',
                    cloverReportFileName: 'clover.xml'   // optional, default is none
                    ])

                String report = readFile ('target/site/clover/dashboard.html')
                String[] lines = report.split('\n')

                def foundPassedLine = lines.find { line-> line =~ /div class="barPositive contribBarPositive "/ }
                def passedMatch = (foundPassedLine =~ /[0-9]+[.][0-9]+/ )
                masterCoverage = passedMatch[0] as Float

                println ("Master Coverage: ${masterCoverage}")
            } catch (all) {
                def error = "${all}"
                echo "Error ${all}"
            }
        }
    }
    stage('Analyzing UTs') {
        checkout([
          $class: 'GitSCM',
          branches: [[name: "*/${env.BRANCH_NAME}"]],
          userRemoteConfigs: [[url: 'https://github.com/eramirez-cs/cloverCodeCoverage']]
        ])
        withEnv(['PATH+EXTRA=/usr/sbin:/usr/bin:/sbin:/bin']) {
            echo sh(script: 'env|sort', returnStdout: true)
            if (env.CHANGE_ID) {
                try {
                    // Checkout to develop and run mvn test
                    sh "${mvn}/bin/mvn clean install clover:setup test clover:aggregate clover:clover"
                    step([
                       $class: 'CloverPublisher',
                       cloverReportDir: 'target/site',
                       cloverReportFileName: 'clover.xml',
                       healthyTarget: [methodCoverage: 70, conditionalCoverage: 80, statementCoverage: 80], // optional, default is: method=70, conditional=80, statement=80
                       unhealthyTarget: [methodCoverage: 50, conditionalCoverage: 50, statementCoverage: 50], // optional, default is none
                       failingTarget: [methodCoverage: 0, conditionalCoverage: 0, statementCoverage: 0]     // optional, default is none
                     ])

                    String report = readFile ('target/site/clover/dashboard.html')
                    String[] lines = report.split('\n')

                    def foundPassedLine = lines.find { line-> line =~ /div class="barPositive contribBarPositive "/ }
                    def passedMatch = (foundPassedLine =~ /[0-9]+[.][0-9]+/)
                    branchCoverage = passedMatch[0] as Float

                    println ("Master Coverage: ${masterCoverage}")
                    println ("Branch Coverage: ${branchCoverage}")

                    diff = branchCoverage - masterCoverage
                    println ("Diff: ${diff}")
                    echo "Current Pull Request ID: ${pullRequest.id}"

                    if(diff > 0){
                        try {
                            pullRequest.review('APPROVE', 'The execution, coverage and unit test failure verification passed successfully.')
                            pullRequest.addLabel('JenkinsReviewPassed')
                            pullRequest.removeLabel('JenkinsReviewFailed')
                            pullRequest.createStatus(status: 'success',
                                                     context: 'continuous-integration/jenkins/pr-merge',
                                                     description: 'All tests are passing.',
                                                     targetUrl: "${env.JOB_URL}/env.BUILD_ID")
                        } catch (ex) {
                            echo "Fail trying to add Labels ${ex}"
                        }
                    }
                    else{
                        try {
                            pullRequest.review('REQUEST_CHANGES', 'The execution, branch coverage is less than master coverage')
                            pullRequest.addLabel('JenkinsReviewFailed')
                            pullRequest.removeLabel('JenkinsReviewPassed')
                            pullRequest.createStatus(status: 'failure',
                                                     context: 'continuous-integration/jenkins/pr-merge',
                                                     description: 'Code Coverage has decreased.',
                                                     targetUrl: "${env.JOB_URL}/${env.BUILD_ID}")
                        } catch (ex) {
                            echo "Fail trying to add Labels ${ex}"
                        }
                    }

                } catch (all) {
                        def error = "${all}"
                        echo "Error ${all}"
                        if (error.contains('hudson.AbortException: script returned exit code 1')) {
                            echo 'Exception detected: test errors'
                            pullRequest.review('REQUEST_CHANGES', 'The build had failed. Maybe some of your unit tests are failing up')
                            pullRequest.createStatus(status: 'error',
                                                     context: 'continuous-integration/jenkins/pr-merge',
                                                     description: 'Some unit tests are failing up.',
                                                     targetUrl: "${env.JOB_URL}/${env.BUILD_ID}")
                        } else {
                                echo 'Exception detected: error on the build'
                                pullRequest.review('REQUEST_CHANGES', 'Error on the build')
                        }
                        try {
                            pullRequest.addLabel('JenkinsReviewFailed')
                            pullRequest.removeLabel('JenkinsReviewPassed')
                        } catch (ex) {
                                echo 'Finished'
                        }
                }
            }
        }
    }
}
