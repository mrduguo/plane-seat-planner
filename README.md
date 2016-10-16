# Plane Seat Planner
## Documentation

### Folder Structure


```
|---------------------------------------------------------
|- gradle                   # build system support files
|- build.gradle
|- gradlew
|- gradlew.bat
|---------------------------------------------------------
|- src
 |- main
  |- groovy
   |- com/github/mrduguo/planeseatplanner
    |- PlannerRunner.groovy # the main class to run application
    |- model                # data structures
    |- algorithm            # the implementation of the planner
 |- test
  |- groovy
   |- com/github/mrduguo/planeseatplanner
    |- PlanRunnerSpec.groovy # Spock based integration tests
    |- model                 # unit tests
  |- resources
   |- data
    |- invalid               # invalid input data for negative tests
    |- valid                 # use cases to test the algorithm
    
```


### The Algorithm
1. Normalize group
1.1 Sort group by size reversely
1.2 Split large group to fit into row
2. Remove over subscribed traveller from end of normalized group
2.2 Same size with higher window seat preference first
3. Allocate seat sequentially
4. Swap person for natural order
5. Sort rows by minimal person id for natural order

Notes:

* satisfaction is calculated based on all travellers include over subscribed
* doesn't apply the traveller weight even distribution across the plane
* take group seat together as priority than split the group for window seat
* the output was to produce best result for the requirements and sample data
* it may not reflex real life or additional situations 


### Technology Selection
* selected spring-boot/groovy/spock to maximize the readability with minimal code.
* aim to write readable code without inline comments
* more unit test could be written for core algorithm 

### Input Validation
There are basic input validation with enough information for developer to troubleshooting, could add more code to produce better error feedback for business user.



## Usage

### Requirements

* JAVA SDK 7 or newer

Tested on Windows 7 / CentOS / macOS 

### Build

Linux or macOS:

    ./gradlew
    
Windows:
    
    gradlew.bat
    
It will compile, package and run test at the end. 
Once it success, you may proceed to run the tool in next step.    

### Run

Syntax:

    java -jar build/libs/plane-seat-planner-0.0.1-SNAPSHOT.jar <INPUT_FILE_PATH>

Example:

    java -jar build/libs/plane-seat-planner-0.0.1-SNAPSHOT.jar src/test/resources/data/valid/full-capacity-100-percent-satisfaction/input-file.txt
