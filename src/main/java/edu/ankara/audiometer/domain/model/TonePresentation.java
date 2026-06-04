package edu.ankara.audiometer.domain.model;

import java.util.Optional;

public record TonePresentation(
        Ear ear,
        FrequencyHz frequency,
        IntensityDbHL intensity,
        int orderIndex,
        Optional<PatientResponse> response,
        PresentationDirection direction,
        boolean validAscendingTrial,
        int frequencyOrderIndex
) {
    public TonePresentation(
            Ear ear,
            FrequencyHz frequency,
            IntensityDbHL intensity,
            int orderIndex,
            Optional<PatientResponse> response,
            PresentationDirection direction,
            boolean validAscendingTrial
    ) {
        this(ear, frequency, intensity, orderIndex, response, direction, validAscendingTrial, -1);
    }

    public TonePresentation {
        response = response == null ? Optional.empty() : response;
    }

    public TonePresentation withResponse(PatientResponse nextResponse) {
        return new TonePresentation(
                ear,
                frequency,
                intensity,
                orderIndex,
                Optional.of(nextResponse),
                direction,
                validAscendingTrial,
                frequencyOrderIndex
        );
    }
}
