package com.tuum.testassignment.validation;

import jakarta.validation.GroupSequence;

@GroupSequence({ValidationGroup1.class, ValidationGroup2.class})
public interface OrderedValidation {
}
