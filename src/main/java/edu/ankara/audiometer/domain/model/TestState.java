package edu.ankara.audiometer.domain.model;
import edu.ankara.audiometer.domain.config.*;import java.util.*;
public record TestState(TestSession session, AudiometryConfig config, Ear currentEar, FrequencyHz currentFrequency, IntensityDbHL currentIntensity, TestPhase phase, PresentationDirection direction, List<TonePresentation> presentations, List<ProtocolEvent> eventHistory, Audiogram audiogram, List<String> errors) {
 public TestState { presentations=List.copyOf(presentations); eventHistory=List.copyOf(eventHistory); errors=List.copyOf(errors); }
 public static TestState initial(AudiometryConfig c){ return new TestState(TestSession.simulation(), c, c.ears().getFirst(), c.frequencyPlan().activeOrder().getFirst(), c.algorithm().startIntensity(), TestPhase.READY, PresentationDirection.INITIAL, List.of(), List.of(), Audiogram.empty(), List.of()); }
 public int nextOrder(){return presentations.size()+1;}
 public TestState withPresentation(TonePresentation p){var n=new ArrayList<>(presentations); n.add(p); return new TestState(session,config,currentEar,currentFrequency,currentIntensity,phase,direction,n,eventHistory,audiogram,errors);} 
 public TestState withEvent(ProtocolEvent e){var n=new ArrayList<>(eventHistory); n.add(e); return new TestState(session,config,currentEar,currentFrequency,currentIntensity,phase,direction,presentations,n,audiogram,errors);} 
 public TestState transition(Ear e, FrequencyHz f, IntensityDbHL i, TestPhase ph, PresentationDirection d){return new TestState(session,config,e,f,i,ph,d,presentations,eventHistory,audiogram,errors);} 
 public TestState withAudiogram(Audiogram a){return new TestState(session,config,currentEar,currentFrequency,currentIntensity,phase,direction,presentations,eventHistory,a,errors);} 
 public TestState withError(String er){var n=new ArrayList<>(errors); n.add(er); return new TestState(session,config,currentEar,currentFrequency,currentIntensity,TestPhase.ERROR,direction,presentations,eventHistory,audiogram,n);} 
}
