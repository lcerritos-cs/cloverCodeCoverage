def coverage(String cloverFile){
    echo "RUNNING COVERAGE"
    String report = readFile (cloverFile)
    String[] lines = report.split('\n')

    def foundPassedLine = lines.find { line-> line =~ /div class="barPositive contribBarPositive "/ }
    def passedMatch = (foundPassedLine =~ /[0-9]+[.][0-9]+/)

    return passedMatch[0] as Float
}

return this