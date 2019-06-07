// generate uuid as string
def uuid = java.util.UUID.randomUUID().toString()

// define date formate to be generated later
def dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")

// get current date
def currentDate = Calendar.getInstance()

// add 60 days to the current date
currentDate.add(Calendar.DAY_OF_MONTH,60)
 
// get current date with time
def dateWithTime = Calendar.getInstance().getTime()

// set Test Case Properties 
testRunner.testCase.setPropertyValue( "Current date", currentDate) 
testRunner.testCase.setPropertyValue( "Current date with Time", dateWithTime) 
