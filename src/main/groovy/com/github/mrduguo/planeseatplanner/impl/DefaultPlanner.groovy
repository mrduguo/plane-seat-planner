package com.github.mrduguo.planeseatplanner.impl

import com.github.mrduguo.planeseatplanner.Planner
import com.github.mrduguo.planeseatplanner.model.ScheduledFlight
import com.github.mrduguo.planeseatplanner.model.SeatPlan
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

import static com.github.mrduguo.planeseatplanner.impl.OverSubscribeHandler.handleOverSubscribe
import static com.github.mrduguo.planeseatplanner.impl.SatisfactionCalculator.calculateSatisfaction
import static com.github.mrduguo.planeseatplanner.impl.SeatAllocator.generateNaturalOrderBasedSeats
import static com.github.mrduguo.planeseatplanner.impl.SegmentBuilder.buildSegmentsFromGroups
import static com.github.mrduguo.planeseatplanner.impl.SegmentShapeBuilder.buildSegmentShape

@CompileStatic
@Component
class DefaultPlanner implements Planner{
    SeatPlan plan(ScheduledFlight scheduledFlight) {
        buildSegmentsFromGroups(scheduledFlight)
        handleOverSubscribe(scheduledFlight)
        buildSegmentShape(scheduledFlight)

        def travellerSeats = generateNaturalOrderBasedSeats(scheduledFlight)
        def satisfaction = calculateSatisfaction(scheduledFlight)
        new SeatPlan(travellerSeats: travellerSeats, satisfaction: satisfaction)
    }
}
