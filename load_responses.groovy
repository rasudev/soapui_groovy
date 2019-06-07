/* This script load xml contento from the soap response and searches in this case for the status.
If the status matches the expected status from the property then this is written and the execution of the test case continues.

For this to work properly the referenced request should be disabled in the test case.
*/

def groovyUtils = new com.eviware.soapui.support.GroovyUtils( context )
def projectPath = groovyUtils.projectPath

// initiate values for the loop and functions
def statusFound = false
def retryAttempts = 0
 
//------- SETUP HERE -----------
// define which step should be running within the loop
def requestStep = "%request%"
// define from which test case property to load value which is expected in the response
def expectedStatus = testRunner.testCase.getPropertyValue("expectedStatus")
// define from which test case property to load value which is not expected in the response and imidiately cancel run, like FAILED.
def unexpectedStatus = testRunner.testCase.getPropertyValue("unexpectedStatus")
// define how many times the loop should run if the expected or unexpected is not find, defined in the project property
def attempts = testRunner.testCase.testSuite.project.getPropertyValue('attempts').toInteger()
// define delay between each run, defined in the project property
def sleepTime = testRunner.testCase.testSuite.project.getPropertyValue('sleepTime').toInteger()
// define used namespaces
def nsx = [["ns", "http://ns.com/ns"]]
// define the response message xpath
def nodeXpath = "//ns:response/*"
// define the xpath of the status
def dataXpath = "//ns:response/ns:status"
//------------------------------
 
//------- RUN REQUEST IN THE LOOP -----------
while ( retryAttempts < attempts && !statusFound){
	retryAttempts++
	log.info ("Request number = " + retryAttempts )
	sleep(sleepTime)
 
	testRunner.runTestStepByName( requestStep )
	//------- GET STATUS CODE OF RESPONSE -----------
  // get the response time
	def responseTime = testRunner.testCase.testSteps[requestStep].testRequest.response.timeTaken
	// get the response status code, e.g. 200 = OK
  def httpResponseHeaders = testRunner.testCase.testSteps[requestStep].testRequest.response.getResponseHeaders()
	def httpStatus = httpResponseHeaders["#status#"]
	httpStatusCode = (httpStatus =~ "[1-5]\\d\\d")[0].toInteger()
  // check the status code 200 and continue with execution
	if (httpStatusCode == 200) {
		// get the xml content from the response
          def holder = groovyUtils.getXmlHolder( requestStep+"#Response" )
		// define namespace
		nsx.each {
			holder.declareNamespace (it[0], it[1])
		}
		// get the status
		def node = holder.getDomNodes(nodeXpath)
		def status = holder.getNodeValues(dataXpath).toString()
    // write the status into the test step Properties
		testRunner.testCase.getTestStepByName("Properties").setPropertyValue( "testStatus", status )
		//------- CHECK STATUS -----------
		if ( node ) {
			log.info "Node Status = " + status
			if (status.contains(expectedStatus) || status.contains(unexpectedStatus)) {
				statusFound = true
				log.info "statusFound = true, for status: " + status
			}
		} else {  
				log.info ("statusFound = false, status is: " + status)
		}	
		// exit execution if the status code != 200 -----------
	} else {
		log.info ("Empty response or status code is not 200")
		assert false : "Empty response or status code is not 200"
	}
}
 
def statusFromProperty = testRunner.testCase.getTestStepByName("Properties").getPropertyValue( "testStatus" )
 
//------- FAILED FOR UNEXPECTED STATUS -----------
if (statusFound == true && statusFromProperty.contains(unexpectedStatus)) {
	log.info "Check FAILED = " + "Expected result was: " + expectedStatus + " | Actual Resutls is: " + statusFromProperty + "\r\n"
	assert false : "Error - Expected result was: " + expectedStatus + " | Actual Resutls is: " + testRunner.testCase.getTestStepByName("Properties").getPropertyValue( "testStatus" ) + "\r\n"
//------- PASSED FOR EXPECTED STATUS -----------
} else if	(statusFound == true && statusFromProperty.contains(expectedStatus)) {
	log.info "Check PASSED = " + "Expected result was: " + expectedStatus + " | Actual Resutls is: " + testRunner.testCase.getTestStepByName("Properties").getPropertyValue( "testStatus" ) + "\r\n"
//------- FAILED FOR TIMEOUT -----------
} else if	(statusFound == false){ 
	log.info "FAILED timeout = " + sleepTime * attempts + "ms | " + "Expected result was: " + expectedStatus + " | Actual Resutls is: " + testRunner.testCase.getTestStepByName("Properties").getPropertyValue( "testStatus" ) + "\r\n"
	assert false : "TIMEOUT = didnt found expected/unexpected result in the time = " + sleepTime * attempts + " ms"
}
