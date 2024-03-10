package com.stundb.api.providers;

import jakarta.inject.Provider;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.util.Optional;

public class ValidatorProvider implements Provider<Validator> {

    private Validator validator;

    @Override
    public Validator get() {
        validator =
                Optional.ofNullable(this.validator)
                        .orElseGet(
                                () -> {
                                    try (var factory =
                                            Validation.byDefaultProvider()
                                                    .configure()
                                                    .messageInterpolator(
                                                            new ParameterMessageInterpolator())
                                                    .buildValidatorFactory()) {
                                        return factory.getValidator();
                                    }
                                });
        return validator;
    }
}
