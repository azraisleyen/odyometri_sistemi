package edu.ankara.audiometer.domain.model;
import java.util.Optional;
public record TonePresentation(Ear ear, FrequencyHz frequency, IntensityDbHL intensity, int orderIndex, Optional<PatientResponse> response, PresentationDirection direction, boolean validAscendingTrial) {
    public TonePresentation { response = response == null ? Optional.empty() : response; }
    public TonePresentation withResponse(PatientResponse r){ return new TonePresentation(ear, frequency, intensity, orderIndex, Optional.of(r), direction, validAscendingTrial); }
}
