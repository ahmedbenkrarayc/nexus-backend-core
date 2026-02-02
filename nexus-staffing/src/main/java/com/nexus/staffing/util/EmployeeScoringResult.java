package com.nexus.staffing.util;

import java.util.Map;

public record EmployeeScoringResult(int percentage, Map<String, Integer> breakdown) {
}
