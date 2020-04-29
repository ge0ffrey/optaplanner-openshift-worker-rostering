/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.domain.tenant;

import java.time.DayOfWeek;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;

@Entity
@ConstraintConfiguration(constraintPackage = "org.optaweb.employeerostering.service.solver")
public class RosterConstraintConfiguration extends AbstractPersistable {

    public static final String LOW_RISK_EMPLOYEE_ASSIGNED_TO_A_COVID_WARD =
            "Low-risk employee assigned to a COVID ward";
    public static final String MODERATE_RISK_EMPLOYEE_ASSIGNED_TO_A_COVID_WARD =
            "Moderate-risk employee assigned to a COVID ward";
    public static final String HIGH_RISK_EMPLOYEE_ASSIGNED_TO_A_COVID_WARD =
            "High-risk employee assigned to a COVID ward";
    public static final String EXTREME_RISK_EMPLOYEE_ASSIGNED_TO_A_COVID_WARD =
            "Extreme-risk employee assigned to a COVID ward";
    public static final String INOCULATED_EMPLOYEE_OUTSIDE_A_COVID_WARD = "Inoculated employee outside a COVID ward";
    public static final String UNIFORM_DISTRIBUTION_OF_INOCULATED_HOURS = "Uniform distribution of inoculated hours";
    public static final String MAXIMIZE_INOCULATED_HOURS = "Maximize inoculated hours";
    public static final String MIGRATION_BETWEEN_COVID_AND_NON_COVID_WARDS =
            "Migration between COVID and non-COVID wards";
    public static final String REQUIRED_SKILL_FOR_A_SHIFT = "Required skill for a shift";
    public static final String UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE = "Unavailable time slot for an employee";
    public static final String NO_OVERLAPPING_SHIFTS = "No overlapping shifts";
    public static final String NO_MORE_THAN_2_CONSECUTIVE_SHIFTS = "No more than 2 consecutive shifts";
    public static final String BREAK_BETWEEN_NON_CONSECUTIVE_SHIFTS_IS_AT_LEAST_10_HOURS =
            "Break between non-consecutive shifts is at least 10 hours";
    public static final String DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Daily minutes must not exceed contract maximum";
    public static final String WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Weekly minutes must not exceed contract maximum";
    public static final String MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Monthly minutes must not exceed contract maximum";
    public static final String YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Yearly minutes must not exceed contract maximum";
    public static final String ASSIGN_EVERY_SHIFT = "Assign every shift";
    public static final String EMPLOYEE_IS_NOT_ORIGINAL_EMPLOYEE = "Employee is not original employee";
    public static final String UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE = "Undesired time slot for an employee";
    public static final String DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE = "Desired time slot for an employee";
    public static final String EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE = "Employee is not rotation employee";
    @NotNull
    private DayOfWeek weekStartDay = DayOfWeek.MONDAY;

    // COVID-specific constraints
    @ConstraintWeight(LOW_RISK_EMPLOYEE_ASSIGNED_TO_A_COVID_WARD)
    private HardMediumSoftLongScore lowRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofSoft(10);
    @ConstraintWeight(MODERATE_RISK_EMPLOYEE_ASSIGNED_TO_A_COVID_WARD)
    private HardMediumSoftLongScore moderateRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofSoft(50);
    @ConstraintWeight(HIGH_RISK_EMPLOYEE_ASSIGNED_TO_A_COVID_WARD)
    private HardMediumSoftLongScore highRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofSoft(100);
    @ConstraintWeight(EXTREME_RISK_EMPLOYEE_ASSIGNED_TO_A_COVID_WARD)
    private HardMediumSoftLongScore extremeRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight(INOCULATED_EMPLOYEE_OUTSIDE_A_COVID_WARD)
    private HardMediumSoftLongScore inoculatedEmployeeOutsideCovidWardMatchWeight =
            HardMediumSoftLongScore.ofSoft(1_000);
    @ConstraintWeight(UNIFORM_DISTRIBUTION_OF_INOCULATED_HOURS)
    private HardMediumSoftLongScore uniformDistributionOfInoculatedHoursMatchWeight =
            HardMediumSoftLongScore.ofSoft(1);
    @ConstraintWeight(MAXIMIZE_INOCULATED_HOURS)
    private HardMediumSoftLongScore maximizeInoculatedHoursMatchWeight = HardMediumSoftLongScore.ofSoft(50);
    @ConstraintWeight(MIGRATION_BETWEEN_COVID_AND_NON_COVID_WARDS)
    private HardMediumSoftLongScore migrationBetweenCovidAndNonCovidWardMatchWeight =
            HardMediumSoftLongScore.ofSoft(100);

    @ConstraintWeight(REQUIRED_SKILL_FOR_A_SHIFT)
    private HardMediumSoftLongScore requiredSkill = HardMediumSoftLongScore.ofHard(100);
    @ConstraintWeight(UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE)
    private HardMediumSoftLongScore unavailableTimeSlot = HardMediumSoftLongScore.ofHard(50);
    @ConstraintWeight(NO_OVERLAPPING_SHIFTS)
    private HardMediumSoftLongScore noOverlappingShifts = HardMediumSoftLongScore.ofHard(20);
    @ConstraintWeight(NO_MORE_THAN_2_CONSECUTIVE_SHIFTS)
    private HardMediumSoftLongScore noMoreThan2ConsecutiveShifts = HardMediumSoftLongScore.ofHard(10);
    @ConstraintWeight(BREAK_BETWEEN_NON_CONSECUTIVE_SHIFTS_IS_AT_LEAST_10_HOURS)
    private HardMediumSoftLongScore breakBetweenNonConsecutiveShiftsAtLeast10Hours = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight(DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM)
    private HardMediumSoftLongScore contractMaximumDailyMinutes = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight(WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM)
    private HardMediumSoftLongScore contractMaximumWeeklyMinutes = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight(MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM)
    private HardMediumSoftLongScore contractMaximumMonthlyMinutes = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight(YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM)
    private HardMediumSoftLongScore contractMaximumYearlyMinutes = HardMediumSoftLongScore.ofHard(1);

    @ConstraintWeight(ASSIGN_EVERY_SHIFT)
    private HardMediumSoftLongScore assignEveryShift = HardMediumSoftLongScore.ofMedium(1);

    @ConstraintWeight(EMPLOYEE_IS_NOT_ORIGINAL_EMPLOYEE)
    private HardMediumSoftLongScore notOriginalEmployee = HardMediumSoftLongScore.ofSoft(100_000_000_000L);
    @ConstraintWeight(UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE)
    private HardMediumSoftLongScore undesiredTimeSlot = HardMediumSoftLongScore.ofSoft(20);
    @ConstraintWeight(DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE)
    private HardMediumSoftLongScore desiredTimeSlot = HardMediumSoftLongScore.ofSoft(10);
    @ConstraintWeight(EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE)
    private HardMediumSoftLongScore notRotationEmployee = HardMediumSoftLongScore.ofSoft(50);

    @SuppressWarnings("unused")
    public RosterConstraintConfiguration() {
        super(-1);
    }

    public RosterConstraintConfiguration(Integer tenantId, DayOfWeek weekStartDay) {
        super(tenantId);
        this.weekStartDay = weekStartDay;
    }

    // ************************************************************************
    // COVID-specific getters and setters
    // ************************************************************************

    public HardMediumSoftLongScore getLowRiskEmployeeInCovidWardMatchWeight() {
        return lowRiskEmployeeInCovidWardMatchWeight;
    }

    public void setLowRiskEmployeeInCovidWardMatchWeight(
            HardMediumSoftLongScore lowRiskEmployeeInCovidWardMatchWeight) {
        this.lowRiskEmployeeInCovidWardMatchWeight = lowRiskEmployeeInCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getModerateRiskEmployeeInCovidWardMatchWeight() {
        return moderateRiskEmployeeInCovidWardMatchWeight;
    }

    public void setModerateRiskEmployeeInCovidWardMatchWeight(
            HardMediumSoftLongScore moderateRiskEmployeeInCovidWardMatchWeight) {
        this.moderateRiskEmployeeInCovidWardMatchWeight = moderateRiskEmployeeInCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getHighRiskEmployeeInCovidWardMatchWeight() {
        return highRiskEmployeeInCovidWardMatchWeight;
    }

    public void setHighRiskEmployeeInCovidWardMatchWeight(
            HardMediumSoftLongScore highRiskEmployeeInCovidWardMatchWeight) {
        this.highRiskEmployeeInCovidWardMatchWeight = highRiskEmployeeInCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getExtremeRiskEmployeeInCovidWardMatchWeight() {
        return extremeRiskEmployeeInCovidWardMatchWeight;
    }

    public void setExtremeRiskEmployeeInCovidWardMatchWeight(
            HardMediumSoftLongScore extremeRiskEmployeeInCovidWardMatchWeight) {
        this.extremeRiskEmployeeInCovidWardMatchWeight = extremeRiskEmployeeInCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getInoculatedEmployeeOutsideCovidWardMatchWeight() {
        return inoculatedEmployeeOutsideCovidWardMatchWeight;
    }

    public void setInoculatedEmployeeOutsideCovidWardMatchWeight(
            HardMediumSoftLongScore inoculatedEmployeeOutsideCovidWardMatchWeight) {
        this.inoculatedEmployeeOutsideCovidWardMatchWeight = inoculatedEmployeeOutsideCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getUniformDistributionOfInoculatedHoursMatchWeight() {
        return uniformDistributionOfInoculatedHoursMatchWeight;
    }

    public void setUniformDistributionOfInoculatedHoursMatchWeight(
            HardMediumSoftLongScore uniformDistributionOfInoculatedHoursMatchWeight) {
        this.uniformDistributionOfInoculatedHoursMatchWeight = uniformDistributionOfInoculatedHoursMatchWeight;
    }

    public HardMediumSoftLongScore getMaximizeInoculatedHoursMatchWeight() {
        return maximizeInoculatedHoursMatchWeight;
    }

    public void setMaximizeInoculatedHoursMatchWeight(HardMediumSoftLongScore maximizeInoculatedHoursMatchWeight) {
        this.maximizeInoculatedHoursMatchWeight = maximizeInoculatedHoursMatchWeight;
    }

    public HardMediumSoftLongScore getMigrationBetweenCovidAndNonCovidWardMatchWeight() {
        return migrationBetweenCovidAndNonCovidWardMatchWeight;
    }

    public void setMigrationBetweenCovidAndNonCovidWardMatchWeight(
            HardMediumSoftLongScore migrationBetweenCovidAndNonCovidWardMatchWeight) {
        this.migrationBetweenCovidAndNonCovidWardMatchWeight = migrationBetweenCovidAndNonCovidWardMatchWeight;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public DayOfWeek getWeekStartDay() {
        return weekStartDay;
    }

    public void setWeekStartDay(DayOfWeek weekStartDay) {
        this.weekStartDay = weekStartDay;
    }

    public HardMediumSoftLongScore getRequiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(HardMediumSoftLongScore requiredSkill) {
        this.requiredSkill = requiredSkill;
    }

    public HardMediumSoftLongScore getUnavailableTimeSlot() {
        return unavailableTimeSlot;
    }

    public void setUnavailableTimeSlot(HardMediumSoftLongScore unavailableTimeSlot) {
        this.unavailableTimeSlot = unavailableTimeSlot;
    }

    public HardMediumSoftLongScore getNoOverlappingShifts() {
        return noOverlappingShifts;
    }

    public void setNoOverlappingShifts(HardMediumSoftLongScore noOverlappingShifts) {
        this.noOverlappingShifts = noOverlappingShifts;
    }

    public HardMediumSoftLongScore getNoMoreThan2ConsecutiveShifts() {
        return noMoreThan2ConsecutiveShifts;
    }

    public void setNoMoreThan2ConsecutiveShifts(HardMediumSoftLongScore noMoreThan2ConsecutiveShifts) {
        this.noMoreThan2ConsecutiveShifts = noMoreThan2ConsecutiveShifts;
    }

    public HardMediumSoftLongScore getBreakBetweenNonConsecutiveShiftsAtLeast10Hours() {
        return breakBetweenNonConsecutiveShiftsAtLeast10Hours;
    }

    public void setBreakBetweenNonConsecutiveShiftsAtLeast10Hours(
            HardMediumSoftLongScore breakBetweenNonConsecutiveShiftsAtLeast10Hours) {
        this.breakBetweenNonConsecutiveShiftsAtLeast10Hours = breakBetweenNonConsecutiveShiftsAtLeast10Hours;
    }

    public HardMediumSoftLongScore getContractMaximumDailyMinutes() {
        return contractMaximumDailyMinutes;
    }

    public void setContractMaximumDailyMinutes(HardMediumSoftLongScore contractMaximumDailyMinutes) {
        this.contractMaximumDailyMinutes = contractMaximumDailyMinutes;
    }

    public HardMediumSoftLongScore getContractMaximumWeeklyMinutes() {
        return contractMaximumWeeklyMinutes;
    }

    public void setContractMaximumWeeklyMinutes(HardMediumSoftLongScore contractMaximumWeeklyMinutes) {
        this.contractMaximumWeeklyMinutes = contractMaximumWeeklyMinutes;
    }

    public HardMediumSoftLongScore getContractMaximumMonthlyMinutes() {
        return contractMaximumMonthlyMinutes;
    }

    public void setContractMaximumMonthlyMinutes(HardMediumSoftLongScore contractMaximumMonthlyMinutes) {
        this.contractMaximumMonthlyMinutes = contractMaximumMonthlyMinutes;
    }

    public HardMediumSoftLongScore getContractMaximumYearlyMinutes() {
        return contractMaximumYearlyMinutes;
    }

    public void setContractMaximumYearlyMinutes(HardMediumSoftLongScore contractMaximumYearlyMinutes) {
        this.contractMaximumYearlyMinutes = contractMaximumYearlyMinutes;
    }

    public HardMediumSoftLongScore getAssignEveryShift() {
        return assignEveryShift;
    }

    public void setAssignEveryShift(HardMediumSoftLongScore assignEveryShift) {
        this.assignEveryShift = assignEveryShift;
    }

    public HardMediumSoftLongScore getNotOriginalEmployee() {
        return notOriginalEmployee;
    }

    public void setNotOriginalEmployee(HardMediumSoftLongScore notOriginalEmployee) {
        this.notOriginalEmployee = notOriginalEmployee;
    }

    public HardMediumSoftLongScore getUndesiredTimeSlot() {
        return undesiredTimeSlot;
    }

    public void setUndesiredTimeSlot(HardMediumSoftLongScore undesiredTimeSlot) {
        this.undesiredTimeSlot = undesiredTimeSlot;
    }

    public HardMediumSoftLongScore getDesiredTimeSlot() {
        return desiredTimeSlot;
    }

    public void setDesiredTimeSlot(HardMediumSoftLongScore desiredTimeSlot) {
        this.desiredTimeSlot = desiredTimeSlot;
    }

    public HardMediumSoftLongScore getNotRotationEmployee() {
        return notRotationEmployee;
    }

    public void setNotRotationEmployee(HardMediumSoftLongScore notRotationEmployee) {
        this.notRotationEmployee = notRotationEmployee;
    }
}
