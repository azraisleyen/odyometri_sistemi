package edu.ankara.audiometer.domain.fp;
import java.util.*;
public record Validation<T>(Optional<T> value, List<String> errors) { public Validation{value=value==null?Optional.empty():value; errors=List.copyOf(errors);} public boolean valid(){return errors.isEmpty();} public static <T> Validation<T> ok(T v){return new Validation<>(Optional.of(v),List.of());} public static <T> Validation<T> invalid(String e){return new Validation<>(Optional.empty(),List.of(e));} }
