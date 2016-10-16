package com.github.mrduguo.planeseatplanner.model

import groovy.transform.CompileStatic

@CompileStatic
class ScheduledFlight {
    int seatsPerRow
    int rowsInPlane
    List<TravellerGroup> travellerGroups = []


    static ScheduledFlight parse(String inputFilePath) {
        ScheduledFlight result
        new File(inputFilePath).eachLine { String lineText, int lineNumber ->
            if (!result) {
                result = parsePlaneDimensions(lineText)
            } else {
                parseTravellerGroup(result, lineText, lineNumber)
            }
        }
        result
    }

    private static ScheduledFlight parsePlaneDimensions(String lineText) {
        try {
            def result = new ScheduledFlight()
            def plainDimensions = lineText.split('\\s')
            result.seatsPerRow = plainDimensions[0].toInteger()
            result.rowsInPlane = plainDimensions[1].toInteger()
            assert result.seatsPerRow >= 2: 'No jet fighter or drone is allowed for traveller'
            assert result.rowsInPlane >= 1: 'No drone is allowed for traveller'
            result
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Invalid plane dimensions defined '$lineText' at line 1", ex)
        }
    }

    private static void parseTravellerGroup(ScheduledFlight result, String lineText, int lineNumber) {
        def travellerGroup=new TravellerGroup(groupId: lineNumber-1)
        lineText.split('\\s').each { String travellersInfo ->
            parseTraveller(travellerGroup,travellersInfo, lineNumber)
        }
        result.travellerGroups.add(travellerGroup)
    }

    private static void parseTraveller(TravellerGroup travellerGroup,String traveller, int lineNumber) {
        try {
            def preferWindowSeat = traveller.endsWith('W')
            def travellerId = preferWindowSeat ? traveller[0..-2] : traveller
            travellerGroup.travellers << new Traveller(
                    travellerGroup: travellerGroup,
                    personId: travellerId.toInteger(),
                    preferWindowSeat: preferWindowSeat
            )
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid traveller defined '$traveller' at line $lineNumber", ex)
        }
    }

}
