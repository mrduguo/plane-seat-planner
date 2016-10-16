# Plane Seat Planner


## Documentation

### Folder Structure


```
├──────────────────────────────────────────────────────────────────────────────────────────────────
├── gradle                                # build system support files
├── build.gradle
├── gradlew
├── gradlew.bat
├──────────────────────────────────────────────────────────────────────────────────────────────────
└── src
    ├── main/groovy/com/github/mrduguo/planeseatplanner
    │   ├── Planner                       # the interface to support multiple implementations
    │   ├── PlannerRunner                 # contain main method to execute the planner 
    │   ├── impl                          # the default implementation
    │   │   ├── DefaultPlanner            # class name has suggestions to their responsibilities
    │   │   ├── OverSubscribeHandler      
    │   │   ├── SatisfactionCalculator     
    │   │   ├── SeatAllocator             
    │   │   ├── SegmentBuilder            
    │   │   └── SegmentShapeBuilder       
    │   └── model                         # data structures
    │       ├── ScheduledFlight           # contain all user input data and intermediate processing data
    │       ├── SeatPlan                  # the output data structure 
    │       ├── SegmentShape              # segment is a subset of group who will seat together
    │       ├── Traveller                 # people who plan to travel with the plane
    │       └── TravellerGroup            # group of traveller want to seat together
    └── test
        ├── groovy/com/github/mrduguo/planeseatplanner
        │   ├── PlanRunnerSpec            # spock based integration tests
        │   └── model                     # model unit tests
        │      └── ScheduledFlightParseSpec
        └── resources/data
            ├── invalid
            │   ├── plane-dimensions-drone
            │   ├── plane-dimensions-not-number
            │   └── traveller
            └── valid
                ├── full-capacity-100-percent-satisfaction
                ├── large-group
                ├── low-occupy-but-high-window-seat-demand
                └── over-subscribed
    
```


### The Algorithm Implementation
```

    SeatPlan plan(ScheduledFlight scheduledFlight) {
        buildSegmentsFromGroups(scheduledFlight)
        handleOverSubscribe(scheduledFlight)
        buildSegmentShape(scheduledFlight)

        def travellerSeats = generateNaturalOrderBasedSeats(scheduledFlight)
        def satisfaction = calculateSatisfaction(scheduledFlight)
        new SeatPlan(travellerSeats: travellerSeats, satisfaction: satisfaction)
    }

```

Notes:

* doesn't apply the traveller weight even distribution across the plane
* take group seat together as priority than split the group for window seat
* the output was to produce best result for the requirements and sample data
* it may not reflex real life or additional situations 
* satisfaction is calculated based on all travellers include over subscribed


### Technology Selection

* selected spring-boot/groovy/spock to maximize the readability with minimal code.
* aim to write readable code without inline comments
* more unit test could be written for core algorithm 

### Input Validation

There are basic input validation with enough information for developer to troubleshooting. Could add more code to produce better error feedback for business user.



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

### Sample Data

`src/test/resources/data/valid/full-capacity-100-percent-satisfaction/input-file.txt`:

```
4 4
1W 2 3
4 5 6 7
8
9 10 11W
12W
13 14
15 16
```    

`src/test/resources/data/valid/full-capacity-100-percent-satisfaction/expected-output.txt`:

```
1 2 3 8
4 5 6 7
11 9 10 12
13 14 15 16
100%
``` 