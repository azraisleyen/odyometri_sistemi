package edu.ankara.audiometer.domain.model;
import java.util.List;
public record ValidationResult(boolean valid, List<String> errors) { public ValidationResult{errors=List.copyOf(errors);} public static ValidationResult ok(){return new ValidationResult(true,List.of());} public static ValidationResult invalid(String e){return new ValidationResult(false,List.of(e));} }
