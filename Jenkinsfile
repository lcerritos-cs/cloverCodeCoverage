node {
  git 'https://github.com/eramirez-cs/cloverCodeCoverage'
  stage('SCM') {
    checkout scm
  }
  stage('Analyzing UTs') {
    withEnv(['PATH+EXTRA=/usr/sbin:/usr/bin:/sbin:/bin']) {

        def mvn = tool 'Maven 3.6.3';
        def coverage = '100'// replace with a Jenkins parameter or create a job to read from env
        echo sh(script: 'env|sort', returnStdout: true)
        if (env.CHANGE_ID) {

            try {
                // Checkout to develop and run mvn test
                 sh "${mvn}/bin/mvn clean clover:setup test clover:aggregate clover:clover"
                 step([
                       $class: 'CloverPublisher',
                       cloverReportDir: 'target/site',
                       cloverReportFileName: 'clover.xml',
                       healthyTarget: [methodCoverage: 70, conditionalCoverage: 80, statementCoverage: 80], // optional, default is: method=70, conditional=80, statement=80
                       unhealthyTarget: [methodCoverage: 50, conditionalCoverage: 50, statementCoverage: 50], // optional, default is none
                       failingTarget: [methodCoverage: 0, conditionalCoverage: 0, statementCoverage: 0]     // optional, default is none
                     ])

                 // read our HTML report into a String
                 String report = readFile ("target/site/clover/dashboard.html")
                 // split each line break into its own String
                 String[] lines = report.split("\n")

                 def foundPassedLine = lines.find{ line-> line =~ /div class="barPositive contribBarPositive "/ }

                 // match for the numeric values
                 def passedMatch = (foundPassedLine =~ /[0-9]+[.][0-9]+/)

                 // cast to Integer so we can work with the number values
                 e2ePassed = passedMatch[0] as Float

                 println ("Passed: ${e2ePassed}")
                 echo "Current Pull Request ID: ${pullRequest.id}"

                 try {
                    pullRequest.review('APPROVE', "The execution, coverage and unit test failure verification passed successfully.")
                    pullRequest.addLabel('JenkinsReviewPassed')
                    pullRequest.removeLabel('JenkinsReviewFailed')
                 } catch(ex) {
                    echo "Fail trying to add Labels ${ex}"
                 }
            } catch (all) {
                def error = "${all}"
                echo "Error ${all}"
                if(error.contains("hudson.AbortException: script returned exit code 1")) {
                    echo "Exception detected: test errors"
                    pullRequest.review('REQUEST_CHANGES', 'The build had failed. Maybe some of your unit tests are failing up')
                } else {
                    echo "Exception detected: error on the build"
                    pullRequest.review('REQUEST_CHANGES', 'Error on the build')
                }

                try {
                    pullRequest.addLabel('JenkinsReviewFailed')
                    pullRequest.removeLabel('JenkinsReviewPassed')
                } catch(ex) {
                    echo 'Finished'
                }

            }
        }
    }
  }
}