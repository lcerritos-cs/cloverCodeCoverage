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
            // echo sh(script: 'env|sort', returnStdout: true)
            if (env.CHANGE_ID) {
                try {
                    // Checkout to develop and run mvn test
                    def output = sh(
                        script: "${mvn}/bin/mvn --log-file commandResult clean install clover:setup test clover:aggregate clover:clover",
                        returnStatus: true )
                    def result = readFile('commandResult').trim()
                    echo "RESULTS: ${result}"

                    if(result.contains('There are test failures')) {
                        currentBuild.result = 'NOT_BUILT'
                        echo 'UNIT TEST FAILURE'
                        pullRequest.review('REQUEST_CHANGES', 'The build has failed. Some of your unit tests are failing up.')
                        pullRequest.createStatus(status: 'failure',
                                                 context: 'continuous-integration/jenkins/pr-merge',
                                                 description: 'Unit Test has failed.',
                                                 targetUrl: "${env.JOB_URL}/${env.BUILD_ID}/clover-report/dashboard.html")
                        pullRequest.labels = ['JenkinsReviewPassed', 'JenkinsReviewFailed']
                        return
                    }
                    if(result.contains('did not meet target')) {
                        String mvnResult = readFile ('commandResult')
                        String[] linesT = mvnResult.split('\n')
                        def foundTargetLine = linesT.find { line-> line =~ /Checking for coverage of/ }
                        def targetMatch = (foundTargetLine =~ /[0-9]+[.][0-9]+/ )

                        echo "TARGET: ${targetMatch[0]}"
                        currentBuild.result = 'NOT_BUILT'
                        echo 'BUILD FAILURE'
                        pullRequest.review('REQUEST_CHANGES',"Error on the build. Coverage of did not meet target ${targetMatch[0]}%")
                        pullRequest.createStatus(status: 'failure',
                                                 context: 'continuous-integration/jenkins/pr-merge',
                                                 description: ' Coverage of actual brach did not meet target.',
                                                 targetUrl: "${env.JOB_URL}/${env.BUILD_ID}/clover-report/dashboard.html")
                        pullRequest.labels = ['JenkinsReviewPassed', 'JenkinsReviewFailed']
                        return
                    }
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

                    if(diff >= 0){
                        try {
                            currentBuild.result = 'SUCCESS'
                            echo 'BUILD SUCCESS'
                            pullRequest.review('APPROVE', 'The execution, coverage and unit test failure verification passed successfully.')
                            pullRequest.createStatus(status: 'success',
                                                     context: 'continuous-integration/jenkins/pr-merge',
                                                     description: 'All tests are passing.',
                                                     targetUrl: "${env.JOB_URL}/env.BUILD_ID/clover-report/dashboard.html")
                            pullRequest.labels = ['JenkinsReviewPassed', 'JenkinsReviewFailed']
                            return
                        } catch (ex) {
                            echo "Fail trying to add Labels ${ex}"
                        }
                    }
                    else{
                        try {
                            currentBuild.result = 'NOT_BUILT'
                            echo 'BUILD FAILURE'
                            pullRequest.review('REQUEST_CHANGES', "Code Coverage has decrease; from ${masterCoverage}% to ${branchCoverage}%")
                            pullRequest.createStatus(status: 'failure',
                                                     context: 'continuous-integration/jenkins/pr-merge',
                                                     description: 'The execution fails, branch coverage is less than master coverage',
                                                     targetUrl: "${env.JOB_URL}/${env.BUILD_ID}/clover-report/dashboard.html")
                            pullRequest.labels = ['JenkinsReviewFailed', 'JenkinsReviewPassed']
                            return
                        } catch (ex) {
                            echo "Fail trying to add Labels ${ex}"
                        }
                    }
                } catch (all) {
                    def error = "${all}"
                    echo "Error ${all}"
                    if (error.contains('hudson.AbortException: script returned exit code 1')) {
                        echo 'Exception detected: test errors'
                        pullRequest.review('REQUEST_CHANGES', 'The build has failed.')
                    } else {
                        if(error.contains('Label does not exist')){
                            echo 'Replacing Labels'
                            return
                        } else{
                            echo 'Exception detected: error on the build'
                            pullRequest.review('REQUEST_CHANGES', 'Error on the build')
                        }
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
